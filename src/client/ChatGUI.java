package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Chat Interface GUI for the Chat Client application.
 *
 * <p>Displayed after successful login. Contains:</p>
 * <ul>
 *   <li>Active Users List — displays all currently connected users</li>
 *   <li>Chat Messages Area — displays chat messages and system notifications</li>
 *   <li>Message Input Field — allows the user to type messages</li>
 *   <li>Send Button — sends the typed message</li>
 *   <li>Exit Button — allows the user to leave the chat</li>
 * </ul>
 */
public class ChatGUI extends JFrame {

    /* ===== Color Palette ===== */
    private static final Color BG_DARK = new Color(30, 30, 36);
    private static final Color BG_PANEL = new Color(40, 42, 50);
    private static final Color BG_INPUT = new Color(50, 53, 63);
    private static final Color ACCENT_BLUE = new Color(74, 144, 226);
    private static final Color ACCENT_RED = new Color(220, 70, 70);
    private static final Color TEXT_PRIMARY = new Color(220, 220, 230);
    private static final Color TEXT_SECONDARY = new Color(150, 150, 165);
    private static final Color TEXT_SYSTEM = new Color(255, 200, 60);
    private static final Color TEXT_SENDER = new Color(100, 180, 255);
    private static final Color BORDER_COLOR = new Color(60, 63, 75);

    /* ===== Components ===== */
    private JTextPane chatArea;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JTextField messageField;
    private JButton sendButton;
    private JButton exitButton;

    /* ===== Client Reference ===== */
    private ChatClient client;
    private final String username;

    /**
     * Creates the Chat GUI for the given username.
     *
     * @param username the logged-in username
     */
    public ChatGUI(String username) {
        super("Chat Room — " + username);
        this.username = username;
        initializeUI();
    }

    /**
     * Sets the ChatClient reference (called after construction).
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /**
     * Initializes and lays out all UI components.
     */
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 550);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // Handle window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitChat();
            }
        });

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates the header panel with title and exit button.
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 15, 8, 15)
        ));

        JLabel titleLabel = new JLabel("Welcome, " + username);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);

        exitButton = createButton("Exit Chat", ACCENT_RED);
        exitButton.addActionListener(e -> exitChat());

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(exitButton, BorderLayout.EAST);

        return panel;
    }

    /**
     * Creates the center panel with chat area and user list.
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(BG_DARK);

        // Chat Messages Area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(BG_INPUT);
        chatArea.setForeground(TEXT_PRIMARY);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        chatArea.setCaretColor(TEXT_PRIMARY);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(createTitledBorder("Messages"));
        chatScroll.getViewport().setBackground(BG_INPUT);

        // Active Users List
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(BG_INPUT);
        userList.setForeground(TEXT_PRIMARY);
        userList.setSelectionBackground(ACCENT_BLUE);
        userList.setSelectionForeground(Color.WHITE);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(createTitledBorder("Active Users"));
        userScroll.setPreferredSize(new Dimension(180, 0));
        userScroll.getViewport().setBackground(BG_INPUT);

        panel.add(chatScroll, BorderLayout.CENTER);
        panel.add(userScroll, BorderLayout.EAST);

        return panel;
    }

    /**
     * Creates the bottom panel with message input and send button.
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(BG_DARK);

        messageField = new JTextField();
        messageField.setBackground(BG_INPUT);
        messageField.setForeground(TEXT_PRIMARY);
        messageField.setCaretColor(TEXT_PRIMARY);
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Enter key sends message
        messageField.addActionListener(e -> sendMessage());

        sendButton = createButton("Send", ACCENT_BLUE);
        sendButton.setPreferredSize(new Dimension(90, 38));
        sendButton.addActionListener(e -> sendMessage());

        panel.add(messageField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    /* ========== Actions ========== */

    /**
     * Sends the message from the input field.
     */
    private void sendMessage() {
        String text = messageField.getText().trim();
        if (!text.isEmpty() && client != null) {
            client.sendMessage(text);
            messageField.setText("");
        }
        messageField.requestFocusInWindow();
    }

    /**
     * Exits the chat: sends logout and closes the window.
     */
    private void exitChat() {
        if (client != null) {
            client.logout();
        }
        dispose();
        // Reopen login screen
        SwingUtilities.invokeLater(LoginGUI::new);
    }

    /* ========== GUI Update Methods (called from ChatClient listener thread) ========== */

    /**
     * Displays a chat message in the chat area.
     */
    public void displayChatMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatArea.getStyledDocument();

            // Sender name in blue/accent color
            SimpleAttributeSet senderAttrs = new SimpleAttributeSet();
            StyleConstants.setForeground(senderAttrs, TEXT_SENDER);
            StyleConstants.setBold(senderAttrs, true);
            StyleConstants.setFontFamily(senderAttrs, "Consolas");
            StyleConstants.setFontSize(senderAttrs, 13);

            // Message text in white
            SimpleAttributeSet msgAttrs = new SimpleAttributeSet();
            StyleConstants.setForeground(msgAttrs, TEXT_PRIMARY);
            StyleConstants.setFontFamily(msgAttrs, "Consolas");
            StyleConstants.setFontSize(msgAttrs, 13);

            try {
                doc.insertString(doc.getLength(), sender + ": ", senderAttrs);
                doc.insertString(doc.getLength(), message + "\n", msgAttrs);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            chatArea.setCaretPosition(doc.getLength());
        });
    }

    /**
     * Displays a system message (join/leave/kick notifications).
     */
    public void displaySystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatArea.getStyledDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs, TEXT_SYSTEM);
            StyleConstants.setItalic(attrs, true);
            StyleConstants.setFontFamily(attrs, "Consolas");
            StyleConstants.setFontSize(attrs, 13);

            try {
                doc.insertString(doc.getLength(), "● " + message + "\n", attrs);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            chatArea.setCaretPosition(doc.getLength());
        });
    }

    /**
     * Updates the active users list.
     */
    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }

    /**
     * Handles being kicked by the server.
     */
    public void handleKicked() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "You have been removed from the chat by the server.",
                    "Kicked", JOptionPane.WARNING_MESSAGE);
            dispose();
            new LoginGUI();
        });
    }

    /**
     * Handles server shutdown notification.
     */
    public void handleServerShutdown() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "The server has been shut down. You have been disconnected.",
                    "Server Shutdown", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginGUI();
        });
    }

    /**
     * Handles a login error (e.g., duplicate username).
     */
    public void handleError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    message, "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            new LoginGUI();
        });
    }

    /**
     * Handles unexpected connection loss.
     */
    public void handleConnectionLost() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Connection to the server has been lost.",
                    "Connection Lost", JOptionPane.ERROR_MESSAGE);
            dispose();
            new LoginGUI();
        });
    }

    /* ========== UI Helper Methods ========== */

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        Color hoverColor = bgColor.brighter();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), title);
        border.setTitleColor(TEXT_SECONDARY);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        return border;
    }
}
