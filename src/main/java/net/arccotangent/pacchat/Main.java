package net.arccotangent.pacchat;

import net.arccotangent.pacchat.crypto.MsgCrypto;
import net.arccotangent.pacchat.filesystem.KeyManager;
import net.arccotangent.pacchat.logging.Logger;
import net.arccotangent.pacchat.net.Client;
import net.arccotangent.pacchat.net.NetUtils;
import net.arccotangent.pacchat.net.Server;
import net.arccotangent.pacchat.net.UPNPManager;

import java.security.KeyPair;
import java.util.Scanner;

public class Main {
	
	private static Logger core_log = new Logger("CORE");
	private static final String VERSION = "20161014-1";
	private static KeyPair keyPair;
	private static final String ANSI_BOLD = "\u001B[1m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_RESET = "\u001B[0m";
	
	public static KeyPair getKeypair(){
		return keyPair;
	}
	
	private static void printHelpMsg() {
		System.out.println(ANSI_BOLD + ANSI_BLUE + "help - This help message" + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "exit - Exit chat mode and shut down PacChat." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "send/s <ip address> - Send a message. PacChat will prompt you to enter your message after you enter the command." + ANSI_RESET);
		System.out.println(ANSI_BOLD + ANSI_BLUE + "reply/r - Reply to the last person to send you a message." + ANSI_RESET);
	}
	
	public static void main(String[] args) {
		core_log.i("Initializing PacChat " + VERSION);
		core_log.i("Creating installation if it doesn't already exist.");
		KeyManager.createInstallationIfNotExist(); //This function handles everything from the installation to key gen
		
		core_log.i("Loading keys from disk.");
		keyPair = KeyManager.loadRSAKeys();
		
		assert keyPair != null;
		
		core_log.i("Performing crypto test..");
		String testmsg = "test message";
		String crypted = MsgCrypto.encryptAndSignMessage(testmsg, keyPair.getPublic(), keyPair.getPrivate());
		if (testmsg.equals(MsgCrypto.decryptAndVerifyMessage(crypted, keyPair.getPrivate(), keyPair.getPublic()))) {
			core_log.i("Crypto test successful!");
		} else {
			core_log.e("Crypto test failed!");
		}
		
		NetUtils.updateLocalIPAddr();
		Server server = new Server();
		server.start();
		
		try {
			Thread.sleep(1000); //wait a little bit for the server and UPNP to start
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		core_log.i("PacChat is ready for use!");
		core_log.i("Entering chat mode, type exit to exit, and type send <ip address> to send a message.");
		core_log.i("Type 'help' for command help.");
		boolean active = true;
		Scanner stdin = new Scanner(System.in);
		
		while (active) {
			System.out.print(ANSI_BOLD + ANSI_BLUE + "Command: " + ANSI_RESET);
			String cmd_str = stdin.nextLine();
			String[] cmd = cmd_str.split(" "); //command and arguments
			
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
						Client.sendMessage(msg, server.getLastSender());
					} else if (buf.equals(",")) {
						core_log.i("Message cancelled.");
					}
					break;
				default:
					core_log.e("Invalid chat command!");
					printHelpMsg();
					break;
			}
		}
		
		core_log.i("Shutting down now.");
		
		//Shutdown sequence
		if (UPNPManager.isOpen())
			UPNPManager.UPNPClosePorts();
		server.closeServer();
		
		System.exit(0);
	}
	
}
