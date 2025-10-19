package dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String DB_FILENAME = "application.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILENAME;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // --- helpers for troubleshooting ---
    public static String dbFilePath() {
        return new File(DB_FILENAME).getAbsolutePath();
    }

    public static boolean deleteDbFile() {
        File f = new File(DB_FILENAME);
        return f.exists() && f.delete();
    }
}
