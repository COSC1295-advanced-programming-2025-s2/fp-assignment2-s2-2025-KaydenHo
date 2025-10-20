package dao;

import model.Project;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDaoDb implements ProjectDao {
    @Override
    public List<Project> loadAll() {
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
            throw new RuntimeException("Load projects from DB failed", e);
        }
    }
}
