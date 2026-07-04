package com.code.musicapp.exception;

// Nem ra khi khong tim thay resource theo id (song, category, user...)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}