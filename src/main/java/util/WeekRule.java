package util;

import java.time.*;

public final class WeekRule {
    private WeekRule(){}

    // dayStr is "Mon".."Sun"
    public static boolean isAllowedThisWeek(String dayStr, ZoneId zone) {
        DayOfWeek today = LocalDate.now(zone).getDayOfWeek(); // MON..SUN
        DayOfWeek target = parse(dayStr);
        if (target == null) return false;

        // Start Monday, allow today or later this week
        return target.getValue() >= today.getValue();
    }

    private static DayOfWeek parse(String d) {
        return switch (d) {
            case "Mon" -> DayOfWeek.MONDAY;
            case "Tue" -> DayOfWeek.TUESDAY;
            case "Wed" -> DayOfWeek.WEDNESDAY;
            case "Thu" -> DayOfWeek.THURSDAY;
            case "Fri" -> DayOfWeek.FRIDAY;
            case "Sat" -> DayOfWeek.SATURDAY;
            case "Sun" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }
} 
