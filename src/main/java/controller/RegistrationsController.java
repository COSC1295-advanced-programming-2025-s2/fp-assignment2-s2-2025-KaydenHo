package controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Model;
import model.RegistrationDetail;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RegistrationsController {
    private final Stage stage = new Stage();
    private final Stage parent;
    private final Model model;

    @FXML private TableView<Row> tbl;
    @FXML private TableColumn<Row, String> colRegId, colWhen, colTitle, colLocation, colDay;
    @FXML private TableColumn<Row, Number> colSlots, colHours, colTotal;
    @FXML private Button btnExport, btnClose;
    @FXML private Label status;

    public RegistrationsController(Stage parent, Model model) {
        this.parent = parent;
        this.model = model;
    }

    @FXML
    public void initialize() {
        colRegId.setCellValueFactory(c -> c.getValue().regId);
        colWhen.setCellValueFactory(c -> c.getValue().when);
        colTitle.setCellValueFactory(c -> c.getValue().title);
        colLocation.setCellValueFactory(c -> c.getValue().location);
        colDay.setCellValueFactory(c -> c.getValue().day);
        colSlots.setCellValueFactory(c -> c.getValue().slots);
        colHours.setCellValueFactory(c -> c.getValue().hours);
        colTotal.setCellValueFactory(c -> c.getValue().total);

        // currency column
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty ? "" : String.format("$%.2f", n.doubleValue()));
            }
        });

        btnExport.setOnAction(e -> exportTxt());
        btnClose.setOnAction(e -> { stage.close(); if (parent != null) parent.show(); });

        refresh();
    }

    private void refresh() {
        try {
            List<RegistrationDetail> details =
                    model.getRegistrationDao().listDetailsByUser(model.getCurrentUser().getUsername());
            var rows = details.stream().map(Row::from).toList();
            tbl.setItems(FXCollections.observableArrayList(rows));
            status.setText("Total: " + rows.size());
        } catch (Exception e) {
            status.setText("Load failed: " + e.getMessage());
        }
    }

    private void exportTxt() {
        var rows = tbl.getItems();
        if (rows.isEmpty()) { status.setText("Nothing to export."); return; }

        String user = model.getCurrentUser().getUsername();
        var fc = new javafx.stage.FileChooser();
        fc.setTitle("Export Participation History");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fc.setInitialFileName("history_" + user + ".txt");
        var file = fc.showSaveDialog(stage);
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            out.println("Volunteer: " + user);
            out.println();
            for (var r : rows) {
                out.printf("Reg ID: %s%n", r.regId.get());
                out.printf("When:   %s%n", r.when.get());
                out.printf("Project: %s (%s) — %s%n", r.title.get(), r.location.get(), r.day.get());
                out.printf("Slots:   %d, Hours: %d%n", r.slots.get(), r.hours.get());
                out.printf("Total:   $%.2f%n", r.total.get());
                out.println("----------------------------------------");
            }
            status.setText("Exported to " + file.getName());
        } catch (Exception e) {
            util.Ui.error("Export failed", e.getMessage());
        }
    }

    public void showStage(Parent root) {
        stage.setScene(new Scene(root, 780, 460));
        stage.setResizable(false);
        stage.setTitle("My Participation History");
        stage.show();
        tbl.requestFocus();
    }

    // table row
    static class Row {
        final StringProperty regId = new SimpleStringProperty();   // 4-digit formatted
        final StringProperty when = new SimpleStringProperty();
        final StringProperty title = new SimpleStringProperty();
        final StringProperty location = new SimpleStringProperty();
        final StringProperty day = new SimpleStringProperty();
        final IntegerProperty slots = new SimpleIntegerProperty();
        final IntegerProperty hours = new SimpleIntegerProperty();
        final DoubleProperty total = new SimpleDoubleProperty();

        static Row from(RegistrationDetail d) {
            Row r = new Row();
            r.regId.set(String.format("%04d", d.regId()));
            r.when.set(d.dateTime().toString());
            r.title.set(d.title());
            r.location.set(d.location());
            r.day.set(d.day());
            r.slots.set(d.slots());
            r.hours.set(d.hours());
            r.total.set(d.totalValue());
            return r;
        }
    }
}
