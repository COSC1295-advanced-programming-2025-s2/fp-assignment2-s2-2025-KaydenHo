package controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Model;
import util.PasswordPolicy;
import util.PasswordHasher;

public class UpdatePasswordController {
    private final Stage stage = new Stage();
    private final Stage parent;
    private final Model model;

    @FXML private PasswordField curPw, newPw, newPw2;
    @FXML private Button btnSave, btnClose;
    @FXML private Label status;

    public UpdatePasswordController(Stage parent, Model model) {
        this.parent = parent;
        this.model = model;
    }

    @FXML
    public void initialize() {
        btnSave.setOnAction(e -> save());
        btnClose.setOnAction(e -> { stage.close(); if (parent != null) parent.show(); });
    }

    private void save() {
        String c = curPw.getText();
        String n1 = newPw.getText();
        String n2 = newPw2.getText();
        if (c.isEmpty() || n1.isEmpty() || n2.isEmpty()) { status.setText("Fill all fields."); return; }
        if (!n1.equals(n2)) { status.setText("New passwords do not match."); return; }
        if (!PasswordPolicy.valid(n1)) { status.setText("Password ≥8, upper, lower, digit, special."); return; }

        var user = model.getCurrentUser();
        try {
            // verify current password
            var found = model.getUserDao().getUserByCredentials(user.getUsername(), c);
            if (found == null) { status.setText("Current password incorrect."); return; }

            boolean ok = model.getUserDao().updatePassword(user.getUsername(), n1);
            status.setText(ok ? "Password updated." : "Update failed.");
        } catch (Exception ex) {
            status.setText("Error: " + ex.getMessage());
        }
    }


    public void showStage(Parent root) {
        Scene scene = new Scene(root, 400, 220);
        scene.setOnKeyPressed(ev -> { if (ev.getCode() == javafx.scene.input.KeyCode.ESCAPE) stage.close(); });
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Update Password");
        stage.show();
        curPw.requestFocus();
    }
}
