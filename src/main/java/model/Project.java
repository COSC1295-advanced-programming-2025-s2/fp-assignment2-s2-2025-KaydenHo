package model;

public class Project {
    private final String title;
    private final String location;
    private final String day;
    private final double hourlyValue;
    private final int totalSlots;
    private final int registeredSlots;

    public Project(String title, String location, String day,
                   double hourlyValue, int totalSlots, int registeredSlots) {
        this.title = title;
        this.location = location;
        this.day = day;
        this.hourlyValue = hourlyValue;
        this.totalSlots = totalSlots;
        this.registeredSlots = registeredSlots;
    }

    public String getTitle() { 
    	return title; }
    
    public String getLocation() { 
    	return location; }
    
    public String getDay() { 
    	return day; }
    
    public double getHourlyValue() { 
    	return hourlyValue; }
    
    public int getTotalSlots() { 
    	return totalSlots; }
    
    public int getRegisteredSlots() { 
    	return registeredSlots; }
    
    public int getAvailableSlots() { 
    	return Math.max(0, totalSlots - registeredSlots); }
}
