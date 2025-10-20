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
    }

    // Always load from DB so projects have real IDs
    public void loadProjects() {
        List<Project> all = projectDaoDb.loadAll();
        projects.setAll(all);
    }

  
    private void importProjectsIfEmpty() 
    		throws SQLException {
        if (projectsTableCount() > 0) 
        	return;

        System.out.println("Importing projects from CSV into DB...");
        try {
            List<Project> csv = projectDaoCsv.loadAll(); // may throw IOException

            try (Connection c = Database.getConnection()) {
                String ins = """
                  INSERT INTO projects(title, location, day, hourly_value, total_slots, registered_slots, active)
                  VALUES(?,?,?,?,?,?,1)
                """;
                try (PreparedStatement ps = c.prepareStatement(ins)) {
                    for (Project p : csv) {
                        ps.setString(1, p.getTitle());
                        ps.setString(2, p.getLocation());
                        ps.setString(3, p.getDay());
                        ps.setDouble(4, p.getHourlyValue());
                        ps.setInt(5, p.getTotalSlots());
                        ps.setInt(6, p.getRegisteredSlots());
                        try {
                            ps.executeUpdate();
                        } catch (SQLException ignoreDup) {
                            // projects(title,location,day) is UNIQUE — skip duplicates
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Convert checked IOException to SQLException so callers only handle SQL
            throw new SQLException("CSV import failed: " + e.getMessage(), e);
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
