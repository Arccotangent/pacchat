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
import net.arccotangent.pacchat.net.UPNPManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class P2PServer extends Thread {
	
	public static final int P2P_PORT = 14581;
	private static final Logger p2p_server_log = new Logger("P2P/SERVER");
	private ServerSocket serverSocket = null;
	private boolean active = true;
	private long connection_id = 0;
	private String last_sender = "";
	private boolean shutting_down = false;
	private static ArrayList<P2PConnectionHandler> clients = new ArrayList<>();
	
	public P2PServer() {
		p2p_server_log.i("New P2P server created.");
	}
	
	public String getLastSender() {
		return last_sender;
	}
	
	ArrayList<P2PConnectionHandler> getClients() {
		return clients;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void run() {
		active = true;
		p2p_server_log.i("Starting PacChat P2P server on port " + P2P_PORT);
		p2p_server_log.i("Attempting to open UPNP ports if not already open...");
		if (UPNPManager.isOpen()) {
			p2p_server_log.i("UPNP ports are already open.");
		} else {
			p2p_server_log.i("Opening UPNP ports now.");
			UPNPManager.UPNPOpenPorts();
		}
		
		try {
			serverSocket = new ServerSocket(P2P_PORT);
			while (active) {
				p2p_server_log.i("P2P server listening for connections...");
				Socket clientSocket = serverSocket.accept();
				
				String src_ip_addr = clientSocket.getInetAddress().getHostAddress();
				p2p_server_log.i("Incoming P2P transmission from " + src_ip_addr);
				BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				
				connection_id++;
				p2p_server_log.i("Handing connection over to P2P connection handler " + connection_id);
				P2PConnectionHandler conn = new P2PConnectionHandler(input, output, connection_id, src_ip_addr);
				conn.start();
				clients.add(conn);
				Thread.sleep(250);
				last_sender = conn.getOrigin();
			}
		} catch (IOException | InterruptedException e) {
			if (!shutting_down) {
				p2p_server_log.e("Error in server operation!");
				e.printStackTrace();
			}
		}
	}
	
	public void closeP2PServer() {
		p2p_server_log.i("Terminating server.");
		shutting_down = true;
		
		if (!serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				p2p_server_log.e("Error while closing server socket!");
				e.printStackTrace();
			}
		}
		
		active = false;
	}
	
}
