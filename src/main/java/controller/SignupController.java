package controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Model;
import model.User;
import util.PasswordPolicy;

public class SignupController {
    @FXML private TextField fullName;
    @FXML private TextField username;
    @FXML private TextField email;
    @FXML private TextField password;
    @FXML private Button createUser;
    @FXML private Button close;
    @FXML private Label status;

    private Stage stage;
    private Stage parentStage;
    private Model model;

    public SignupController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        createUser.setOnAction(event -> {
            String fn = fullName.getText();
            String u  = username.getText();
            String em = email.getText();
            String pw = password.getText();

            if (fn.isEmpty() || u.isEmpty() || em.isEmpty() || pw.isEmpty()) {
                setError("All fields are required");
                return;
            }
            if (!PasswordPolicy.valid(pw)) {
                setError("Password ≥8 with upper, lower, digit, special");
                return;
            }

            try {
                if (model.getUserDao().usernameExists(u)) {
                    setError("Username already exists");
                    return;
                }
                User user = model.getUserDao().createUser(fn, u, em, pw);
                if (user != null) {
                    status.setText("Created " + user.getUsername());
                    status.setTextFill(Color.GREEN);
                    // (Optional) clear fields
                    // fullName.clear(); username.clear(); email.clear(); password.clear();
                } else {
                    setError("Cannot create user");
                }
            } catch (SQLException e) {
                setError(e.getMessage());
            }
        });

        close.setOnAction(event -> {
            stage.close();
            parentStage.show();
        });
    }

    private void setError(String msg) {
        status.setText(msg);
        status.setTextFill(Color.RED);
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Sign up");
        stage.show();
    }
}
