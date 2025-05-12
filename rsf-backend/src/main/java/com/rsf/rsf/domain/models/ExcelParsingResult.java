package com.rsf.rsf.domain.models;

import com.rsf.rsf.domain.validation.RsfValidationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Holds the results of parsing an Excel file (either HORAIRE or LIGNES).
 */
@RequiredArgsConstructor
@Getter
public class ExcelParsingResult {
    private final List<HoraireUpdateRecord> horaireUpdates; // Populated for HORAIRE files
    private final List<Map<String, String>> lignesRecords; // Populated for LIGNES files
    private final RsfValidationResult validationResult; // Contains errors found during parsing
    private final int totalRowsProcessed;
    private final char parsedLineType; // Relevant for LIGNES files ('A', 'B', etc.) or '\0' for HORAIRE

    // Static factory for HORAIRE results
    public static ExcelParsingResult horaireResult(List<HoraireUpdateRecord> updates, RsfValidationResult validationResult, int totalRows) {
        return new ExcelParsingResult(updates, null, validationResult, totalRows, '\0');
    }

    // Static factory for LIGNES results
    public static ExcelParsingResult lignesResult(List<Map<String, String>> records, RsfValidationResult validationResult, int totalRows, char lineType) {
        return new ExcelParsingResult(null, records, validationResult, totalRows, lineType);
    }

    // Static factory for error results (e.g., file not found, bad headers)
    public static ExcelParsingResult errorResult(RsfValidationResult validationResult) {
        return new ExcelParsingResult(null, null, validationResult, 0, '\0');
    }
} 