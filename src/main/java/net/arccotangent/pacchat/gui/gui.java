package net.arccotangent.pacchat.gui;

import net.arccotangent.pacchat.Main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class gui extends Main {
    public static String command;
    public class mainGui extends JFrame {
        private JTextPane copyrightPanel;
        private JPanel mainGui;
        private JTabbedPane tabbedPane1;
        private JButton haltServerButton;
        private JButton restartServerButton;
        private JButton startServerButton;
        private JTextPane textPane1;
        private JPanel copyrightGui;
        private JButton backButton;

        public mainGui() {
            super("Pacchat GUI");
            setContentPane(mainGui);
            serverManager();
            copyrightGui();
            pack();
            setSize(700, 400);
            setVisible(true);
        }

        public void main(String[] args) {
            mainGui mainForm = new mainGui();
        }

        public void serverManager() {

            haltServerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setCommand("haltserver");
                }
            });

            startServerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setCommand("startserver");
                }
            });

            restartServerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setCommand("restartserver");
                }
            });
        }

        public void copyrightGui() {
            copyrightPanel.setText("PacChat - Direct P2P secure, encrypted private chats\nCopyright (C) 2016 Arccotangent\n\nThis program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses.");
        }

    }

    public static void setCommand(String c) {
        command = c;
    }
    public static String getCommand() {
        return command;
    }
}
