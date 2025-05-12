package com.rsf.rsf.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class RsfValidationException extends RuntimeException {

    private final List<String> validationErrors;

    public RsfValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public RsfValidationException(List<String> validationErrors) {
        super("RSF file validation failed with " + validationErrors.size() + " errors.");
        this.validationErrors = validationErrors;
    }
} 