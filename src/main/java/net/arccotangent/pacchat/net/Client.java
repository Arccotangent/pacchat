package net.arccotangent.pacchat.net;

import net.arccotangent.pacchat.Main;
import net.arccotangent.pacchat.crypto.MsgCrypto;
import net.arccotangent.pacchat.filesystem.KeyManager;
import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class Client {
	
	private static Logger client_log = new Logger("CLIENT");
	
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
			try {
				Socket socketGetkey = new Socket();
				socketGetkey.connect(new InetSocketAddress(InetAddress.getByName(ip_address), Server.PORT), 1000);
				BufferedReader inputGetkey = new BufferedReader(new InputStreamReader(socketGetkey.getInputStream()));
				BufferedWriter outputGetkey = new BufferedWriter(new OutputStreamWriter(socketGetkey.getOutputStream()));
				
				outputGetkey.write("301 getkey");
				outputGetkey.newLine();
				outputGetkey.flush();
				
				String pubkeyB64 = inputGetkey.readLine();
				byte[] pubEncoded = Base64.decodeBase64(pubkeyB64);
				X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				
				outputGetkey.close();
				inputGetkey.close();
				
				KeyManager.saveKeyByIP(ip_address, keyFactory.generatePublic(pubSpec));
			} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
				client_log.e("Error saving recipient's key!");
				e.printStackTrace();
			}
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
			output.write(cryptedMsg);
			output.newLine();
			output.flush();
			
			client_log.i("Transmission successful, closing sockets.");
			output.close();
			input.close();
		} catch (IOException e) {
			client_log.e("Error sending message to recipient!");
			e.printStackTrace();
		}
	}
	
}
