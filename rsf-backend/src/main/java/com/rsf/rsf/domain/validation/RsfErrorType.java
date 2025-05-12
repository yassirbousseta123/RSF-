package com.rsf.rsf.domain.validation;

/**
 * Enum representing types of RSF validation errors.
 */
public enum RsfErrorType {
    DATA_ERROR,       // Field-level data errors (missing data, invalid format)
    STRUCTURAL,       // File structure errors (missing required lines)
    SEQUENCE_ERROR,   // Sequence mismatch errors (identifier discrepancies)
    DEPENDENCY_ERROR, // Inter-line dependency violations
    FILE_NAME_ERROR,  // Issues with the ZIP or RSF file name
    STRUCTURAL_ERROR, // Legacy name for STRUCTURAL
    FORMAT_ERROR,     // Issues with line format or invalid line types
    SYSTEM_ERROR      // Added system error type
} 