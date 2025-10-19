package controller;

import java.io.IOException;
import java.sql.SQLException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Model;
import model.User;

public class LoginController {
    @FXML private TextField name;
    @FXML private PasswordField password;
    @FXML private Label message;
    @FXML private Button login;
    @FXML private Button signup;

    private final Model model;
    private final Stage stage;

    public LoginController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        login.setOnAction(event -> {
            final String u = name.getText().trim();
            final String p = password.getText(); // don't trim passwords

            if (u.isEmpty() || p.isEmpty()) {
                showError("Empty username or password");
                return;
            }

            try {
                User user = model.getUserDao().getUserByCredentials(u, p);
                if (user == null) {
                    showError("Wrong username or password");
                    return;
                }

                model.setCurrentUser(user);

                // ---- Load HomeView.fxml safely ----
                URL url = getClass().getResource("/view/HomeView.fxml");
                System.out.println("FXML URL (Home): " + url);
                if (url == null) {
                    showError("HomeView.fxml not found at /view/HomeView.fxml");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(url);
                HomeController homeController = new HomeController(stage, model);
                loader.setController(homeController);
                VBox root = loader.load();                  
                homeController.showStage(root);
                stage.close();
           

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Database error: " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                showError("Failed to load Home view (see console)");
            }
        });

        signup.setOnAction(event -> {
            try {
                URL url = getClass().getResource("/view/SignupView.fxml");
                System.out.println("FXML URL (Signup): " + url);
                if (url == null) {
                    showError("SignupView.fxml not found at /view/SignupView.fxml");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(url);
                SignupController signupController = new SignupController(stage, model);
                loader.setController(signupController);
                VBox root = loader.load();                   
                signupController.showStage(root);

                message.setText(""); 
                stage.close();

            } catch (IOException e) {
                e.printStackTrace();
                showError("Failed to load Signup view (see console)");
            }
        });
    }

    private void showError(String text) {
        message.setText(text);
        message.setTextFill(Color.RED);
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Welcome");
        stage.show();
    }
}
