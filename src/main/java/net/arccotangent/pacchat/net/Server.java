package net.arccotangent.pacchat.net;

import net.arccotangent.pacchat.logging.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
	
	static final int PORT = 14761;
	private static Logger server_log = new Logger("SERVER");
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private boolean active = true;
	private long connection_id = 0;
	private String last_sender = "";
	
	public Server() {
		server_log.i("New server created.");
	}
	
	public String getLastSender() {
		return last_sender;
	}
	
	public void run() {
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
				clientSocket = serverSocket.accept();
				
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
			server_log.e("Error in server operation!");
			server_log.w("If the server is being terminated, this is normal. Otherwise, please report this error to developers.");
			e.printStackTrace();
		}
	}
	
	public void closeServer() {
		server_log.i("Terminating server.");
		
		if (!serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				server_log.e("Error while closing server socket!");
				e.printStackTrace();
			}
		}
		
		if (!clientSocket.isClosed()) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				server_log.e("Error while closing client socket!");
				e.printStackTrace();
			}
		}
		
		server_log.i("Ignore any SocketException errors thrown by server, those are normal.");
		
		active = false;
	}
	
}
