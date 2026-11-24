package com.peertutor.controller;

import com.peertutor.exception.ValidationException;
import com.peertutor.model.Availability;
import com.peertutor.service.AvailabilityService;
import com.peertutor.util.AlertUtil;
import com.peertutor.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Availability management page for tutors.
 */
public class AvailabilityController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final AvailabilityService availService = new AvailabilityService();
    private final int userId = SessionManager.getCurrentUser().getUserId();

    private VBox root;
    private TableView<Availability> table;
    private DatePicker datePicker;
    private TextField startField, endField;

    public AvailabilityController() {
        buildUI();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setPadding(new Insets(32, 36, 32, 36));
        root.setStyle("-fx-background-color: #f5f0ff;");

        Label title = new Label("My Availability");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Add time slots when you are available to tutor. Students can book these slots.");
        sub.getStyleClass().add("subtitle-text");

        // ── Add Slot Form ─────────────────────────────────────────────────
        VBox formCard = new VBox(14);
        formCard.getStyleClass().add("card");
        formCard.setMaxWidth(560);

        Label formTitle = new Label("Add a New Availability Slot");
        formTitle.getStyleClass().add("section-title");

        Label dateLbl = new Label("Date");
        dateLbl.getStyleClass().add("field-label");
        datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setPrefWidth(200);

        HBox timeRow = new HBox(16);
        VBox startBox = new VBox(4);
        Label startLbl = new Label("Start Time (HH:mm)");
        startLbl.getStyleClass().add("field-label");
        startField = new TextField("09:00");
        startField.setPrefWidth(140);
        startBox.getChildren().addAll(startLbl, startField);

        VBox endBox = new VBox(4);
        Label endLbl = new Label("End Time (HH:mm)");
        endLbl.getStyleClass().add("field-label");
        endField = new TextField("10:00");
        endField.setPrefWidth(140);
        endBox.getChildren().addAll(endLbl, endField);
        timeRow.getChildren().addAll(startBox, endBox);

        Label hint = new Label("💡 Minimum slot duration is 30 minutes. Use 24-hour format (e.g. 14:30).");
        hint.getStyleClass().add("info-label");

        Button addBtn = new Button("Add Slot");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> handleAddSlot());

        formCard.getChildren().addAll(formTitle, dateLbl, datePicker, timeRow, hint, addBtn);

        // ── Slots Table ───────────────────────────────────────────────────
        Label tableTitle = new Label("My Availability Slots");
        tableTitle.getStyleClass().add("section-title");

        table = new TableView<>();
        table.setPlaceholder(new Label("No availability slots added yet."));
        table.setPrefHeight(340);

        TableColumn<Availability, String> dateCol  = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(140);

        TableColumn<Availability, String> startCol = new TableColumn<>("Start");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startCol.setPrefWidth(100);

        TableColumn<Availability, String> endCol = new TableColumn<>("End");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endCol.setPrefWidth(100);

        TableColumn<Availability, Boolean> bookedCol = new TableColumn<>("Status");
        bookedCol.setPrefWidth(100);
        bookedCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); setText(null); return;
                }
                Availability av = getTableView().getItems().get(getIndex());
                Label lbl = new Label(av.isBooked() ? "Booked" : "Available");
                lbl.getStyleClass().add(av.isBooked() ? "status-approved" : "status-pending");
                setGraphic(lbl);
                setText(null);
            }
        });

        TableColumn<Availability, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(110);
        actionCol.setCellFactory(col -> new TableCell<>() {
            final Button delBtn = new Button("Delete");
            {
                delBtn.getStyleClass().add("btn-danger");
                delBtn.setOnAction(e -> {
                    Availability av = getTableView().getItems().get(getIndex());
                    handleDeleteSlot(av);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Availability av = getTableView().getItems().get(getIndex());
                if (!av.isBooked()) setGraphic(delBtn); else setGraphic(null);
            }
        });

        table.getColumns().addAll(dateCol, startCol, endCol, bookedCol, actionCol);
        refreshTable();

        root.getChildren().addAll(title, sub, formCard, tableTitle, table);
    }

    private void handleAddSlot() {
        try {
            LocalDate date = datePicker.getValue();
            if (date == null) {
                AlertUtil.showWarning("Input Required", "Please select a date.");
                return;
            }
            LocalTime start = parseTime(startField.getText(), "Start time");
            LocalTime end   = parseTime(endField.getText(),   "End time");

            availService.addSlot(userId, date, start, end);
            AlertUtil.showInfo("Slot Added", "Availability slot added successfully.");
            refreshTable();
        } catch (ValidationException ex) {
            AlertUtil.showError("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
        }
    }

    private void handleDeleteSlot(Availability av) {
        if (!AlertUtil.showConfirm("Delete Slot", "Delete this availability slot?\n" + av.getSlotLabel()))
            return;
        try {
            availService.deleteSlot(av.getAvailabilityId());
            AlertUtil.showInfo("Slot Deleted", "Availability slot removed.");
            refreshTable();
        } catch (ValidationException ex) {
            AlertUtil.showError("Cannot Delete", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
        }
    }

    private LocalTime parseTime(String text, String field) throws ValidationException {
        try {
            return LocalTime.parse(text.trim(), TIME_FMT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field + " must be in HH:mm format (e.g. 09:30).");
        }
    }

    private void refreshTable() {
        List<Availability> slots = availService.getTutorSlots(userId);
        table.setItems(FXCollections.observableArrayList(slots));
    }

    public VBox getView() { return root; }
}
