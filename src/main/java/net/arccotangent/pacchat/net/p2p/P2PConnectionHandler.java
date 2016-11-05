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

package net.arccotangent.pacchat.net.p2p;

import net.arccotangent.pacchat.Main;
import net.arccotangent.pacchat.crypto.MsgCrypto;
import net.arccotangent.pacchat.crypto.PacchatMessage;
import net.arccotangent.pacchat.filesystem.KeyManager;
import net.arccotangent.pacchat.logging.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;

class P2PConnectionHandler extends Thread {
	
	private final BufferedReader input;
	private final BufferedWriter output;
	private final long connection_id;
	private final Logger p2p_ch_log;
	private final String ip;
	private String origin = "";
	
	P2PConnectionHandler(BufferedReader in, BufferedWriter out, long conn_id, String source_ip) {
		input = in;
		output = out;
		connection_id = conn_id;
		p2p_ch_log = new Logger("P2P/SERVER/CONNECTION-" + connection_id);
		ip = source_ip;
		PeerManager.addPeer(ip);
	}
	
	String getOrigin() {
		return origin;
	}
	
	public void run() {
		try {
			String line1 = input.readLine();
			switch (line1) {
				case "101 ping":
					p2p_ch_log.i("Client pinged us, responding with an acknowledgement.");
					output.write("102 pong");
					output.newLine();
					output.flush();
					run();
					break;
				case "200 message":
					String origin = input.readLine();
					this.origin = origin;
					String destination = input.readLine();
					long timestamp = Long.parseLong(input.readLine());
					long mid = Long.parseLong(input.readLine());
					if (destination.equals(KeyManager.fingerprint(Main.getKeypair().getPublic()))) {
						p2p_ch_log.i("Received P2P message from peer " + ip);
						p2p_ch_log.i("P2P message is sent by " + origin);
						String message = input.readLine();
						String raw_message = P2PConnectionManager.decode(message);
						String[] lines = raw_message.split("\n");
						handleMessage(lines, origin, destination, mid);
					} else {
						P2PConnectionManager.propagate(origin, destination, timestamp, mid, input.readLine());
					}
					run();
					break;
				case "300 getaddr":
					p2p_ch_log.i("Client requested a list of peers.");
					ArrayList<String> peers = PeerManager.getPeers();
					if (peers.size() <= 1) {
						output.write("302 no peers");
						output.newLine();
						output.flush();
						run();
						break;
					}
					
					output.write(peers.size());
					output.newLine();
					
					for (String peer : peers) {
						output.write(peer);
						output.newLine();
						output.flush();
						run();
					}
					break;
				default:
					p2p_ch_log.i("Client sent an invalid request header: " + line1);
					output.write("401 invalid p2p transmission header");
					output.newLine();
					output.flush();
					run();
					break;
			}
		} catch (IOException e) {
			p2p_ch_log.e("Error in P2P connection handler " + connection_id);
			e.printStackTrace();
		}
	}
	
	private void handleMessage(String[] messageLines, String origin, String destination, long mid) {
		String line1 = messageLines[0];
		switch (line1) {
			case "101 ping":
				P2PConnectionManager.propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("102 pong"));
				break;
			case "200 encrypted message":
				p2p_ch_log.i("Client sent an encrypted message, attempting verification and decryption.");
				PrivateKey privkey = Main.getKeypair().getPrivate();
				String cryptedMsg = messageLines[1] + "\n" + messageLines[2] + "\n" + messageLines[3];
				
				p2p_ch_log.i("Checking for sender's public key.");
				if (KeyManager.checkIfIPKeyExists(ip)) {
					p2p_ch_log.i("Public key found.");
				} else {
					p2p_ch_log.i("Public key not found.");
					p2p_ch_log.w("Requesting a public key over the P2P network can be dangerous, as the authenticity of the public key cannot be verified.");
					p2p_ch_log.w("As such, PacChat will not request the key over the P2P network.");
					p2p_ch_log.i("You can ask the sender to send you their public key file, or request it directly from their server.");
					p2p_ch_log.i("Run the command 'getkey <ip address>' to download a key from a server.");
					break;
					/*
					* ==============================================================================================================================
					* The below code, if executed, would request a public key over the P2P network. However, the recipient would not recognize the message.
					* Requesting a raw public key over the P2P network is VERY INSECURE and is subject to man in the middle attacks.
					* The real key can be captured and replaced with a forged key under the control of a third party.
					* For this reason, 301 getkey is not implemented on the P2P network.
					* ==============================================================================================================================
					*
					* 301 getkey may be implemented in the future if a secure key exchange method such as Diffie-Hellman is implemented.
					*
					P2PConnectionManager.propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("301 getkey"));
					String keyOrigin = "";
					String b64key = "";
					String keyip = "";
					try {
						String l1 = input.readLine();
						if (l1.equals("200 message")) {
							keyOrigin = input.readLine();
							input.readLine(); //destination
							input.readLine(); //timestamp
							input.readLine(); //message ID (MID)
							b64key = input.readLine();
							keyip = input.readLine();
						}
					} catch (IOException e) {
						p2p_ch_log.e("Error reading key request response!");
						e.printStackTrace();
					}
					
					PublicKey key = P2PConnectionManager.decodeKey(b64key);
					if (KeyManager.fingerprint(key).equals(keyOrigin)) //essentially no verification
						KeyManager.saveKeyByIP(ip, key);
					else
						p2p_ch_log.e("Received invalid key! Not saving.");
					*/
				}
				
				PacchatMessage message = MsgCrypto.decryptAndVerifyMessage(cryptedMsg, privkey, KeyManager.loadKeyByIP(ip));
				
				String msg = message.getMessage();
				boolean verified = message.isVerified();
				boolean decrypted = message.isDecryptedSuccessfully();
				
				String ANSI_RESET = "\u001B[0m";
				String ANSI_CYAN = "\u001B[36m";
				String ANSI_BOLD = "\u001B[1m";
				
				if (verified && decrypted) {
					p2p_ch_log.i("Acknowledging message.");
					P2PConnectionManager.propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("201 message acknowledgement"));
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----BEGIN MESSAGE-----" + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + msg + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----END MESSAGE-----" + ANSI_RESET);
				} else if (!verified && decrypted) {
					p2p_ch_log.w("Notifying client that message authenticity was not verified.");
					P2PConnectionManager.propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("203 unable to verify"));
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----BEGIN MESSAGE-----" + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + msg + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----END MESSAGE-----" + ANSI_RESET);
				} else if (!verified) {
					p2p_ch_log.w("Notifying client that message could not be decrypted.");
					P2PConnectionManager.propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("202 unable to decrypt"));
				}
				
				if (Main.isGuiVisible()) {
					Main.addReceivedMessageToGui(ip, msg, verified);
				}
				break;
			case "400 invalid transmission header":
				p2p_ch_log.i("Received invalid header response.");
				break;
			default:
				p2p_ch_log.i("Server sent invalid header: " + line1);
				P2PConnectionManager.propagate(destination, origin, System.currentTimeMillis(), mid, P2PConnectionManager.encode("400 invalid transmission header"));
				break;
		}
	}
	
}
