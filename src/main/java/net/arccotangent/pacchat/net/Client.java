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
import net.arccotangent.pacchat.crypto.MsgCrypto;
import net.arccotangent.pacchat.filesystem.KeyManager;
import net.arccotangent.pacchat.logging.Logger;

import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Client {
	
	private static final Logger client_log = new Logger("CLIENT");
	private static long kuc_id = 0;
	
	public static void incrementKUC_ID() {
		kuc_id++;
	}
	
	public static long getKUC_ID() {
		return kuc_id;
	}
	
	public static void sendMessage(String msg, String ip_address) {
		client_log.i("Sending message to " + ip_address);
		client_log.i("Connecting to server...");
		
		PublicKey pub;
		PrivateKey priv;
		Socket socket;
		BufferedReader input;
		BufferedWriter output;
		
		client_log.i("Checking for recipient's public key...");
		if (KeyManager.checkIfIPKeyExists(ip_address)) {
			client_log.i("Public key found.");
		} else {
			client_log.i("Public key not found, requesting key from their server.");
			PublicKey server_key = KeyManager.downloadKeyFromIP(ip_address);
			KeyManager.saveKeyByIP(ip_address, server_key);
		}
		
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(InetAddress.getByName(ip_address), Server.PORT), 1000);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (SocketTimeoutException e) {
			client_log.e("Connection to server timed out!");
			e.printStackTrace();
			return;
		} catch (ConnectException e) {
			client_log.e("Connection to server was refused!");
			e.printStackTrace();
			return;
		} catch (UnknownHostException e) {
			client_log.e("You entered an invalid IP address!");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			client_log.e("Error connecting to server!");
			e.printStackTrace();
			return;
		}
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		pub = KeyManager.loadKeyByIP(ip_address);
		priv = Main.getKeypair().getPrivate();
		
		String cryptedMsg = MsgCrypto.encryptAndSignMessage(msg, pub, priv);
		try {
			client_log.i("Sending message to recipient.");
			output.write("200 encrypted message");
			output.newLine();
			output.write(cryptedMsg);
			output.newLine();
			output.flush();
			
			String ack = input.readLine();
			switch (ack) {
				case "201 message acknowledgement":
					client_log.i("Transmission successful, received server acknowledgement.");
					break;
				case "202 unable to decrypt":
					client_log.e("Transmission failure! Server reports that the message could not be decrypted. Did your keys change? Asking recipient for key update.");
					kuc_id++;
					KeyUpdateClient kuc = new KeyUpdateClient(kuc_id, ip_address);
					kuc.start();
					break;
				case "203 unable to verify":
					client_log.w("**********************************************");
					client_log.w("Transmission successful, but the receiving server reports that the authenticity of the message could not be verified!");
					client_log.w("Someone may be tampering with your connection! This is an unlikely, but not impossible scenario!");
					client_log.w("If you are sure the connection was not tampered with, consider downloading the key from the sender's server by running 'getkey " + ip_address + "'.");
					client_log.w("**********************************************");
					break;
				case "400 invalid transmission header":
					client_log.e("Transmission failure! Server reports that the message is invalid. Try updating your software and have the recipient do the same. If this does not fix the problem, report the error to developers.");
					break;
				default:
					client_log.w("Server responded with unexpected code: " + ack);
					client_log.w("Transmission might not have been successful.");
					break;
			}
			
			output.close();
			input.close();
		} catch (IOException e) {
			client_log.e("Error sending message to recipient!");
			e.printStackTrace();
		}
	}
	
}
