package com.ktpm.backend.exception;

public class WrongPassWordException extends RuntimeException {
    public WrongPassWordException(String message) {
        super(message);
    }
}
