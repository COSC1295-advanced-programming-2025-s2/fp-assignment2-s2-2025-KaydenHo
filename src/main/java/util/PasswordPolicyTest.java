package util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PasswordPolicyTest {
    @Test
    void acceptsStrongPassword() {
        assertTrue(PasswordPolicy.valid("Abcdef1!"));
    }

    @Test
    void rejectsTooShort() {
        assertFalse(PasswordPolicy.valid("Ab1!a"));
    }

    @Test
    void rejectsMissingDigit() {
        assertFalse(PasswordPolicy.valid("Abcdefg!"));
    }

    @Test
    void rejectsMissingUpper() {
        assertFalse(PasswordPolicy.valid("abcdef1!"));
    }

    @Test
    void rejectsMissingSpecial() {
        assertFalse(PasswordPolicy.valid("Abcdef12"));
    }
}
