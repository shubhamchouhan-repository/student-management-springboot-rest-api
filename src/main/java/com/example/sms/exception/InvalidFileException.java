package com.example.sms.exception;

public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}