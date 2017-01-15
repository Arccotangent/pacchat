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
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Random;

public class P2PConnectionManager {
	
	private static Logger p2p_cm_log = new Logger("P2P/CONNECTION-MANAGER");
	private static ArrayList<P2PClient> connectedPeers = new ArrayList<>();
	private static ArrayList<Long> messageIDs = new ArrayList<>();
	
	public static void init() {
		p2p_cm_log.i("Initializing P2P network connections.");
		PeerManager.randomizePeers();
		ArrayList<String> peers = PeerManager.getPeers();
		for (String peer : peers) {
			connectToPeer(peer);
		}
	}
	
	public static boolean havePeers() {
		return connectedPeers.size() > 0;
	}
	
	static boolean checkIPValidity(String ip_address) {
		//return (ip_address.equals("127.0.0.1") || ip_address.equalsIgnoreCase("localhost") || ip_address.isEmpty() || P2PConnectionManager.connectedToPeer(ip_address));
		if (ip_address == null) {
			p2p_cm_log.d("IP is null!");
			return false;
		} else if (ip_address.equals("127.0.0.1")) {
			p2p_cm_log.d("IP = 127.0.0.1");
			return false;
		} else if (ip_address.equalsIgnoreCase("localhost")) {
			p2p_cm_log.d("IP is localhost!");
			return false;
		} else if (ip_address.isEmpty()) {
			p2p_cm_log.d("IP is empty!");
			return false;
		} else if (P2PConnectionManager.connectedToPeer(ip_address)) {
			p2p_cm_log.d("Already connected to this peer!");
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean connectedToPeer(String peer_addr) {
		for (P2PClient peer : connectedPeers) {
			if (peer.getConnectedAddress().equals(peer_addr))
				return true;
		}
		return false;
	}
	
	private static P2PClient getPeer(String peer_addr) {
		for (P2PClient peer : connectedPeers) {
			if (peer.getConnectedAddress().equals(peer_addr))
				return peer;
		}
		return null;
	}
	
	public static ArrayList<P2PClient> getConnectedPeers() {
		return connectedPeers;
	}
	
	public static void connectToPeer(String peer_addr) {
		if (!checkIPValidity(peer_addr)) {
			p2p_cm_log.e("Peer address '" + peer_addr + "' is invalid!");
			return;
		}
		p2p_cm_log.i("Connecting to peer at " + peer_addr);
		
		P2PClient peer = new P2PClient(peer_addr);
		
		if (!connectedToPeer(peer_addr)) {
			peer.connect();
			if (peer.isConnected()) {
				p2p_cm_log.i("Connection successful!");
				connectedPeers.add(peer);
				PeerManager.addPeer(peer_addr);
			} else {
				p2p_cm_log.e("Connecting to peer " + peer_addr + " was unsuccessful.");
			}
		}
	}
	
	public static void disconnectFromPeer(String peer_addr) {
		P2PClient peer = getPeer(peer_addr);
		if (connectedToPeer(peer_addr)) {
			assert peer != null;
			peer.disconnect();
		}
	}
	
	private static String encode(String message) {
		return Base64.encodeBase64String(message.getBytes());
	}
	
	private static String decode(String b64message) {
		return new String(Base64.decodeBase64(b64message));
	}
	
	private static long getRandomMID() {
		Random random = new Random();
		return random.nextLong();
	}
	
	private static boolean MIDProcessed(long mid) {
		return messageIDs.contains(mid);
	}
	
	private static void propagateToClients(String origin, String destination, long timestamp, long mid, String message) {
		ArrayList<P2PConnectionHandler> clients = Main.getP2PServer().getClients();
		
		for (P2PConnectionHandler client : clients) {
			client.sendToClient(origin, destination, mid, message);
		}
	}
	
	private static void propagate(String origin, String destination, long timestamp, long mid, String message) {
		if (origin.equalsIgnoreCase(KeyManager.fingerprint((RSAPublicKey) Main.getKeypair().getPublic()))) {
			p2p_cm_log.i("Propagating message through network.");
			for (P2PClient peer : connectedPeers) {
				peer.sendMessage(origin, destination, message, mid);
			}
			propagateToClients(origin, destination, timestamp, mid, message);
			return;
		}
		
		if (!messageIDs.contains(mid)) {
			p2p_cm_log.i("Propagating message through network.");
			for (P2PClient peer : connectedPeers) {
				peer.sendMessage(origin, destination, message, mid, timestamp);
			}
			propagateToClients(origin, destination, timestamp, mid, message);
			messageIDs.add(mid);
		}
	}
	
	public static void sendChat(String chat_message, RSAPublicKey targetKey, RSAPrivateKey ownPriv) {
		String origin = KeyManager.fingerprint((RSAPublicKey) Main.getKeypair().getPublic());
		String destination = KeyManager.fingerprint(targetKey);
		long mid = getRandomMID();
		long timestamp = System.currentTimeMillis();
		
		String cryptedMsg = MsgCrypto.encryptAndSignMessage(chat_message, targetKey, ownPriv);
		String packet = "200 encrypted message\n" + cryptedMsg;
		
		propagate(origin, destination, timestamp, mid, Base64.encodeBase64String(packet.getBytes()));
	}
	
	static String handleP2PConnection(BufferedReader input, BufferedWriter output, String ip) {
		try {
			String originFingerprint = "";
			String line1 = input.readLine();
			p2p_cm_log.d("Origin IP = " + ip);
			p2p_cm_log.d("Header = " + line1);
			switch (line1) {
				case "101 ping":
					p2p_cm_log.i("Client pinged us, responding with an acknowledgement.");
					output.write("102 pong");
					output.newLine();
					output.flush();
					break;
				case "103 disconnecting":
					p2p_cm_log.i("Peer is disconnecting.");
					output.close();
					input.close();
					break;
				case "200 message":
					String origin = input.readLine();
					originFingerprint = origin;
					String destination = input.readLine();
					long timestamp = Long.parseLong(input.readLine());
					long mid = Long.parseLong(input.readLine());
					if (MIDProcessed(mid))
						break;
					if (destination.equals(KeyManager.fingerprint((RSAPublicKey) Main.getKeypair().getPublic()))) {
						p2p_cm_log.i("Received P2P message from peer " + ip);
						p2p_cm_log.i("P2P message is sent by " + origin);
						String message = input.readLine();
						String raw_message = decode(message);
						String[] lines = raw_message.split("\n");
						handleMessage(lines, origin, destination, ip);
					} else {
						propagate(origin, destination, timestamp, mid, input.readLine());
					}
					break;
				case "300 getaddr":
					p2p_cm_log.i("Client requested a list of peers.");
					PeerManager.randomizePeers();
					ArrayList<String> peers = PeerManager.getPeers();
					if (peers.size() <= 1) {
						p2p_cm_log.i("We have no peers to send to client.");
						output.write("302 no peers");
						output.newLine();
						output.flush();
						break;
					}
					
					p2p_cm_log.i("Sending peers to client.");
					output.write("301 peers");
					output.newLine();
					
					output.write(Integer.toString(peers.size()));
					output.newLine();
					
					for (String peer : peers) {
						output.write(peer);
						output.newLine();
						output.flush();
					}
					break;
				default:
					p2p_cm_log.i("Client sent an invalid request header: " + line1);
					output.write("401 invalid p2p transmission header");
					output.newLine();
					output.flush();
					break;
			}
			return originFingerprint;
		} catch (IOException e) {
			p2p_cm_log.e("Error in P2P connection handling!");
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static void handleMessage(String[] messageLines, String origin, String destination, String ip) {
		String line1 = messageLines[0];
		switch (line1) {
			case "101 ping":
				propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("102 pong"));
				break;
			case "200 encrypted message":
				p2p_cm_log.i("Client sent an encrypted message, attempting verification and decryption.");
				RSAPrivateKey privkey = (RSAPrivateKey) Main.getKeypair().getPrivate();
				String cryptedMsg = messageLines[1] + "\n" + messageLines[2] + "\n" + messageLines[3];
				
				p2p_cm_log.i("Checking for sender's public key.");
				if (KeyManager.checkIfIPKeyExists(ip)) {
					p2p_cm_log.i("Public key found.");
				} else {
					p2p_cm_log.i("Public key not found.");
					p2p_cm_log.w("Requesting a public key over the P2P network can be dangerous, as the authenticity of the public key cannot be verified.");
					p2p_cm_log.w("As such, PacChat will not request the key over the P2P network.");
					p2p_cm_log.i("You can ask the sender to send you their public key file, or request it directly from their server.");
					p2p_cm_log.i("Run the command 'getkey <ip address>' to download a key from a server.");
					break;
					/*
					* ==============================================================================================================================
					* The below code, if executed, would request a public key over the P2P network. However, the recipient would not recognize the message.
					* Requesting a raw public key over the P2P network is VERY INSECURE and is subject to man in the middle attacks.
					* The real key can be captured and replaced with a forged key under the control of a third party.
					* For this reason, 301 getkey is not implemented on the P2P network.
					* ==============================================================================================================================
					*
					* 301 getkey may be implemented in the future if a secure key exchange method is implemented.
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
						p2p_cm_log.e("Error reading key request response!");
						e.printStackTrace();
					}
					
					PublicKey key = P2PConnectionManager.decodeKey(b64key);
					if (KeyManager.fingerprint(key).equals(keyOrigin)) //essentially no verification
						KeyManager.saveKeyByIP(ip, key);
					else
						p2p_cm_log.e("Received invalid key! Not saving.");
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
					p2p_cm_log.i("Acknowledging message.");
					propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("201 message acknowledgement"));
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----BEGIN MESSAGE-----" + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + msg + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----END MESSAGE-----" + ANSI_RESET);
				} else if (!verified && decrypted) {
					p2p_cm_log.w("Notifying client that message authenticity was not verified.");
					propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("203 unable to verify"));
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----BEGIN MESSAGE-----" + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + msg + ANSI_RESET);
					System.out.println(ANSI_BOLD + ANSI_CYAN + "-----END MESSAGE-----" + ANSI_RESET);
				} else {
					p2p_cm_log.w("Notifying client that message could not be decrypted.");
					propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("202 unable to decrypt"));
				}
				
				if (Main.isGuiVisible()) {
					Main.addReceivedMessageToGui(ip, msg, verified);
				}
				break;
			case "400 invalid transmission header":
				p2p_cm_log.i("Received invalid header response.");
				break;
			default:
				p2p_cm_log.i("Server sent invalid header: " + line1);
				propagate(destination, origin, System.currentTimeMillis(), P2PConnectionManager.getRandomMID(), P2PConnectionManager.encode("400 invalid transmission header"));
				break;
		}
	}
	
}
