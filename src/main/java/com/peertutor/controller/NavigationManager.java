package com.peertutor.controller;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Central navigation hub — builds and shows each screen.
 */
public final class NavigationManager {

    private static Stage stage;

    private NavigationManager() {}

    public static void initialize(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void showLogin() {
        LoginController ctrl = new LoginController();
        Scene scene = new Scene(ctrl.getView(), stage.getWidth(), stage.getHeight());
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    public static void showRegister() {
        RegisterController ctrl = new RegisterController();
        Scene scene = new Scene(ctrl.getView(), stage.getWidth(), stage.getHeight());
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    public static void showDashboard() {
        MainLayoutController ctrl = new MainLayoutController();
        ctrl.showPage(MainLayoutController.Page.DASHBOARD);
        Scene scene = new Scene(ctrl.getView(), stage.getWidth(), stage.getHeight());
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    private static void applyStylesheet(Scene scene) {
        String css = NavigationManager.class
                .getResource("/com/peertutor/css/styles.css")
                .toExternalForm();
        scene.getStylesheets().add(css);
    }

    public static Stage getStage() { return stage; }
}
