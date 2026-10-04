package com.equishare.ui.swing;

import com.equishare.model.User;
import com.equishare.service.UserManager;

import javax.swing.*;
import java.awt.*;

/**
 * Authentication dialog for User Login and Registration with Quick-Demo buttons.
 */
public class LoginDialog extends JDialog {
    private final UserManager userManager;
    private User authenticatedUser;

    private JTextField txtLoginUsername;
    private JPasswordField txtLoginPassword;

    private JTextField txtRegUsername;
    private JTextField txtRegFullName;
    private JTextField txtRegEmail;
    private JPasswordField txtRegPassword;

    public LoginDialog(Frame owner, UserManager userManager) {
        super(owner, "EquiShare Pro - Authentication Gateway", true);
        this.userManager = userManager;
        this.authenticatedUser = null;

        initComponents();
        setSize(420, 480);
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 37, 41));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("EquiShare Pro");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Expense Settlement & Financial Audit Engine");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(173, 181, 189));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane (Login / Register)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // TAB 1: LOGIN
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel lblUser = new JLabel("Username:");
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(lblUser);
        loginPanel.add(Box.createVerticalStrut(4));

        txtLoginUsername = new JTextField(20);
        txtLoginUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtLoginUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtLoginUsername.setPreferredSize(new Dimension(340, 32));
        txtLoginUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(txtLoginUsername);
        loginPanel.add(Box.createVerticalStrut(10));

        JLabel lblPass = new JLabel("Password:");
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(lblPass);
        loginPanel.add(Box.createVerticalStrut(4));

        txtLoginPassword = new JPasswordField(20);
        txtLoginPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtLoginPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtLoginPassword.setPreferredSize(new Dimension(340, 32));
        txtLoginPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(txtLoginPassword);
        loginPanel.add(Box.createVerticalStrut(15));

        JButton btnLogin = new JButton("Sign In");
        btnLogin.setBackground(new Color(13, 110, 253));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnLogin.setPreferredSize(new Dimension(340, 36));
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(e -> performLogin());
        loginPanel.add(btnLogin);

        loginPanel.add(Box.createVerticalStrut(20));
        JLabel quickLabel = new JLabel("⚡ Quick 1-Click Evaluation Accounts:");
        quickLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        quickLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPanel.add(quickLabel);
        loginPanel.add(Box.createVerticalStrut(8));

        JPanel demoButtons = new JPanel(new GridLayout(1, 3, 6, 0));
        demoButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        demoButtons.setPreferredSize(new Dimension(340, 32));
        demoButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAlice = new JButton("Alice");
        btnAlice.setToolTipText("Login as Alice Sharma (Creator)");
        btnAlice.addActionListener(e -> quickDemoLogin("alice"));
        JButton btnBob = new JButton("Bob");
        btnBob.setToolTipText("Login as Bob Verma");
        btnBob.addActionListener(e -> quickDemoLogin("bob"));
        JButton btnCharlie = new JButton("Charlie");
        btnCharlie.setToolTipText("Login as Charlie Singh");
        btnCharlie.addActionListener(e -> quickDemoLogin("charlie"));

        demoButtons.add(btnAlice);
        demoButtons.add(btnBob);
        demoButtons.add(btnCharlie);
        loginPanel.add(demoButtons);

        tabbedPane.addTab("Login", loginPanel);

        // TAB 2: REGISTER
        JPanel regPanel = new JPanel();
        regPanel.setLayout(new BoxLayout(regPanel, BoxLayout.Y_AXIS));
        regPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel lblRegUser = new JLabel("Username (unique ID, e.g. john):");
        lblRegUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPanel.add(lblRegUser);
        regPanel.add(Box.createVerticalStrut(3));

        txtRegUsername = new JTextField(20);
        txtRegUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRegUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtRegUsername.setPreferredSize(new Dimension(340, 30));
        txtRegUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPanel.add(txtRegUsername);
        regPanel.add(Box.createVerticalStrut(8));

        JLabel lblRegName = new JLabel("Full Name (e.g. John Doe):");
        lblRegName.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPanel.add(lblRegName);
        regPanel.add(Box.createVerticalStrut(3));

        txtRegFullName = new JTextField(20);
        txtRegFullName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRegFullName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtRegFullName.setPreferredSize(new Dimension(340, 30));
        txtRegFullName.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPanel.add(txtRegFullName);
        regPanel.add(Box.createVerticalStrut(8));

        JLabel lblRegEmail = new JLabel("Email Address:");
        lblRegEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPanel.add(lblRegEmail);
        regPanel.add(Box.createVerticalStrut(3));

        txtRegEmail = new JTextField(20);
        txtRegEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRegEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtRegEmail.setPreferredSize(new Dimension(340, 30));
        txtRegEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPanel.add(txtRegEmail);
        regPanel.add(Box.createVerticalStrut(8));

        JLabel lblRegPass = new JLabel("Password (min 4 chars):");
        lblRegPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPanel.add(lblRegPass);
        regPanel.add(Box.createVerticalStrut(3));

        txtRegPassword = new JPasswordField(20);
        txtRegPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRegPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtRegPassword.setPreferredSize(new Dimension(340, 30));
        txtRegPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPanel.add(txtRegPassword);
        regPanel.add(Box.createVerticalStrut(14));

        JButton btnRegister = new JButton("Create Account");
        btnRegister.setBackground(new Color(25, 135, 84));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnRegister.setPreferredSize(new Dimension(340, 36));
        btnRegister.setFocusPainted(false);
        btnRegister.addActionListener(e -> performRegister());
        regPanel.add(btnRegister);
        tabbedPane.addTab("Register", regPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void performLogin() {
        String username = txtLoginUsername.getText().trim();
        String password = new String(txtLoginPassword.getPassword());

        try {
            authenticatedUser = userManager.authenticate(username, password);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quickDemoLogin(String username) {
        try {
            authenticatedUser = userManager.authenticate(username, "password123");
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Demo login failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performRegister() {
        String username = txtRegUsername.getText().trim();
        String fullName = txtRegFullName.getText().trim();
        String email = txtRegEmail.getText().trim();
        String password = new String(txtRegPassword.getPassword());

        try {
            authenticatedUser = userManager.registerUser(username, password, fullName, email);
            JOptionPane.showMessageDialog(this, "Registration successful! Welcome, " + fullName, "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}
