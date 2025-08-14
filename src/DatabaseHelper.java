import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

class DataBaseHelper {
    private String url;
    private Connection conn;

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
}
