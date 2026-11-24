package com.peertutor.controller;

import com.peertutor.exception.ValidationException;
import com.peertutor.model.Availability;
import com.peertutor.model.Session;
import com.peertutor.service.AvailabilityService;
import com.peertutor.service.SessionService;
import com.peertutor.util.AlertUtil;
import com.peertutor.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Booking page — browse available tutors and request sessions.
 * Also contains a static helper used by FindTutors for the booking dialog.
 */
public class BookingController {

    private final SessionService     sessionService = new SessionService();
    private final AvailabilityService availService  = new AvailabilityService();

    private VBox root;
    private TableView<Session> pendingTable;

    public BookingController() {
        buildUI();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setPadding(new Insets(32, 36, 32, 36));
        root.setStyle("-fx-background-color: #f5f0ff;");

        int userId = SessionManager.getCurrentUser().getUserId();

        Label title = new Label("Book a Session");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Go to 'Find Tutors' to browse available slots and request a session.\n" +
                              "Your pending requests are listed below.");
        sub.getStyleClass().add("subtitle-text");

        // ── Pending requests table ────────────────────────────────────────
        Label pendTitle = new Label("My Pending Requests");
        pendTitle.getStyleClass().add("section-title");

        pendingTable = new TableView<>();
        pendingTable.setPlaceholder(new Label("No pending requests."));
        pendingTable.setPrefHeight(300);

        TableColumn<Session, String> tutorCol = new TableColumn<>("Tutor");
        tutorCol.setCellValueFactory(new PropertyValueFactory<>("tutorName"));
        tutorCol.setPrefWidth(160);

        TableColumn<Session, Void> dateCol = new TableColumn<>("Date & Time");
        dateCol.setPrefWidth(200);
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null); return;
                }
                setText(getTableView().getItems().get(getIndex()).getDateTimeLabel());
            }
        });

        TableColumn<Session, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null); setGraphic(null); return;
                }
                Session sess = getTableView().getItems().get(getIndex());
                Label lbl = new Label(sess.getStatus().name());
                lbl.getStyleClass().add(statusClass(sess.getStatus()));
                setGraphic(lbl); setText(null);
            }
        });

        pendingTable.getColumns().addAll(tutorCol, dateCol, statusCol);
        refreshPending(userId);

        root.getChildren().addAll(title, sub, pendTitle, pendingTable);
    }

    private void refreshPending(int userId) {
        List<Session> sessions = sessionService.getStudentSessionsByStatus(
                userId, Session.Status.PENDING);
        pendingTable.setItems(FXCollections.observableArrayList(sessions));
    }

    /** Opens a confirmation dialog and books the session. Called from FindTutorsController. */
    public static void openBookingDialog(int tutorId, String tutorName, int availabilityId) {
        SessionService svc = new SessionService();
        AvailabilityService availSvc = new AvailabilityService();

        Availability slot = availSvc.getAvailableSlots(tutorId).stream()
                .filter(a -> a.getAvailabilityId() == availabilityId)
                .findFirst().orElse(null);

        String slotLabel = slot != null ? slot.getSlotLabel() : "Selected slot";

        boolean confirmed = AlertUtil.showConfirm(
            "Confirm Booking Request",
            "Request a session with " + tutorName + "?\n\n" +
            "Slot: " + slotLabel + "\n\n" +
            "The tutor will need to approve your request.");
        if (!confirmed) return;

        try {
            int studentId = SessionManager.getCurrentUser().getUserId();
            svc.bookSession(studentId, tutorId, availabilityId);
            AlertUtil.showInfo("Request Sent",
                "Your session request has been sent to " + tutorName +
                ".\nYou can track its status in 'My Sessions'.");
        } catch (ValidationException ex) {
            AlertUtil.showError("Booking Failed", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
        }
    }

    private String statusClass(Session.Status s) {
        return switch (s) {
            case PENDING   -> "status-pending";
            case APPROVED  -> "status-approved";
            case COMPLETED -> "status-completed";
            case CANCELLED -> "status-cancelled";
            case REJECTED  -> "status-rejected";
        };
    }

    public VBox getView() { return root; }
}
