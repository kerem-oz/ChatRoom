package server;

import common.Request;

/**
 * Dispatcher Thread — acts as the CONSUMER in the Producer-Consumer pattern.
 *
 * <p>Continuously takes requests from the shared bounded buffer.
 * For each request removed, the Dispatcher creates and starts a new
 * Worker Thread to process that request.</p>
 */
public class Dispatcher extends Thread {

    private final BoundedBuffer buffer;
    private final ChatServer server;
    private volatile boolean running;

    /**
     * Creates a new Dispatcher.
     *
     * @param buffer the shared bounded buffer to consume requests from
     * @param server reference to the ChatServer
     */
    public Dispatcher(BoundedBuffer buffer, ChatServer server) {
        super("Dispatcher-Thread");
        this.buffer = buffer;
        this.server = server;
        this.running = true;
    }

    @Override
    public void run() {
        server.log("Dispatcher thread started.");

        while (running) {
            try {
                // CONSUMER: take from shared bounded buffer (blocks if empty)
                Request request = buffer.take();

                // Create a new Worker Thread to process this request
                WorkerThread worker = new WorkerThread(request, server);
                worker.start();

            } catch (InterruptedException e) {
                if (!running) {
                    break; // Graceful shutdown
                }
                Thread.currentThread().interrupt();
            }
        }

        server.log("Dispatcher thread stopped.");
    }

    /**
     * Signals the Dispatcher to stop and interrupts it if blocked on take().
     */
    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
