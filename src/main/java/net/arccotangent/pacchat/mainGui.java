package net.arccotangent.pacchat;

import javax.swing.*;
import java.awt.*;

public class mainGui extends JFrame {

    private JTextPane copyrightPanel;
    private JPanel mainGui;
    private JTabbedPane tabbedPane1;
    private JPanel copyrightGui;
    private JButton backButton;

    public mainGui() {
        super("Hello World");
        setContentPane(mainGui);
        copyrightGui();
        pack();
        setSize(500,300);
        setVisible(true);
    }

    public static void main(String[] args) {
        mainGui mainForm = new mainGui();
    }

    public void copyrightGui() {
        copyrightPanel.setText("PacChat - Direct P2P secure, encrypted private chats\nCopyright (C) 2016 Arccotangent\n\nThis program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses.");
    }
}

