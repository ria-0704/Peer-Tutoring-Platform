package com.peertutor.controller;

import com.peertutor.exception.ValidationException;
import com.peertutor.model.User;
import com.peertutor.service.UserService;
import com.peertutor.util.AlertUtil;
import com.peertutor.util.SessionManager;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Login screen — built programmatically.
 */
public class LoginController {

    private final UserService userService = new UserService();
    private VBox root;

    public LoginController() {
        buildUI();
    }

    private void buildUI() {
        // ── Outer page ───────────────────────────────────────────────────
        StackPane page = new StackPane();
        page.getStyleClass().add("auth-container");
        page.setPrefSize(1100, 720);

        // ── Auth Card ────────────────────────────────────────────────────
        VBox card = new VBox(16);
        card.getStyleClass().add("auth-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(440);

        // Logo / emoji
        Label logo = new Label("📚");
        logo.getStyleClass().add("auth-logo");
        logo.setStyle("-fx-font-size:40px;");

        Label title    = new Label("Welcome Back");
        Label subtitle = new Label("Sign in to your Peer Tutoring account");
        title.getStyleClass().add("auth-title");
        subtitle.getStyleClass().add("auth-subtitle");

        // Email
        Label emailLbl = new Label("University Email");
        emailLbl.getStyleClass().add("field-label");
        TextField emailField = new TextField();
        emailField.setPromptText("you@university.edu");
        emailField.setPrefWidth(360);

        // Password
        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("field-label");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setPrefWidth(360);

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setPrefWidth(360);
        loginBtn.setPrefHeight(42);

        // Register link
        HBox linkBox = new HBox(4);
        linkBox.setAlignment(Pos.CENTER);
        Label noAccLbl = new Label("Don't have an account?");
        noAccLbl.getStyleClass().add("subtitle-text");
        Button registerLink = new Button("Create Account");
        registerLink.getStyleClass().add("btn-link");
        linkBox.getChildren().addAll(noAccLbl, registerLink);

        card.getChildren().addAll(
            logo, title, subtitle,
            new Region() {{ setPrefHeight(8); }},
            emailLbl, emailField,
            passLbl,  passField,
            new Region() {{ setPrefHeight(4); }},
            loginBtn, linkBox
        );

        page.getChildren().add(card);

        // ── Event Handlers ───────────────────────────────────────────────
        loginBtn.setOnAction(e -> handleLogin(emailField, passField));
        passField.setOnAction(e -> handleLogin(emailField, passField));
        registerLink.setOnAction(e -> NavigationManager.showRegister());

        root = new VBox(page);
        VBox.setVgrow(page, Priority.ALWAYS);
    }

    private void handleLogin(TextField emailField, PasswordField passField) {
        try {
            User user = userService.login(emailField.getText(), passField.getText());
            SessionManager.login(user);
            NavigationManager.showDashboard();
        } catch (ValidationException ex) {
            AlertUtil.showError("Login Failed", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Error", "An unexpected error occurred: " + ex.getMessage());
        }
    }

    public VBox getView() { return root; }
}
