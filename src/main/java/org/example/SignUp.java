package org.example;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SignUp extends JFrame {

    private JTextField usernameField, fullNameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleCombo;
    private JLabel messageLabel;
    private JButton signUpBtn, backToLoginBtn;

    public SignUp() {
        setTitle("HealthFirst PIMS - Sign Up");
        setSize(450, 560);
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
        header.setPreferredSize(new Dimension(450, 100));
        JLabel title = new JLabel("Create Account");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("HealthFirst Pharmacy - PIMS");
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
        form.setBorder(new EmptyBorder(30, 50, 20, 50));

        usernameField = new JTextField();
        fullNameField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        roleCombo = new JComboBox<>(new String[]{"Cashier", "Admin"});

        form.add(fieldBlock("Full Name", fullNameField));
        form.add(Box.createVerticalStrut(15));
        form.add(fieldBlock("Username", usernameField));
        form.add(Box.createVerticalStrut(15));
        form.add(fieldBlock("Password", passwordField));
        form.add(Box.createVerticalStrut(15));
        form.add(fieldBlock("Confirm Password", confirmPasswordField));
        form.add(Box.createVerticalStrut(15));
        form.add(fieldBlock("Role", roleCombo));
        form.add(Box.createVerticalStrut(15));

        messageLabel = new JLabel(" ");
        messageLabel.setForeground(UIStyle.DANGER);
        messageLabel.setFont(UIStyle.FONT_LABEL);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(messageLabel);
        form.add(Box.createVerticalStrut(10));

        signUpBtn = UIStyle.styledButton("SIGN UP", UIStyle.SUCCESS);
        signUpBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signUpBtn.addActionListener(e -> attemptSignUp());
        form.add(signUpBtn);
        form.add(Box.createVerticalStrut(10));

        backToLoginBtn = new JButton("Already have an account? Login");
        backToLoginBtn.setFont(UIStyle.FONT_LABEL);
        backToLoginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backToLoginBtn.setBorderPainted(false);
        backToLoginBtn.setContentAreaFilled(false);
        backToLoginBtn.setForeground(UIStyle.PRIMARY);
        backToLoginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToLoginBtn.addActionListener(e -> {
            dispose();
            new Login().setVisible(true);
        });
        form.add(backToLoginBtn);

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel fieldBlock(String label, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(UIStyle.FONT_LABEL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setFont(UIStyle.FONT_FIELD);
        field.setMaximumSize(new Dimension(400, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(5));
        p.add(field);
        return p;
    }

    private void attemptSignUp() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            messageLabel.setForeground(UIStyle.DANGER);
            messageLabel.setText("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirm)) {
            messageLabel.setForeground(UIStyle.DANGER);
            messageLabel.setText("Passwords do not match.");
            return;
        }

        boolean success = DBConnection.registerUser(username, password, role, fullName);
        if (success) {
            JOptionPane.showMessageDialog(this, "Account created successfully! Please log in.",
                    "Sign Up Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new Login().setVisible(true);
        } else {
            messageLabel.setForeground(UIStyle.DANGER);
            messageLabel.setText("Sign up failed - username may already be taken.");
        }
    }
}