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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class PeerManager {
	
	private static Logger p2p_log = new Logger("P2P/PEER-MANAGER");
	private static ArrayList<String> peers = new ArrayList<>();
	private static String user_home = System.getProperty("user.home");
	private static final File peerFile = new File(user_home + File.separator + ".pacchat" + File.separator + "peers.txt");
	
	private static ArrayList<String> readAllPeers() {
		try {
			String peers_raw = new String(Files.readAllBytes(peerFile.toPath()));
			ArrayList<String> peers_list = new ArrayList<>(Arrays.asList(peers_raw.split("\n")));
			return peers_list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static boolean existsInList(String addr) {
		for (String peer : peers) {
			if (peer.equals(addr))
				return true;
		}
		return false;
	}
	
	private static boolean existsOnDisk(String addr) {
		ArrayList<String> peerList = readAllPeers();
		for (String peer : peerList) {
			if (peer.equals(addr))
				return true;
		}
		return false;
	}
	
	public static void addPeer(String peer) {
		if (!existsInList(peer)) {
			peers.add(peer);
		}
	}
	
}
