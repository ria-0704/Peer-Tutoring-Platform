package com.peertutor.controller;

import com.peertutor.exception.ValidationException;
import com.peertutor.model.Session;
import com.peertutor.model.Session.Status;
import com.peertutor.service.SessionService;
import com.peertutor.util.AlertUtil;
import com.peertutor.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

/**
 * My Sessions page — shows all sessions for the logged-in user, split by role.
 */
public class MySessionsController {

    private final SessionService sessionService = new SessionService();
    private final int userId = SessionManager.getCurrentUser().getUserId();

    private VBox root;

    // As student
    private TableView<Session> studentTable;
    // As tutor (incoming requests)
    private TableView<Session> tutorTable;

    public MySessionsController() {
        buildUI();
    }

    private void buildUI() {
        root = new VBox(24);
        root.setPadding(new Insets(32, 36, 32, 36));
        root.setStyle("-fx-background-color: #f5f0ff;");

        Label title = new Label("My Sessions");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Manage your sessions — as a student and as a tutor.");
        sub.getStyleClass().add("subtitle-text");

        // ── As Student ────────────────────────────────────────────────────
        Label studentTitle = new Label("Sessions I've Booked  (as Student)");
        studentTitle.getStyleClass().add("section-title");

        studentTable = buildTable(false);
        refreshStudentTable();

        // ── As Tutor ──────────────────────────────────────────────────────
        Label tutorTitle = new Label("Sessions I'm Teaching  (as Tutor)");
        tutorTitle.getStyleClass().add("section-title");

        tutorTable = buildTable(true);
        refreshTutorTable();

        root.getChildren().addAll(title, sub,
            studentTitle, studentTable,
            tutorTitle,   tutorTable);
    }

    private TableView<Session> buildTable(boolean isTutorView) {
        TableView<Session> table = new TableView<>();
        table.setPlaceholder(new Label("No sessions found."));
        table.setPrefHeight(260);

        TableColumn<Session, String> otherCol = new TableColumn<>(isTutorView ? "Student" : "Tutor");
        otherCol.setPrefWidth(150);
        otherCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                Session s = getTableView().getItems().get(getIndex());
                setText(isTutorView ? s.getStudentName() : s.getTutorName());
            }
        });

        TableColumn<Session, Void> dateCol = new TableColumn<>("Date & Time");
        dateCol.setPrefWidth(190);
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                setText(getTableView().getItems().get(getIndex()).getDateTimeLabel());
            }
        });

        TableColumn<Session, Void> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(110);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null); setGraphic(null); return;
                }
                Session sess = getTableView().getItems().get(getIndex());
                Label lbl = new Label(sess.getStatus().name());
                lbl.getStyleClass().add(statusStyleClass(sess.getStatus()));
                setGraphic(lbl); setText(null);
            }
        });

        TableColumn<Session, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(260);
        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); return;
                }
                Session sess = getTableView().getItems().get(getIndex());
                HBox box = new HBox(6);
                buildActionButtons(box, sess, isTutorView, table);
                setGraphic(box.getChildren().isEmpty() ? null : box);
            }
        });

        table.getColumns().addAll(otherCol, dateCol, statusCol, actionCol);
        return table;
    }

    private void buildActionButtons(HBox box, Session sess, boolean isTutorView,
                                    TableView<Session> table) {
        Status status = sess.getStatus();

        if (isTutorView) {
            // Tutor actions
            if (status == Status.PENDING) {
                Button approve = new Button("Approve");
                approve.getStyleClass().add("btn-success");
                approve.setOnAction(e -> {
                    try {
                        sessionService.approveSession(sess.getSessionId(), userId,
                            sessionService.getTutorSessions(userId));
                        AlertUtil.showInfo("Approved", "Session approved.");
                        refreshTutorTable();
                    } catch (ValidationException ex) {
                        AlertUtil.showError("Error", ex.getMessage());
                    }
                });

                Button reject = new Button("Reject");
                reject.getStyleClass().add("btn-danger");
                reject.setOnAction(e -> {
                    if (!AlertUtil.showConfirm("Reject", "Reject this session request?")) return;
                    try {
                        sessionService.rejectSession(sess.getSessionId(), userId,
                            sessionService.getTutorSessions(userId));
                        AlertUtil.showInfo("Rejected", "Session request rejected.");
                        refreshTutorTable();
                    } catch (ValidationException ex) {
                        AlertUtil.showError("Error", ex.getMessage());
                    }
                });
                box.getChildren().addAll(approve, reject);
            }
            if (status == Status.APPROVED) {
                Button complete = new Button("Mark Complete");
                complete.getStyleClass().add("btn-info");
                complete.setOnAction(e -> {
                    if (!AlertUtil.showConfirm("Complete Session",
                        "Mark this session as completed?")) return;
                    try {
                        sessionService.completeSession(sess.getSessionId(), userId,
                            sessionService.getTutorSessions(userId));
                        AlertUtil.showInfo("Completed", "Session marked as completed.");
                        refreshTutorTable();
                    } catch (ValidationException ex) {
                        AlertUtil.showError("Error", ex.getMessage());
                    }
                });

                Button cancel = new Button("Cancel");
                cancel.getStyleClass().add("btn-warning");
                cancel.setOnAction(e -> {
                    if (!AlertUtil.showConfirm("Cancel Session",
                        "Cancel this approved session?")) return;
                    try {
                        sessionService.cancelSession(sess.getSessionId(), userId,
                            sessionService.getTutorSessions(userId));
                        AlertUtil.showInfo("Cancelled", "Session cancelled.");
                        refreshTutorTable();
                    } catch (ValidationException ex) {
                        AlertUtil.showError("Error", ex.getMessage());
                    }
                });
                box.getChildren().addAll(complete, cancel);
            }
        } else {
            // Student actions
            if (status == Status.APPROVED) {
                Button cancel = new Button("Cancel");
                cancel.getStyleClass().add("btn-warning");
                cancel.setOnAction(e -> {
                    if (!AlertUtil.showConfirm("Cancel Session",
                        "Cancel your session with " + sess.getTutorName() + "?")) return;
                    try {
                        sessionService.cancelSession(sess.getSessionId(), userId,
                            sessionService.getStudentSessions(userId));
                        AlertUtil.showInfo("Cancelled", "Session cancelled.");
                        refreshStudentTable();
                    } catch (ValidationException ex) {
                        AlertUtil.showError("Error", ex.getMessage());
                    }
                });
                box.getChildren().add(cancel);
            }
            if (status == Status.COMPLETED) {
                // Check if review already exists before showing button
                Button reviewBtn = new Button("Leave Review");
                reviewBtn.getStyleClass().add("btn-secondary");
                reviewBtn.setOnAction(e -> {
                    ReviewController.openReviewDialog(sess.getSessionId(),
                        sess.getTutorName(), () -> refreshStudentTable());
                });
                box.getChildren().add(reviewBtn);
            }
        }
    }

    private void refreshStudentTable() {
        List<Session> sessions = sessionService.getStudentSessions(userId);
        studentTable.setItems(FXCollections.observableArrayList(sessions));
    }

    private void refreshTutorTable() {
        List<Session> sessions = sessionService.getTutorSessions(userId);
        tutorTable.setItems(FXCollections.observableArrayList(sessions));
    }

    private String statusStyleClass(Status s) {
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
