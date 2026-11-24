package com.peertutor.controller;

import com.peertutor.util.AlertUtil;
import com.peertutor.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Persistent main layout: sidebar navigation + content area.
 * Swaps content pages without changing the sidebar.
 */
public class MainLayoutController {

    public enum Page {
        DASHBOARD, TUTOR_PROFILE, AVAILABILITY, FIND_TUTORS, BOOKINGS, MY_SESSIONS, REVIEWS
    }

    private BorderPane root;
    private StackPane  contentArea;

    // Nav buttons — kept as fields for active-state styling
    private Button btnDashboard, btnTutorProfile, btnAvailability,
                   btnFindTutors, btnBookings, btnMySessions, btnReviews;
    private Button activeBtn;

    public MainLayoutController() {
        buildLayout();
    }

    private void buildLayout() {
        root = new BorderPane();
        root.getStyleClass().add("page-bg");

        // ── Sidebar ───────────────────────────────────────────────────────
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // ── Content area ──────────────────────────────────────────────────
        // Wrapped in a ScrollPane so tall pages scroll instead of clipping.
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f5f0ff;");

        ScrollPane contentScroll = new ScrollPane(contentArea);
        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(false);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setStyle("-fx-background: #f5f0ff; -fx-background-color: #f5f0ff;");
        root.setCenter(contentScroll);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);

        // Brand
        Label appName = new Label("PeerTutor");
        appName.getStyleClass().add("sidebar-title");
        Label tagline = new Label("Study Together, Grow Together");
        tagline.getStyleClass().add("sidebar-subtitle");
        tagline.setWrapText(true);

        // User info
        VBox userBox = new VBox(2);
        userBox.setPadding(new Insets(0, 20, 16, 20));
        Label userIcon = new Label("👤 " + SessionManager.getCurrentUser().getName());
        userIcon.setStyle("-fx-text-fill: #c9b8f0; -fx-font-size: 12px;");
        userBox.getChildren().add(userIcon);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #5340a0; -fx-pref-height: 1;");

        // Nav buttons
        btnDashboard    = navBtn("🏠  Dashboard",         Page.DASHBOARD);
        btnFindTutors   = navBtn("🔍  Find Tutors",       Page.FIND_TUTORS);
        btnBookings     = navBtn("📅  Book a Session",    Page.BOOKINGS);
        btnMySessions   = navBtn("📋  My Sessions",       Page.MY_SESSIONS);
        btnTutorProfile = navBtn("🎓  My Tutor Profile",  Page.TUTOR_PROFILE);
        btnAvailability = navBtn("🕐  My Availability",   Page.AVAILABILITY);
        btnReviews      = navBtn("⭐  Reviews",            Page.REVIEWS);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout
        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.getStyleClass().addAll("nav-btn", "nav-btn-logout");
        logoutBtn.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(
            appName, tagline, userBox, sep,
            btnDashboard, btnFindTutors, btnBookings, btnMySessions,
            btnTutorProfile, btnAvailability, btnReviews,
            spacer, logoutBtn,
            new Region() {{ setPrefHeight(16); }}
        );

        return sidebar;
    }

    private Button navBtn(String label, Page page) {
        Button btn = new Button(label);
        btn.getStyleClass().add("nav-btn");
        btn.setOnAction(e -> showPage(page));
        return btn;
    }

    public void showPage(Page page) {
        // Update active button styling
        Button target = switch (page) {
            case DASHBOARD      -> btnDashboard;
            case TUTOR_PROFILE  -> btnTutorProfile;
            case AVAILABILITY   -> btnAvailability;
            case FIND_TUTORS    -> btnFindTutors;
            case BOOKINGS       -> btnBookings;
            case MY_SESSIONS    -> btnMySessions;
            case REVIEWS        -> btnReviews;
        };
        if (activeBtn != null) activeBtn.getStyleClass().remove("nav-btn-active");
        target.getStyleClass().add("nav-btn-active");
        activeBtn = target;

        // Load the content
        Node content = switch (page) {
            case DASHBOARD      -> new DashboardController().getView();
            case TUTOR_PROFILE  -> new TutorProfileController().getView();
            case AVAILABILITY   -> new AvailabilityController().getView();
            case FIND_TUTORS    -> new FindTutorsController().getView();
            case BOOKINGS       -> new BookingController().getView();
            case MY_SESSIONS    -> new MySessionsController().getView();
            case REVIEWS        -> new ReviewController().getView();
        };
        contentArea.getChildren().setAll(content);
    }

    private void handleLogout() {
        if (AlertUtil.showConfirm("Logout", "Are you sure you want to logout?")) {
            SessionManager.logout();
            NavigationManager.showLogin();
        }
    }

    public BorderPane getView() { return root; }
}
