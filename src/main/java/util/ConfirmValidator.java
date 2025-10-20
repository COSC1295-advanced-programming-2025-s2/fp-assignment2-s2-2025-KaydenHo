package util;

public final class ConfirmValidator {
    private ConfirmValidator(){}
    public static boolean isSixDigit(String s) {
        return s != null && s.matches("^\\d{6}$");
    }
}
