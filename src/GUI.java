import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

class GUI extends JFrame {
    private DataBaseHelper db;
    private JPanel mainPanel, loginPanel, homePanel, registerPanel, addNewPasswordPanel;
    private JLabel username, password, homeLabel;
    private JButton login, register, homeBackButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private CardLayout cardLayout;
    private JTable passwordTable; 


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
        passwordTable = new JTable(new DefaultTableModel(
            new String[]{"Site Name", "Username", "Password"}, 0
        ));
        JScrollPane scrollPane = new JScrollPane(passwordTable);
        homePanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom: Button panel
        JPanel bottomPanel = new JPanel();
        JButton addPasswordButton = new JButton("Add New Password");
        JButton logoutButton = new JButton("Logout");
        JButton deletePasswordButton = new JButton("Delete Selected");

        deletePasswordButton.addActionListener(e -> {
            int selectedRow = passwordTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(homePanel, "Please select a row to delete!");
                return;
            }

            // Get values from table
            String siteName = passwordTable.getValueAt(selectedRow, 1).toString();
            String siteUsername = passwordTable.getValueAt(selectedRow, 2).toString();

            int confirm = JOptionPane.showConfirmDialog(
                homePanel,
                "Are you sure you want to delete this password?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = db.deletePassword(db.currentUserId, siteName, siteUsername);
                if (success) {
                    JOptionPane.showMessageDialog(homePanel, "Password deleted successfully!");
                    passwordTable.setModel(db.getPasswordsTableModel()); // Refresh table
                } else {
                    JOptionPane.showMessageDialog(homePanel, "Error deleting password!");
                }
            }
        });

        
        bottomPanel.add(deletePasswordButton);
        bottomPanel.add(addPasswordButton);
        bottomPanel.add(logoutButton);

        homePanel.add(bottomPanel, BorderLayout.SOUTH);

        addPasswordButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "addPassword");
        });

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                homePanel,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // 1. Clear current user session
                db.currentUserId = -1;

                // 2. Wipe password fields from memory (if available in scope)
                char[] pwdChars = passwordField.getPassword();
                java.util.Arrays.fill(pwdChars, '0');
                passwordField.setText("");


                // 3. Clear username field
                usernameField.setText("");

                // 4. Optionally clear any cached sensitive lists
                // if (passwordList != null) passwordList.clear();

                // 5. Navigate to login screen
                cardLayout.show(mainPanel, "Login");
            }
        });


        // Create Password Panel
        JPanel addNewPasswordPanel = new JPanel();
        addNewPasswordPanel.setLayout(new GridLayout(5, 2, 10, 10));

        JLabel siteNameLabel = new JLabel("Site Name:");
        JTextField siteNameField = new JTextField(15);

        JLabel siteUsernameLabel = new JLabel("Site Username:");
        JTextField siteUsernameField = new JTextField(15);

        JLabel sitePasswordLabel = new JLabel("Site Password:");
        JPasswordField sitePasswordField = new JPasswordField(15);

        JButton savePasswordButton = new JButton("Save Password");
        JButton backButton = new JButton(" Back");

        // Add components to the panel
        addNewPasswordPanel.add(siteNameLabel);
        addNewPasswordPanel.add(siteNameField);
        addNewPasswordPanel.add(siteUsernameLabel);
        addNewPasswordPanel.add(siteUsernameField);
        addNewPasswordPanel.add(sitePasswordLabel);
        addNewPasswordPanel.add(sitePasswordField);
        addNewPasswordPanel.add(savePasswordButton);
        addNewPasswordPanel.add(backButton);

        // Action to save password
        savePasswordButton.addActionListener(e -> {
            String siteName = siteNameField.getText().trim();
            String siteUsername = siteUsernameField.getText().trim();
            String sitePassword = new String(sitePasswordField.getPassword());

            if (siteName.isEmpty() || siteUsername.isEmpty() || sitePassword.isEmpty()) {
                JOptionPane.showMessageDialog(addNewPasswordPanel, "Please fill in all fields!");
                return;
            }

            // Optional: Encrypt sitePassword here before saving
            boolean success = db.addPassword(db.currentUserId, siteName, siteUsername, sitePassword);
            if (success) {
                JOptionPane.showMessageDialog(addNewPasswordPanel, "Password saved successfully!");
                siteNameField.setText("");
                siteUsernameField.setText("");
                sitePasswordField.setText("");

                // Refresh table after adding password
                passwordTable.setModel(db.getPasswordsTableModel());
            } else {
                JOptionPane.showMessageDialog(addNewPasswordPanel, "Error saving password!");
            }
        });



        // Back button action
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "Home"));

        // Add to CardLayout
        mainPanel.add(addNewPasswordPanel, "addPassword");


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
        JButton regBackButton = new JButton("Go To Login");
        registerPanel.add(regBackButton);

        regBackButton.addActionListener(e->{
            cardLayout.show(mainPanel, "Login");
        });

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
            registerUser(username, hashedPassword, salt);

            // Wipe plaintext passwords from memory
            java.util.Arrays.fill(pwdChars, '\0');
            java.util.Arrays.fill(cfmpwdChars, '\0');
            pwdChars = null;
            cfmpwdChars = null;
            

            JOptionPane.showMessageDialog(registerPanel, "Registration successful!");
            cardLayout.show(mainPanel, "Login");
        });


        // Add panels to mainPanel
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(homePanel, "Home");
        mainPanel.add(registerPanel, "registeration");
        mainPanel.add(addNewPasswordPanel, "addPassword");

        // Login button action
        login.addActionListener(e -> {
            String usernameInput = usernameField.getText();
            char[] passwordInput = passwordField.getPassword();


            int result = db.login(usernameInput, passwordInput); 

            if (result == db.LOGIN_SUCCESS) {
                JOptionPane.showMessageDialog(loginPanel, "Welcome! " + usernameInput);
                homeLabel.setText("Welcome to Home Screen! " + usernameInput);

                // Update table with user's passwords
                passwordTable.setModel(db.getPasswordsTableModel());

                

                cardLayout.show(mainPanel, "Home");
            }
            else if (result == db.LOGIN_WRONG_PASSWORD) {
                JOptionPane.showMessageDialog(loginPanel, "Incorrect Password!");
            } else if (result == db.LOGIN_NO_USER) {
                JOptionPane.showMessageDialog(loginPanel, "User Doesn't Exist!");
            } else if (result == db.LOGIN_ERROR) {
                JOptionPane.showMessageDialog(loginPanel, "Database Error! Please try again later.");
            }
        });

        // Back button action
        //homeBackButton.addActionListener(e -> cardLayout.show(mainPanel, "Login"));
        register.addActionListener(e -> cardLayout.show(mainPanel, "registeration"));

        add(mainPanel);

        URL iconURL = getClass().getResource("/resources/secureVault.png");
        if (iconURL == null) {
            System.err.println("Image not found!");
        }

        ImageIcon icon = new ImageIcon(iconURL);

        setIconImage(icon.getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setTitle("SecureVault");
        setLocationRelativeTo(null);
        //setResizable(false);
        //setVisible(true);
    }
}
