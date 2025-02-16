package com.exception;

/**
 * Custom exception for authentication failures.
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
