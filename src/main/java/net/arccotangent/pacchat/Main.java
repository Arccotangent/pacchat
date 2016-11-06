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
import net.arccotangent.pacchat.net.p2p.P2PClient;
import net.arccotangent.pacchat.net.p2p.P2PConnectionManager;
import net.arccotangent.pacchat.net.p2p.P2PServer;
import net.arccotangent.pacchat.net.p2p.PeerManager;

import java.awt.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class Main {
	private static final Logger core_log = new Logger("CORE");
	public static final String VERSION = "0.2-B15";
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
	
	private static Scanner stdin = new Scanner(System.in);
	
	public static void addReceivedMessageToGui(String sender, String msg, boolean verified) {
		gui.addReceivedMessage(sender, msg, verified);
	}
	
	public static Server getServer() {
		return server;
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
		System.out.println(ANSI_BOLD + ANSI_BLUE + "updatelist/ul <ID> - List all pending update IDs." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "updateinfo/ui <ID> - Print info about a pending update request with the specified ID." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "getkey/gk <ip address> - Download a key from the specified IP address." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_WHITE + "Note: If you update a key, it will permanently delete the old key, so be careful!" + ANSI_RESET);
		System.out.println();
		System.out.println(ANSI_BOLD + ANSI_CYAN + "---P2P Network---" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "p2pconnect/p2p - Connect to the P2P network. If this is your first time connecting to the network, you will need to specify a node to connect to." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "p2pdisconnect/unp2p - Disconnect from the P2P network." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "viewpeers/vp - View all connected peer addresses." + ANSI_RESET);
		System.out.println();
		System.out.println(ANSI_BOLD + ANSI_CYAN + "---Miscellaneous---" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "copyright/c - Show the full copyright message." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "gui/g - Open the PacChat GUI." + ANSI_RESET);
	}
	
	public static void main(String[] args) {

		printCopyright();
		System.out.println();
		core_log.i("Initializing PacChat " + VERSION);
		core_log.i("Creating installation if it doesn't already exist.");
		KeyManager.createInstallationIfNotExist(); //This function handles everything from the installation to key gen

		core_log.i("Loading keys from disk.");
		keyPair = KeyManager.loadRSAKeys();

		assert keyPair != null;

		core_log.i("Performing crypto test..");
		String testmsg = "test message";
		String crypted = MsgCrypto.encryptAndSignMessage(testmsg, keyPair.getPublic(), keyPair.getPrivate());
		if (testmsg.equals(MsgCrypto.decryptAndVerifyMessage(crypted, keyPair.getPrivate(), keyPair.getPublic()).getMessage())) {
			core_log.i("Crypto test successful!");
		} else {
			core_log.e("Crypto test failed! Something might break later on.");
		}

		NetUtils.updateLocalIPAddr();
		NetUtils.updateExternalIPAddr();
		core_log.i("Starting server.");
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

		core_log.i("PacChat is ready for use!");
		core_log.i("Entering chat mode, type exit to exit, and type send <ip address> to send a message.");
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
						
						if (p2p)
							P2PConnectionManager.sendChat(msg, KeyManager.loadKeyByIP(cmd[1]), keyPair.getPrivate());
						else
							Client.sendMessage(msg, cmd[1]);
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
			case "":
				break;
			case "r":
			case "reply":
				if (server == null) {
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
					Client.sendMessage(msg, server.getLastSender());
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
				} else
					core_log.i("Server is already running.");
				break;
			case "hs":
			case "haltserver":
				if (server != null && server.isActive()) {
					core_log.i("Stopping server.");
					server.closeServer();
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
					KeyUpdate update = KeyUpdateManager.getUpdate(Long.parseLong(cmd[1]));
					if (update == null) {
						core_log.e("Update ID " + cmd[1] + " does not exist.");
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
					KeyUpdate update = KeyUpdateManager.getUpdate(Long.parseLong(cmd[1]));
					if (update == null) {
						core_log.e("Update ID " + cmd[1] + " does not exist.");
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
					KeyUpdate update = KeyUpdateManager.getUpdate(Long.parseLong(cmd[1]));
					if (update == null) {
						core_log.e("Update ID " + cmd[1] + " does not exist.");
						break;
					}
					if (!update.isProcessed()) {
						core_log.w("[PENDING] Update ID " + cmd[1] + " source IP = " + update.getSource());
					} else {
						if (update.isAccepted()) {
							core_log.i("[ACCEPTED] Update ID " + cmd[1] + " source IP = " + update.getSource());
						} else {
							core_log.i("[REJECTED] Update ID " + cmd[1] + " source IP = " + update.getSource());
						}
					}
				} else {
					core_log.e("An update ID was not specified.");
				}
				break;
			case "ul":
			case "updatelist":
				Collection<Long> keys = KeyUpdateManager.getAllIncomingKeys();
				ArrayList<Long> ids = new ArrayList<>();
				ids.addAll(keys);
				for (Long id : ids) {
					KeyUpdate update = KeyUpdateManager.getUpdate(id);
					if (update == null) {
						core_log.e("Update ID " + id + " does not exist.");
						break;
					}
					if (!update.isProcessed()) {
						core_log.w("[PENDING] Update ID " + id + " source IP = " + update.getSource());
					} else {
						if (update.isAccepted()) {
							core_log.i("[ACCEPTED] Update ID " + id + " source IP = " + update.getSource());
						} else {
							core_log.i("[REJECTED] Update ID " + id + " source IP = " + update.getSource());
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
					P2PClient peer = P2PConnectionManager.getConnectedPeers().get(0);
					peer.getaddr();
					PeerManager.writePeersToDisk();
					core_log.i("Initialized P2P network. Please run 'p2pconnect' or 'p2p' again to connect to any new peers.");
					p2p = true;
					core_log.i("P2P network is now preferred over direct communication and it will be used when applicable.");
				} else {
					P2PConnectionManager.init();
					p2p = true;
					core_log.i("P2P network is now preferred over direct communication and it will be used when applicable.");
				}
				break;
			case "unp2p":
			case "p2pdisconnect":
				core_log.i("Disabling P2P network usage and disconnecting from all peers.");
				ArrayList<P2PClient> peerList = P2PConnectionManager.getConnectedPeers();
				for (P2PClient peer : peerList) {
					P2PConnectionManager.disconnectFromPeer(peer.getConnectedAddress());
				}
				p2p = false;
				break;
			case "vp":
			case "viewpeers":
				ArrayList<P2PClient> peers = P2PConnectionManager.getConnectedPeers();
				for (int i = 0; i < peers.size(); i++) {
					core_log.i("Peer " + i + ": " + peers.get(i).getConnectedAddress());
				}
				break;
			case "getkey":
			case "gk":
				if (cmd.length >= 2 && !cmd[1].isEmpty()) {
					PublicKey key = KeyManager.downloadKeyFromIP(cmd[1]);
					KeyManager.saveKeyByIP(cmd[1], key);
				} else {
					core_log.e("An IP address was not specified.");
				}
				break;
			case "c":
			case "copyright":
				printFullCopyright();
				break;
			default:
				core_log.e("Invalid chat command!");
				printHelpMsg();
				break;
		}
	}
}


