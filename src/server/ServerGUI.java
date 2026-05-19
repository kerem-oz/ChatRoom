package server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Java Swing GUI for the Chat Server application.
 *
 * <p>Contains:</p>
 * <ul>
 *   <li>Port Number field</li>
 *   <li>Start / Stop Server buttons</li>
 *   <li>Chat Messages area (displays messages and system notifications)</li>
 *   <li>Active Users List</li>
 *   <li>Kick Selected User button</li>
 *   <li>Server Log Area</li>
 * </ul>
 */
public class ServerGUI extends JFrame {

    /* ===== Color Palette ===== */
    private static final Color BG_DARK = new Color(30, 30, 36);
    private static final Color BG_PANEL = new Color(40, 42, 50);
    private static final Color BG_INPUT = new Color(50, 53, 63);
    private static final Color ACCENT_BLUE = new Color(74, 144, 226);
    private static final Color ACCENT_RED = new Color(220, 70, 70);
    private static final Color ACCENT_GREEN = new Color(80, 200, 120);
    private static final Color TEXT_PRIMARY = new Color(220, 220, 230);
    private static final Color TEXT_SECONDARY = new Color(150, 150, 165);
    private static final Color TEXT_SYSTEM = new Color(255, 200, 60);
    private static final Color BORDER_COLOR = new Color(60, 63, 75);

    /* ===== Components ===== */
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JTextPane chatArea;
    private JTextArea logArea;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JButton kickButton;

    /* ===== Server Reference ===== */
    private ChatServer server;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public ServerGUI() {
        super("Chat Server — Control Panel");
        this.server = new ChatServer(this);
        initializeUI();
    }

    /**
     * Initializes and lays out all UI components.
     */
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Creates the top panel with port input and Start/Stop buttons.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(5, 10, 5, 10)
        ));

        JLabel portLabel = createLabel("Port:");
        portField = createTextField("12345", 8);

        startButton = createButton("Start Server", ACCENT_GREEN);
        stopButton = createButton("Stop Server", ACCENT_RED);
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        panel.add(portLabel);
        panel.add(portField);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(startButton);
        panel.add(stopButton);

        return panel;
    }

    /**
     * Creates the center panel with chat area and user list side by side.
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
        chatScroll.setBorder(createTitledBorder("Chat Messages"));
        chatScroll.getViewport().setBackground(BG_INPUT);

        // Right side: user list + kick button
        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setBackground(BG_DARK);
        rightPanel.setPreferredSize(new Dimension(200, 0));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(BG_INPUT);
        userList.setForeground(TEXT_PRIMARY);
        userList.setSelectionBackground(ACCENT_BLUE);
        userList.setSelectionForeground(Color.WHITE);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(createTitledBorder("Active Users"));
        userScroll.getViewport().setBackground(BG_INPUT);

        kickButton = createButton("Kick Selected User", ACCENT_RED);
        kickButton.setEnabled(false);
        kickButton.addActionListener(e -> kickSelectedUser());

        rightPanel.add(userScroll, BorderLayout.CENTER);
        rightPanel.add(kickButton, BorderLayout.SOUTH);

        panel.add(chatScroll, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Creates the bottom panel with the server log area.
     */
    private JPanel createBottomPanel() {
        logArea = new JTextArea(6, 0);
        logArea.setEditable(false);
        logArea.setBackground(BG_INPUT);
        logArea.setForeground(TEXT_SECONDARY);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setCaretColor(TEXT_SECONDARY);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(createTitledBorder("Server Log"));
        logScroll.getViewport().setBackground(BG_INPUT);
        logScroll.setPreferredSize(new Dimension(0, 150));

        return wrapInPanel(logScroll);
    }

    /* ========== Server Actions ========== */

    private void startServer() {
        String portText = portField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid port number (1–65535).",
                    "Invalid Port", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            server.start(port);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            kickButton.setEnabled(true);
            portField.setEditable(false);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to start server: " + ex.getMessage(),
                    "Server Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopServer() {
        server.requestStop();
    }

    /**
     * Called by ChatServer when the server has fully stopped.
     */
    public void onServerStopped() {
        SwingUtilities.invokeLater(() -> {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            kickButton.setEnabled(false);
            portField.setEditable(true);
            // Create a fresh server instance for potential restart
            server = new ChatServer(this);
        });
    }

    private void kickSelectedUser() {
        String selected = userList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to kick.",
                    "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to kick '" + selected + "'?",
                "Kick User", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            server.kickUser(selected);
        }
    }

    /* ========== GUI Update Methods (called from server threads) ========== */

    /**
     * Updates the active users list in the GUI.
     */
    public void updateUserList(Set<String> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }

    /**
     * Appends a message to the chat area with styling.
     *
     * @param message the message text
     * @param type    "chat" for regular messages, "system" for system notifications
     */
    public void appendChatMessage(String message, String type) {
        final String displayMessage = "system".equals(type) ? "● " + message : message;
        SwingUtilities.invokeLater(() -> {
            StyledDocument doc = chatArea.getStyledDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();

            if ("system".equals(type)) {
                StyleConstants.setForeground(attrs, TEXT_SYSTEM);
                StyleConstants.setItalic(attrs, true);
            } else {
                StyleConstants.setForeground(attrs, TEXT_PRIMARY);
            }

            StyleConstants.setFontFamily(attrs, "Consolas");
            StyleConstants.setFontSize(attrs, 13);

            try {
                doc.insertString(doc.getLength(), displayMessage + "\n", attrs);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            // Auto-scroll to bottom
            chatArea.setCaretPosition(doc.getLength());
        });
    }

    /**
     * Appends a log entry with timestamp to the server log area.
     */
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /* ========== UI Helper Methods ========== */

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_PRIMARY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    private JTextField createTextField(String defaultText, int columns) {
        JTextField field = new JTextField(defaultText, columns);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        // Hover effect
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

    private JPanel wrapInPanel(Component comp) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.add(comp, BorderLayout.CENTER);
        return panel;
    }
}
