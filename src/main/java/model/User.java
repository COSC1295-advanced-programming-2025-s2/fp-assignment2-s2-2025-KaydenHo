package model;

public class User {
    private String username;
    private String fullName;
    private String email;
    // Store only the hash for security
    private String passwordHash;

    public User() {}

    public User(String username, String fullName, String email, String passwordHash) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getUsername() { 
    	return username; }
    
    public String getFullName() { 
    	return fullName; }
    
    public String getEmail() { 
    	return email; }
    public String getPasswordHash() { 
    	return passwordHash; }

    public void setUsername(String username) { 
    	this.username = username; }
    
    public void setFullName(String fullName) { 
    	this.fullName = fullName; }
    
    public void setEmail(String email) { 
    	this.email = email; }
    
    public void setPasswordHash(String passwordHash) { 
    	this.passwordHash = passwordHash; }
}
