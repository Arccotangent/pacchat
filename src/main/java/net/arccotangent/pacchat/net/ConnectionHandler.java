package net.arccotangent.pacchat.net;

import net.arccotangent.pacchat.Main;
import net.arccotangent.pacchat.crypto.MsgCrypto;
import net.arccotangent.pacchat.logging.Logger;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.PrivateKey;

class ConnectionHandler extends Thread {
	
	private BufferedReader input;
	private BufferedWriter output;
	private long connection_id;
	private Logger ch_log;
	
	ConnectionHandler(BufferedReader in, BufferedWriter out, long conn_id) {
		input = in;
		output = out;
		connection_id = conn_id;
		ch_log = new Logger("SERVER/CONNECTION-" + connection_id);
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
				case "301 getkey":
					ch_log.i("Client requested our public key, sending.");
					String pubkeyB64 = Base64.encodeBase64String(Main.getKeypair().getPublic().getEncoded());
					output.write(pubkeyB64);
					output.newLine();
					output.flush();
					output.close();
					break;
				default: //incoming encrypted message
					ch_log.i("Client sent what appears to be a message, attempting decryption.");
					PrivateKey privkey = Main.getKeypair().getPrivate();
					String cryptedMsg = line1 + "\n" + input.readLine();
					String msg = MsgCrypto.decryptMessage(cryptedMsg, privkey);
					System.out.println("-----BEGIN MESSAGE-----");
					System.out.println(msg);
					System.out.println("-----END MESSAGE-----");
					break;
			}
		} catch (IOException e) {
			ch_log.e("Error in connection handler " + connection_id);
			e.printStackTrace();
		}
	}
	
}
