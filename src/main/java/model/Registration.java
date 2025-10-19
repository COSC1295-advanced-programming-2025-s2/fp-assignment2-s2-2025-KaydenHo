package model;

import java.time.LocalDateTime;

public record Registration(long regId, String username, int projectId, int slots,
                           int hours, LocalDateTime dateTime, double totalValue) { }
