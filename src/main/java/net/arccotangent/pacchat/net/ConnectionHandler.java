/*
This file is part of PacChat.

PacChat is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PacChat is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PacChat.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.arccotangent.pacchat.net;

import net.arccotangent.pacchat.KeyUpdate;
import net.arccotangent.pacchat.KeyUpdateManager;
import net.arccotangent.pacchat.Main;
import net.arccotangent.pacchat.crypto.MsgCrypto;
import net.arccotangent.pacchat.crypto.PacchatMessage;
import net.arccotangent.pacchat.filesystem.KeyManager;
import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

class ConnectionHandler extends Thread {
	
	private final BufferedReader input;
	private final BufferedWriter output;
	private final long connection_id;
	private final Logger ch_log;
	private final String ip;
	
	ConnectionHandler(BufferedReader in, BufferedWriter out, long conn_id, String source_ip) {
		input = in;
		output = out;
		connection_id = conn_id;
		ch_log = new Logger("SERVER/CONNECTION-" + connection_id);
		ip = source_ip;
	}
	
	public void run() {
		try {
			String line1 = input.readLine();
			switch (line1) {
				case "101 ping":
					ch_log.i("Client pinged us, responding with an acknowledgement.");
					output.write("102 pong");
					output.newLine();
					output.flush();
					output.close();
					break;
				case "103 version":
					ch_log.i("Client asked for server version, responding with server version (" + Main.VERSION + ").");
					output.write(Main.VERSION);
					output.newLine();
					output.flush();
					output.close();
					break;
				case "302 request key update":
					ch_log.i("Client is requesting a key update.");
					String newFingerprint = input.readLine();
					ch_log.w("Client reports new key fingerprint as '" + newFingerprint + "'");
					
					if (KeyManager.checkIfIPKeyExists(ip)) {
						String fingerprint = KeyManager.fingerprint(KeyManager.loadKeyByIP(ip));
						ch_log.w(ip + " current key fingerprint is: '" + fingerprint + "'");
					} else {
						ch_log.w(ip + " key doesn't exist!");
					}
					
					KeyUpdate update = new KeyUpdate(ip, false);
					KeyUpdateManager.addPendingUpdate(connection_id, update);
					while (KeyUpdateManager.getIncomingUpdate(connection_id).isProcessed()) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					boolean accepted = KeyUpdateManager.getIncomingUpdate(connection_id).isAccepted();
					KeyUpdateManager.completeIncomingUpdate(connection_id, KeyUpdateManager.getIncomingUpdate(connection_id));
					if (accepted) {
						ch_log.i("Accepting key update");
						try {
							output.write("303 update");
							output.newLine();
							output.flush();
							
							String pubkeyB64 = input.readLine();
							
							byte[] pubEncoded = Base64.decodeBase64(pubkeyB64);
							X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
							KeyFactory keyFactory = KeyFactory.getInstance("RSA");
							
							output.close();
							input.close();
							
							KeyManager.saveKeyByIP(ip, (RSAPublicKey) keyFactory.generatePublic(pubSpec));
						} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
							ch_log.e("Error updating sender's key!");
							e.printStackTrace();
						}
					} else {
						ch_log.i("Rejecting key update.");
						output.write("304 no update");
						output.newLine();
						output.flush();
						output.close();
					}
					break;
				case "301 getkey":
					ch_log.i("Client requested our public key, sending.");
					String pubkeyB64 = Base64.encodeBase64String(Main.getKeypair().getPublic().getEncoded());
					output.write(pubkeyB64);
					output.newLine();
					output.flush();
					output.close();
					break;
				case "200 encrypted message": //incoming encrypted message
					ch_log.i("Client sent an encrypted message, attempting verification and decryption.");
					RSAPrivateKey privkey = (RSAPrivateKey) Main.getKeypair().getPrivate();
					String cryptedMsg = input.readLine() + "\n" + input.readLine() + "\n" + input.readLine();
					
					ch_log.i("Checking for sender's public key.");
					if (KeyManager.checkIfIPKeyExists(ip)) {
						ch_log.i("Public key found.");
					} else {
						ch_log.i("Public key not found, requesting key from their server.");
						try {
							Socket socketGetkey = new Socket();
							socketGetkey.connect(new InetSocketAddress(InetAddress.getByName(ip), Server.PORT), 1000);
							BufferedReader inputGetkey = new BufferedReader(new InputStreamReader(socketGetkey.getInputStream()));
							BufferedWriter outputGetkey = new BufferedWriter(new OutputStreamWriter(socketGetkey.getOutputStream()));
							
							outputGetkey.write("301 getkey");
							outputGetkey.newLine();
							outputGetkey.flush();
							
							String sender_pubkeyB64 = inputGetkey.readLine();
							byte[] pubEncoded = Base64.decodeBase64(sender_pubkeyB64);
							X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
							KeyFactory keyFactory = KeyFactory.getInstance("RSA");
							
							outputGetkey.close();
							inputGetkey.close();
							
							KeyManager.saveKeyByIP(ip, (RSAPublicKey) keyFactory.generatePublic(pubSpec));
						} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
							ch_log.e("Error saving sender's key!");
							e.printStackTrace();
						}
					}
					
					PacchatMessage message = MsgCrypto.decryptAndVerifyMessage(cryptedMsg, privkey, KeyManager.loadKeyByIP(ip));
					
					String msg = message.getMessage();
					boolean verified = message.isVerified();
					boolean decrypted = message.isDecryptedSuccessfully();
					
					String ANSI_RESET = "\u001B[0m";
					String ANSI_CYAN = "\u001B[36m";
					String ANSI_BOLD = "\u001B[1m";
					if (verified && decrypted) {
						ch_log.i("Acknowledging message.");
						output.write("201 message acknowledgement");
						output.newLine();
						output.flush();
						output.close();
						System.out.println(ANSI_BOLD + ANSI_CYAN + "-----BEGIN MESSAGE-----" + ANSI_RESET);
						System.out.println(ANSI_BOLD + ANSI_CYAN + msg + ANSI_RESET);
						System.out.println(ANSI_BOLD + ANSI_CYAN + "-----END MESSAGE-----" + ANSI_RESET);
					} else if (!verified && decrypted) {
						ch_log.w("Notifying client that message authenticity was not verified.");
						output.write("203 unable to verify");
						output.newLine();
						output.flush();
						output.close();
						System.out.println(ANSI_BOLD + ANSI_CYAN + "-----BEGIN MESSAGE-----" + ANSI_RESET);
						System.out.println(ANSI_BOLD + ANSI_CYAN + msg + ANSI_RESET);
						System.out.println(ANSI_BOLD + ANSI_CYAN + "-----END MESSAGE-----" + ANSI_RESET);
					} else if (!verified) {
						ch_log.w("Notifying client that message could not be decrypted.");
						output.write("202 unable to decrypt");
						output.newLine();
						output.flush();
						output.close();
					}
					
					if (Main.isGuiVisible()) {
						Main.addReceivedMessageToGui(ip, msg, verified);
					}
					break;
				case "201 message acknowledgement":
					ch_log.i("Client sent an invalid message acknowledgement.");
					output.write("400 invalid transmission header");
					output.newLine();
					output.flush();
					output.close();
					break;
				case "202 unable to decrypt":
					ch_log.i("Client sent an invalid 'unable to decrypt' transmission.");
					output.write("400 invalid transmission header");
					output.newLine();
					output.flush();
					output.close();
					break;
				case "203 unable to verify":
					ch_log.i("Client sent an invalid 'unable to verify' transmission.");
					output.write("400 invalid transmission header");
					output.newLine();
					output.flush();
					output.close();
					break;
				default:
					ch_log.i("Client sent an invalid request header: " + line1);
					output.write("400 invalid transmission header");
					output.newLine();
					output.flush();
					output.close();
					break;
			}
		} catch (IOException e) {
			ch_log.e("Error in connection handler " + connection_id);
			e.printStackTrace();
		}
	}
	
}
