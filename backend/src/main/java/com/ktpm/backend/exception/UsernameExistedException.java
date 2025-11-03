package com.ktpm.backend.exception;

public class UsernameExistedException extends RuntimeException {
    public UsernameExistedException(String message) {
        super(message);
    }
}
