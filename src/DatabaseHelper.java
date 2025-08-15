import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

        String createPasswordsTable = "CREATE TABLE IF NOT EXISTS passwords ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "site_name TEXT NOT NULL, "
                + "site_username TEXT NOT NULL, "
                + "site_password TEXT NOT NULL, "
                + "FOREIGN KEY (user_id) REFERENCES users(id)"
                + ");";

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

            // If a row is returned, username already exists
            return !rs.next();

        } catch (SQLException e) {
            System.out.println("Error checking username: " + e.getMessage());
            // In case of DB error, assume unavailable
            return false;
        }
    }
    public boolean addUser(String username, String hashedPassword, String salt) {
        String insertUser = "INSERT INTO users(username, password_hash, salt) VALUES (?, ?, ?)";
        boolean success;

        try (PreparedStatement stmt = conn.prepareStatement(insertUser)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.executeUpdate();
            System.out.println("User added successfully!");
            success = true;
        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
            success = false;
        }
        return success;
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

            if (rs.next()) { // Check if a row exists
                String dbHash = rs.getString("password_hash");
                String salt = rs.getString("salt");

                String hashedPassword = SecurityUtils.hashPassword(new String(passwordChars), salt);
                java.util.Arrays.fill(passwordChars, '\0'); // wipe immediately

                if (hashedPassword.equals(dbHash)) {
                    System.out.println("Login Successful!");
                    currentUserId = rs.getInt("id");
                    return LOGIN_SUCCESS;
                } else {
                    System.out.println("Incorrect Password");
                    return LOGIN_WRONG_PASSWORD;
                }
            } else {
                System.out.println("User does not exist");
                java.util.Arrays.fill(passwordChars, '\0'); // wipe even if user not found
                return LOGIN_NO_USER;
            }
        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
            return LOGIN_ERROR;
        }
    }

    public boolean addPassword(int currentUserId, String siteName, String siteUsername, String sitePassword){
        String sql = "INSERT INTO passwords(user_id, site_name, site_username, site_password) VALUES(?, ?, ?, ?)";

        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, currentUserId );
            stmt.setString(2, siteName);
            stmt.setString(3, siteUsername);
            stmt.setString(4, sitePassword);

            stmt.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            System.out.println("Error saving password : " + e.getMessage());
            return false;
        }
    }
}
