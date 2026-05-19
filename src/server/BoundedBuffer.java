package server;

import common.Request;

/**
 * Thread-safe bounded buffer implementing the core of the Producer-Consumer pattern.
 * Uses a circular array with synchronized methods, wait(), and notifyAll()
 * for proper thread coordination.
 *
 * <p>Producers (ClientHandlers) call {@link #put(Request)} to insert requests.
 * The Consumer (Dispatcher) calls {@link #take()} to remove requests.</p>
 *
 * <p>Only ONE shared instance of this buffer exists on the server,
 * as required by the project specification.</p>
 */
public class BoundedBuffer {

    private final Request[] buffer;
    private final int capacity;
    private int count;
    private int putIndex;
    private int takeIndex;

    /**
     * Creates a bounded buffer with the given fixed capacity.
     *
     * @param capacity the maximum number of requests the buffer can hold
     */
    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new Request[capacity];
        this.count = 0;
        this.putIndex = 0;
        this.takeIndex = 0;
    }

    /**
     * Inserts a request into the buffer. Blocks if the buffer is full
     * until space becomes available.
     * Called by Producer threads (ClientHandler).
     *
     * @param request the request to insert
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public synchronized void put(Request request) throws InterruptedException {
        // Wait while buffer is full
        while (count == capacity) {
            wait();
        }

        buffer[putIndex] = request;
        putIndex = (putIndex + 1) % capacity;
        count++;

        // Notify consumers that data is available
        notifyAll();
    }

    /**
     * Removes and returns a request from the buffer. Blocks if the buffer
     * is empty until data becomes available.
     * Called by the Consumer thread (Dispatcher).
     *
     * @return the next request from the buffer
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public synchronized Request take() throws InterruptedException {
        // Wait while buffer is empty
        while (count == 0) {
            wait();
        }

        Request request = buffer[takeIndex];
        buffer[takeIndex] = null;
        takeIndex = (takeIndex + 1) % capacity;
        count--;

        // Notify producers that space is available
        notifyAll();
        return request;
    }

    /**
     * Returns the current number of items in the buffer.
     */
    public synchronized int size() {
        return count;
    }

    /**
     * Returns the maximum capacity of the buffer.
     */
    public int getCapacity() {
        return capacity;
    }
}
