package dao;

import java.sql.SQLException;

import model.User;

/**
 * A data access object (DAO) is a pattern that provides an abstract interface 
 * to a database or other persistence mechanism. 
 * the DAO maps application calls to the persistence layer and provides some specific data operations 
 * without exposing details of the database. 
 */

public interface UserDao {
    void setup() throws SQLException;

    // Login: returns user if username + raw password match
    User getUserByCredentials(String username, String rawPassword) throws SQLException;

    // Signup: create a user with full name, username, email, raw password
    User createUser(String fullName, String username, String email, String rawPassword) throws SQLException;

    boolean usernameExists(String username) throws SQLException;
}
