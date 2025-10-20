package util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class WeekRuleTest {
    @Test
    void todayIsAllowed() {
        ZoneId zone = ZoneId.of("Australia/Melbourne");
        String today = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
                .format(LocalDate.now(zone));
        // Normal to "Mon","Tue",... 
        assertTrue(WeekRule.isAllowedThisWeek(today, zone));
    }

    @Test
    void invalidDayReturnsFalse() {
        assertFalse(WeekRule.isAllowedThisWeek("Funday", ZoneId.of("Australia/Melbourne")));
    }
}
