/*
PacChat - Direct P2P secure, encrypted private chats
Copyright (C) 2016 Arccotangent

PacChat is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see http://www.gnu.org/licenses.
*/

package net.arccotangent.pacchat;

import net.arccotangent.pacchat.crypto.MsgCrypto;
import net.arccotangent.pacchat.filesystem.KeyManager;
import net.arccotangent.pacchat.gui.PacchatGUI;
import net.arccotangent.pacchat.logging.Logger;
import net.arccotangent.pacchat.net.*;
import net.arccotangent.pacchat.net.p2p.*;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.awt.*;
import java.security.KeyPair;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

public class Main {
	private static final Logger core_log = new Logger("CORE");
	public static final String VERSION = "0.2-B27";
	private static KeyPair keyPair;
	private static final String ANSI_BOLD = "\u001B[1m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_WHITE = "\u001B[37m";
	private static final String ANSI_RESET = "\u001B[0m";
	private static boolean active = false;
	private static Server server;
	private static P2PServer p2p_server;
	private static boolean p2p = false;
	private static PacchatGUI gui;
	private static final boolean guiPossible = !GraphicsEnvironment.isHeadless();
	private static P2PClientChecker clientChecker;
	
	private static Scanner stdin = new Scanner(System.in);
	
	public static void addReceivedMessageToGui(String sender, String msg, boolean verified) {
		gui.addReceivedMessage(sender, msg, verified);
	}
	
	public static Server getServer() {
		return server;
	}
	
	public static P2PServer getP2PServer() {
		return p2p_server;
	}
	
	public static P2PClientChecker getClientChecker() {
		return clientChecker;
	}
	
	public static boolean isP2PEnabled() {
		return p2p;
	}
	
	public static boolean isGuiVisible() {
		return gui != null && gui.isVisible();
	}

	private static void printCopyright() {
		System.out.println("PacChat Copyright (C) 2016 Arccotangent");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY");
		System.out.println("This is free software, and you are welcome to redistribute it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.");
	}
	
	private static void printFullCopyright() {
		System.out.println("PacChat - Direct P2P secure, encrypted private chats");
		System.out.println("Copyright (C) 2016 Arccotangent");
		System.out.println();
		System.out.println("This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.");
		System.out.println();
		System.out.println("This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.");
		System.out.println();
		System.out.println("You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses.");
	}
	
	public static KeyPair getKeypair(){
		return keyPair;
	}
	
	private static void printCLOHelp() {
		System.out.println("pacchat [arguments]");
		System.out.println();
		System.out.println("Enter no arguments to run PacChat normally.");
		System.out.println("Arguments can be one of the following:");
		System.out.println();
		System.out.println("help - Print this help");
		System.out.println("version - Print PacChat version and exit");
	}
	
	private static void printHelpMsg() {
		System.out.println(ANSI_BOLD + ANSI_CYAN + "---Help/Exit---" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "help - This help message" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "exit - Exit chat mode and shut down PacChat." + ANSI_RESET);
		System.out.println();
		System.out.println(ANSI_BOLD + ANSI_CYAN + "---Chatting---" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "send/s <ip address> - Send a message. PacChat will prompt you to enter your message after you enter the command." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "reply/r - Reply to the last person to send you a message." + ANSI_RESET);
		System.out.println();
		System.out.println(ANSI_BOLD + ANSI_CYAN + "---Server Management---" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "haltserver/hs - Halt the server if it is running." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "startserver/ss - Start the server if it is not currently running." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "restartserver/rs - Restart the server. If the server is not running, has the same effect as startserver." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_WHITE + "Note: If you halt your server, you will not be able to receive messages until you start it again." + ANSI_RESET);
		System.out.println();
		System.out.println(ANSI_BOLD + ANSI_CYAN + "---Key Management---" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "update/u <ip address> - Request that the server at the specified IP address update their copy of your key." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "updateaccept/ua <ID> - Accept a pending update request with the specified ID." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "updatereject/ur <ID> - Reject a pending update request with the specified ID." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "updatelist/ul - List all pending update IDs." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "updateinfo/ui <ID> - Print info about a pending update request with the specified ID." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "getkey/gk <ip address> - Download a key from the specified IP address." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "encrypt - Encrypt an unencrypted private key on disk." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "decrypt - Decrypt an encrypted private key on disk." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "fingerprint/f [ip address] - Fingerprint your own key or that of someone else." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_WHITE + "Note: If you update a key, it will permanently delete the old key, so be careful!" + ANSI_RESET);
		System.out.println();
		System.out.println(ANSI_BOLD + ANSI_CYAN + "---P2P Network---" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "p2pconnect/p2p - Connect to the P2P network. If this is your first time connecting to the network, you will need to specify a node to connect to." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "p2pdisconnect/unp2p - Disconnect from the P2P network." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "viewpeers/vp - View all connected peer addresses." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "connectpeer/cp <ip address> - Connect to a peer by IP address." + ANSI_RESET);
		System.out.println();
		System.out.println(ANSI_BOLD + ANSI_CYAN + "---Miscellaneous---" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "copyright/c - Show the full copyright message." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "gui/g - Open the PacChat GUI." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "debug/d - Toggle log debug mode. Debug mode increases log verbosity." + ANSI_RESET);
	}
	
	public static void main(String[] args) {
		
		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
				case "help":
					printCLOHelp();
					break;
				case "version":
					System.out.println(VERSION);
					break;
				default:
					System.out.println("Unrecognized command line option. Try 'help'");
					break;
			}
			System.exit(0);
		}

		printCopyright();
		System.out.println();
		core_log.i("Initializing PacChat " + VERSION);
		core_log.d("Registering Bouncy Castle cryptography provider.");
		Security.addProvider(new BouncyCastleProvider());
		core_log.i("Creating installation if it doesn't already exist.");
		KeyManager.createInstallationIfNotExist(); //This function handles everything from the installation to key gen

		core_log.i("Loading keys from disk.");
		RSAPrivateKey privkey;
		
		if (KeyManager.keysEncrypted()) {
			core_log.i("Private key is encrypted!");
			core_log.i("Please enter password to decrypt. The password will not be shown.");
			System.out.print(ANSI_BOLD + ANSI_WHITE + "Decryption password: " + ANSI_RESET);
			char[] password = System.console().readPassword();
			core_log.i("Attempting decryption.");
			String b64Privkey_crypt = KeyManager.loadCryptedPrivkey();
			String b64Privkey = KeyManager.decryptPrivkey(b64Privkey_crypt, password);
			if (b64Privkey == null) {
				core_log.e("Decrypted private key is null, PacChat cannot be used without a valid private key. Exiting.");
				System.exit(1);
			}
			privkey = KeyManager.Base64ToPrivkey(b64Privkey);
		} else {
			core_log.w("Private key is unencrypted!");
			core_log.w("An unencrypted private key is a threat to security!");
			core_log.w("You can encrypt your key using the 'encrypt' command.");
			privkey = KeyManager.loadUnencryptedPrivkey();
		}

		keyPair = new KeyPair(KeyManager.loadPubkey(), privkey);

		core_log.i("Performing crypto tests..");
		core_log.i("Testing message crypto...");
		String testmsg = "test message";
		String cryptedMsg = MsgCrypto.encryptAndSignMessage(testmsg, (RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
		String decryptedMsg = MsgCrypto.decryptAndVerifyMessage(cryptedMsg, (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic()).getMessage();
		if (testmsg.equals(decryptedMsg)) {
			core_log.i("Message crypto test successful!");
		} else {
			core_log.e("Message crypto test failed! Something might break later on.");
			core_log.d("testmsg = " + testmsg);
			core_log.d("decryptedMsg = " + decryptedMsg);
		}
		
		core_log.i("Testing password based encryption (PBE)...");
		String testb64 = "test key";
		String cryptedKey = KeyManager.encryptPrivkey(Base64.encodeBase64String(testb64.getBytes()), "password".toCharArray());
		String decryptedKey = new String(Base64.decodeBase64(KeyManager.decryptPrivkey(cryptedKey, "password".toCharArray())));
		if (testb64.equals(decryptedKey)) {
			core_log.i("PBE test successful!");
		} else {
			core_log.e("PBE test failed! Something might break later on.");
			core_log.d("testb64 = " + testb64);
			core_log.d("decryptedKey = " + decryptedKey);
		}
		
		core_log.i("Testing key fingerprinting...");
		String fingerprint = KeyManager.fingerprint((RSAPublicKey) keyPair.getPublic());
		core_log.d("fingerprint = " + fingerprint);
		
		core_log.i("Crypto tests complete.");

		core_log.i("Retrieving IP addresses.");
		NetUtils.updateLocalIPAddr();
		NetUtils.updateExternalIPAddr();
		
		core_log.i("Starting servers.");
		server = new Server();
		server.start();
		
		try {
			Thread.sleep(500); //wait a little bit for the server and UPNP to start
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		p2p_server = new P2PServer();
		p2p_server.start();

		try {
			Thread.sleep(500); //wait a little bit for P2P server
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		clientChecker = new P2PClientChecker();
		clientChecker.start();

		core_log.i("PacChat is ready for use!");
		core_log.i("Entering chat mode, type exit to exit, and type send <ip address> to send a message.");
		core_log.i("Log debug mode is currently " + (Logger.debugEnabled() ? "ENABLED." : "DISABLED.") + " Use the 'debug' command to toggle debug mode.");
		core_log.w("Type 'help' for command help.");
		core_log.w("If applicable, run 'gui' to start the PacChat GUI.");
		active = true;
		while (active) {
			System.out.print(ANSI_BOLD + ANSI_BLUE + "Command: " + ANSI_RESET);
			String cmd_raw = stdin.nextLine();
			String[] cmd = cmd_raw.split(" "); //command and arguments
			
			doCommand(cmd);
		}

		core_log.i("Shutting down now.");

		//Shutdown sequence
		
		ArrayList<P2PClient> peers = P2PConnectionManager.getConnectedPeers();
		
		for (P2PClient peer : peers) {
			P2PConnectionManager.disconnectFromPeer(peer.getConnectedAddress());
		}

		if (UPNPManager.isOpen())
			UPNPManager.UPNPClosePorts();

		if (server != null)
			server.closeServer();
		
		if (p2p_server != null)
			p2p_server.closeP2PServer();

		System.exit(0);
	}
	
	public static void doCommand(String cmd_raw) {
		String[] cmd = cmd_raw.split(" ");
		doCommand(cmd);
	}
	
	private static void doCommand(String[] cmd) {
		switch (cmd[0]) {
			case "exit":
				core_log.i("Exiting chat mode.");
				active = false;
				break;
			case "s":
			case "send":
				if (cmd.length >= 2 && !cmd[1].isEmpty()) {
					core_log.i("Preparing to send message to IP address " + cmd[1]);
					core_log.i("Enter your message below, end with a single dot on its own line when finished, end with a single comma to cancel.");
					core_log.i("The message will not include the single dot at the end.");
					if (p2p)
						core_log.w("This message will be sent over the P2P network.");
					StringBuilder msgBuilder = new StringBuilder();
					String buf;
					
					while (!(buf = stdin.nextLine()).equals(".")) {
						if (!buf.equals(",")) {
							msgBuilder.append(buf).append("\n");
						} else {
							break;
						}
					}
					
					if (buf.equals(".")) {
						core_log.i("Message accepted, attempting to send to target.");
						String msg = msgBuilder.toString();
						if (!KeyManager.checkIfIPKeyExists(cmd[1])) {
							KeyManager.downloadKeyFromIP(cmd[1]);
						}
						
						if (p2p) {
							if (KeyManager.checkIfIPKeyExists(cmd[1]))
								P2PConnectionManager.sendChat(msg, KeyManager.loadKeyByIP(cmd[1]), (RSAPrivateKey) keyPair.getPrivate());
							else
								core_log.e("Target's key does not exist. You can try to download their key using by running 'getkey " + cmd[1] + "'");
						} else {
							boolean success = Client.sendMessage(msg, cmd[1]);
							if (success) {
								core_log.i("Message sent successfully!");
							} else {
								core_log.e("Message was not sent successfully!");
							}
						}
					} else if (buf.equals(",")) {
						core_log.i("Message cancelled.");
					}
				} else {
					core_log.e("An IP address was not specified.");
				}
				break;
			case "help":
				printHelpMsg();
				break;
			case "r":
			case "reply":
				if (server == null || !server.isActive()) {
					core_log.e("Server is not running.");
					break;
				}
				if (server.getLastSender().isEmpty()) {
					core_log.e("No one has sent us a message yet.");
					break;
				}
				
				core_log.i("Replying to last sender IP address.");
				core_log.i("Preparing to send message to IP address " + server.getLastSender());
				core_log.i("Enter your message below, end with a single dot on its own line when finished, end with a single comma to cancel.");
				core_log.i("The message will not include the single dot at the end.");
				StringBuilder msgBuilder = new StringBuilder();
				String buf;
				while (!(buf = stdin.nextLine()).equals(".")) {
					if (!buf.equals(",")) {
						msgBuilder.append(buf).append("\n");
					} else {
						break;
					}
				}
				if (buf.equals(".")) {
					core_log.i("Message accepted, attempting to send to target.");
					String msg = msgBuilder.toString();
					//TODO reply over P2P
					boolean success = Client.sendMessage(msg, server.getLastSender());
					if (success) {
						core_log.i("Message sent successfully!");
					} else {
						core_log.e("Message was not sent successfully!");
					}
				} else if (buf.equals(",")) {
					core_log.i("Message cancelled.");
				}
				break;
			case "ss":
			case "startserver":
				if (server == null || !server.isActive()) {
					core_log.i("Starting server.");
					server = new Server();
					server.start();
					try {
						Thread.sleep(500); //wait for the server to start
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (guiPossible)
						gui.updateServerText(server.isActive());
				} else
					core_log.i("Server is already running.");
				break;
			case "hs":
			case "haltserver":
				if (server != null && server.isActive()) {
					core_log.i("Stopping server.");
					server.closeServer();
					if (guiPossible)
						gui.updateServerText(server.isActive());
					server = null;
				} else
					core_log.i("Server is not running.");
				break;
			case "rs":
			case "restartserver":
				if (server != null && server.isActive()) {
					core_log.i("Restarting server.");
					server.closeServer();
					server = new Server();
					server.start();
					try {
						Thread.sleep(500); //wait for the server to start
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (guiPossible)
						gui.updateServerText(server.isActive());
					core_log.i("Server restarted.");
				} else {
					core_log.i("Server is not running. Starting server.");
					server = new Server();
					server.start();
					try {
						Thread.sleep(500); //wait for the server to start
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case "u":
			case "update":
				if (cmd.length >= 2 && !cmd[1].isEmpty()) {
					core_log.i("Requesting that the server at " + cmd[1] + " update their copy of your key.");
					Client.incrementKUC_ID();
					KeyUpdateClient kuc = new KeyUpdateClient(Client.getKUC_ID(), cmd[1]);
					kuc.start();
					try {
						Thread.sleep(500); //wait for request to be made, makes output less sloppy
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					core_log.e("An IP address was not specified.");
				}
				break;
			case "ua":
			case "updateaccept":
				if (cmd.length >= 2 && !cmd[1].isEmpty()) {
					core_log.i("Accepting update with ID " + cmd[1]);
					KeyUpdate update = KeyUpdateManager.getIncomingUpdate(Long.parseLong(cmd[1]));
					if (update == null) {
						core_log.e("Update ID " + cmd[1] + " does not exist or is not incoming.");
						break;
					}
					update.acceptUpdate();
					KeyUpdateManager.completeIncomingUpdate(Long.parseLong(cmd[1]), update);
				} else {
					core_log.e("An update ID was not specified.");
				}
				break;
			case "ur":
			case "updatereject":
				if (cmd.length >= 2 && !cmd[1].isEmpty()) {
					core_log.i("Rejecting update with ID " + cmd[1]);
					KeyUpdate update = KeyUpdateManager.getIncomingUpdate(Long.parseLong(cmd[1]));
					if (update == null) {
						core_log.e("Update ID " + cmd[1] + " does not exist or is not incoming.");
						break;
					}
					update.rejectUpdate();
					KeyUpdateManager.completeIncomingUpdate(Long.parseLong(cmd[1]), update);
				} else {
					core_log.e("An update ID was not specified.");
				}
				break;
			case "ui":
			case "updateinfo":
				if (cmd.length >= 2 && !cmd[1].isEmpty()) {
					KeyUpdate update = KeyUpdateManager.getIncomingUpdate(Long.parseLong(cmd[1]));
					if (update == null) {
						core_log.e("Update ID " + cmd[1] + " does not exist.");
						break;
					}
					if (!update.isProcessed()) {
						core_log.w((update.isOutgoing() ? "[OUTGOING]" : "[INCOMING]") + " [PENDING] Update ID " + cmd[1] + " source IP = " + update.getAddress());
					} else {
						if (update.isAccepted()) {
							core_log.i((update.isOutgoing() ? "[OUTGOING]" : "[INCOMING]") + " [ACCEPTED] Update ID " + cmd[1] + " source IP = " + update.getAddress());
						} else {
							core_log.i((update.isOutgoing() ? "[OUTGOING]" : "[INCOMING]") + " [REJECTED] Update ID " + cmd[1] + " source IP = " + update.getAddress());
						}
					}
				} else {
					core_log.e("An update ID was not specified.");
				}
				break;
			case "ul":
			case "updatelist":
				Collection<Long> incomingKeys = KeyUpdateManager.getAllIncomingKeys();
				ArrayList<Long> ids = new ArrayList<>();
				ids.addAll(incomingKeys);
				for (Long id : ids) {
					KeyUpdate update = KeyUpdateManager.getIncomingUpdate(id);
					if (update == null) {
						core_log.e("Update ID " + id + " does not exist.");
						break;
					}
					if (!update.isProcessed()) {
						core_log.w((update.isOutgoing() ? "[OUTGOING]" : "[INCOMING]") + " [PENDING] Update ID " + id + " source IP = " + update.getAddress());
					} else {
						if (update.isAccepted()) {
							core_log.i((update.isOutgoing() ? "[OUTGOING]" : "[INCOMING]") + " [ACCEPTED] Update ID " + id + " source IP = " + update.getAddress());
						} else {
							core_log.i((update.isOutgoing() ? "[OUTGOING]" : "[INCOMING]") + " [REJECTED] Update ID " + id + " source IP = " + update.getAddress());
						}
					}
				}
				if (ids.size() == 0) {
					core_log.i("No key updates.");
				}
				break;
			case "g":
			case "gui":
				if (guiPossible) {
					core_log.i("Opening GUI.");
					gui = new PacchatGUI();
					gui.setVisible(true);
				} else {
					core_log.e("Cannot open GUI, graphics environment is headless.");
				}
				break;
			case "p2p":
			case "p2pconnect":
				core_log.i("Connecting to P2P network.");
				if (PeerManager.firstTime()) {
					core_log.w("It appears that this is your first time connecting to the PacChat network.");
					core_log.i("You will need to provide 1 node's IP address to connect to the P2P network.");
					System.out.print(ANSI_BOLD + ANSI_BLUE + "Peer IP: " + ANSI_RESET);
					String ip = stdin.nextLine();
					P2PConnectionManager.connectToPeer(ip);
					if (!P2PConnectionManager.connectedToPeer(ip)) {
						core_log.e("Error connecting to specified peer!");
						break;
					}
					P2PClient peer = P2PConnectionManager.getConnectedPeers().get(0);
					peer.getaddr();
					PeerManager.writePeersToDisk();
					core_log.i("Initialized P2P network. Please run 'p2pconnect' or 'p2p' again to connect to any new peers.");
					p2p = P2PConnectionManager.havePeers();
					if (p2p)
						core_log.i("P2P network is now preferred over direct communication and it will be used when applicable.");
					else
						core_log.e("Could not reach any peers! P2P shall remain disabled. You can run the command 'connectpeer <ip address>' to connect to peers.");
				} else {
					P2PConnectionManager.init();
					p2p = P2PConnectionManager.havePeers();
					if (p2p)
						core_log.i("P2P network is now preferred over direct communication and it will be used when applicable.");
					else
						core_log.e("Could not reach any peers! P2P shall remain disabled. You can run the command 'connectpeer <ip address>' to connect to peers.");
				}
				break;
			case "unp2p":
			case "p2pdisconnect":
				if (!p2p)
					core_log.e("P2P is already disabled.");
				else {
					core_log.i("Disabling P2P network usage and disconnecting from all peers.");
					ArrayList<P2PClient> peerList = P2PConnectionManager.getConnectedPeers();
					for (P2PClient peer : peerList) {
						P2PConnectionManager.disconnectFromPeer(peer.getConnectedAddress());
					}
					p2p = false;
				}
				break;
			case "vp":
			case "viewpeers":
				if (!p2p)
					core_log.e("P2P is currently disabled. Run the 'p2p' command to enable P2P.");
				ArrayList<P2PClient> peers = P2PConnectionManager.getConnectedPeers();
				for (int i = 0; i < peers.size(); i++) {
					core_log.i("Peer " + i + ": " + peers.get(i).getConnectedAddress());
				}
				break;
			case "cp":
			case "connectpeer":
				if (cmd.length >= 2 && !cmd[1].isEmpty()) {
					if (cmd[1].equals("127.0.0.1") || cmd[1].equalsIgnoreCase("localhost") || cmd[1].isEmpty()) {
						core_log.e("Invalid IP address!");
					} else if (P2PConnectionManager.connectedToPeer(cmd[1])) {
						core_log.e("Already connected to this peer!");
					} else {
						core_log.i("Connecting to peer " + cmd[1]);
						P2PConnectionManager.connectToPeer(cmd[1]);
						if (P2PConnectionManager.connectedToPeer(cmd[1])) {
							core_log.i("Connection successful, P2P enabled!");
							p2p = true;
						}
					}
				} else {
					core_log.e("An IP address was not specified.");
				}
				break;
			case "getkey":
			case "gk":
				if (cmd.length >= 2 && !cmd[1].isEmpty()) {
					RSAPublicKey key = KeyManager.downloadKeyFromIP(cmd[1]);
					KeyManager.saveKeyByIP(cmd[1], key);
				} else {
					core_log.e("An IP address was not specified.");
				}
				break;
			case "d":
			case "debug":
				Logger.toggleDebug();
				core_log.i(Logger.debugEnabled() ? "Debug mode enabled." : "Debug mode disabled.");
				core_log.d("Example of a debug message");
				break;
			case "encrypt":
				if (KeyManager.keysEncrypted()) {
					core_log.e("Keys are already encrypted!");
					break;
				}
				core_log.i("Encrypting private key, please enter your password below.");
				core_log.i("The password will not be shown. To cancel encryption, enter a blank password.");
				core_log.w("------------------------------------------------------------------");
				core_log.w("!!!!WARNING: IF YOU LOSE YOUR PASSWORD, YOU WILL LOSE ACCESS TO YOUR PRIVATE KEY!!!!");
				core_log.w("------------------------------------------------------------------");
				System.out.print(ANSI_BOLD + ANSI_WHITE + "Encryption password: " + ANSI_RESET);
				char[] password_encrypt = System.console().readPassword();
				if (password_encrypt.length == 0) {
					core_log.i("Cancelled encryption.");
					break;
				}
				System.out.print(ANSI_BOLD + ANSI_WHITE + "Encryption password (confirm): " + ANSI_RESET);
				char[] password_encrypt_confirm = System.console().readPassword();
				if (password_encrypt_confirm.length == 0) {
					core_log.i("Cancelled encryption.");
					break;
				}
				
				if (!Arrays.equals(password_encrypt, password_encrypt_confirm)) {
					core_log.e("Passwords did not match! Aborting encryption.");
					break;
				}
				
				String encryptedB64 = KeyManager.encryptPrivkey(Base64.encodeBase64String(keyPair.getPrivate().getEncoded()), password_encrypt);
				if (encryptedB64 == null) {
					core_log.e("Encrypted private key is null, aborting encryption.");
					break;
				}
				
				core_log.i("Encrypted key successfully, saving to disk.");
				KeyManager.saveEncryptedKeys(encryptedB64, (RSAPublicKey) keyPair.getPublic());
				break;
			case "decrypt":
				if (!KeyManager.keysEncrypted()) {
					core_log.e("Keys are not encrypted!");
					break;
				}
				core_log.w("An unencrypted private key is a threat to security!");
				core_log.w("If you do not wish to decrypt, enter a blank password below.");
				System.out.print(ANSI_BOLD + ANSI_WHITE + "Decryption password: " + ANSI_RESET);
				char[] password_decrypt = System.console().readPassword();
				if (password_decrypt.length == 0) {
					core_log.i("Cancelled decryption.");
					break;
				}
				String currentPrivkey = Base64.encodeBase64String(keyPair.getPrivate().getEncoded());
				String privkeyB64 = KeyManager.decryptPrivkey(KeyManager.loadCryptedPrivkey(), password_decrypt);
				if (privkeyB64 == null) {
					core_log.e("Decrypted private key is null, aborting decryption.");
					break;
				}
				
				if (currentPrivkey.equals(privkeyB64)) {
					core_log.i("Decryption successful. Saving unencrypted key to disk.");
					KeyManager.saveUnencryptedKeys((RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
				} else {
					core_log.e("Decrypted private key doesn't match current private key. Incorrect password?");
				}
				break;
			case "f":
			case "fingerprint":
				if (cmd.length == 1) {
					String fingerprint = KeyManager.fingerprint((RSAPublicKey) keyPair.getPublic());
					core_log.i("Your key fingerprint is: '" + fingerprint + "'");
				} else {
					if (!KeyManager.checkIfIPKeyExists(cmd[1])) {
						core_log.e("Key for IP " + cmd[1] + " doesn't exist!");
					} else {
						String fingerprint = KeyManager.fingerprint(KeyManager.loadKeyByIP(cmd[1]));
						core_log.i(cmd[1] + " key fingerprint is: '" + fingerprint + "'");
					}
				}
				break;
			case "c":
			case "copyright":
				printFullCopyright();
				break;
			case "":
				break;
			default:
				core_log.e("Invalid chat command!");
				printHelpMsg();
				break;
		}
	}
}


