package dao;

import model.CartItem;
import java.sql.*;
import java.time.LocalDateTime;

public class RegistrationDaoImpl implements RegistrationDao {
    @Override
    public void confirm(String username, java.util.List<CartItem> items) throws SQLException {
        if (items == null || items.isEmpty()) return;
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                for (CartItem it : items) {
                    // 1) check availability
                    int available = getAvailable(c, it.projectId());
                    if (it.slots() > available) {
                        throw new SQLException("Insufficient slots for project " + it.title());
                    }
                    // 2) insert registration
                    double total = it.hourlyValue() * it.hours() * it.slots();
                    try (PreparedStatement ins = c.prepareStatement("""
                        INSERT INTO registrations(username, project_id, slots, hours, date_time, total_value)
                        VALUES(?,?,?,?,?,?)
                    """)) {
                        ins.setString(1, username);
                        ins.setInt(2, it.projectId());
                        ins.setInt(3, it.slots());
                        ins.setInt(4, it.hours());
                        ins.setString(5, LocalDateTime.now().toString());
                        ins.setDouble(6, total);
                        ins.executeUpdate();
                    }
                    // 3) bump registered_slots
                    try (PreparedStatement upd = c.prepareStatement("""
                        UPDATE projects SET registered_slots = registered_slots + ?
                        WHERE id=?
                    """)) {
                        upd.setInt(1, it.slots());
                        upd.setInt(2, it.projectId());
                        upd.executeUpdate();
                    }
                }
                // 4) clear cart
                try (PreparedStatement del = c.prepareStatement("DELETE FROM cart_items WHERE username=?")) {
                    del.setString(1, username);
                    del.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e instanceof SQLException ? (SQLException) e : new SQLException(e);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private int getAvailable(Connection c, int projectId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
            "SELECT total_slots-registered_slots FROM projects WHERE id=?")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                return Math.max(0, rs.getInt(1));
            }
        }
    }
}
