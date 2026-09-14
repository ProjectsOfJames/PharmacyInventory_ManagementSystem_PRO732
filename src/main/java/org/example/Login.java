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

        JButton signUpLink = new JButton("Don't have an account? Sign Up");
        signUpLink.setFont(UIStyle.FONT_LABEL);
        signUpLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        signUpLink.setBorderPainted(false);
        signUpLink.setContentAreaFilled(false);
        signUpLink.setForeground(UIStyle.PRIMARY);
        signUpLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLink.addActionListener(e -> {
            dispose();
            new SignUp().setVisible(true);
        });

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
        form.add(Box.createVerticalStrut(12));
        form.add(signUpLink);

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
        messageLabel.setForeground(UIStyle.SUCCESS);
        messageLabel.setText("Login successful. Welcome, " + user.getFullName() + "!");
    }

    // Package-private getters so later segments can attach logic without altering UI structure
    JTextField getUsernameField() { return usernameField; }
    JPasswordField getPasswordField() { return passwordField; }
    JLabel getMessageLabel() { return messageLabel; }
    JButton getLoginBtn() { return loginBtn; }
    Models.User getLoggedInUser() { return loggedInUser; }
}