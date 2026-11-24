package com.peertutor.controller;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Central navigation hub — builds and shows each screen.
 * Also holds a reference to the active MainLayoutController so that
 * any page can navigate to another page without creating a new scene.
 */
public final class NavigationManager {

    private static Stage                stage;
    private static MainLayoutController activeLayout;

    private NavigationManager() {}

    public static void initialize(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void showLogin() {
        activeLayout = null;
        LoginController ctrl = new LoginController();
        Scene scene = new Scene(ctrl.getView(), stage.getWidth(), stage.getHeight());
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    public static void showRegister() {
        activeLayout = null;
        RegisterController ctrl = new RegisterController();
        Scene scene = new Scene(ctrl.getView(), stage.getWidth(), stage.getHeight());
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    public static void showDashboard() {
        MainLayoutController ctrl = new MainLayoutController();
        activeLayout = ctrl;
        ctrl.showPage(MainLayoutController.Page.DASHBOARD);
        Scene scene = new Scene(ctrl.getView(), stage.getWidth(), stage.getHeight());
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    /**
     * Navigate to a page inside the existing main layout without
     * replacing the scene. Safe to call from any page controller.
     */
    public static void navigateTo(MainLayoutController.Page page) {
        if (activeLayout != null) {
            activeLayout.showPage(page);
        }
    }

    private static void applyStylesheet(Scene scene) {
        String css = NavigationManager.class
                .getResource("/com/peertutor/css/styles.css")
                .toExternalForm();
        scene.getStylesheets().add(css);
    }

    public static Stage              getStage()       { return stage;        }
    public static MainLayoutController getActiveLayout() { return activeLayout; }
}
