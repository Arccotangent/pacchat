package net.arccotangent.pacchat.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import net.arccotangent.pacchat.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PacchatGUI extends JFrame {
	private JTextPane copyrightPanel;
	private JPanel mainGui;
	private JTabbedPane tabbedPane1;
	private JButton haltServerButton;
	private JButton restartServerButton;
	private JButton startServerButton;
	private JTextPane textPane1;
	private JPanel copyrightGui;
	private JButton backButton;
	
	public PacchatGUI() {
		super("PacChat GUI");
		setContentPane(mainGui);
		initServerManager();
		initCopyrightGui();
		pack();
		setSize(700, 400);
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
	
	private void initCopyrightGui() {
		copyrightPanel.setText("PacChat - Direct P2P secure, encrypted private chats\nCopyright (C) 2016 Arccotangent\n\nThis program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses.");
	}
	
	{
		$$$setupUI$$$();
	}
	
	private void $$$setupUI$$$() {
		mainGui = new JPanel();
		mainGui.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1 = new JTabbedPane();
		tabbedPane1.setTabPlacement(SwingConstants.TOP);
		mainGui.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Welcome", panel1);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Messages", panel2);
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("Contacts", panel3);
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel4.setEnabled(true);
		tabbedPane1.addTab("Key Manager", panel4);
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
		textPane1 = new JTextPane();
		panel5.add(textPane1, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
		final JPanel panel6 = new JPanel();
		panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		tabbedPane1.addTab("About", panel6);
		final JScrollPane scrollPane1 = new JScrollPane();
		panel6.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		copyrightPanel = new JTextPane();
		copyrightPanel.setEditable(false);
		scrollPane1.setViewportView(copyrightPanel);
	}
	
	public JComponent $$$getRootComponent$$$() {
		return mainGui;
	}
}
