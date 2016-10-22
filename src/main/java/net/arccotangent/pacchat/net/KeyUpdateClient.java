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

import net.arccotangent.pacchat.Main;
import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.*;

public class KeyUpdateClient extends Thread {
	
	private Logger kuc_log;
	private String server_ip;
	
	public KeyUpdateClient(long keyupdate_id, String server) {
		kuc_log = new Logger("CLIENT/KEY-UPDATE-" + keyupdate_id);
		server_ip = server;
	}
	
	public void run() {
		Socket socket;
		BufferedReader input;
		BufferedWriter output;
		
		kuc_log.i("Connecting to server at " + server_ip);
		
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(InetAddress.getByName(server_ip), Server.PORT), 1000);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (SocketTimeoutException e) {
			kuc_log.e("Connection to server timed out!");
			e.printStackTrace();
			return;
		} catch (ConnectException e) {
			kuc_log.e("Connection to server was refused!");
			e.printStackTrace();
			return;
		} catch (UnknownHostException e) {
			kuc_log.e("You entered an invalid IP address!");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			kuc_log.e("Error connecting to server!");
			e.printStackTrace();
			return;
		}
		
		try {
			kuc_log.i("Requesting a key update.");
			output.write("302 request key update");
			output.newLine();
			output.flush();
			
			kuc_log.i("Awaiting response from server.");
			String update = input.readLine();
			switch (update) {
				case "303 update":
					kuc_log.i("Server accepted update request, sending public key.");
					String pubkeyB64 = Base64.encodeBase64String(Main.getKeypair().getPublic().getEncoded());
					output.write(pubkeyB64);
					output.newLine();
					output.flush();
					output.close();
					break;
				case "304 no update":
					kuc_log.i("Server rejected update request, closing connection.");
					input.close();
					output.close();
					break;
				case "305 update unavailable":
					kuc_log.i("Server cannot update at this time, try again later.");
					input.close();
					output.close();
					break;
				default:
					kuc_log.i("Server sent back invalid response");
					input.close();
					output.close();
					break;
			}
		} catch (IOException e) {
			kuc_log.e("Error in key update request!");
			e.printStackTrace();
		}
	}
	
}
