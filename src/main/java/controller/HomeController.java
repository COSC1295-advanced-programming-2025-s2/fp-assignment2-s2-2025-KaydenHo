package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;

import model.Model;
import model.Project;

public class HomeController {
    private final Model model;
    private final Stage stage;
    private final Stage parentStage;

    @FXML private Label welcomeLabel;
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> colTitle;
    @FXML private TableColumn<Project, String> colLocation;
    @FXML private TableColumn<Project, String> colDay;
    @FXML private TableColumn<Project, Number> colHourly;
    @FXML private TableColumn<Project, Number> colAvail;

    @FXML private MenuItem viewProfile;   
    @FXML private MenuItem updateProfile; // (not required for the demo)

    public HomeController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        // Bind columns
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colLocation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));
        colDay.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDay()));
        colHourly.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getHourlyValue()));
        colAvail.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getAvailableSlots()));

        // Welcome banner
        if (model.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + model.getCurrentUser().getUsername());
        }

        try {
            model.loadProjects();
            projectsTable.setItems(model.getProjects());
        } catch (Exception e) {
            welcomeLabel.setText("Error loading projects: " + e.getMessage());
        }
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 640, 420);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Dashboard");
        stage.show();
    }
}
