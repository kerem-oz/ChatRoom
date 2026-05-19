package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles the client-side networking for the Chat Room application.
 *
 * <p>Manages the socket connection to the server, sends protocol-encoded
 * messages, and runs a listener thread to receive and route server responses
 * to the GUI.</p>
 */
public class ChatClient {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private ChatGUI gui;
    private String username;
    private volatile boolean running;

    /**
     * Creates a new ChatClient.
     *
     * @param gui the ChatGUI to update with received messages
     */
    public ChatClient(ChatGUI gui) {
        this.gui = gui;
    }

    /**
     * Connects to the chat server, sends the LOGIN request,
     * and starts the listener thread for incoming messages.
     *
     * @param host     server IP address
     * @param port     server port number
     * @param username the username to login with
     * @throws IOException if connection fails
     */
    public void connect(String host, int port, String username) throws IOException {
        this.username = username;
        this.socket = new Socket(host, port);
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.running = true;

        // Send login request
        output.println("LOGIN|" + username);

        // Start the listener thread for incoming server messages
        Thread listenerThread = new Thread(this::listenForMessages, "Client-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Listener loop: reads messages from the server and routes them
     * to the appropriate GUI handler based on protocol type.
     */
    private void listenForMessages() {
        try {
            String line;
            while (running && (line = input.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                String type = parts[0];

                switch (type) {
                    case "CHAT":
                        if (parts.length >= 3) {
                            gui.displayChatMessage(parts[1], parts[2]);
                        }
                        break;
                    case "SYSTEM":
                        if (parts.length >= 2) {
                            gui.displaySystemMessage(parts[1]);
                        }
                        break;
                    case "USERLIST":
                        if (parts.length >= 2) {
                            String[] users = parts[1].isEmpty()
                                    ? new String[0]
                                    : parts[1].split(",");
                            gui.updateUserList(users);
                        }
                        break;
                    case "KICKED":
                        gui.handleKicked();
                        running = false;
                        break;
                    case "SHUTDOWN":
                        gui.handleServerShutdown();
                        running = false;
                        break;
                    case "ERROR":
                        if (parts.length >= 2) {
                            gui.handleError(parts[1]);
                        }
                        running = false;
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            if (running) {
                gui.handleConnectionLost();
            }
        } finally {
            disconnect();
        }
    }

    /**
     * Sends a chat message to the server.
     * Protocol: MESSAGE|username|text
     */
    public void sendMessage(String text) {
        if (output != null && running) {
            output.println("MESSAGE|" + username + "|" + text);
        }
    }

    /**
     * Sends a logout request and disconnects from the server.
     */
    public void logout() {
        if (output != null && running) {
            output.println("LOGOUT|" + username);
        }
        running = false;
        disconnect();
    }

    /**
     * Closes the socket connection.
     */
    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore on disconnect
        }
    }

    public String getUsername() {
        return username;
    }

    public boolean isRunning() {
        return running;
    }
}
