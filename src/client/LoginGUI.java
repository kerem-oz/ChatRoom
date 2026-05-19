package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Login Screen GUI for the Chat Client application.
 *
 * <p>Contains input fields for:</p>
 * <ul>
 *   <li>Server IP Address</li>
 *   <li>Port Number</li>
 *   <li>Username</li>
 * </ul>
 *
 * <p>On successful connection, opens the {@link ChatGUI}.</p>
 */
public class LoginGUI extends JFrame {

    /* ===== Color Palette ===== */
    private static final Color BG_DARK = new Color(30, 30, 36);
    private static final Color BG_INPUT = new Color(50, 53, 63);
    private static final Color ACCENT_BLUE = new Color(74, 144, 226);
    private static final Color TEXT_PRIMARY = new Color(220, 220, 230);
    private static final Color TEXT_SECONDARY = new Color(150, 150, 165);
    private static final Color BORDER_COLOR = new Color(60, 63, 75);

    /* ===== Components ===== */
    private JTextField ipField;
    private JTextField portField;
    private JTextField usernameField;
    private JButton connectButton;

    public LoginGUI() {
        super("Chat Room — Login");
        initializeUI();
    }

    /**
     * Initializes and lays out all UI components.
     */
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 380);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Chat Room", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ACCENT_BLUE);
        titleLabel.setBorder(new EmptyBorder(25, 0, 5, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Center form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BG_DARK);
        formPanel.setBorder(new EmptyBorder(10, 50, 20, 50));

        ipField = createFieldWithLabel(formPanel, "Server IP Address", "127.0.0.1");
        portField = createFieldWithLabel(formPanel, "Port Number", "12345");
        usernameField = createFieldWithLabel(formPanel, "Username", "");

        formPanel.add(Box.createVerticalStrut(20));

        connectButton = new JButton("Connect");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        connectButton.setBackground(ACCENT_BLUE);
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        connectButton.setFocusPainted(false);
        connectButton.setBorder(new EmptyBorder(10, 0, 10, 0));
        connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectButton.setOpaque(true);

        Color hoverColor = ACCENT_BLUE.brighter();
        connectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                connectButton.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                connectButton.setBackground(ACCENT_BLUE);
            }
        });

        connectButton.addActionListener(e -> attemptConnection());
        formPanel.add(connectButton);

        add(formPanel, BorderLayout.CENTER);

        // Allow Enter key to trigger connect
        getRootPane().setDefaultButton(connectButton);

        setVisible(true);
    }

    /**
     * Creates a labeled text field and adds it to the given panel.
     */
    private JTextField createFieldWithLabel(JPanel panel, String labelText, String defaultValue) {
        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT_SECONDARY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(4));

        JTextField field = new JTextField(defaultValue);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(field);
        panel.add(Box.createVerticalStrut(12));

        return field;
    }

    /**
     * Validates input and attempts to connect to the server.
     */
    private void attemptConnection() {
        String ip = ipField.getText().trim();
        String portText = portField.getText().trim();
        String username = usernameField.getText().trim();

        // Validate input
        if (ip.isEmpty()) {
            showError("Please enter the server IP address.");
            return;
        }
        if (username.isEmpty()) {
            showError("Please enter a username.");
            return;
        }
        if (username.contains("|") || username.contains(",")) {
            showError("Username cannot contain '|' or ',' characters.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            showError("Please enter a valid port number (1–65535).");
            return;
        }

        // Disable button during connection attempt
        connectButton.setEnabled(false);
        connectButton.setText("Connecting...");

        // Connect in background thread to avoid freezing GUI
        new Thread(() -> {
            try {
                // Create ChatGUI and ChatClient
                SwingUtilities.invokeAndWait(() -> {
                    // Nothing to do here, just ensuring EDT is free
                });

                ChatGUI chatGUI = new ChatGUI(username);
                ChatClient client = new ChatClient(chatGUI);
                chatGUI.setClient(client);
                client.connect(ip, port, username);

                // Show chat GUI and close login
                SwingUtilities.invokeLater(() -> {
                    chatGUI.setVisible(true);
                    this.dispose();
                });

            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    showError("Failed to connect: " + ex.getMessage());
                    connectButton.setEnabled(true);
                    connectButton.setText("Connect");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showError("Unexpected error: " + ex.getMessage());
                    connectButton.setEnabled(true);
                    connectButton.setText("Connect");
                });
            }
        }, "Connect-Thread").start();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
