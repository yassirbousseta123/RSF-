package com.rsf.rsf.domain.validation;

import lombok.Data;

/**
 * Represents a validation rule for a specific field.
 */
@Data
public class FieldRule {
    private boolean mandatory;
    private FieldType type;
    private Integer minLength;
    private Integer maxLength;
    private String regex;
    private String description;
    
    /**
     * Types of fields in RSF data.
     */
    public enum FieldType {
        TEXT,
        NUMERIC,
        DATE,
        BOOLEAN,
        DECIMAL
    }
} 