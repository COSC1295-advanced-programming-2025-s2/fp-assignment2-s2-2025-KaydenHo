package dao;

import java.sql.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import model.User;
import util.PasswordHasher;

public class UserDaoImpl implements UserDao {

    // current expected columns
    private static final Set<String> EXPECTED_COLS = Set.of(
        "username", "full_name", "email", "password_hash", "created_at"
    );

    @Override
    public void setup() throws SQLException {
        try (Connection c = Database.getConnection()) {
            if (!usersTableExists(c)) {
                createUsersTable(c);
            } else if (!schemaIsCorrect(c)) {
                // migrate: drop and recreate users with the correct schema
                // (since legacy schema stored plaintext password, we can't transform)
                try (Statement s = c.createStatement()) {
                    s.executeUpdate("DROP TABLE IF EXISTS users");
                }
                createUsersTable(c);
            }
            seedAdminIfMissing(c);
        }
    }

    private boolean usersTableExists(Connection c) throws SQLException {
        try (ResultSet rs = c.getMetaData().getTables(null, null, "users", null)) {
            return rs.next();
        }
    }

    private boolean schemaIsCorrect(Connection c) throws SQLException {
        // check columns present
        try (PreparedStatement ps = c.prepareStatement("PRAGMA table_info(users)");
             ResultSet rs = ps.executeQuery()) {
            Set<String> cols = new HashSet<>();
            while (rs.next()) cols.add(rs.getString("name"));
            return cols.containsAll(EXPECTED_COLS);
        }
    }

    private void createUsersTable(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                  username TEXT PRIMARY KEY,
                  full_name TEXT NOT NULL,
                  email TEXT NOT NULL UNIQUE,
                  password_hash TEXT NOT NULL,
                  created_at TEXT NOT NULL
                )
            """);
        }
    }

    private void seedAdminIfMissing(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM users WHERE username = 'admin'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO users(username, full_name, email, password_hash, created_at) VALUES (?,?,?,?,?)")) {
            ps.setString(1, "admin");
            ps.setString(2, "System Administrator");
            ps.setString(3, "admin@example.com");
            ps.setString(4, PasswordHasher.sha256("Admin654!@"));
            ps.setString(5, Instant.now().toString());
            ps.executeUpdate();
        }
    }

    @Override
    public User getUserByCredentials(String username, String rawPassword) throws SQLException {
        final String sql = "SELECT username, full_name, email, password_hash FROM users WHERE username = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String stored = rs.getString("password_hash");
                if (PasswordHasher.sha256(rawPassword).equals(stored)) {
                    return new User(
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        stored
                    );
                }
                return null;
            }
        }
    }

    @Override
    public User createUser(String fullName, String username, String email, String rawPassword) throws SQLException {
        final String insert =
            "INSERT INTO users(username, full_name, email, password_hash, created_at) VALUES (?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(insert)) {
            ps.setString(1, username.trim());
            ps.setString(2, fullName.trim());
            ps.setString(3, email.trim());
            ps.setString(4, PasswordHasher.sha256(rawPassword));
            ps.setString(5, Instant.now().toString());
            ps.executeUpdate();
        }
        return new User(username.trim(), fullName.trim(), email.trim(), null);
    }

    @Override
    public boolean usernameExists(String username) throws SQLException {
        final String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    @Override
    public boolean updatePassword(String username, String newRawPassword) throws SQLException {
        final String sql = "UPDATE users SET password_hash=? WHERE username=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, util.PasswordHasher.sha256(newRawPassword));
            ps.setString(2, username.trim());
            return ps.executeUpdate() == 1;
        }
    }
}
