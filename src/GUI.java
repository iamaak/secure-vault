import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class GUI extends JFrame {
    private JPanel mainPanel, loginPanel, homePanel, registerPanel;
    private JLabel username, password, homeLabel;
    private JButton login, register, homeBackButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private CardLayout cardLayout;

    GUI() {
        prepareGUI();
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
        homePanel = new JPanel();
        homeLabel = new JLabel();
        homeBackButton = new JButton("<-- Go Back");

        homePanel.add(homeLabel);
        homePanel.add(homeBackButton);

        // Register Panel
        registerPanel = new JPanel();

        // Add panels to mainPanel
        mainPanel.add(loginPanel, "Login");
        mainPanel.add(homePanel, "Home");
        mainPanel.add(registerPanel, "registeration");

        // Login button action
        login.addActionListener(e -> {
            String usernameInput = usernameField.getText();
            String passwordInput = new String(passwordField.getPassword());

            if (usernameInput.equals("admin") && passwordInput.equals("123")) {
                JOptionPane.showMessageDialog(loginPanel, "Welcome! " + usernameInput);
                homeLabel.setText("Welcome to Home Screen! " + usernameInput);
                cardLayout.show(mainPanel, "Home");
            } else {
                JOptionPane.showMessageDialog(loginPanel, "Invalid Credentials");
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
