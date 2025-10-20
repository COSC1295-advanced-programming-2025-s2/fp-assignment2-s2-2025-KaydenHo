package controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Model;
import model.Registration;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RegistrationsController {
    private final Stage stage = new Stage();
    private final Stage parent;
    private final Model model;

    @FXML private TableView<RegistrationRow> tbl;
    @FXML private TableColumn<RegistrationRow, Number> colRegId, colProject, colSlots, colHours, colTotal;
    @FXML private TableColumn<RegistrationRow, String> colWhen;
    @FXML private Button btnExport, btnClose;
    @FXML private Label status;

    public RegistrationsController(Stage parent, Model model) {
        this.parent = parent;
        this.model = model;
    }

    @FXML
    public void initialize() {
        colRegId.setCellValueFactory(c -> c.getValue().regId);
        colProject.setCellValueFactory(c -> c.getValue().projectId);
        colSlots.setCellValueFactory(c -> c.getValue().slots);
        colHours.setCellValueFactory(c -> c.getValue().hours);
        colWhen.setCellValueFactory(c -> c.getValue().when);
        colTotal.setCellValueFactory(c -> c.getValue().total);

        btnExport.setOnAction(e -> export());
        btnClose.setOnAction(e -> { stage.close(); if (parent != null) parent.show(); });

        refresh();
    }

    private void refresh() {
        try {
            List<Registration> regs = model.getRegistrationDao().listByUser(model.getCurrentUser().getUsername());
            var rows = regs.stream().map(RegistrationRow::from).toList();
            tbl.setItems(FXCollections.observableArrayList(rows));
            status.setText("Total: " + rows.size());
        } catch (Exception e) {
            status.setText("Load failed: " + e.getMessage());
        }
    }

    private void export() {
        var rows = tbl.getItems();
        if (rows.isEmpty()) { status.setText("Nothing to export."); return; }

        var fc = new javafx.stage.FileChooser();
        fc.setTitle("Export Participation CSV");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName("participation.csv");
        var file = fc.showSaveDialog(stage);
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            out.println("reg_id,project_id,slots,hours,date_time,total_value");
            for (var r : rows) {
                out.printf("%d,%d,%d,%d,%s,%.2f%n",
                    r.regId.get(), r.projectId.get(), r.slots.get(), r.hours.get(), r.when.get(), r.total.get());
            }
            status.setText("Exported to " + file.getName());
        } catch (Exception e) {
            status.setText("Export failed: " + e.getMessage());
        }
    }

    public void showStage(Parent root) {
        stage.setScene(new Scene(root, 700, 420));
        stage.setResizable(false);
        stage.setTitle("My Participation History");
        stage.show();
    }

    // Row model for table
    static class RegistrationRow {
        final LongProperty regId = new SimpleLongProperty();
        final IntegerProperty projectId = new SimpleIntegerProperty();
        final IntegerProperty slots = new SimpleIntegerProperty();
        final IntegerProperty hours = new SimpleIntegerProperty();
        final StringProperty when = new SimpleStringProperty();
        final DoubleProperty total = new SimpleDoubleProperty();

        static RegistrationRow from(Registration r) {
            var w = new RegistrationRow();
            w.regId.set(r.regId());
            w.projectId.set(r.projectId());
            w.slots.set(r.slots());
            w.hours.set(r.hours());
            w.when.set(r.dateTime().toString());
            w.total.set(r.totalValue());
            return w;
        }
    }
}
