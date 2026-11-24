package com.peertutor;

import com.peertutor.util.DBConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application entry point.
 */
public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("Peer Tutoring and Study Session Platform");
        stage.setWidth(1100);
        stage.setHeight(720);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // Test DB connection early so we show a helpful error immediately
        try {
            DBConnection.getConnection();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection failed on startup", e);
            com.peertutor.util.AlertUtil.showError(
                "Database Connection Error",
                "Cannot connect to the database.\n\n" +
                "Please check:\n" +
                "  • MySQL is running\n" +
                "  • db.properties has correct credentials\n\n" +
                "Details: " + e.getMessage());
            Platform.exit();
            return;
        }

        // Show login screen
        com.peertutor.controller.NavigationManager.initialize(stage);
        com.peertutor.controller.NavigationManager.showLogin();

        stage.show();
    }

    @Override
    public void stop() {
        DBConnection.close();
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
