package model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import dao.ProjectDao;
import dao.ProjectDaoCsv;   
import dao.UserDao;
import dao.UserDaoImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    private UserDao userDao;
    private User currentUser;

    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final ProjectDao projectDao = new ProjectDaoCsv("/data/projects.csv"); 

    public Model() {
        userDao = new UserDaoImpl();
    }

    public void setup() throws SQLException {
        System.out.println("SQLite file: " + dao.Database.dbFilePath());
        userDao.setup();
        dao.Schema.setupAll();
    }

    public void loadProjects() throws IOException {
        List<Project> all = projectDao.loadAll();
        projects.setAll(all);
    }

    public UserDao getUserDao() { 
    	return userDao; }
    
    public User getCurrentUser() { 
    	return this.currentUser; }
    
    public void setCurrentUser(User user) { 
    	currentUser = user; }
    
    public ObservableList<Project> getProjects() { 
    	return projects; }
    
    
}
