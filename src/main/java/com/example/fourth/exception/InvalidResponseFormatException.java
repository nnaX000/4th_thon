package com.example.fourth.exception;

public class InvalidResponseFormatException extends ExternalApiException {

    public InvalidResponseFormatException(String message) {
        super(message);
    }

    public InvalidResponseFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}