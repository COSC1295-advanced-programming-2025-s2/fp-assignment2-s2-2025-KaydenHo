package util;

public final class PasswordPolicy {
    // ≥8 chars, at least one upper, one lower, one digit, one special
    private static final String REGEX =
        "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";
    public static boolean valid(String raw) {
        return raw != null && raw.matches(REGEX);
    }
}
