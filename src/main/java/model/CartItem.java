package model;

public record CartItem(int projectId, String title, String location, String day,
                       double hourlyValue, int slots, int hours) { }