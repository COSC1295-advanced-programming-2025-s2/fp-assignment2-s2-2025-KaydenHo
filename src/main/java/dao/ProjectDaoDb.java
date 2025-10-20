package dao;

import model.Project;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDaoDb implements ProjectDao {
    @Override
    public List<Project> loadAll() throws IOException {
        String sql = """
            SELECT id, title, location, day, hourly_value, total_slots, registered_slots
            FROM projects
            WHERE active = 1
            ORDER BY title, location, day
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Project> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Project(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("location"),
                    rs.getString("day"),
                    rs.getDouble("hourly_value"),
                    rs.getInt("total_slots"),
                    rs.getInt("registered_slots")
                ));
            }
            return out;
        } catch (SQLException e) {
            throw new IOException("DB load projects failed", e);
        }
    }
    
    public List<model.Project> loadAllIncludingInactive() throws IOException {
        String sql = """
            SELECT id, title, location, day, hourly_value, total_slots, registered_slots
            FROM projects
            ORDER BY active DESC, title, location, day
        """;
        try (var c = Database.getConnection();
             var ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            var out = new ArrayList<model.Project>();
            while (rs.next()) {
                out.add(new model.Project(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("location"),
                    rs.getString("day"),
                    rs.getDouble("hourly_value"),
                    rs.getInt("total_slots"),
                    rs.getInt("registered_slots")
                ));
            }
            return out;
        } catch (SQLException e) { throw new IOException(e); }
    }

    public void upsertProject(String title, String location, String day,
                              double hourly, int total, int registered, boolean active) throws SQLException {
        String sql = """
            INSERT INTO projects(title, location, day, hourly_value, total_slots, registered_slots, active)
            VALUES(?,?,?,?,?,?,?)
            ON CONFLICT(title, location, day)
            DO UPDATE SET hourly_value=excluded.hourly_value,
                          total_slots=excluded.total_slots,
                          registered_slots=excluded.registered_slots,
                          active=excluded.active
        """;
        try (var c = Database.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setString(1, title); ps.setString(2, location); ps.setString(3, day);
            ps.setDouble(4, hourly); ps.setInt(5, total); ps.setInt(6, registered);
            ps.setInt(7, active ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void setActive(int id, boolean active) throws SQLException {
        try (var c = Database.getConnection();
             var ps = c.prepareStatement("UPDATE projects SET active=? WHERE id=?")) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
