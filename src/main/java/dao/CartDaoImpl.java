package dao;

import model.CartItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDaoImpl implements CartDao {
    @Override
    public void upsertCartItem(String username, int projectId, int slots, int hours) throws SQLException {
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("""
                INSERT INTO cart_items(username, project_id, slots, hours, added_at)
                VALUES(?,?,?,?,datetime('now'))
                ON CONFLICT(username, project_id) DO UPDATE SET
                  slots=excluded.slots, hours=excluded.hours, added_at=datetime('now')
            """)) {
                ps.setString(1, username);
                ps.setInt(2, projectId);
                ps.setInt(3, slots);
                ps.setInt(4, hours);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void removeCartItem(String username, int projectId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM cart_items WHERE username=? AND project_id=?")) {
            ps.setString(1, username);
            ps.setInt(2, projectId);
            ps.executeUpdate();
        }
    }

    @Override
    public void clearCart(String username) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM cart_items WHERE username=?")) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    @Override
    public List<CartItem> listCart(String username) throws SQLException {
        String sql = """
            SELECT p.id, p.title, p.location, p.day, p.hourly_value, c.slots, c.hours
            FROM cart_items c JOIN projects p ON p.id=c.project_id
            WHERE c.username=?
            """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                List<CartItem> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new CartItem(
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getDouble(5), rs.getInt(6), rs.getInt(7)
                    ));
                }
                return out;
            }
        }
    }
}
