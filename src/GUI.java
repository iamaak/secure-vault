import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class GUI extends JFrame {
    private DataBaseHelper db;
    private JPanel mainPanel, loginPanel, homePanel, registerPanel;
    private JLabel username, password, homeLabel;
    private JButton login, register, homeBackButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private CardLayout cardLayout;

    GUI(DataBaseHelper db) {
        this.db = db;
        prepareGUI();
    }
    
    public boolean registerUser(String username, String hashedPassword, String salt) {
        boolean isRegistered = db.addUser(username, hashedPassword, salt);
        return isRegistered;
    }

    private void prepareGUI() {
        // CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Login Panel
        loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        username = new JLabel("Username : ");
        password = new JLabel("Password : ");
        usernameField = new JTextField(10);
        passwordField = new JPasswordField(10);
        login = new JButton("Log In");
        register = new JButton("Register");

        loginPanel.add(username);
        loginPanel.add(usernameField);
        loginPanel.add(password);
        loginPanel.add(passwordField);
        loginPanel.add(login);
        loginPanel.add(register);

        // Home Panel
        homePanel = new JPanel(new BorderLayout(10, 10));

        // Top: Welcome Label
        homeLabel = new JLabel("", JLabel.CENTER);
        homeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        homePanel.add(homeLabel, BorderLayout.NORTH);

        // Center: Table of saved credentials
        String[] columnNames = {"Site Name", "Username", "Password"};
        Object[][] data = {
            {"example.com", "user123", "********"},
            {"gmail.com", "myemail", "********"}
        };
        JTable passwordTable = new JTable(data, columnNames);
        passwordTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(passwordTable);
        homePanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom: Button panel
        JPanel bottomPanel = new JPanel();
        JButton addPasswordButton = new JButton("Add New Password");
        JButton logoutButton = new JButton("Logout");
        homeBackButton = new JButton("<-- Go Back");

        bottomPanel.add(addPasswordButton);
        bottomPanel.add(homeBackButton);
        bottomPanel.add(logoutButton);

        homePanel.add(bottomPanel, BorderLayout.SOUTH);


        // Register Panel
        registerPanel = new JPanel();
        JLabel regUsername = new JLabel("Username : ");
        registerPanel.add(regUsername);
        JTextField  userField= new JTextField(10);
        registerPanel.add(userField);
        JLabel regPassword = new JLabel("Password : ");
        registerPanel.add(regPassword);
        JPasswordField  pwdField= new JPasswordField(10);
        registerPanel.add(pwdField);
        JLabel cfmPassword = new JLabel("Conform Password : ");
        registerPanel.add(cfmPassword);
        JPasswordField  cfmpwdField= new JPasswordField(10);
        registerPanel.add(cfmpwdField);
        JButton registerButton = new JButton("Register");
        registerPanel.add(registerButton);

        registerButton.addActionListener(e -> {
            String username = userField.getText().trim();
            char[] pwdChars = pwdField.getPassword();
            char[] cfmpwdChars = cfmpwdField.getPassword();

            // Username validation
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(registerPanel, "Username is empty!");
                return;
            }

            // Password match check
            if (!java.util.Arrays.equals(pwdChars, cfmpwdChars)) {
                JOptionPane.showMessageDialog(registerPanel, "Passwords do not match!");
                return;
            }

            String pwd = new String(pwdChars);

            // Minimum length check
            if (pwd.length() < 8) {
                JOptionPane.showMessageDialog(registerPanel, "Password must be at least 8 characters.");
                return;
            }

            // Complexity checks
            if (!pwd.matches(".*[A-Z].*") ||
                !pwd.matches(".*[a-z].*") ||
                !pwd.matches(".*\\d.*") ||
                //!pwd.matches(".*[!@#$%^&*(),.?\":{}|<>].*")
                !pwd.matches(".*[^a-zA-Z0-9].*")) {
                JOptionPane.showMessageDialog(registerPanel, 
                    "Password must contain upper, lower, number, and special character.");
                return;
            }

            // username availability check 
            if (!db.checkUsernameAvailability(username)){
                JOptionPane.showMessageDialog(registerPanel, "Sorry! This username is already taken");
                return;
            }
            // Generate salt
            String salt = SecurityUtils.generateSalt();

            // Hash password
            String hashedPassword = SecurityUtils.hashPassword(pwd, salt);

            // Store username, hashedPassword, and salt in DB
            // TODO: Use DatabaseHelper to insert
            registerUser(username, hashedPassword, salt);

            // Clear password arrays from memory
            java.util.Arrays.fill(pwdChars, '0');
            java.util.Arrays.fill(cfmpwdChars, '0');

            JOptionPane.showMessageDialog(registerPanel, "Registration successful!");
        });


        // Add panels to mainPanel
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(homePanel, "Home");
        mainPanel.add(registerPanel, "registeration");

        // Login button action
        login.addActionListener(e -> {
            String usernameInput = usernameField.getText();
            String passwordInput = new String(passwordField.getPassword());


            int result = db.login(usernameInput, passwordInput); 

            if (result == 1) {
                JOptionPane.showMessageDialog(loginPanel, "Welcome! " + usernameInput);
                homeLabel.setText("Welcome to Home Screen! " + usernameInput);
                cardLayout.show(mainPanel, "Home");
            } else if (result == 2) {
                JOptionPane.showMessageDialog(loginPanel, "Incorrect Password!");
            } else if (result == 0) {
                JOptionPane.showMessageDialog(loginPanel, "User Doesn't Exist!");
            } else if (result == -1) {
                JOptionPane.showMessageDialog(loginPanel, "Database Error! Please try again later.");
            }
        });

        // Back button action
        homeBackButton.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        register.addActionListener(e -> cardLayout.show(mainPanel, "registeration"));

        add(mainPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setTitle("SecureVault");
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
}
