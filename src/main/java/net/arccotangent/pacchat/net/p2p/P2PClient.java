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

import java.io.*;
import java.net.*;

public class P2PClient {
	
	private Logger p2p_client_log;
	private String ip;
	private Socket socket;
	private BufferedReader input;
	private BufferedWriter output;
	private boolean connected = false;
	
	public P2PClient(String ip_address) {
		ip = ip_address;
		p2p_client_log = new Logger("P2P CLIENT/" + ip);
	}
	
	public void connect() {
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(InetAddress.getByName(ip), P2PServer.P2P_PORT), 1000);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			connected = true;
		} catch (SocketTimeoutException e) {
			p2p_client_log.e("Connection to server timed out!");
			e.printStackTrace();
			connected = false;
		} catch (ConnectException e) {
			p2p_client_log.e("Connection to server was refused!");
			e.printStackTrace();
			connected = false;
		} catch (UnknownHostException e) {
			p2p_client_log.e("You entered an invalid IP address!");
			e.printStackTrace();
			connected = false;
		} catch (IOException e) {
			p2p_client_log.e("Error connecting to server!");
			e.printStackTrace();
			connected = false;
		}
	}
	
	public void getaddr() {
		if (!connected) {
			p2p_client_log.e("Error sending getaddr message, client is not connected.");
		}
		try {
			output.write("300 getaddr");
			output.newLine();
			output.flush();
			
			String response = input.readLine();
			
			switch (response) {
				case "301 peers":
					break;
				default:
					p2p_client_log.e("Server sent invalid response " + response);
					output.close();
					input.close();
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
