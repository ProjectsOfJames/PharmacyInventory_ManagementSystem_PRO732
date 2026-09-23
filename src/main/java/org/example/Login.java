package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private JButton loginBtn;

    public Login() {
        setTitle("HealthFirst PIMS - Login");
        setSize(450, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIStyle.BG_LIGHT);

        // Header banner
        JPanel header = new JPanel();
        header.setBackground(UIStyle.PRIMARY_DARK);
        header.setPreferredSize(new Dimension(450, 110));
        JLabel title = new JLabel("HealthFirst Pharmacy");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Inventory Management System");
        subtitle.setFont(UIStyle.FONT_LABEL);
        subtitle.setForeground(new Color(210, 225, 240));
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);
        header.add(textPanel);
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setBackground(UIStyle.BG_LIGHT);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(UIStyle.FONT_LABEL);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        usernameField.setFont(UIStyle.FONT_FIELD);
        usernameField.setMaximumSize(new Dimension(400, 36));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UIStyle.FONT_LABEL);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(UIStyle.FONT_FIELD);
        passwordField.setMaximumSize(new Dimension(400, 36));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        messageLabel = new JLabel(" ");
        messageLabel.setForeground(UIStyle.DANGER);
        messageLabel.setFont(UIStyle.FONT_LABEL);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginBtn = UIStyle.styledButton("LOGIN", UIStyle.PRIMARY);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin()); // Enter key submits


        form.add(userLbl);
        form.add(Box.createVerticalStrut(5));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(20));
        form.add(passLbl);
        form.add(Box.createVerticalStrut(5));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(15));
        form.add(messageLabel);
        form.add(Box.createVerticalStrut(15));
        form.add(loginBtn);

        root.add(form, BorderLayout.CENTER);

        JLabel footer = new JLabel("© HealthFirst Pharmacy | PROGRAMMING 732", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footer.setForeground(Color.GRAY);
        footer.setBorder(new EmptyBorder(0, 0, 10, 0));
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private Models.User loggedInUser; // set on successful login; used for navigation in Segment 4

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        Models.User user = DBConnection.authenticate(username, password);
        if (user == null) {
            messageLabel.setText("Invalid username or password.");
            passwordField.setText("");
            return;
        }

        loggedInUser = user;
        dispose();
        SwingUtilities.invokeLater(() -> openDashboard(user));
    }

    private void openDashboard(Models.User user) {
        JFrame dashboard = new JFrame("HealthFirst PIMS - " + user.getRole() + " Dashboard");
        dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboard.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        // Top bar (shared by both roles)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIStyle.PRIMARY_DARK);
        topBar.setPreferredSize(new Dimension(100, 60));
        JLabel welcome = new JLabel("  Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
        welcome.setFont(UIStyle.FONT_HEADER);
        welcome.setForeground(Color.WHITE);
        topBar.add(welcome, BorderLayout.WEST);

        JButton logoutBtn = UIStyle.styledButton("Logout", UIStyle.DANGER);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dashboard, "Are you sure you want to logout?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dashboard.dispose();
                new Login().setVisible(true);
            }
        });
        JPanel logoutWrap = new JPanel();
        logoutWrap.setOpaque(false);
        logoutWrap.add(logoutBtn);
        topBar.add(logoutWrap, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);

        // Role-specific content
        if ("Admin".equalsIgnoreCase(user.getRole())) {
            JTabbedPane roleTabs = new JTabbedPane();
            roleTabs.setFont(UIStyle.FONT_LABEL);
            roleTabs.addTab("Administration", new Admin());
            roleTabs.addTab("Reports", new Reports());
            root.add(roleTabs, BorderLayout.CENTER);
            dashboard.setSize(1150, 720);
        } else {
            root.add(new PointOfSales(user), BorderLayout.CENTER);
            dashboard.setSize(1050, 680);
        }

        dashboard.setContentPane(root);
        dashboard.setVisible(true);
    }

    // Package-private getters so later segments can attach logic without altering UI structure
    JTextField getUsernameField() { return usernameField; }
    JPasswordField getPasswordField() { return passwordField; }
    JLabel getMessageLabel() { return messageLabel; }
    JButton getLoginBtn() { return loginBtn; }
    Models.User getLoggedInUser() { return loggedInUser; }
}