package controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;

import model.Model;
import model.Project;
import util.WeekRule;

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

    @FXML private Button btnAddToCart;
    @FXML private Button btnViewCart;
    @FXML private Label homeStatus;

    @FXML private MenuItem viewProfile;
    @FXML private MenuItem updateProfile;

    public HomeController(Stage parentStage, Model model) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
    }

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colLocation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));
        colDay.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDay()));
        colHourly.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getHourlyValue()));
        colAvail.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getAvailableSlots()));

        if (model.getCurrentUser() != null) {
            welcomeLabel.setText("Welcome, " + model.getCurrentUser().getUsername());
        }

        try {
            model.loadProjects();
            projectsTable.setItems(model.getProjects());
        } catch (Exception e) {
            welcomeLabel.setText("Error loading projects: " + e.getMessage());
        }

        // wire buttons
        if (btnAddToCart != null) btnAddToCart.setOnAction(e -> handleAddToCart());
        if (btnViewCart  != null) btnViewCart.setOnAction(e -> openCart());
    }

    public void showStage(Pane root) {
        Scene scene = new Scene(root, 640, 420);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Dashboard");
        stage.show();
    }

    public void refreshProjects() {
        try {
            model.loadProjects();
            projectsTable.setItems(model.getProjects());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAddToCart() {
        Project p = projectsTable.getSelectionModel().getSelectedItem();
        if (p == null) { setStatus("Select a project first."); return; }

        Integer slots = promptInt("Slots (1–3)", 1, 3);
        if (slots == null) { setStatus("Cancelled."); return; }

        Integer hours = promptInt("Hours per slot (1–3)", 1, 3);
        if (hours == null) { setStatus("Cancelled."); return; }

        if (slots > p.getAvailableSlots()) { setStatus("Not enough slots available."); return; }
        if (!WeekRule.isAllowedThisWeek(p.getDay(), java.time.ZoneId.of("Australia/Melbourne"))) {
            setStatus("You can’t register for a past day this week."); return;
        }

        try {
            model.getCartDao().upsertCartItem(
                model.getCurrentUser().getUsername(),
                p.getId(),   // now a real DB id
                slots,
                hours
            );
            setStatus("Added to cart.");
        } catch (Exception ex) {
            setStatus("Add failed: " + ex.getMessage());
        }
    }

    private void openCart() {
        try {
            var url = getClass().getResource("/view/CartView.fxml");
            var loader = new javafx.fxml.FXMLLoader(url);
            var cc = new CartController(stage, model);
            loader.setController(cc);

            Parent root = loader.load();   // <-- typed as Parent
            cc.showStage(root);            // <-- now matches the method

        } catch (Exception e) {
            setStatus("Open cart failed: " + e.getMessage());
        }
    }

    private Integer promptInt(String title, int min, int max) {
        TextInputDialog td = new TextInputDialog();
        td.setTitle(title);
        td.setHeaderText(null);
        td.setContentText(title + ": ");
        return td.showAndWait().map(s -> {
            try { int v = Integer.parseInt(s); return (v >= min && v <= max) ? v : null; }
            catch (Exception e) { return null; }
        }).orElse(null);
    }

    private void setStatus(String msg) { if (homeStatus != null) homeStatus.setText(msg); }
}
