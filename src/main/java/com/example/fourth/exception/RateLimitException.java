package com.example.fourth.exception;

public class RateLimitException extends ExternalApiException {

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
