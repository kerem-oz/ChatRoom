package client;

import javax.swing.*;

/**
 * Entry point for the Chat Client application.
 * Launches the Login GUI on the Event Dispatch Thread.
 */
public class ClientApp {

    public static void main(String[] args) {
        // Set system properties for better font rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(LoginGUI::new);
    }
}
