package com.peertutor.exception;

/**
 * Thrown when a JDBC database operation fails unexpectedly.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
