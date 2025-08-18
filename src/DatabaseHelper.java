import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;

class DataBaseHelper {
    private String url;
    private Connection conn;
    protected int currentUserId;
    public static final int LOGIN_SUCCESS = 1;
    public static final int LOGIN_WRONG_PASSWORD = 2;
    public static final int LOGIN_NO_USER = 0;
    public static final int LOGIN_ERROR = -1;

    DataBaseHelper() {
        url = "jdbc:sqlite:data/securevault.db";

        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connected");
            createTables();
        } catch (SQLException e) {
            System.out.println("Error! " + e.getMessage());
        }
    }

    private void createTables() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, "
                + "password_hash TEXT NOT NULL, "
                + "salt TEXT NOT NULL"
                + ");";

        String createPasswordsTable = "CREATE TABLE IF NOT EXISTS passwords (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "service TEXT NOT NULL, " +
                "username TEXT NOT NULL, " +
                "password_encrypted TEXT NOT NULL, " +
                "iv TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createPasswordsTable);
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }

    public boolean checkUsernameAvailability(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return !rs.next(); // if a row exists, username is taken
        } catch (SQLException e) {
            System.out.println("Error checking username: " + e.getMessage());
            return false;
        }
    }

    public boolean addUser(String username, String hashedPassword, String salt) {
        String insertUser = "INSERT INTO users(username, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertUser)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.executeUpdate();
            System.out.println("User added successfully!");
            return true;
        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Login result codes:
     *  1 = success
     *  2 = incorrect password
     *  0 = user not found
     * -1 = database error
     */
    public int login(String username, char[] passwordChars) {
        String sql = "SELECT id, password_hash, salt FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbHash = rs.getString("password_hash");
                String salt = rs.getString("salt");

                String provided = new String(passwordChars);
                String hashedPassword = SecurityUtils.hashPassword(provided, salt);

                // wipe passwordChars immediately
                java.util.Arrays.fill(passwordChars, '\0');

                if (hashedPassword.equals(dbHash)) {
                    currentUserId = rs.getInt("id");
                    // derive session AES key here
                    try {
                        SecurityUtils.initSessionKeyFromPassword(provided);
                    } catch (Exception e) {
                        e.printStackTrace();  // or handle more gracefully
                        return 5;  // login fails if encryption key can't be initialized
                    }

                    return LOGIN_SUCCESS;
                } else {
                    return LOGIN_WRONG_PASSWORD;
                }
            } else {
                java.util.Arrays.fill(passwordChars, '\0');
                return LOGIN_NO_USER;
            }
        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
            return LOGIN_ERROR;
        }
    }

    public boolean addPassword(int userId, String service, String username, String plainPassword) {
        try {
            // encrypt the password before inserting
            SecurityUtils.EncryptedData enc = SecurityUtils.encrypt(plainPassword);
            String sql = "INSERT INTO passwords(user_id, service, username, password_encrypted, iv) VALUES(?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, service);
                stmt.setString(3, username);
                stmt.setString(4, enc.ciphertextBase64);
                stmt.setString(5, enc.ivBase64);
                stmt.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error saving password: " + e.getMessage());
            return false;
        }
    }

    public List<Object[]> getPasswords() {
        List<Object[]> passwordList = new ArrayList<>();
        String sql = "SELECT id, service, username, password_encrypted, iv FROM passwords WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                try {
                    String decrypted = SecurityUtils.decrypt(rs.getString("password_encrypted"), rs.getString("iv"));
                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("service"),
                        rs.getString("username"),
                        decrypted
                    };
                    passwordList.add(row);
                } catch (Exception e) {
                    System.out.println("Decryption failed for entry id=" + rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Couldn't retrieve passwords: " + e.getMessage());
        }
        return passwordList;
    }

    public DefaultTableModel getPasswordsTableModel() {
        String[] columnNames = { "ID", "Service", "Username", "Password" };
        List<Object[]> passwordList = getPasswords();

        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        for (Object[] row : passwordList) {
            model.addRow(row);
        }
        return model;
    }

    public boolean deletePassword(int userId, String siteName, String siteUsername) {
        String sql = "DELETE FROM passwords WHERE user_id=? AND service=? AND username=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, siteName);
            stmt.setString(3, siteUsername);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
