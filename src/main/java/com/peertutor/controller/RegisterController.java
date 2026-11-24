package com.peertutor.controller;

import com.peertutor.exception.ValidationException;
import com.peertutor.service.UserService;
import com.peertutor.util.AlertUtil;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Registration screen — built programmatically.
 */
public class RegisterController {

    private final UserService userService = new UserService();
    private VBox root;

    public RegisterController() {
        buildUI();
    }

    private void buildUI() {
        StackPane page = new StackPane();
        page.getStyleClass().add("auth-container");
        page.setPrefSize(1100, 720);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox card = new VBox(14);
        card.getStyleClass().add("auth-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(460);
        card.setStyle("-fx-padding: 36 48;");

        Label logo = new Label("📚");
        logo.setStyle("-fx-font-size:36px;");

        Label title    = new Label("Create Your Account");
        Label subtitle = new Label("Join the Peer Tutoring Platform");
        title.getStyleClass().add("auth-title");
        subtitle.getStyleClass().add("auth-subtitle");

        // Fields
        Label nameLbl = new Label("Full Name");     nameLbl.getStyleClass().add("field-label");
        TextField nameField = new TextField();      nameField.setPromptText("Your full name");

        Label emailLbl = new Label("University Email"); emailLbl.getStyleClass().add("field-label");
        TextField emailField = new TextField();     emailField.setPromptText("you@university.edu");

        Label sidLbl = new Label("Student ID");     sidLbl.getStyleClass().add("field-label");
        TextField sidField = new TextField();       sidField.setPromptText("e.g. STU2024001");

        Label passLbl = new Label("Password");      passLbl.getStyleClass().add("field-label");
        PasswordField passField = new PasswordField(); passField.setPromptText("At least 6 characters");

        Label confLbl = new Label("Confirm Password"); confLbl.getStyleClass().add("field-label");
        PasswordField confField = new PasswordField(); confField.setPromptText("Re-enter password");

        for (Control c : new Control[]{nameField, emailField, sidField, passField, confField}) {
            c.setPrefWidth(360);
        }

        Button registerBtn = new Button("Create Account");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setPrefWidth(360);
        registerBtn.setPrefHeight(42);

        HBox linkBox = new HBox(4);
        linkBox.setAlignment(Pos.CENTER);
        Label hasAccLbl = new Label("Already have an account?");
        hasAccLbl.getStyleClass().add("subtitle-text");
        Button loginLink = new Button("Sign In");
        loginLink.getStyleClass().add("btn-link");
        linkBox.getChildren().addAll(hasAccLbl, loginLink);

        card.getChildren().addAll(
            logo, title, subtitle,
            new Region() {{ setPrefHeight(6); }},
            nameLbl, nameField,
            emailLbl, emailField,
            sidLbl, sidField,
            passLbl, passField,
            confLbl, confField,
            new Region() {{ setPrefHeight(4); }},
            registerBtn, linkBox
        );

        // Center card inside a VBox for scroll
        VBox wrapper = new VBox(card);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new javafx.geometry.Insets(40, 0, 40, 0));
        wrapper.setStyle("-fx-background-color: #f5f0ff;");

        scroll.setContent(wrapper);
        page.getChildren().add(scroll);

        // ── Events ─────────────────────────────────────────────────────
        registerBtn.setOnAction(e -> handleRegister(
            nameField, emailField, sidField, passField, confField));
        loginLink.setOnAction(e -> NavigationManager.showLogin());

        root = new VBox(page);
        VBox.setVgrow(page, Priority.ALWAYS);
    }

    private void handleRegister(TextField nameField, TextField emailField,
                                TextField sidField, PasswordField passField,
                                PasswordField confField) {
        try {
            userService.register(
                nameField.getText(), emailField.getText(),
                passField.getText(), confField.getText(), sidField.getText());
            AlertUtil.showInfo("Registration Successful",
                "Your account has been created. You can now sign in.");
            NavigationManager.showLogin();
        } catch (ValidationException ex) {
            AlertUtil.showError("Registration Failed", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Error", "An unexpected error occurred: " + ex.getMessage());
        }
    }

    public VBox getView() { return root; }
}
