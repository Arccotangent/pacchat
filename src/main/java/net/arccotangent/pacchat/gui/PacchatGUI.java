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

package net.arccotangent.pacchat.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import net.arccotangent.pacchat.Main;
import net.arccotangent.pacchat.filesystem.ContactManager;
import net.arccotangent.pacchat.filesystem.KeyManager;
import net.arccotangent.pacchat.net.Client;
import net.arccotangent.pacchat.net.p2p.P2PConnectionManager;
import org.apache.commons.codec.binary.Base64;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.interfaces.RSAPrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PacchatGUI extends JFrame {
	private JTextArea copyrightPanel;
	private JPanel mainGui;
	private JTabbedPane tabbedPane1;
	private JButton haltServerButton;
	private JButton restartServerButton;
	private JButton startServerButton;
	private JTextArea serverTextArea;
	private JTextArea keyTextArea;
	private JButton downloadKeyButton;
	private JButton requestKeyUpdateButton;
	private JButton viewRawKeyButton;
	private JButton sendButton;
	private JTextField ipField;
	private JTextArea enteredText;
	private JButton selectButton;
	private JButton addButton;
	private JButton deleteButton;
	private JList<String> contactList;
	private JTextArea messagePanel;
	private JLabel contactName;
	private JScrollPane messagePanelScroll;
	private JTextPane instructionsArea;
	
	public PacchatGUI() {
		super("PacChat GUI");
		setContentPane(mainGui);
		initKeyManager();
		initServerManager();
		initMessages();
		initContactManager();
		initCopyrightGui();
		pack();
		setSize(700, 400);
	}
	
	private void updateContactList() {
		ArrayList<String> contacts = ContactManager.getAllContacts();
		DefaultListModel<String> model = (DefaultListModel<String>) contactList.getModel();
		
		assert contacts != null;
		model.clear();
		
		for (String contact : contacts) {
			model.addElement(contact);
		}
		
		contactList.setModel(model);
	}
	
	private String getTime() {
		Calendar c = Calendar.getInstance();
		Date d = c.getTime();
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS aa z");
		return df.format(d);
	}
	
	public void addReceivedMessage(String sender, String msg, boolean verified) {
		messagePanel.setText(messagePanel.getText() + "\n[" + getTime() + "] [" + (verified ? "VERIFIED" : "NOT VERIFIED") + "] " + sender + ": " + msg);
	}
	
	private void initMessages() {
		
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = ipField.getText();
				String msg = enteredText.getText();
				if (Main.isP2PEnabled()) {
					P2PConnectionManager.sendChat(msg, KeyManager.loadKeyByIP(ip), (RSAPrivateKey) Main.getKeypair().getPrivate());
					messagePanel.append("\n[" + getTime() + "] You -> P2P Network -> " + ip + ": " + msg);
				} else {
					boolean success = Client.sendMessage(msg, ip);
					messagePanel.append("\n[" + getTime() + "] " + (success ? "[SUCCESSFUL]" : "[FAILURE]") + " You -> " + ip + ": " + msg);
				}
				
				JScrollBar verticalBar = messagePanelScroll.getVerticalScrollBar();
				verticalBar.setValue(verticalBar.getMaximum()); //scroll to bottom
				
				enteredText.setText("");
			}
		});
	}
	
	private void initContactManager() {
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String addIp = JOptionPane.showInputDialog(null, "Enter IP of new contact", "New Contact", JOptionPane.QUESTION_MESSAGE);
				String addName = JOptionPane.showInputDialog(null, "Enter name of new contact, " + addIp, "New Contact", JOptionPane.QUESTION_MESSAGE);
				
				if (addName.contains(":") || addIp.contains(":")) {
					JOptionPane.showMessageDialog(null, "The name or IP address cannot contain a colon.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ContactManager.addContact(addName, addIp);
				updateContactList();
			}
		});
		
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String rawContact = contactList.getSelectedValue();
				String[] ip = rawContact.split(":");
				contactName.setText(ip[0]);
				ipField.setText(ip[1]);
				tabbedPane1.setSelectedIndex(1); //Message tab
			}
		});
		
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = contactList.getSelectedIndex();
				ContactManager.deleteByIndex(index);
				updateContactList();
			}
		});
		
		updateContactList();
	}
	
	private void initServerManager() {
		
		haltServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Main.doCommand("haltserver");
			}
		});
		
		startServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Main.doCommand("startserver");
			}
		});
		
		restartServerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Main.doCommand("restartserver");
			}
		});
	}
	
	private void initKeyManager() {
		
		downloadKeyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = JOptionPane.showInputDialog(null, "Enter the IP from which you would like to download the public key.", "Download Key", JOptionPane.QUESTION_MESSAGE);
				KeyManager.loadKeyByIP(ip);
			}
		});
		
		requestKeyUpdateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = JOptionPane.showInputDialog(null, "Here you can request that a server update their copy of your key. The server operator must manually accept or reject the key update.\n\nEnter the IP from which you would like to request a key update.\nYou can view the results of the key update in your PacChat log.", "Request Key Update", JOptionPane.QUESTION_MESSAGE);
				Main.doCommand("update " + ip);
			}
		});
		
		viewRawKeyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int ownKey = JOptionPane.showConfirmDialog(null, "Would you like to view your own public key?", "View Raw Key", JOptionPane.YES_NO_OPTION);
				if (ownKey == JOptionPane.YES_OPTION) {
					byte[] publicKeyBytes = KeyManager.loadPubkey().getEncoded();
					String pubkeyB64 = Base64.encodeBase64String(publicKeyBytes);
					keyTextArea.setText(pubkeyB64);
				} else {
					String ip = JOptionPane.showInputDialog(null, "Enter the IP of the person whose key you would like to view.", "View Raw Public Key", JOptionPane.QUESTION_MESSAGE);
					byte[] publicKeyBytes = KeyManager.loadKeyByIP(ip).getEncoded();
					String pubkeyB64 = Base64.encodeBase64String(publicKeyBytes);
					keyTextArea.setText(pubkeyB64);
				}
			}
		});
		
	}
	
	private void initCopyrightGui() {
		copyrightPanel.setText("PacChat - Direct P2P secure, encrypted private chats\nCopyright (C) 2016 Arccotangent\n\nThis program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses.");
	}
	
	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}
	
	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		mainGui = new JPanel();
		mainGui.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1 = new JTabbedPane();
		tabbedPane1.setForeground(new Color(-16777216));
		tabbedPane1.setTabPlacement(1);
		mainGui.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Welcome", panel1);
		final JScrollPane scrollPane1 = new JScrollPane();
		panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		instructionsArea = new JTextPane();
		instructionsArea.setContentType("text/html");
		instructionsArea.setEditable(false);
		instructionsArea.setFont(UIManager.getFont("Label.font"));
		instructionsArea.setText("<html>\n  <head>\n    \n  </head>\n  <body>\n    <h1>\n      PacChat GUI Usage Instructions\n    </h1>\n    <br>\n    \n\n    <h2>\n      Tabs\n    </h2>\n    <p>\n      Messages - Send and receive messages.<br>Contacts - Manage your contacts.<br>Key \n      Manager - Manage keys, key updates, and download public keys from other \n      people.<br>Server Manager - Manage your server, essentially whether you \n      can receive messages or not.<br>About - For the time being, a simple \n      copyright message.<br>\n    </p>\n    <h2>\n      Basic Usage\n    </h2>\n    <p>\n      To send messages to other people, you need their IP address or a domain \n      that resolves to their IP address.<br>Navigate to the Messages tab.<br><br>1. \n      Type the IP address/domain in the small bottom text box.<br>2. Type your \n      message in the text box directly above the IP text box.<br>3. Click \n      Send, and your message has been sent.<br>\n    </p>\n    <h2>\n      Miscellaneous\n    </h2>\n    <h3>\n      Links\n    </h3>\n    <p>\n      If you would like to follow development or contribute, check out our \n      GitHub repository linked below.<br>GitHub Repository: <a href=\"https://github.com/Arccotangent/pacchat\">https://github.com/Arccotangent/pacchat</a><br>\n    </p>\n    <h3>\n      Bug Reporting\n    </h3>\n    <p>\n      Bugs should be reported on the GitHub issue tracker.<br>Link: <a href=\"https://github.com/Arccotangent/pacchat/issues\">https://github.com/Arccotangent/pacchat/issues</a><br>\n    </p>\n  </body>\n</html>\n");
		scrollPane1.setViewportView(instructionsArea);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Messages", panel2);
		sendButton = new JButton();
		sendButton.setText("Send");
		panel2.add(sendButton, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		messagePanelScroll = new JScrollPane();
		panel2.add(messagePanelScroll, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		messagePanel = new JTextArea();
		messagePanel.setEnabled(false);
		messagePanel.setForeground(new Color(-16777216));
		messagePanel.setLineWrap(true);
		messagePanel.setToolTipText("Sent/received messages are displayed here");
		messagePanel.setWrapStyleWord(true);
		messagePanelScroll.setViewportView(messagePanel);
		ipField = new JTextField();
		ipField.setForeground(new Color(-16777216));
		ipField.setText("IP");
		ipField.setToolTipText("Type the IP address of the person to whom you are sending this message");
		panel2.add(ipField, new GridConstraints(2, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		contactName = new JLabel();
		contactName.setText("No Contact Selected");
		panel2.add(contactName, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane2 = new JScrollPane();
		panel2.add(scrollPane2, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		enteredText = new JTextArea();
		enteredText.setForeground(new Color(-16777216));
		enteredText.setLineWrap(true);
		enteredText.setText("Message");
		enteredText.setToolTipText("Type your message here");
		enteredText.setWrapStyleWord(true);
		scrollPane2.setViewportView(enteredText);
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Contacts", panel3);
		addButton = new JButton();
		addButton.setText("Add");
		panel3.add(addButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		contactList = new JList();
		contactList.setForeground(new Color(-16777216));
		final DefaultListModel defaultListModel1 = new DefaultListModel();
		contactList.setModel(defaultListModel1);
		panel3.add(contactList, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		deleteButton = new JButton();
		deleteButton.setText("Delete");
		panel3.add(deleteButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		selectButton = new JButton();
		selectButton.setText("Select");
		panel3.add(selectButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		panel4.setEnabled(true);
		tabbedPane1.addTab("Key Manager", panel4);
		keyTextArea = new JTextArea();
		keyTextArea.setEditable(false);
		keyTextArea.setForeground(new Color(-16777216));
		keyTextArea.setLineWrap(true);
		keyTextArea.setWrapStyleWord(true);
		panel4.add(keyTextArea, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		downloadKeyButton = new JButton();
		downloadKeyButton.setText("Download Key");
		panel4.add(downloadKeyButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		requestKeyUpdateButton = new JButton();
		requestKeyUpdateButton.setText("Request Key Update");
		panel4.add(requestKeyUpdateButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		viewRawKeyButton = new JButton();
		viewRawKeyButton.setText("View Raw Key");
		panel4.add(viewRawKeyButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel5 = new JPanel();
		panel5.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Server Manager", panel5);
		haltServerButton = new JButton();
		haltServerButton.setText("Halt Server");
		panel5.add(haltServerButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		restartServerButton = new JButton();
		restartServerButton.setText("Restart Server");
		panel5.add(restartServerButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		startServerButton = new JButton();
		startServerButton.setText("Start Server");
		panel5.add(startServerButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		serverTextArea = new JTextArea();
		serverTextArea.setEditable(false);
		serverTextArea.setForeground(new Color(-16777216));
		serverTextArea.setLineWrap(true);
		serverTextArea.setWrapStyleWord(true);
		panel5.add(serverTextArea, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		final JPanel panel6 = new JPanel();
		panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("About", panel6);
		final JScrollPane scrollPane3 = new JScrollPane();
		panel6.add(scrollPane3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		copyrightPanel = new JTextArea();
		copyrightPanel.setEditable(false);
		copyrightPanel.setForeground(new Color(-16777216));
		copyrightPanel.setLineWrap(true);
		copyrightPanel.setText("");
		copyrightPanel.setWrapStyleWord(true);
		scrollPane3.setViewportView(copyrightPanel);
	}
	
	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return mainGui;
	}
}
