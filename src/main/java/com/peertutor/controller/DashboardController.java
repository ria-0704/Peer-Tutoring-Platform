package com.peertutor.controller;

import com.peertutor.model.Session;
import com.peertutor.service.SessionService;
import com.peertutor.service.TutorService;
import com.peertutor.service.UserService;
import com.peertutor.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard page — welcome message, stats, upcoming sessions, clickable quick actions.
 */
public class DashboardController {

    private final UserService    userService    = new UserService();
    private final TutorService   tutorService   = new TutorService();
    private final SessionService sessionService = new SessionService();

    private VBox root;

    public DashboardController() {
        buildUI();
    }

    private void buildUI() {
        root = new VBox(24);
        root.setPadding(new Insets(32, 36, 32, 36));
        root.setStyle("-fx-background-color: #f5f0ff;");

        int    userId = SessionManager.getCurrentUser().getUserId();
        String name   = SessionManager.getCurrentUser().getName();

        // ── Header ────────────────────────────────────────────────────────
        Label welcome = new Label("Welcome back, " + name + " 👋");
        welcome.getStyleClass().add("page-title");
        Label sub = new Label("Here's what's happening on your Peer Tutoring platform.");
        sub.getStyleClass().add("subtitle-text");

        // ── Stats row ─────────────────────────────────────────────────────
        int tutorCount    = userService.countTutors();
        int subjectCount  = tutorService.countSubjects();
        int upcomingCount = sessionService.countUpcoming(userId);

        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
            statCard("👨‍🏫", String.valueOf(tutorCount),   "Available Tutors"),
            statCard("📚",  String.valueOf(subjectCount),  "Subjects Offered"),
            statCard("📅",  String.valueOf(upcomingCount), "Upcoming Sessions")
        );

        // ── Upcoming sessions ─────────────────────────────────────────────
        Label sessTitle = new Label("Your Upcoming Sessions");
        sessTitle.getStyleClass().add("section-title");

        VBox sessBox = new VBox(10);
        List<Session> upcoming = sessionService.getStudentSessionsByStatus(
                userId, Session.Status.APPROVED);

        if (upcoming.isEmpty()) {
            Label none = new Label("No upcoming sessions. Browse tutors to book one!");
            none.getStyleClass().add("info-label");
            sessBox.getChildren().add(none);
        } else {
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            for (Session s : upcoming) {
                HBox card = new HBox(16);
                card.getStyleClass().addAll("card", "card-hover");
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(14, 20, 14, 20));

                Label icon = new Label("🗓️");
                icon.setStyle("-fx-font-size:22px;");

                VBox info = new VBox(2);
                Label tutorLbl = new Label("Tutor: " + s.getTutorName());
                tutorLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #3d2b7a;");
                String dateTime =
                    (s.getSessionDate() != null ? s.getSessionDate().format(dateFmt) : "") +
                    "  " +
                    (s.getStartTime() != null ? s.getStartTime().format(timeFmt) : "") +
                    " – " +
                    (s.getEndTime()   != null ? s.getEndTime().format(timeFmt)   : "");
                Label dtLbl = new Label(dateTime);
                dtLbl.getStyleClass().add("subtitle-text");
                info.getChildren().addAll(tutorLbl, dtLbl);

                Label statusLbl = new Label("APPROVED");
                statusLbl.getStyleClass().add("status-approved");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                card.getChildren().addAll(icon, info, spacer, statusLbl);
                sessBox.getChildren().add(card);
            }
        }

        // ── Quick actions ──────────────────────────────────────────────────
        Label actTitle = new Label("Quick Actions");
        actTitle.getStyleClass().add("section-title");

        HBox actRow = new HBox(16);
        actRow.getChildren().addAll(
            actionCard("🔍", "Find a Tutor",
                "Browse available tutors and subjects",
                MainLayoutController.Page.FIND_TUTORS),
            actionCard("🎓", "Be a Tutor",
                "Create or update your tutor profile",
                MainLayoutController.Page.TUTOR_PROFILE),
            actionCard("📋", "My Sessions",
                "View all your session bookings",
                MainLayoutController.Page.MY_SESSIONS)
        );

        root.getChildren().addAll(welcome, sub, statsRow, sessTitle, sessBox, actTitle, actRow);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private VBox statCard(String icon, String value, String label) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl  = new Label(icon);
        iconLbl.setStyle("-fx-font-size:22px;");
        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("stat-number");
        Label labelLbl = new Label(label);
        labelLbl.getStyleClass().add("stat-label");
        card.getChildren().addAll(iconLbl, valueLbl, labelLbl);
        return card;
    }

    /**
     * Builds a clickable quick-action card.
     * The entire card area responds to click and shows hand cursor + hover effect.
     */
    private VBox actionCard(String icon, String title, String desc,
                            MainLayoutController.Page destination) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("card", "action-card");
        card.setPrefWidth(220);
        card.setAlignment(Pos.TOP_LEFT);
        card.setCursor(Cursor.HAND);

        Label iconLbl  = new Label(icon);
        iconLbl.setStyle("-fx-font-size:28px;");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("action-card-title");

        Label descLbl = new Label(desc);
        descLbl.getStyleClass().add("subtitle-text");
        descLbl.setWrapText(true);

        card.getChildren().addAll(iconLbl, titleLbl, descLbl);

        // Navigate to the target page when the card is clicked anywhere
        card.setOnMouseClicked(e -> NavigationManager.navigateTo(destination));

        return card;
    }

    public VBox getView() { return root; }
}
