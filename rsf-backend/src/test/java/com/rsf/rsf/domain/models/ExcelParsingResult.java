package com.rsf.rsf.domain.models;

import com.rsf.rsf.domain.validation.RsfValidationResult;

import java.util.List;
import java.util.Map;

/**
 * Test version of ExcelParsingResult with explicit getters for testing.
 */
public class ExcelParsingResult {
    private final List<HoraireUpdateRecord> horaireUpdates;
    private final List<Map<String, String>> lignesRecords;
    private final RsfValidationResult validationResult;
    private final int totalRowsProcessed;
    private final char parsedLineType;

    public ExcelParsingResult(List<HoraireUpdateRecord> horaireUpdates, 
                          List<Map<String, String>> lignesRecords, 
                          RsfValidationResult validationResult, 
                          int totalRowsProcessed, 
                          char parsedLineType) {
        this.horaireUpdates = horaireUpdates;
        this.lignesRecords = lignesRecords;
        this.validationResult = validationResult;
        this.totalRowsProcessed = totalRowsProcessed;
        this.parsedLineType = parsedLineType;
    }

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

    // Explicit getters
    public List<HoraireUpdateRecord> getHoraireUpdates() {
        return horaireUpdates;
    }

    public List<Map<String, String>> getLignesRecords() {
        return lignesRecords;
    }

    public RsfValidationResult getValidationResult() {
        return validationResult;
    }

    public int getTotalRowsProcessed() {
        return totalRowsProcessed;
    }

    public char getParsedLineType() {
        return parsedLineType;
    }
} 