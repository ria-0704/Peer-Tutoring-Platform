package com.peertutor.controller;

import com.peertutor.model.Availability;
import com.peertutor.model.Skill;
import com.peertutor.service.AvailabilityService;
import com.peertutor.service.ReviewService;
import com.peertutor.service.TutorService;
import com.peertutor.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

/**
 * Find Tutors page — search, browse tutor profiles, see their availability.
 */
public class FindTutorsController {

    private final TutorService       tutorService   = new TutorService();
    private final AvailabilityService availService  = new AvailabilityService();
    private final ReviewService      reviewService  = new ReviewService();

    private VBox root;
    private TextField searchField;
    private VBox tutorListPane;

    public FindTutorsController() {
        buildUI();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setPadding(new Insets(32, 36, 32, 36));
        root.setStyle("-fx-background-color: #f5f0ff;");

        Label title = new Label("Find a Tutor");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Search by tutor name or subject. Click a profile to view their availability.");
        sub.getStyleClass().add("subtitle-text");

        // ── Search bar ────────────────────────────────────────────────────
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("Search by tutor name or subject...");
        searchField.setPrefWidth(360);
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("btn-primary");
        Button clearBtn = new Button("Show All");
        clearBtn.getStyleClass().add("btn-secondary");
        searchBar.getChildren().addAll(searchField, searchBtn, clearBtn);

        // ── Results pane ──────────────────────────────────────────────────
        tutorListPane = new VBox(12);

        ScrollPane scroll = new ScrollPane(tutorListPane);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        searchBtn.setOnAction(e -> loadTutors(searchField.getText()));
        clearBtn.setOnAction(e -> { searchField.clear(); loadTutors(""); });
        searchField.setOnAction(e -> loadTutors(searchField.getText()));

        root.getChildren().addAll(title, sub, searchBar, scroll);

        // Load all tutors initially
        loadTutors("");
    }

    private void loadTutors(String query) {
        tutorListPane.getChildren().clear();
        List<Skill> skills = tutorService.searchTutors(query);

        if (skills.isEmpty()) {
            Label none = new Label("No tutors found matching your search.");
            none.getStyleClass().add("info-label");
            tutorListPane.getChildren().add(none);
            return;
        }

        // Group skills by tutor user ID
        Map<Integer, List<Skill>> byTutor = new LinkedHashMap<>();
        for (Skill sk : skills) {
            byTutor.computeIfAbsent(sk.getUserId(), k -> new ArrayList<>()).add(sk);
        }

        int currentUserId = SessionManager.getCurrentUser().getUserId();

        for (Map.Entry<Integer, List<Skill>> entry : byTutor.entrySet()) {
            int tutorId        = entry.getKey();
            List<Skill> tutorSkills = entry.getValue();
            String tutorName   = tutorSkills.get(0).getTutorName();
            if (tutorName == null) tutorName = "Tutor";

            VBox card = buildTutorCard(tutorId, tutorName, tutorSkills, currentUserId);
            tutorListPane.getChildren().add(card);
        }
    }

    private VBox buildTutorCard(int tutorId, String tutorName,
                                List<Skill> skills, int currentUserId) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "card-hover");
        card.setPadding(new Insets(16, 20, 16, 20));

        // Header row
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label avatar = new Label("👤");
        avatar.setStyle("-fx-font-size:28px;");
        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(tutorName);
        nameLbl.getStyleClass().add("tutor-name");

        // Rating
        double avg = reviewService.getAverageRating(tutorId);
        String ratingStr = avg > 0 ? String.format("%.1f ★", avg) : "No reviews yet";
        Label ratingLbl = new Label(ratingStr);
        ratingLbl.setStyle("-fx-text-fill: #f9a825; -fx-font-size:12px;");
        nameBox.getChildren().addAll(nameLbl, ratingLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // View slots button
        Button viewBtn = new Button("View Availability");
        viewBtn.getStyleClass().add("btn-secondary");

        header.getChildren().addAll(avatar, nameBox, spacer, viewBtn);

        // Subject chips
        javafx.scene.layout.FlowPane chips = new javafx.scene.layout.FlowPane(8, 6);
        for (Skill sk : skills) {
            Label chip = new Label(sk.getSubjectName());
            chip.getStyleClass().add("tutor-subject-chip");
            chips.getChildren().add(chip);
        }

        // Description (first skill's description shown)
        String desc = skills.get(0).getDescription();
        VBox bodyBox = new VBox(6);
        bodyBox.getChildren().add(chips);
        if (desc != null && !desc.trim().isEmpty()) {
            Label descLbl = new Label(desc.trim());
            descLbl.getStyleClass().add("subtitle-text");
            descLbl.setWrapText(true);
            bodyBox.getChildren().add(descLbl);
        }

        // Availability panel (hidden until toggled)
        VBox availPanel = new VBox(8);
        availPanel.setVisible(false);
        availPanel.setManaged(false);
        availPanel.setStyle("-fx-background-color: #faf8ff; -fx-background-radius:8; -fx-padding:12;");

        viewBtn.setOnAction(e -> {
            if (!availPanel.isVisible()) {
                loadAvailabilityPanel(availPanel, tutorId, tutorName, currentUserId);
                availPanel.setVisible(true);
                availPanel.setManaged(true);
                viewBtn.setText("Hide Availability");
            } else {
                availPanel.setVisible(false);
                availPanel.setManaged(false);
                viewBtn.setText("View Availability");
            }
        });

        card.getChildren().addAll(header, bodyBox, availPanel);
        return card;
    }

    private void loadAvailabilityPanel(VBox panel, int tutorId, String tutorName,
                                       int currentUserId) {
        panel.getChildren().clear();
        List<Availability> slots = availService.getAvailableSlots(tutorId);
        if (slots.isEmpty()) {
            Label none = new Label("No available slots right now.");
            none.getStyleClass().add("info-label");
            panel.getChildren().add(none);
            return;
        }
        Label slotTitle = new Label("Available Slots");
        slotTitle.getStyleClass().add("section-title");
        panel.getChildren().add(slotTitle);

        for (Availability av : slots) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #ffffff; -fx-background-radius:8; -fx-padding:10 14;");
            row.setStyle(row.getStyle() + " -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.05),4,0,0,1);");

            Label slotLbl = new Label(av.getSlotLabel());
            slotLbl.setStyle("-fx-font-size:13px; -fx-text-fill:#2c2c54;");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button bookBtn = new Button("Request Session");
            bookBtn.getStyleClass().add("btn-primary");

            if (currentUserId == tutorId) {
                bookBtn.setText("Your own slot");
                bookBtn.setDisable(true);
            }

            final int availId = av.getAvailabilityId();
            bookBtn.setOnAction(e -> {
                BookingController.openBookingDialog(tutorId, tutorName, availId);
            });

            row.getChildren().addAll(slotLbl, sp, bookBtn);
            panel.getChildren().add(row);
        }
    }

    public VBox getView() { return root; }
}
