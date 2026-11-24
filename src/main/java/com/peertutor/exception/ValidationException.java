package com.peertutor.exception;

/**
 * Thrown when user-supplied input fails business-rule validation.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
