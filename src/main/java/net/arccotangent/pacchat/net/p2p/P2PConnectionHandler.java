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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

class P2PConnectionHandler extends Thread {
	
	private final BufferedReader input;
	private final BufferedWriter output;
	private final long connection_id;
	private final Logger ch_log;
	private final String ip;
	
	P2PConnectionHandler(BufferedReader in, BufferedWriter out, long conn_id, String source_ip) {
		input = in;
		output = out;
		connection_id = conn_id;
		ch_log = new Logger("P2P SERVER/CONNECTION-" + connection_id);
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
				default:
					ch_log.i("Client sent an invalid request header: " + line1);
					output.write("401 invalid p2p transmission header");
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
