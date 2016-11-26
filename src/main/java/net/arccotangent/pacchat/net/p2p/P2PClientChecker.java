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

import java.util.ArrayList;

public class P2PClientChecker extends Thread {
	
	private Logger pcc_log = new Logger("P2P/CLIENT/CHECKER");
	private boolean active = false;
	
	public P2PClientChecker() {
		active = true;
		pcc_log.i("P2P client checker is active.");
	}
	
	public void run() {
		while (active) {
			ArrayList<P2PClient> clients = P2PConnectionManager.getConnectedPeers();
			
			if (clients.size() >= 1) {
				for (P2PClient client : clients) {
					if (client.isInputStreamReady()) {
						pcc_log.d("P2P client connected to " + client.getConnectedAddress() + " has received a message.");
						client.handleReadyConnection();
					}
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deactivate() {
		active = false;
	}
	
}
