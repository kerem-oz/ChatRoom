package common;

/**
 * Enum representing the types of requests in the chat system.
 * Each type corresponds to a specific action that can be processed
 * by the Worker Thread.
 */
public enum RequestType {
    /** Client login request */
    LOGIN,
    /** Chat message from a client */
    MESSAGE,
    /** Client logout request */
    LOGOUT,
    /** Server admin kicks a user */
    KICK,
    /** Server shutdown command */
    STOP
}
