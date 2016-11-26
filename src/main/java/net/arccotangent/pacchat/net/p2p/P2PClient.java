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
	
	P2PClient(String ip_address) {
		ip = ip_address;
		p2p_client_log = new Logger("P2P/CLIENT/" + ip);
		if (!P2PConnectionManager.checkIPValidity(ip_address))
			p2p_client_log.e("Invalid IP address!");
	}
	
	boolean isInputStreamReady() {
		if (!connected)
			return false;
		try {
			return input.ready();
		} catch (IOException e) {
			e.printStackTrace();
			if (e.getMessage().equalsIgnoreCase("Stream closed")) {
				connected = false;
			}
		}
		return false;
	}
	
	void handleReadyConnection() {
		if (!isInputStreamReady()) {
			p2p_client_log.e("Attempted to handle a connection that didn't exist!");
			return;
		}
		p2p_client_log.d("Handling connection.");
		
		P2PConnectionManager.handleP2PConnection(input, output, ip);
	}
	
	public String getConnectedAddress() {
		return ip;
	}
	
	boolean isConnected() {
		return connected;
	}
	
	void connect() {
		p2p_client_log.i("Connecting to peer.");
		p2p_client_log.d("Address = " + ip);
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(InetAddress.getByName(ip), P2PServer.P2P_PORT), 1000);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			connected = true;
			PeerManager.addPeer(ip);
			PeerManager.writePeersToDisk();
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
			p2p_client_log.i("Asking peer for more nodes.");
			output.write("300 getaddr");
			output.newLine();
			output.flush();
			
			String response = input.readLine();
			
			switch (response) {
				case "301 peers":
					int peers = Integer.parseInt(input.readLine());
					p2p_client_log.d("Server sent us " + peers + " peers.");
					PeerManager.log_write = false;
					for (int i = 1; i <= peers; i++) {
						PeerManager.addPeer(input.readLine());
					}
					PeerManager.log_write = true;
					PeerManager.writePeersToDisk();
					PeerManager.randomizePeers();
					break;
				case "302 no peers":
					p2p_client_log.i("Server sent us no peers.");
					break;
				default:
					p2p_client_log.e("Server sent invalid response: " + response);
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void sendMessage(String origin, String destination, String base64Message, long mid) {
		p2p_client_log.i("Sending P2P message.");
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
			p2p_client_log.e("Error while sending message!");
			e.printStackTrace();
		}
	}
	
	void sendMessage(String origin, String destination, String base64Message, long mid, long timestamp) {
		p2p_client_log.i("Sending P2P message.");
		try {
			output.write("200 message");
			output.newLine();
			output.write(origin);
			output.newLine();
			output.write(destination);
			output.newLine();
			output.write(Long.toString(timestamp));
			output.newLine();
			output.write(Long.toString(mid));
			output.newLine();
			output.write(base64Message);
			output.newLine();
			output.flush();
		} catch (IOException e) {
			p2p_client_log.e("Error while sending message!");
			e.printStackTrace();
		}
	}
	
	void disconnect() {
		p2p_client_log.i("Disconnecting from peer.");
		try {
			output.write("103 disconnecting");
			output.newLine();
			output.flush();
			connected = false;
			output.close();
			input.close();
			socket.close();
			socket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
