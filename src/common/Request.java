package common;

/**
 * Represents a request in the chat system.
 * Used as the data unit passed through the bounded buffer
 * between Producer (ClientHandler) and Consumer (Dispatcher/Worker).
 *
 * The attachment field holds a reference to the ClientHandler
 * that originated this request (null for server-generated requests).
 */
public class Request {

    private final RequestType type;
    private final String sender;
    private final String content;
    private final Object attachment;

    /**
     * Constructs a new Request.
     *
     * @param type       the type of the request
     * @param sender     the username of the sender (may be null for server commands)
     * @param content    the content/payload of the request
     * @param attachment reference to the originating ClientHandler (or null)
     */
    public Request(RequestType type, String sender, String content, Object attachment) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.attachment = attachment;
    }

    /* ========== Factory Methods ========== */

    public static Request createLogin(String username, Object handler) {
        return new Request(RequestType.LOGIN, username, null, handler);
    }

    public static Request createMessage(String username, String message, Object handler) {
        return new Request(RequestType.MESSAGE, username, message, handler);
    }

    public static Request createLogout(String username, Object handler) {
        return new Request(RequestType.LOGOUT, username, null, handler);
    }

    public static Request createKick(String targetUsername) {
        return new Request(RequestType.KICK, null, targetUsername, null);
    }

    public static Request createStop() {
        return new Request(RequestType.STOP, null, null, null);
    }

    /* ========== Protocol Decoding ========== */

    /**
     * Decodes a raw protocol string received from a client into a Request object.
     * Protocol format: TYPE|sender|content
     *
     * @param rawMessage the raw string from the client socket
     * @param handler    the ClientHandler that received this message
     * @return a Request object, or null if the message is invalid
     */
    public static Request decode(String rawMessage, Object handler) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            return null;
        }

        String[] parts = rawMessage.split("\\|", 3);
        if (parts.length == 0) {
            return null;
        }

        String typeStr = parts[0].trim().toUpperCase();

        try {
            RequestType type = RequestType.valueOf(typeStr);
            switch (type) {
                case LOGIN:
                    return createLogin(parts.length > 1 ? parts[1] : "", handler);
                case MESSAGE:
                    return createMessage(
                            parts.length > 1 ? parts[1] : "",
                            parts.length > 2 ? parts[2] : "",
                            handler
                    );
                case LOGOUT:
                    return createLogout(parts.length > 1 ? parts[1] : "", handler);
                default:
                    return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /* ========== Getters ========== */

    public RequestType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Object getAttachment() {
        return attachment;
    }

    @Override
    public String toString() {
        return String.format("Request{type=%s, sender='%s', content='%s'}", type, sender, content);
    }
}
