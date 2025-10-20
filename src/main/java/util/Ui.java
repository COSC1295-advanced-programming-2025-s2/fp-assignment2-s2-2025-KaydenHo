package util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public final class Ui {
    private Ui() {}

    public static void info(String title, String msg)  { show(AlertType.INFORMATION, title, msg); }
    public static void warn(String title, String msg)  { show(AlertType.WARNING,     title, msg); }
    public static void error(String title, String msg) { show(AlertType.ERROR,       title, msg); }

    private static void show(AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
