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

public class P2PConnectionHandler extends Thread {
	
	private final BufferedReader input;
	private final BufferedWriter output;
	private final long connection_id;
	private final Logger p2p_ch_log;
	private final String ip;
	private String origin = "";
	private boolean connected = false;
	
	P2PConnectionHandler(BufferedReader in, BufferedWriter out, long conn_id, String source_ip) {
		input = in;
		output = out;
		connection_id = conn_id;
		p2p_ch_log = new Logger("P2P/SERVER/CONNECTION-" + connection_id);
		ip = source_ip;
		PeerManager.addPeer(ip);
		connected = true;
	}
	
	boolean isConnected() {
		try {
			input.ready();
		} catch (IOException e) {
			if (e.getMessage().equalsIgnoreCase("Stream closed")) {
				connected = false;
			} else
				e.printStackTrace();
		}
		
		return connected;
	}
	
	public String getIP() {
		return ip;
	}
	
	private boolean inputReady() {
		if (!connected)
			return false;
		try {
			return input.ready();
		} catch (IOException e) {
			if (e.getMessage().equalsIgnoreCase("Stream closed")) {
				connected = false;
				p2p_ch_log.i("Client " + ip + " closed connection.");
			} else
				e.printStackTrace();
		}
		return false;
	}
	
	String getOrigin() {
		return origin;
	}
	
	void sendToClient(String origin, String destination, long mid, String base64Message) {
		p2p_ch_log.i("Sending P2P message.");
		try {
			output.write("200 message");
			output.newLine();
			output.write(origin);
			output.newLine();
			output.write(destination);
			output.newLine();
			output.write(Long.toString(System.currentTimeMillis()));
			output.newLine();
			output.write(Long.toString(mid));
			output.newLine();
			output.write(base64Message);
			output.newLine();
			output.flush();
		} catch (IOException e) {
			p2p_ch_log.e("Error while sending message!");
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (!inputReady()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//Connection is ready for handling
		origin = P2PConnectionManager.handleP2PConnection(input, output, ip);
		
		run();
		
	}
	
}
