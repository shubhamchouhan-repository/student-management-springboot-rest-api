package com.example.sms.exception;

/**
 * Thrown when someone tries to approve/reject an enrollment request
 * that has already been processed (not still PENDING).
 */
public class InvalidRequestStateException extends RuntimeException {

    public InvalidRequestStateException(String message) {
        super(message);
    }
}
