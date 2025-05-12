package com.rsf.rsf.exceptions;

public class RsfParsingException extends RuntimeException {
    public RsfParsingException(String message) {
        super(message);
    }

    public RsfParsingException(String message, Throwable cause) {
        super(message, cause);
    }
} 