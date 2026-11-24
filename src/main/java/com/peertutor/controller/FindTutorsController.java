package com.peertutor.controller;

import com.peertutor.model.Availability;
import com.peertutor.model.Review;
import com.peertutor.model.Skill;
import com.peertutor.service.AvailabilityService;
import com.peertutor.service.ReviewService;
import com.peertutor.service.TutorService;
import com.peertutor.util.AlertUtil;
import com.peertutor.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Find Tutors page — search, full-width card listing, detail profile modal.
 *
 * Layout contract:
 *   - root is a VBox with no internal ScrollPane.
 *   - The outer ScrollPane in MainLayoutController handles vertical overflow.
 *   - Tutor cards fill the full available width.
 *   - Pagination keeps the list manageable without nesting scrollbars.
 */
public class FindTutorsController {

    private static final int PAGE_SIZE = 8;

    private final TutorService        tutorService  = new TutorService();
    private final AvailabilityService availService  = new AvailabilityService();
    private final ReviewService       reviewService = new ReviewService();

    // Current search result grouped by tutor (preserves insertion order)
    private final List<TutorEntry> currentResults = new ArrayList<>();
    private int currentPage = 0;   // 0-based

    private VBox      root;
    private TextField searchField;
    private VBox      tutorListPane;
    private Label     pageLabel;
    private Button    prevBtn, nextBtn;

    public FindTutorsController() {
        buildUI();
    }

    // ── Build ─────────────────────────────────────────────────────────────

    private void buildUI() {
        root = new VBox(0);
        root.setStyle("-fx-background-color: #f5f0ff;");

        // ── Page header ───────────────────────────────────────────────────
        VBox header = new VBox(4);
        header.setPadding(new Insets(32, 36, 16, 36));
        header.setStyle("-fx-background-color: #f5f0ff;");

        Label title = new Label("Find a Tutor");
        title.getStyleClass().add("page-title");

        Label sub = new Label(
            "Search by name or subject · Click a card to view full profile and book a session");
        sub.getStyleClass().add("subtitle-text");

        // ── Search bar ────────────────────────────────────────────────────
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(8, 0, 0, 0));

        searchField = new TextField();
        searchField.setPromptText("Search by tutor name or subject...");
        searchField.setPrefWidth(380);
        searchField.setPrefHeight(40);

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("btn-primary");
        searchBtn.setPrefHeight(40);

        Button clearBtn = new Button("Show All");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setPrefHeight(40);

        searchBar.getChildren().addAll(searchField, searchBtn, clearBtn);

        searchBtn.setOnAction(e -> runSearch(searchField.getText()));
        clearBtn.setOnAction(e -> { searchField.clear(); runSearch(""); });
        searchField.setOnAction(e -> runSearch(searchField.getText()));

        header.getChildren().addAll(title, sub, searchBar);

        // ── Divider ───────────────────────────────────────────────────────
        Separator divider = new Separator();
        divider.setPadding(new Insets(0, 36, 0, 36));

        // ── Tutor list ────────────────────────────────────────────────────
        tutorListPane = new VBox(14);
        tutorListPane.setPadding(new Insets(20, 36, 10, 36));

        // ── Pagination bar ────────────────────────────────────────────────
        HBox paginationBar = new HBox(12);
        paginationBar.setAlignment(Pos.CENTER_RIGHT);
        paginationBar.setPadding(new Insets(8, 36, 24, 36));

        prevBtn   = new Button("← Previous");
        nextBtn   = new Button("Next →");
        pageLabel = new Label();

        prevBtn.getStyleClass().add("btn-secondary");
        nextBtn.getStyleClass().add("btn-secondary");
        pageLabel.getStyleClass().add("subtitle-text");

        prevBtn.setOnAction(e -> { currentPage--; renderPage(); });
        nextBtn.setOnAction(e -> { currentPage++; renderPage(); });

        paginationBar.getChildren().addAll(pageLabel, prevBtn, nextBtn);

        root.getChildren().addAll(header, divider, tutorListPane, paginationBar);

        // Initial load
        runSearch("");
    }

    // ── Search & pagination ───────────────────────────────────────────────

    private void runSearch(String query) {
        currentPage = 0;
        currentResults.clear();

        List<Skill> skills = tutorService.searchTutors(query == null ? "" : query.trim());

        // Group by tutorId preserving order
        Map<Integer, TutorEntry> map = new LinkedHashMap<>();
        for (Skill sk : skills) {
            map.computeIfAbsent(sk.getUserId(),
                id -> new TutorEntry(id, sk.getTutorName())).skills.add(sk);
        }
        currentResults.addAll(map.values());
        renderPage();
    }

    private void renderPage() {
        tutorListPane.getChildren().clear();

        int total = currentResults.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

        if (total == 0) {
            VBox emptyState = buildEmptyState();
            tutorListPane.getChildren().add(emptyState);
            pageLabel.setText("");
            prevBtn.setVisible(false);
            nextBtn.setVisible(false);
            return;
        }

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, total);
        int currentUserId = SessionManager.getCurrentUser().getUserId();

        for (int i = from; i < to; i++) {
            TutorEntry entry = currentResults.get(i);
            tutorListPane.getChildren().add(buildTutorCard(entry, currentUserId));
        }

        // Pagination controls
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages +
                          "  (" + total + " tutor" + (total == 1 ? "" : "s") + ")");
        prevBtn.setVisible(currentPage > 0);
        nextBtn.setVisible(currentPage < totalPages - 1);
    }

    // ── Tutor card ────────────────────────────────────────────────────────

    private VBox buildTutorCard(TutorEntry entry, int currentUserId) {
        double avgRating = reviewService.getAverageRating(entry.tutorId);
        List<Availability> freeSlots = availService.getAvailableSlots(entry.tutorId);

        // ── Outer card container ──────────────────────────────────────────
        VBox card = new VBox(0);
        card.getStyleClass().add("tutor-listing-card");
        card.setCursor(Cursor.HAND);

        // ── Top row: avatar | name+rating | slots badge | buttons ─────────
        HBox topRow = new HBox(16);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setPadding(new Insets(20, 20, 14, 20));

        // Avatar circle
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("tutor-avatar");
        Label avatarLbl = new Label(entry.initials());
        avatarLbl.getStyleClass().add("tutor-avatar-text");
        avatar.getChildren().add(avatarLbl);

        // Name + rating column
        VBox nameCol = new VBox(3);
        Label nameLbl = new Label(entry.tutorName);
        nameLbl.getStyleClass().add("tutor-card-name");

        HBox ratingRow = new HBox(5);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        if (avgRating > 0) {
            Label stars = new Label(starsStr((int) Math.round(avgRating)));
            stars.setStyle("-fx-text-fill:#f9a825; -fx-font-size:13px;");
            Label ratingNum = new Label(String.format("%.1f", avgRating));
            ratingNum.setStyle("-fx-text-fill:#f9a825; -fx-font-weight:bold; -fx-font-size:13px;");
            int reviewCount = reviewService.getTutorReviews(entry.tutorId).size();
            Label reviewCnt = new Label("(" + reviewCount +
                                        (reviewCount == 1 ? " review)" : " reviews)"));
            reviewCnt.getStyleClass().add("subtitle-text");
            ratingRow.getChildren().addAll(stars, ratingNum, reviewCnt);
        } else {
            Label noRating = new Label("No reviews yet");
            noRating.getStyleClass().add("subtitle-text");
            ratingRow.getChildren().add(noRating);
        }
        nameCol.getChildren().addAll(nameLbl, ratingRow);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Slots badge
        VBox badgeCol = new VBox(2);
        badgeCol.setAlignment(Pos.CENTER_RIGHT);
        int slotCount = freeSlots.size();
        Label slotBadge = new Label(slotCount == 0 ? "No slots available"
                                                   : slotCount + " slot" +
                                                     (slotCount == 1 ? "" : "s") + " free");
        slotBadge.getStyleClass().add(slotCount == 0 ? "badge-unavailable" : "badge-available");
        badgeCol.getChildren().add(slotBadge);

        // Buttons
        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.getStyleClass().add("btn-secondary");

        topRow.getChildren().addAll(avatar, nameCol, spacer, badgeCol, viewProfileBtn);

        // ── Subjects row ──────────────────────────────────────────────────
        FlowPane chips = new FlowPane(8, 6);
        chips.setPadding(new Insets(0, 20, 12, 72));  // indent past the avatar
        for (Skill sk : entry.skills) {
            Label chip = new Label(sk.getSubjectName());
            chip.getStyleClass().add("tutor-subject-chip");
            chips.getChildren().add(chip);
        }

        // ── Description preview ───────────────────────────────────────────
        // Show the first non-empty description found across all skills
        String desc = entry.skills.stream()
                .map(Skill::getDescription)
                .filter(d -> d != null && !d.trim().isEmpty())
                .findFirst().orElse(null);

        card.getChildren().addAll(topRow, chips);

        if (desc != null) {
            Label descLbl = new Label(desc.length() > 160 ? desc.substring(0, 157) + "…" : desc);
            descLbl.getStyleClass().add("subtitle-text");
            descLbl.setWrapText(true);
            descLbl.setPadding(new Insets(0, 20, 14, 72));
            card.getChildren().add(descLbl);
        }

        // ── Separator inside card ─────────────────────────────────────────
        Separator innerSep = new Separator();
        innerSep.setStyle("-fx-background-color: #f0eaff;");

        // ── Bottom action row ─────────────────────────────────────────────
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.setPadding(new Insets(10, 20, 14, 20));

        // "Teaches: X subjects" label
        Label teachesLbl = new Label("Teaches " + entry.skills.size() +
                                     " subject" + (entry.skills.size() == 1 ? "" : "s"));
        teachesLbl.getStyleClass().add("subtitle-text");

        Region bSpacer = new Region();
        HBox.setHgrow(bSpacer, Priority.ALWAYS);

        Button bookNowBtn = new Button("Book a Session");
        bookNowBtn.getStyleClass().add("btn-primary");
        bookNowBtn.setVisible(slotCount > 0 && currentUserId != entry.tutorId);

        bottomRow.getChildren().addAll(teachesLbl, bSpacer, bookNowBtn);
        card.getChildren().addAll(innerSep, bottomRow);

        // ── Event handlers ────────────────────────────────────────────────
        // The whole card and "View Profile" open the detail dialog
        card.setOnMouseClicked(e -> openProfileDialog(entry, currentUserId));
        viewProfileBtn.setOnMouseClicked(e -> {
            e.consume();   // stop event bubbling to card
            openProfileDialog(entry, currentUserId);
        });
        bookNowBtn.setOnMouseClicked(e -> {
            e.consume();
            openProfileDialog(entry, currentUserId);  // dialog contains booking
        });

        return card;
    }

    // ── Profile detail dialog ─────────────────────────────────────────────

    private void openProfileDialog(TutorEntry entry, int currentUserId) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(entry.tutorName + " — Tutor Profile");
        dialog.setWidth(680);
        dialog.setHeight(620);
        dialog.setResizable(true);

        // Fetch fresh data
        double avgRating      = reviewService.getAverageRating(entry.tutorId);
        List<Review> reviews  = reviewService.getTutorReviews(entry.tutorId);
        List<Availability> slots = availService.getAvailableSlots(entry.tutorId);

        // ── Root scroll ───────────────────────────────────────────────────
        VBox body = new VBox(0);
        body.setStyle("-fx-background-color: #f5f0ff;");

        // ── Banner / header ───────────────────────────────────────────────
        VBox banner = new VBox(10);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(28, 32, 20, 32));
        banner.setStyle("-fx-background-color: #3d2b7a;");

        StackPane bigAvatar = new StackPane();
        bigAvatar.getStyleClass().add("tutor-avatar-large");
        Label bigInitials = new Label(entry.initials());
        bigInitials.getStyleClass().add("tutor-avatar-large-text");
        bigAvatar.getChildren().add(bigInitials);

        Label dialogName = new Label(entry.tutorName);
        dialogName.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#ffffff;");

        HBox ratingBanner = new HBox(6);
        ratingBanner.setAlignment(Pos.CENTER);
        if (avgRating > 0) {
            Label stars = new Label(starsStr((int) Math.round(avgRating)));
            stars.setStyle("-fx-text-fill:#f9a825; -fx-font-size:16px;");
            Label num = new Label(String.format("%.1f / 5.0", avgRating));
            num.setStyle("-fx-text-fill:#f9a825; -fx-font-size:14px; -fx-font-weight:bold;");
            Label cnt = new Label("(" + reviews.size() + " review" +
                                  (reviews.size() == 1 ? ")" : "s)"));
            cnt.setStyle("-fx-text-fill:#c9b8f0; -fx-font-size:12px;");
            ratingBanner.getChildren().addAll(stars, num, cnt);
        } else {
            Label noRev = new Label("No reviews yet");
            noRev.setStyle("-fx-text-fill:#c9b8f0; -fx-font-size:13px;");
            ratingBanner.getChildren().add(noRev);
        }

        int slotCount = slots.size();
        Label slotLabel = new Label(slotCount == 0
                ? "No available slots right now"
                : slotCount + " available slot" + (slotCount == 1 ? "" : "s"));
        slotLabel.setStyle("-fx-text-fill:#c9b8f0; -fx-font-size:12px;");

        banner.getChildren().addAll(bigAvatar, dialogName, ratingBanner, slotLabel);

        // ── Scrollable content ────────────────────────────────────────────
        VBox content = new VBox(20);
        content.setPadding(new Insets(24, 32, 24, 32));

        // Subjects & descriptions
        Label subjectsTitle = sectionLabel("Subjects Taught");
        VBox subjectsBox = new VBox(10);
        for (Skill sk : entry.skills) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.TOP_LEFT);
            row.setPadding(new Insets(10, 14, 10, 14));
            row.setStyle("-fx-background-color:#ffffff; -fx-background-radius:8;" +
                         "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),4,0,0,1);");

            Label chip = new Label(sk.getSubjectName());
            chip.getStyleClass().add("tutor-subject-chip");
            chip.setMinWidth(130);

            VBox descCol = new VBox(2);
            if (sk.getDescription() != null && !sk.getDescription().trim().isEmpty()) {
                Label descLbl = new Label(sk.getDescription().trim());
                descLbl.getStyleClass().add("subtitle-text");
                descLbl.setWrapText(true);
                descCol.getChildren().add(descLbl);
            } else {
                Label noDesc = new Label("No description provided");
                noDesc.getStyleClass().add("info-label");
                descCol.getChildren().add(noDesc);
            }
            HBox.setHgrow(descCol, Priority.ALWAYS);
            row.getChildren().addAll(chip, descCol);
            subjectsBox.getChildren().add(row);
        }

        // Availability
        Label availTitle = sectionLabel("Available Time Slots");
        VBox availBox = new VBox(8);
        if (slots.isEmpty()) {
            Label none = new Label("This tutor has no available slots at the moment.");
            none.getStyleClass().add("info-label");
            availBox.getChildren().add(none);
        } else {
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            for (Availability av : slots) {
                HBox slotRow = new HBox(12);
                slotRow.setAlignment(Pos.CENTER_LEFT);
                slotRow.setPadding(new Insets(10, 14, 10, 14));
                slotRow.setStyle("-fx-background-color:#ffffff; -fx-background-radius:8;");

                Label dateLbl = new Label(av.getDate().format(dateFmt));
                dateLbl.setStyle("-fx-font-weight:bold; -fx-text-fill:#3d2b7a; -fx-min-width:160;");
                Label timeLbl = new Label(av.getStartTime().format(timeFmt) +
                                          " – " + av.getEndTime().format(timeFmt));
                timeLbl.getStyleClass().add("subtitle-text");

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                if (currentUserId != entry.tutorId) {
                    Button bookBtn = new Button("Request Session");
                    bookBtn.getStyleClass().add("btn-primary");
                    final int availId = av.getAvailabilityId();
                    bookBtn.setOnAction(e -> {
                        dialog.close();
                        BookingController.openBookingDialog(
                                entry.tutorId, entry.tutorName, availId);
                    });
                    slotRow.getChildren().addAll(dateLbl, timeLbl, sp, bookBtn);
                } else {
                    Label ownLbl = new Label("Your slot");
                    ownLbl.getStyleClass().add("info-label");
                    slotRow.getChildren().addAll(dateLbl, timeLbl, sp, ownLbl);
                }
                availBox.getChildren().add(slotRow);
            }
        }

        // Reviews
        Label reviewsTitle = sectionLabel("Student Reviews");
        VBox reviewsBox = new VBox(10);
        if (reviews.isEmpty()) {
            Label noRev = new Label("No reviews yet for this tutor.");
            noRev.getStyleClass().add("info-label");
            reviewsBox.getChildren().add(noRev);
        } else {
            DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            for (Review r : reviews) {
                VBox rCard = new VBox(4);
                rCard.setPadding(new Insets(10, 14, 10, 14));
                rCard.setStyle("-fx-background-color:#ffffff; -fx-background-radius:8;");

                HBox rTop = new HBox(10);
                rTop.setAlignment(Pos.CENTER_LEFT);
                Label reviewer = new Label(r.getReviewerName() != null
                        ? r.getReviewerName() : "Student");
                reviewer.setStyle("-fx-font-weight:bold; -fx-text-fill:#3d2b7a;");
                Region rSp = new Region();
                HBox.setHgrow(rSp, Priority.ALWAYS);
                Label rStars = new Label(r.getStars());
                rStars.setStyle("-fx-font-size:14px; -fx-text-fill:#f9a825;");
                Label rDate = new Label(r.getCreatedAt() != null
                        ? r.getCreatedAt().toLocalDate().format(dtFmt) : "");
                rDate.getStyleClass().add("subtitle-text");
                rTop.getChildren().addAll(reviewer, rSp, rStars, rDate);
                rCard.getChildren().add(rTop);

                if (r.getComment() != null && !r.getComment().trim().isEmpty()) {
                    Label comment = new Label(r.getComment().trim());
                    comment.getStyleClass().add("subtitle-text");
                    comment.setWrapText(true);
                    rCard.getChildren().add(comment);
                }
                reviewsBox.getChildren().add(rCard);
            }
        }

        content.getChildren().addAll(
                subjectsTitle, subjectsBox,
                availTitle,    availBox,
                reviewsTitle,  reviewsBox
        );

        ScrollPane contentScroll = new ScrollPane(content);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setStyle("-fx-background: #f5f0ff; -fx-background-color: #f5f0ff;");
        VBox.setVgrow(contentScroll, Priority.ALWAYS);

        // Close button
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-secondary");
        closeBtn.setOnAction(e -> dialog.close());
        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.CENTER_RIGHT);
        closeRow.setPadding(new Insets(12, 24, 16, 24));
        closeRow.setStyle("-fx-background-color:#f5f0ff;");

        body.getChildren().addAll(banner, contentScroll, closeRow);

        Scene scene = new Scene(body);
        String css = FindTutorsController.class
                .getResource("/com/peertutor/css/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private VBox buildEmptyState() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 0, 60, 0));
        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size:40px;");
        Label msg = new Label("No tutors found matching your search.");
        msg.getStyleClass().add("subtitle-text");
        Label hint = new Label("Try a different name or subject, or click 'Show All'.");
        hint.getStyleClass().add("info-label");
        box.getChildren().addAll(icon, msg, hint);
        return box;
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("section-title");
        return lbl;
    }

    private static String starsStr(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= n ? "★" : "☆");
        return sb.toString();
    }

    public VBox getView() { return root; }

    // ── Inner data holder ─────────────────────────────────────────────────

    /** Groups all skills belonging to one tutor. */
    private static class TutorEntry {
        final int         tutorId;
        final String      tutorName;
        final List<Skill> skills = new ArrayList<>();

        TutorEntry(int tutorId, String tutorName) {
            this.tutorId   = tutorId;
            this.tutorName = tutorName != null ? tutorName : "Tutor";
        }

        /** Up to two uppercase initials from the tutor's name. */
        String initials() {
            String[] parts = tutorName.trim().split("\\s+");
            if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
    }
}
