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

package net.arccotangent.pacchat.net;

import net.arccotangent.pacchat.logging.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
	
	static int PORT = 14761;
	private static Logger server_log = new Logger("SERVER");
	private ServerSocket serverSocket = null;
	private boolean active = true;
	private long connection_id = 0;
	private String last_sender = "";
	private boolean shutting_down = false;
	
	public Server() {
		server_log.i("New server created.");
	}
	
	public String getLastSender() {
		return last_sender;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void run() {
		active = true;
		server_log.i("Starting PacChat server on port " + PORT);
		server_log.i("Attempting to open UPNP ports if not already open...");
		if (UPNPManager.isOpen()) {
			server_log.i("UPNP ports are already open.");
		} else {
			server_log.i("Opening UPNP ports now.");
			UPNPManager.UPNPOpenPorts();
		}
		
		try {
			serverSocket = new ServerSocket(PORT);
			while (active) {
				server_log.i("Server listening for connections...");
				Socket clientSocket = serverSocket.accept();
				
				String src_ip_addr = clientSocket.getInetAddress().getHostAddress();
				last_sender = src_ip_addr;
				server_log.i("Incoming transmission from " + src_ip_addr);
				BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				
				connection_id++;
				server_log.i("Handing connection over to connection handler " + connection_id);
				ConnectionHandler conn = new ConnectionHandler(input, output, connection_id, src_ip_addr);
				conn.start();
			}
		} catch (IOException e) {
			if (!shutting_down) {
				server_log.e("Error in server operation!");
				e.printStackTrace();
			}
		}
	}
	
	public void closeServer() {
		server_log.i("Terminating server.");
		shutting_down = true;
		
		if (!serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				server_log.e("Error while closing server socket!");
				e.printStackTrace();
			}
		}
		
		active = false;
	}
	
}
