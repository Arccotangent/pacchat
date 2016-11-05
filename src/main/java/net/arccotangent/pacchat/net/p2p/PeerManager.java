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

import net.arccotangent.pacchat.logging.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PeerManager {
	
	private static Logger p2p_log = new Logger("P2P/PEER-MANAGER");
	private static ArrayList<String> peers = new ArrayList<>();
	private static String user_home = System.getProperty("user.home");
	private static final File peerFile = new File(user_home + File.separator + ".pacchat" + File.separator + "peers.txt");
	private static boolean peerListUpToDate = false;
	static boolean log_write = true;
	private static ArrayList<String> allPeers = new ArrayList<>();
	
	public static boolean firstTime() {
		return !peerFile.exists();
	}
	
	private static void createPeerFileIfNotExist() {
		if (!peerFile.exists()) {
			p2p_log.i("Creating new peer database on disk.");
			try {
				peerFile.createNewFile();
			} catch (IOException e) {
				p2p_log.e("Error creating peer database!");
				e.printStackTrace();
			}
		}
	}
	
	static void readAllPeers() {
		p2p_log.i("Reading all peers from disk.");
		try {
			String peers_raw = new String(Files.readAllBytes(peerFile.toPath()));
			allPeers = new ArrayList<>(Arrays.asList(peers_raw.split("\n")));
			peerListUpToDate = true;
		} catch (IOException e) {
			p2p_log.e("Error reading peers from disk!");
			e.printStackTrace();
		}
	}
	
	private static void updatePeerDB() {
		for (String peer : peers) {
			if (!allPeers.contains(peer))
				allPeers.add(peer);
		}
		p2p_log.i("Updated peer DB.");
	}
	
	private static boolean existsInList(String addr) {
		for (String peer : peers) {
			if (peer.equals(addr))
				return true;
		}
		return false;
	}
	
	static ArrayList<String> getPeers() {
		return peers;
	}
	
	static ArrayList<String> getAllPeers() {
		return allPeers;
	}
	
	private static boolean existsOnDisk(String addr) {
		if (!peerListUpToDate)
			readAllPeers();
		for (String peer : allPeers) {
			if (peer.equals(addr))
				return true;
		}
		return false;
	}
	
	static void randomizePeers() {
		p2p_log.i("Shuffling peer lists, fetching new peers from database.");
		Collections.shuffle(allPeers);
		writePeersToDisk();
		
		peers.clear();
		
		for (int i = 0; i < 16; i++) {
			if (i >= allPeers.size())
				break;
			
			peers.add(allPeers.get(i));
		}
	}
	
	private static void writePeersToDisk() {
		if (log_write)
			p2p_log.i("Writing peer database to disk.");
		createPeerFileIfNotExist();
		try {
			BufferedWriter peerWriter = new BufferedWriter(new FileWriter(peerFile));
			
			for (String peer : allPeers) {
				if (!existsOnDisk(peer)) {
					peerWriter.write(peer);
					peerWriter.newLine();
					peerWriter.flush();
				}
			}
			
			peerWriter.close();
		} catch (IOException e) {
			p2p_log.e("Error writing peer database to disk!");
			e.printStackTrace();
		}
	}
	
	static void addPeer(String peer) {
		createPeerFileIfNotExist();
		if (!existsInList(peer) && !peer.equals("127.0.0.1")) {
			p2p_log.i("Adding peer " + peer + " to database.");
			peers.add(peer);
		}
		
		updatePeerDB();
		writePeersToDisk();
	}
	
}
