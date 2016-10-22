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

import net.arccotangent.pacchat.Main;
import net.arccotangent.pacchat.crypto.MsgCrypto;
import net.arccotangent.pacchat.filesystem.KeyManager;
import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

class ConnectionHandler extends Thread {
	
	private BufferedReader input;
	private BufferedWriter output;
	private long connection_id;
	private Logger ch_log;
	private String ip;
	private final String ANSI_BOLD = "\u001B[1m";
	private final String ANSI_CYAN = "\u001B[36m";
	private final String ANSI_RESET = "\u001B[0m";
	
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
				case "301 getkey":
					ch_log.i("Client requested our public key, sending.");
					String pubkeyB64 = Base64.encodeBase64String(Main.getKeypair().getPublic().getEncoded());
					output.write(pubkeyB64);
					output.newLine();
					output.flush();
					output.close();
					break;
				default: //incoming encrypted message
					ch_log.i("Client sent what appears to be a message, attempting decryption and verification.");
					PrivateKey privkey = Main.getKeypair().getPrivate();
					String cryptedMsg = line1 + "\n" + input.readLine() + "\n" + input.readLine();
					
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
							
							KeyManager.saveKeyByIP(ip, keyFactory.generatePublic(pubSpec));
						} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
							ch_log.e("Error saving sender's key!");
							e.printStackTrace();
						}
					}
					
					String msg = MsgCrypto.decryptAndVerifyMessage(cryptedMsg, privkey, KeyManager.loadKeyByIP(ip));
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----BEGIN MESSAGE-----" + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + msg + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----END MESSAGE-----" + ANSI_RESET);
					break;
			}
		} catch (IOException e) {
			ch_log.e("Error in connection handler " + connection_id);
			e.printStackTrace();
		}
	}
	
}
