package server;

import common.Request;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Core server logic for the Chat Room application.
 *
 * <p>Manages the ServerSocket, shared BoundedBuffer, Dispatcher thread,
 * and all connected clients. Provides synchronized access to shared data
 * structures (active users, client handlers) and broadcast operations.</p>
 */
public class ChatServer {

    /** Capacity of the shared bounded buffer */
    private static final int BUFFER_CAPACITY = 20;

    private ServerSocket serverSocket;
    private BoundedBuffer buffer;
    private Dispatcher dispatcher;
    private Thread acceptThread;
    private ServerGUI gui;
    private volatile boolean running;

    /* ===== Shared Data — all access must be synchronized ===== */
    private final List<ClientHandler> clientHandlers = new ArrayList<>();
    private final Set<String> activeUsers = new LinkedHashSet<>();

    /**
     * Creates a ChatServer associated with the given GUI.
     *
     * @param gui the server GUI for display updates
     */
    public ChatServer(ServerGUI gui) {
        this.gui = gui;
    }

    /**
     * Starts the server: opens the ServerSocket, initializes the bounded buffer,
     * starts the Dispatcher, and begins accepting client connections.
     *
     * @param port the port number to listen on
     * @throws IOException if the server socket cannot be opened
     */
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        // Initialize the single shared bounded buffer
        buffer = new BoundedBuffer(BUFFER_CAPACITY);

        // Start the Dispatcher (Consumer thread)
        dispatcher = new Dispatcher(buffer, this);
        dispatcher.setDaemon(true);
        dispatcher.start();

        // Start the connection acceptor thread
        acceptThread = new Thread(this::acceptClients, "Accept-Thread");
        acceptThread.setDaemon(true);
        acceptThread.start();

        log("Server started on port " + port);
        log("Bounded buffer capacity: " + BUFFER_CAPACITY);
    }

    /**
     * Continuously accepts new client connections and creates ClientHandler
     * (Producer) threads for each.
     */
    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, buffer, this);
                synchronized (clientHandlers) {
                    clientHandlers.add(handler);
                }
                handler.setDaemon(true);
                handler.start();
                log("New connection from " + clientSocket.getInetAddress().getHostAddress()
                        + ":" + clientSocket.getPort());
            } catch (IOException e) {
                if (running) {
                    log("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Shuts down the server gracefully:
     * - Notifies all clients
     * - Closes all connections
     * - Stops Dispatcher and accept threads
     * - Closes the ServerSocket
     */
    public void shutdownServer() {
        if (!running) {
            return;
        }
        running = false;

        // Notify and disconnect all clients
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage("SHUTDOWN");
                handler.close();
            }
            clientHandlers.clear();
        }

        synchronized (activeUsers) {
            activeUsers.clear();
        }

        // Stop the Dispatcher
        if (dispatcher != null) {
            dispatcher.shutdown();
        }

        // Close the ServerSocket (will unblock accept())
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error closing server socket: " + e.getMessage());
        }

        updateGUIUsers();
        log("Server stopped.");
        gui.onServerStopped();
    }

    /* ========== User Management (synchronized) ========== */

    /**
     * Adds a username to the active users set.
     */
    public void addUser(String username) {
        synchronized (activeUsers) {
            activeUsers.add(username);
        }
    }

    /**
     * Removes a username from the active users set.
     */
    public void removeUser(String username) {
        synchronized (activeUsers) {
            activeUsers.remove(username);
        }
    }

    /**
     * Checks if a username is already in use.
     */
    public boolean isUsernameTaken(String username) {
        synchronized (activeUsers) {
            return activeUsers.contains(username);
        }
    }

    /**
     * Returns a copy of the active users set.
     */
    public Set<String> getActiveUsers() {
        synchronized (activeUsers) {
            return new LinkedHashSet<>(activeUsers);
        }
    }

    /* ========== Handler Management (synchronized) ========== */

    /**
     * Removes a ClientHandler from the handlers list.
     */
    public void removeHandler(ClientHandler handler) {
        synchronized (clientHandlers) {
            clientHandlers.remove(handler);
        }
    }

    /**
     * Finds a ClientHandler by username.
     *
     * @return the handler, or null if not found
     */
    public ClientHandler getHandlerByUsername(String username) {
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                if (username.equals(handler.getUsername())) {
                    return handler;
                }
            }
        }
        return null;
    }

    /* ========== Broadcast Operations (synchronized) ========== */

    /**
     * Broadcasts a system message to all connected clients.
     * Protocol: SYSTEM|message
     */
    public void broadcastSystemMessage(String message) {
        String protocol = "SYSTEM|" + message;
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(protocol);
            }
        }
    }

    /**
     * Broadcasts a chat message to all connected clients.
     * Protocol: CHAT|sender|message
     */
    public void broadcastChatMessage(String sender, String message) {
        String protocol = "CHAT|" + sender + "|" + message;
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(protocol);
            }
        }
    }

    /**
     * Broadcasts the current active users list to all connected clients.
     * Protocol: USERLIST|user1,user2,...
     */
    public void broadcastUserList() {
        String userListStr;
        synchronized (activeUsers) {
            userListStr = String.join(",", activeUsers);
        }
        String protocol = "USERLIST|" + userListStr;
        synchronized (clientHandlers) {
            for (ClientHandler handler : clientHandlers) {
                handler.sendMessage(protocol);
            }
        }
    }

    /* ========== GUI Update Helpers ========== */

    /**
     * Updates the active users list in the server GUI.
     */
    public void updateGUIUsers() {
        Set<String> users = getActiveUsers();
        gui.updateUserList(users);
    }

    /**
     * Appends a chat/system message to the server GUI chat area.
     *
     * @param message the message text
     * @param type    "chat" or "system"
     */
    public void updateGUIChat(String message, String type) {
        gui.appendChatMessage(message, type);
    }

    /**
     * Appends a log entry to the server GUI log area.
     */
    public void log(String message) {
        gui.appendLog(message);
    }

    /**
     * Puts a KICK request into the bounded buffer for processing.
     */
    public void kickUser(String username) {
        try {
            buffer.put(Request.createKick(username));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Puts a STOP request into the bounded buffer for processing.
     */
    public void requestStop() {
        try {
            buffer.put(Request.createStop());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isRunning() {
        return running;
    }
}
