package server;

import javax.swing.*;

/**
 * Entry point for the Chat Server application.
 * Launches the Server GUI on the Event Dispatch Thread.
 */
public class ServerApp {

    public static void main(String[] args) {
        // Set system properties for better font rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(ServerGUI::new);
    }
}
