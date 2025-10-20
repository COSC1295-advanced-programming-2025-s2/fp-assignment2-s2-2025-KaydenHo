package util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ConfirmValidatorTest {
    @Test
    void sixDigitsOk() {
        assertTrue(ConfirmValidator.isSixDigit("123456"));
    }

    @Test
    void lettersNotOk() {
        assertFalse(ConfirmValidator.isSixDigit("12a456"));
    }

    @Test
    void wrongLengthNotOk() {
        assertFalse(ConfirmValidator.isSixDigit("12345"));
        assertFalse(ConfirmValidator.isSixDigit("1234567"));
    }
}
