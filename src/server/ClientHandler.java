package server;

import common.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client Handler Thread — acts as a PRODUCER in the Producer-Consumer pattern.
 *
 * <p>Each connected client has one ClientHandler thread. This thread reads
 * raw protocol messages from the client's socket and places decoded Request
 * objects into the shared bounded buffer.</p>
 *
 * <p><strong>Important:</strong> ClientHandler does NOT process requests directly.
 * All processing is deferred to Worker Threads via the Dispatcher.</p>
 */
public class ClientHandler extends Thread {

    private final Socket socket;
    private final BoundedBuffer buffer;
    private final ChatServer server;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    private volatile boolean running;

    /**
     * Creates a new ClientHandler for the given client socket.
     *
     * @param socket the client's socket connection
     * @param buffer the shared bounded buffer to insert requests into
     * @param server reference to the ChatServer for logging
     */
    public ClientHandler(Socket socket, BoundedBuffer buffer, ChatServer server) {
        super("ClientHandler-" + socket.getPort());
        this.socket = socket;
        this.buffer = buffer;
        this.server = server;
        this.running = true;

        try {
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            server.log("Error creating streams for client: " + e.getMessage());
            this.running = false;
        }
    }

    @Override
    public void run() {
        try {
            String rawMessage;
            // Continuously read messages from the client
            while (running && (rawMessage = input.readLine()) != null) {
                Request request = Request.decode(rawMessage, this);
                if (request != null) {
                    // PRODUCER: put into shared bounded buffer (never process directly)
                    buffer.put(request);
                }
            }
        } catch (IOException e) {
            if (running) {
                server.log("Connection lost: " + (username != null ? username : "unknown client"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // If the client disconnected unexpectedly and was logged in
            if (running && username != null) {
                try {
                    buffer.put(Request.createLogout(username, this));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Sends a protocol message to this client.
     *
     * @param message the raw protocol string to send
     */
    public void sendMessage(String message) {
        if (output != null && !socket.isClosed()) {
            output.println(message);
        }
    }

    /**
     * Closes this client's connection and stops the handler thread.
     */
    public void close() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            server.log("Error closing client socket: " + e.getMessage());
        }
    }

    /* ========== Getters & Setters ========== */

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isRunning() {
        return running;
    }
}
