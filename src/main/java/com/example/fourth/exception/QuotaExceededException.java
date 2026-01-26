package com.example.fourth.exception;

public class QuotaExceededException extends ExternalApiException {

    public QuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
