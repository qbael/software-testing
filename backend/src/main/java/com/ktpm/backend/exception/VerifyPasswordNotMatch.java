package com.ktpm.backend.exception;

public class VerifyPasswordNotMatch extends RuntimeException {
    public VerifyPasswordNotMatch(String message) {
        super(message);
    }
}
