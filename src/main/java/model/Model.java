package model;

import java.io.IOException;
import java.sql.*;
import java.util.List;

import dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    private UserDao userDao;
    private User currentUser;

    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ProjectDaoCsv projectDaoCsv = new ProjectDaoCsv("/data/projects.csv");
    private final ProjectDaoDb  projectDaoDb  = new ProjectDaoDb();

    private final CartDao cartDao = new CartDaoImpl();
    private final RegistrationDao registrationDao = new RegistrationDaoImpl();

    public Model() {
        userDao = new UserDaoImpl();
    }

    public void setup() throws SQLException {
        System.out.println("SQLite file: " + dao.Database.dbFilePath());
        userDao.setup();
        Schema.setupAll();
        importProjectsIfEmpty();      // seed DB on first run
        try (var c = dao.Database.getConnection();
        	     var ps = c.prepareStatement("SELECT title, total_slots, registered_slots FROM projects");
        	     var rs = ps.executeQuery()) {
        	    while (rs.next()) {
        	        System.out.printf("%s -> total=%d, reg=%d%n",
        	            rs.getString(1), rs.getInt(2), rs.getInt(3));
        	    }
        	} catch (Exception ex) { ex.printStackTrace(); }

    }

    // Always load from DB so projects have real IDs
    public void loadProjects() {
        try {
            List<Project> all = projectDaoDb.loadAll();   // from DB
            projects.setAll(all);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
  
    private void importProjectsIfEmpty() throws SQLException {
        if (projectsTableCount() > 0) return;

        System.out.println("Importing projects from CSV into DB...");
        final java.util.List<model.Project> csv;
        try {
            csv = projectDaoCsv.loadAll();      // may throw IOException
        } catch (IOException e) {
            throw new SQLException("CSV import failed: " + e.getMessage(), e);
        }

        String ins = """
            INSERT INTO projects(title, location, day, hourly_value, total_slots, registered_slots, active)
            VALUES(?,?,?,?,?,?,1)
        """;

        try (var c = dao.Database.getConnection();
             var ps = c.prepareStatement(ins)) {

            int n = 0;
            for (var p : csv) {
                // Guard: if somehow total/registered are reversed, fix them NOW
                int total = p.getTotalSlots();
                int reg   = p.getRegisteredSlots();
                if (reg > total && total > 0) {
                    int tmp = total; total = reg; reg = tmp;
                }

                ps.setString(1, p.getTitle());
                ps.setString(2, p.getLocation());
                ps.setString(3, p.getDay());
                ps.setDouble(4, p.getHourlyValue());
                ps.setInt(5, total);     // total FIRST
                ps.setInt(6, reg);       // registered SECOND
                try {
                    ps.executeUpdate();
                    if (n++ < 3) {
                        System.out.printf("Inserted: %s (%s/%s) total=%d reg=%d%n",
                            p.getTitle(), p.getLocation(), p.getDay(), total, reg);
                    }
                } catch (SQLException dup) {
                    // UNIQUE(title,location,day) => ignore
                }
            }
        }
    }


    private int projectsTableCount() throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM projects");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

 
    public UserDao getUserDao() { 
    	return userDao; }
    public User getCurrentUser() { 
    	return this.currentUser; }
    public void setCurrentUser(User user) { 
    	currentUser = user; }
    public ObservableList<Project> getProjects() { 
    	return projects; }
    public CartDao getCartDao() { 
    	return cartDao; }
    public RegistrationDao getRegistrationDao() { 
    	return registrationDao; }
}
