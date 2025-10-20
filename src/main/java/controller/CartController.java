package controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import model.CartItem;
import model.Model;
import util.ConfirmValidator;
import util.WeekRule;

import java.time.ZoneId;
import java.util.List;

public class CartController {
    private final Stage stage = new Stage();
    private final Stage parent;
    private final Model model;

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colTitle, colDay;
    @FXML private TableColumn<CartItem, Number> colSlots, colHours, colHourly, colTotal;
    @FXML private Button btnRemove, btnConfirm;
    @FXML private Label status;

    public CartController(Stage parent, Model model) {
        this.parent = parent;
        this.model = model;
    }

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().title()));
        colDay.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().day()));
        colSlots.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().slots()));
        colHours.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().hours()));
        colHourly.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().hourlyValue()));
        colTotal.setCellValueFactory(c ->
            new SimpleDoubleProperty(
                c.getValue().hourlyValue() * c.getValue().hours() * c.getValue().slots()
            )
        );

        // pretty currency formatting
        colHourly.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty ? "" : String.format("$%.2f", n.doubleValue()));
            }
        });
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty ? "" : String.format("$%.2f", n.doubleValue()));
            }
        });

        btnRemove.setOnAction(e -> removeSelected());
        btnConfirm.setOnAction(e -> confirm());

        refresh();
    }

    private void refresh() {
        try {
            var items = model.getCartDao().listCart(model.getCurrentUser().getUsername());
            cartTable.setItems(FXCollections.observableArrayList(items));
            status.setText("Items: " + items.size());
        } catch (Exception e) {
            util.Ui.error("Load Cart Failed", e.getMessage());
            status.setText("Load failed");
        }
    }

    private void removeSelected() {
        var it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) { status.setText("Select an item to remove."); return; }
        try {
            model.getCartDao().removeCartItem(model.getCurrentUser().getUsername(), it.projectId());
            refresh();
        } catch (Exception e) {
            util.Ui.error("Remove Failed", e.getMessage());
        }
    }
    
    public void showStage(Parent root) {
        Scene scene = new Scene(root, 680, 420);

        // ESC closes this window
        scene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) stage.close();
        });

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Your Cart");
        stage.show();

        cartTable.requestFocus();
    }

    private void confirm() {
        List<model.CartItem> items = cartTable.getItems();
        if (items.isEmpty()) { status.setText("Cart is empty."); return; }

        TextInputDialog codeDlg = new TextInputDialog();
        codeDlg.setTitle("Confirmation");
        codeDlg.setHeaderText(null);
        codeDlg.setContentText("Enter 6-digit code:");
        var r = codeDlg.showAndWait();
        if (r.isEmpty()) return;
        String code = r.get();
        if (!util.ConfirmValidator.isSixDigit(code)) {
            util.Ui.warn("Invalid Code", "Please enter a 6-digit number.");
            return;
        }

        java.time.ZoneId zone = java.time.ZoneId.of("Australia/Melbourne");
        for (model.CartItem it : items) {
            if (!util.WeekRule.isAllowedThisWeek(it.day(), zone)) {
                util.Ui.warn("Invalid Day", "Past-day item found: " + it.day());
                return;
            }
        }

        try {
            model.getRegistrationDao().confirm(model.getCurrentUser().getUsername(), items);
            util.Ui.info("Success", "Registration confirmed!");
            refresh();
        } catch (Exception e) {
            util.Ui.error("Confirm Failed", e.getMessage());
        }
        
    }
}
