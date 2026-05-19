package server;

import common.Request;
import common.RequestType;

/**
 * Worker Thread — processes a single request taken from the buffer by the Dispatcher.
 *
 * <p>Each Worker Thread is responsible for:</p>
 * <ul>
 *   <li>Decoding the request type</li>
 *   <li>Processing LOGIN, MESSAGE, LOGOUT, KICK, or STOP requests</li>
 *   <li>Updating shared server data (synchronized)</li>
 *   <li>Broadcasting messages or updates to clients</li>
 * </ul>
 */
public class WorkerThread extends Thread {

    private final Request request;
    private final ChatServer server;

    /**
     * Creates a new WorkerThread for the given request.
     *
     * @param request the request to process
     * @param server  reference to the ChatServer
     */
    public WorkerThread(Request request, ChatServer server) {
        super("Worker-" + request.getType());
        this.request = request;
        this.server = server;
    }

    @Override
    public void run() {
        RequestType type = request.getType();

        switch (type) {
            case LOGIN:
                handleLogin();
                break;
            case MESSAGE:
                handleMessage();
                break;
            case LOGOUT:
                handleLogout();
                break;
            case KICK:
                handleKick();
                break;
            case STOP:
                handleStop();
                break;
        }
    }

    /**
     * Handles a LOGIN request:
     * - Validates uniqueness of username
     * - Adds user to active list
     * - Broadcasts join notification and updated user list
     */
    private void handleLogin() {
        String username = request.getSender();
        ClientHandler handler = (ClientHandler) request.getAttachment();

        // Check for duplicate username
        if (server.isUsernameTaken(username)) {
            handler.sendMessage("ERROR|Username '" + username + "' is already taken.");
            handler.close();
            server.removeHandler(handler);
            server.log("Login rejected: '" + username + "' (duplicate username)");
            return;
        }

        // Register the user
        handler.setUsername(username);
        server.addUser(username);

        // Broadcast to all clients
        server.broadcastSystemMessage("User " + username + " has joined the chat");
        server.broadcastUserList();

        // Update server GUI
        server.updateGUIUsers();
        server.updateGUIChat("User " + username + " has joined the chat", "system");
        server.log("User '" + username + "' logged in successfully.");
    }

    /**
     * Handles a MESSAGE request:
     * - Broadcasts the chat message to all connected clients
     */
    private void handleMessage() {
        String sender = request.getSender();
        String content = request.getContent();

        server.broadcastChatMessage(sender, content);
        server.updateGUIChat(sender + ": " + content, "chat");
        server.log("Message from '" + sender + "': " + content);
    }

    /**
     * Handles a LOGOUT request:
     * - Removes user from active list
     * - Closes the client connection
     * - Broadcasts leave notification and updated user list
     */
    private void handleLogout() {
        String username = request.getSender();
        ClientHandler handler = (ClientHandler) request.getAttachment();

        server.removeUser(username);
        if (handler != null) {
            handler.close();
            server.removeHandler(handler);
        }

        server.broadcastSystemMessage("User " + username + " has left the chat");
        server.broadcastUserList();

        server.updateGUIUsers();
        server.updateGUIChat("User " + username + " has left the chat", "system");
        server.log("User '" + username + "' logged out.");
    }

    /**
     * Handles a KICK request (from server admin):
     * - Finds the target user's handler
     * - Sends KICKED notification to the user
     * - Removes user and closes connection
     * - Broadcasts removal notification and updated user list
     */
    private void handleKick() {
        String targetUsername = request.getContent();
        ClientHandler targetHandler = server.getHandlerByUsername(targetUsername);

        if (targetHandler != null) {
            targetHandler.sendMessage("KICKED");
            server.removeUser(targetUsername);
            targetHandler.close();
            server.removeHandler(targetHandler);

            server.broadcastSystemMessage("User " + targetUsername + " was removed by the server");
            server.broadcastUserList();

            server.updateGUIUsers();
            server.updateGUIChat("User " + targetUsername + " was removed by the server", "system");
            server.log("User '" + targetUsername + "' was kicked by admin.");
        } else {
            server.log("Kick failed: user '" + targetUsername + "' not found.");
        }
    }

    /**
     * Handles a STOP request (server shutdown):
     * - Notifies all clients with SHUTDOWN message
     * - Closes all client connections
     * - Stops the server
     */
    private void handleStop() {
        server.log("Server shutdown initiated...");
        server.updateGUIChat("Server is shutting down...", "system");
        server.shutdownServer();
    }
}
