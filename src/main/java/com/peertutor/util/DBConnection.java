package com.peertutor.util;

import com.peertutor.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a single shared database connection loaded from db.properties.
 * Uses a simple singleton pattern suitable for a desktop application.
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private static Connection connection;
    private static Properties props;

    // Load properties once at class initialisation
    static {
        props = new Properties();
        try (InputStream in = DBConnection.class
                .getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new DatabaseException(
                    "db.properties not found on classpath. " +
                    "Please place it in src/main/resources/.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new DatabaseException("Failed to load db.properties", e);
        }
    }

    private DBConnection() {}

    /**
     * Returns the shared connection, creating it if needed or if it was closed.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(props.getProperty("db.driver"));
                connection = DriverManager.getConnection(
                        props.getProperty("db.url"),
                        props.getProperty("db.username"),
                        props.getProperty("db.password"));
                LOGGER.info("Database connection established.");
            }
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL JDBC driver not found.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot connect to database", e);
            throw new DatabaseException(
                "Cannot connect to the database. " +
                "Please check db.properties and ensure MySQL is running.\n" +
                e.getMessage(), e);
        }
        return connection;
    }

    /** Closes the shared connection (call on application shutdown). */
    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection", e);
            }
        }
    }
}
