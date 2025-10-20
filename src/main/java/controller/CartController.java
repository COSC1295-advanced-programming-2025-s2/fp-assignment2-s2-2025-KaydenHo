package controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Parent;
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
            status.setText("Load failed: " + e.getMessage());
        }
    }

    private void removeSelected() {
        var it = cartTable.getSelectionModel().getSelectedItem();
        if (it == null) { status.setText("Select an item to remove."); return; }
        try {
            model.getCartDao().removeCartItem(
                model.getCurrentUser().getUsername(), it.projectId()
            );
            refresh();
        } catch (Exception e) {
            status.setText("Remove failed: " + e.getMessage());
        }
    }

    private void confirm() {
        List<CartItem> items = cartTable.getItems();
        if (items.isEmpty()) { status.setText("Cart is empty."); return; }

        // 6-digit code
        TextInputDialog codeDlg = new TextInputDialog();
        codeDlg.setTitle("Confirmation");
        codeDlg.setHeaderText(null);
        codeDlg.setContentText("Enter 6-digit code:");
        var r = codeDlg.showAndWait();
        if (r.isEmpty()) return;
        String code = r.get();
        if (!ConfirmValidator.isSixDigit(code)) {
            status.setText("Invalid code. Use 6 digits.");
            return;
        }

        // day-rule check (no past day this week)
        ZoneId zone = ZoneId.of("Australia/Melbourne");
        for (CartItem it : items) {
            if (!WeekRule.isAllowedThisWeek(it.day(), zone)) {
                status.setText("Past-day item found: " + it.day());
                return;
            }
        }

        try {
            model.getRegistrationDao().confirm(
                model.getCurrentUser().getUsername(), items
            );
            status.setText("Registration confirmed!");
            refresh();
        } catch (Exception e) {
            status.setText("Confirm failed: " + e.getMessage());
        }
    }

    public void showStage(Parent root) {
        stage.setScene(new Scene(root, 680, 420));
        stage.setResizable(false);
        stage.setTitle("Your Cart");
        stage.show();
    }
}
