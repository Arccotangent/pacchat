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
import net.arccotangent.pacchat.filesystem.KeyManager;
import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import java.security.PrivateKey;
import java.security.PublicKey;
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
	
	static boolean connectedToPeer(String peer_addr) {
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
		if (peer_addr.isEmpty())
			return;
		
		P2PClient peer = new P2PClient(peer_addr);
		
		if (!connectedToPeer(peer_addr)) {
			peer.connect();
			connectedPeers.add(peer);
			PeerManager.addPeer(peer_addr);
		}
	}
	
	public static void disconnectFromPeer(String peer_addr) {
		P2PClient peer = getPeer(peer_addr);
		if (connectedToPeer(peer_addr)) {
			assert peer != null;
			peer.disconnect();
		}
	}
	
	static String encode(String message) {
		return Base64.encodeBase64String(message.getBytes());
	}
	
	static String decode(String b64message) {
		return new String(Base64.decodeBase64(b64message));
	}
	
	static long getRandomMID() {
		Random random = new Random();
		return random.nextLong();
	}
	
	static void propagate(String origin, String destination, long timestamp, long mid, String message) {
		if (origin.equalsIgnoreCase(KeyManager.fingerprint(Main.getKeypair().getPublic()))) {
			p2p_cm_log.i("Propagating message through network.");
			for (P2PClient peer : connectedPeers) {
				peer.sendMessage(origin, destination, message, mid);
			}
			return;
		}
		
		if (!messageIDs.contains(mid)) {
			p2p_cm_log.i("Propagating message through network.");
			for (P2PClient peer : connectedPeers) {
				peer.sendMessage(origin, destination, message, mid, timestamp);
			}
			messageIDs.add(mid);
		}
	}
	
	public static void sendChat(String chat_message, PublicKey targetKey, PrivateKey ownPriv) {
		String origin = KeyManager.fingerprint(Main.getKeypair().getPublic());
		String destination = KeyManager.fingerprint(targetKey);
		long mid = getRandomMID();
		long timestamp = System.currentTimeMillis();
		
		String cryptedMsg = MsgCrypto.encryptAndSignMessage(chat_message, targetKey, ownPriv);
		String packet = "200 encrypted message\n" + cryptedMsg;
		
		propagate(origin, destination, timestamp, mid, packet);
	}
	
}
