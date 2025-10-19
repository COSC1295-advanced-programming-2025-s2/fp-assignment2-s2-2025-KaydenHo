
package dao;

import java.sql.*;

public final class Schema {
    private Schema() {}

    public static void setupAll() throws SQLException {
        try (Connection c = Database.getConnection()) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS projects(
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      title TEXT NOT NULL,
                      location TEXT NOT NULL,
                      day TEXT NOT NULL,            -- Mon..Sun
                      hourly_value REAL NOT NULL,
                      total_slots INTEGER NOT NULL,
                      registered_slots INTEGER NOT NULL,
                      active INTEGER NOT NULL DEFAULT 1,
                      UNIQUE(title, location, day)
                    )
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS cart_items(
                      username TEXT NOT NULL,
                      project_id INTEGER NOT NULL,
                      slots INTEGER NOT NULL,     -- 1..3 (per user cap)
                      hours INTEGER NOT NULL,     -- 1..3
                      added_at TEXT NOT NULL,
                      PRIMARY KEY(username, project_id),
                      FOREIGN KEY(project_id) REFERENCES projects(id)
                    )
                """);

                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS registrations(
                      reg_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      username TEXT NOT NULL,
                      project_id INTEGER NOT NULL,
                      slots INTEGER NOT NULL,
                      hours INTEGER NOT NULL,
                      date_time TEXT NOT NULL,      -- ISO-8601
                      total_value REAL NOT NULL,
                      FOREIGN KEY(project_id) REFERENCES projects(id)
                    )
                """);
            }
        }
    }
}
