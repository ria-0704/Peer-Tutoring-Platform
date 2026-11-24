package com.peertutor.controller;

import com.peertutor.exception.ValidationException;
import com.peertutor.model.Review;
import com.peertutor.model.Session;
import com.peertutor.service.ReviewService;
import com.peertutor.service.SessionService;
import com.peertutor.util.AlertUtil;
import com.peertutor.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Reviews page — view received reviews and submit new ones.
 */
public class ReviewController {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final ReviewService  reviewService  = new ReviewService();
    private final SessionService sessionService = new SessionService();

    private final int userId = SessionManager.getCurrentUser().getUserId();
    private VBox root;

    public ReviewController() {
        buildUI();
    }

    private void buildUI() {
        root = new VBox(24);
        root.setPadding(new Insets(32, 36, 32, 36));
        root.setStyle("-fx-background-color: #f5f0ff;");

        Label title = new Label("Reviews");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Reviews left for you as a tutor appear here. " +
                              "Submit reviews for completed sessions from 'My Sessions'.");
        sub.getStyleClass().add("subtitle-text");
        sub.setWrapText(true);

        // ── Rating summary ─────────────────────────────────────────────────
        double avg = reviewService.getAverageRating(userId);
        List<Review> reviews = reviewService.getTutorReviews(userId);

        HBox summaryBox = new HBox(24);
        summaryBox.setAlignment(Pos.CENTER_LEFT);

        VBox avgCard = new VBox(4);
        avgCard.getStyleClass().add("stat-card");
        avgCard.setAlignment(Pos.CENTER);
        Label avgNum = new Label(avg > 0 ? String.format("%.1f", avg) : "—");
        avgNum.getStyleClass().add("stat-number");
        Label avgLbl = new Label("Average Rating");
        avgLbl.getStyleClass().add("stat-label");
        Label starsLbl = new Label(avg > 0 ? starsString((int) Math.round(avg)) : "No ratings yet");
        starsLbl.setStyle("-fx-font-size:18px; -fx-text-fill:#f9a825;");
        avgCard.getChildren().addAll(avgNum, starsLbl, avgLbl);

        VBox countCard = new VBox(4);
        countCard.getStyleClass().add("stat-card");
        countCard.setAlignment(Pos.CENTER);
        Label countNum = new Label(String.valueOf(reviews.size()));
        countNum.getStyleClass().add("stat-number");
        Label countLbl = new Label("Total Reviews");
        countLbl.getStyleClass().add("stat-label");
        countCard.getChildren().addAll(countNum, countLbl);

        summaryBox.getChildren().addAll(avgCard, countCard);

        // ── Reviews list ───────────────────────────────────────────────────
        Label reviewsTitle = new Label("Reviews Received");
        reviewsTitle.getStyleClass().add("section-title");

        VBox reviewsList = new VBox(10);

        if (reviews.isEmpty()) {
            Label none = new Label("No reviews yet. Complete sessions to receive reviews!");
            none.getStyleClass().add("info-label");
            reviewsList.getChildren().add(none);
        } else {
            for (Review r : reviews) {
                VBox card = new VBox(6);
                card.getStyleClass().add("card");
                card.setPadding(new Insets(14, 18, 14, 18));

                HBox top = new HBox(12);
                top.setAlignment(Pos.CENTER_LEFT);
                Label reviewer = new Label(r.getReviewerName() != null ?
                    r.getReviewerName() : "Student");
                reviewer.setStyle("-fx-font-weight: bold; -fx-text-fill:#3d2b7a;");
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                Label stars = new Label(r.getStars());
                stars.setStyle("-fx-font-size:16px; -fx-text-fill:#f9a825;");
                Label date = new Label(r.getCreatedAt() != null ?
                    r.getCreatedAt().format(DT_FMT) : "");
                date.getStyleClass().add("subtitle-text");
                top.getChildren().addAll(reviewer, sp, stars, date);

                if (r.getComment() != null && !r.getComment().trim().isEmpty()) {
                    Label comment = new Label(r.getComment());
                    comment.setWrapText(true);
                    comment.getStyleClass().add("subtitle-text");
                    card.getChildren().addAll(top, comment);
                } else {
                    card.getChildren().add(top);
                }
                reviewsList.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(reviewsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(title, sub, summaryBox, reviewsTitle, scroll);
    }

    /**
     * Opens a modal dialog to submit a review for a completed session.
     * Called from MySessionsController.
     */
    public static void openReviewDialog(int sessionId, String tutorName, Runnable onSuccess) {
        ReviewService svc       = new ReviewService();
        SessionService sessSvc  = new SessionService();

        // Check if already reviewed
        Optional<Review> existing = svc.getReviewForSession(sessionId);
        if (existing.isPresent()) {
            AlertUtil.showInfo("Already Reviewed",
                "You have already submitted a review for this session.\n" +
                "Rating: " + existing.get().getStars());
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Review Session with " + tutorName);
        dialog.setWidth(440);
        dialog.setResizable(false);

        VBox body = new VBox(16);
        body.setPadding(new Insets(28, 32, 28, 32));
        body.setStyle("-fx-background-color: #ffffff;");

        Label titleLbl = new Label("Leave a Review");
        titleLbl.getStyleClass().add("section-title");

        Label tutorLbl = new Label("Tutor: " + tutorName);
        tutorLbl.getStyleClass().add("subtitle-text");

        Label ratingLbl = new Label("Rating (1–5)");
        ratingLbl.getStyleClass().add("field-label");
        ComboBox<Integer> ratingCombo = new ComboBox<>(
            FXCollections.observableArrayList(1, 2, 3, 4, 5));
        ratingCombo.setPromptText("Select rating");
        ratingCombo.setPrefWidth(200);

        // Live star preview
        Label starPreview = new Label("Select a rating");
        starPreview.setStyle("-fx-font-size:22px; -fx-text-fill:#f9a825;");
        ratingCombo.setOnAction(e -> {
            Integer val = ratingCombo.getValue();
            if (val != null) starPreview.setText(starsStr(val));
        });

        Label commentLbl = new Label("Comment (optional)");
        commentLbl.getStyleClass().add("field-label");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Share your experience...");
        commentArea.setPrefRowCount(4);

        Button submitBtn = new Button("Submit Review");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setPrefWidth(200);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(10, submitBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        body.getChildren().addAll(
            titleLbl, tutorLbl,
            ratingLbl, ratingCombo, starPreview,
            commentLbl, commentArea, btnRow
        );

        submitBtn.setOnAction(e -> {
            Integer rating = ratingCombo.getValue();
            if (rating == null) {
                AlertUtil.showWarning("Rating Required", "Please select a rating.");
                return;
            }
            try {
                // Retrieve full session object
                List<Session> sessions = sessSvc.getStudentSessions(
                    SessionManager.getCurrentUser().getUserId());
                Session sess = sessions.stream()
                    .filter(s -> s.getSessionId() == sessionId)
                    .findFirst().orElse(null);

                svc.submitReview(sess,
                    SessionManager.getCurrentUser().getUserId(),
                    rating, commentArea.getText());
                AlertUtil.showInfo("Review Submitted", "Thank you for your feedback!");
                dialog.close();
                if (onSuccess != null) onSuccess.run();
            } catch (ValidationException ex) {
                AlertUtil.showError("Error", ex.getMessage());
            } catch (Exception ex) {
                AlertUtil.showError("Error", ex.getMessage());
            }
        });

        Scene scene = new Scene(body);
        String css = ReviewController.class
            .getResource("/com/peertutor/css/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static String starsStr(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= n ? "★" : "☆");
        return sb.toString();
    }

    private String starsString(int n) { return starsStr(n); }

    public VBox getView() { return root; }
}
