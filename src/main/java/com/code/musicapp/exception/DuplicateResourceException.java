package com.code.musicapp.exception;

// Nem ra khi username/email da ton tai luc dang ky
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
