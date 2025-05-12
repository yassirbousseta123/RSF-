package com.rsf.rsf.domain.validation;

/**
 * Enum representing types of RSF validation rules.
 */
public enum RsfRuleType {
    FIELD_VALIDATION,     // Field-level validations (mandatory, type, length, format)
    STRUCTURAL,           // File structure validations
    DEPENDENCY,           // Inter-line dependency validations
    SEQUENCE              // Sequence validations
} 