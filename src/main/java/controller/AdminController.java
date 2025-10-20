package controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Model;
import dao.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminController {
    private final Stage stage = new Stage();
    private final Stage parent;
    private final Model model;

    @FXML private TableView<Row> tbl;
    @FXML private TableColumn<Row, Number> colId, colHourly, colTotal, colReg, colAvail;
    @FXML private TableColumn<Row, String> colTitle, colLocation, colDay, colActive;
    @FXML private Button btnAdd, btnToggle, btnRefresh, btnClose;
    @FXML private Label status;

    public AdminController(Stage parent, Model model) {
        this.parent = parent;
        this.model = model;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> c.getValue().id);
        colTitle.setCellValueFactory(c -> c.getValue().title);
        colLocation.setCellValueFactory(c -> c.getValue().location);
        colDay.setCellValueFactory(c -> c.getValue().day);
        colHourly.setCellValueFactory(c -> c.getValue().hourly);
        colTotal.setCellValueFactory(c -> c.getValue().total);
        colReg.setCellValueFactory(c -> c.getValue().registered);
        colAvail.setCellValueFactory(c -> c.getValue().available);
        colActive.setCellValueFactory(c -> c.getValue().active);

        colHourly.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                setText(empty ? "" : String.format("$%.2f", n.doubleValue()));
            }
        });

        btnAdd.setOnAction(e -> addUpsert());
        btnToggle.setOnAction(e -> toggle());
        btnRefresh.setOnAction(e -> refresh());
        btnClose.setOnAction(e -> { stage.close(); if (parent != null) parent.show(); });

        refresh();
    }

    private void refresh() {
        String sql = """
            SELECT id, title, location, day, hourly_value, total_slots, registered_slots, active
            FROM projects
            ORDER BY active DESC, title, location, day
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Row> rows = new ArrayList<>();
            while (rs.next()) {
                Row r = new Row();
                r.id.set(rs.getInt("id"));
                r.title.set(rs.getString("title"));
                r.location.set(rs.getString("location"));
                r.day.set(rs.getString("day"));
                r.hourly.set(rs.getDouble("hourly_value"));
                r.total.set(rs.getInt("total_slots"));
                r.registered.set(rs.getInt("registered_slots"));
                r.available.set(Math.max(0, r.total.get() - r.registered.get()));
                r.active.set(rs.getInt("active") == 1 ? "Yes" : "No");
                rows.add(r);
            }
            tbl.setItems(FXCollections.observableArrayList(rows));
            status.setText("Projects: " + rows.size());
        } catch (Exception e) {
            status.setText("Load failed: " + e.getMessage());
        }
    }

    private void addUpsert() {
        var d = new TextInputDialog("Title,Location,Day(Mon..Sun),Hourly,Total,Registered,Active(1/0)");
        d.setHeaderText("Enter: title,location,day,hourly,total,registered,active");
        d.showAndWait().ifPresent(s -> {
            try {
                var p = s.split(",", -1);
                String sql = """
                    INSERT INTO projects(title, location, day, hourly_value, total_slots, registered_slots, active)
                    VALUES(?,?,?,?,?,?,?)
                    ON CONFLICT(title, location, day)
                    DO UPDATE SET hourly_value=excluded.hourly_value,
                                  total_slots=excluded.total_slots,
                                  registered_slots=excluded.registered_slots,
                                  active=excluded.active
                """;
                try (Connection c = Database.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, p[0].trim());
                    ps.setString(2, p[1].trim());
                    ps.setString(3, p[2].trim());
                    ps.setDouble(4, Double.parseDouble(p[3].trim()));
                    ps.setInt(5, Integer.parseInt(p[4].trim()));
                    ps.setInt(6, Integer.parseInt(p[5].trim()));
                    ps.setInt(7, "1".equals(p[6].trim()) ? 1 : 0);
                    ps.executeUpdate();
                }
                refresh();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Upsert failed: " + ex.getMessage()).showAndWait();
            }
        });
    }

    private void toggle() {
        Row r = tbl.getSelectionModel().getSelectedItem();
        if (r == null) { status.setText("Select a project."); return; }
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE projects SET active=? WHERE id=?")) {
            boolean to = !"Yes".equals(r.active.get());
            ps.setInt(1, to ? 1 : 0);
            ps.setInt(2, r.id.get());
            ps.executeUpdate();
            refresh();
        } catch (Exception e) {
            status.setText("Toggle failed: " + e.getMessage());
        }
    }

    public void showStage(Parent root) {
        Scene scene = new Scene(root, 980, 480);
        scene.setOnKeyPressed(ev -> { if (ev.getCode() == javafx.scene.input.KeyCode.ESCAPE) stage.close(); });
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Admin — Projects");
        stage.show();
        tbl.requestFocus();
    }

    // Row wrapper
    static class Row {
        final IntegerProperty id = new SimpleIntegerProperty();
        final StringProperty title = new SimpleStringProperty();
        final StringProperty location = new SimpleStringProperty();
        final StringProperty day = new SimpleStringProperty();
        final DoubleProperty hourly = new SimpleDoubleProperty();
        final IntegerProperty total = new SimpleIntegerProperty();
        final IntegerProperty registered = new SimpleIntegerProperty();
        final IntegerProperty available = new SimpleIntegerProperty();
        final StringProperty active = new SimpleStringProperty();
    }
}
