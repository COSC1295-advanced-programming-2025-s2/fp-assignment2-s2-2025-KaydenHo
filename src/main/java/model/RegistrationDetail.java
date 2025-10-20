package model;

import java.time.LocalDateTime;

// what the UI needs, already sorted DESC by DAO
public record RegistrationDetail(
        long regId,
        LocalDateTime dateTime,
        String title,
        String location,
        String day,
        int slots,
        int hours,
        double totalValue
) {}
