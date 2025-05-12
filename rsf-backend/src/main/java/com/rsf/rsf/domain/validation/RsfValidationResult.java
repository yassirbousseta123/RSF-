package com.rsf.rsf.domain.validation;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains results of RSF validation, including all errors found.
 */
@Getter
public class RsfValidationResult {

    private final List<RsfError> errors;
    private final long totalLinesProcessed;
    private final Map<Character, Long> linesPerType;
    private final String firstDateSoins; // YYYYMMDD format
    private final String lastDateSoins;  // YYYYMMDD format
    private final BigDecimal totalHonorairesRemboursableAm;
    private final BigDecimal totalRemboursableAmo;
    private final String fileName;

    /**
     * Creates a new validation result with the given errors.
     * Use factory methods for clearer intent.
     *
     * @param errors The validation errors found
     */
    public RsfValidationResult(List<RsfError> errors) {
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.totalLinesProcessed = 0;
        this.linesPerType = null;
        this.firstDateSoins = null;
        this.lastDateSoins = null;
        this.totalHonorairesRemboursableAm = null;
        this.totalRemboursableAmo = null;
        this.fileName = null; // Filename not applicable when creating from errors only
    }

    /**
     * Creates a new validation result for a specific file.
     *
     * @param fileName The name of the file being validated. Cannot be null or blank.
     * @throws IllegalArgumentException if fileName is null or blank.
     */
    public RsfValidationResult(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or blank.");
        }
        this.errors = new ArrayList<>();
        this.totalLinesProcessed = 0;
        this.linesPerType = null;
        this.firstDateSoins = null;
        this.lastDateSoins = null;
        this.totalHonorairesRemboursableAm = null;
        this.totalRemboursableAmo = null;
        this.fileName = fileName;
    }

    /**
     * Creates a new empty validation result with no errors and no file name.
     * Consider using the constructor with a filename if applicable.
     */
    public RsfValidationResult() {
        this.errors = new ArrayList<>();
        this.totalLinesProcessed = 0;
        this.linesPerType = null;
        this.firstDateSoins = null;
        this.lastDateSoins = null;
        this.totalHonorairesRemboursableAm = null;
        this.totalRemboursableAmo = null;
        this.fileName = null;
    }

    /**
     * Adds an error to the result.
     *
     * @param error The error to add
     */
    public void addError(RsfError error) {
        if (error != null) {
            this.errors.add(error);
        }
    }

    /**
     * Adds all errors from another validation result.
     *
     * @param result The result whose errors to add
     */
    public void addErrors(RsfValidationResult result) {
        if (result != null && result.hasErrors()) {
            this.errors.addAll(result.getErrors());
        }
    }

    /**
     * Gets a map of errors grouped by error type.
     * 
     * @return Map with error types as keys and lists of corresponding errors as values
     */
    public Map<RsfErrorType, List<RsfError>> getErrorsByType() {
        return errors.stream()
            .collect(Collectors.groupingBy(RsfError::getErrorType));
    }

    /**
     * Checks if there are any errors.
     *
     * @return true if there are errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Gets the total number of errors.
     *
     * @return Number of errors
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Gets the errors as an unmodifiable list.
     *
     * @return Unmodifiable list of errors
     */
    public List<RsfError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Gets a summary of the validation result.
     *
     * @return Summary string
     */
    public String getSummary() {
        if (!hasErrors()) {
            return "Validation successful: No errors found";
        }
        
        Map<RsfErrorType, Long> errorTypeCounts = errors.stream()
            .collect(Collectors.groupingBy(RsfError::getErrorType, Collectors.counting()));
            
        StringBuilder sb = new StringBuilder()
            .append("Validation failed: ")
            .append(errors.size())
            .append(" errors found (");
            
        boolean first = true;
        for (Map.Entry<RsfErrorType, Long> entry : errorTypeCounts.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getValue()).append(" ").append(entry.getKey());
            first = false;
        }
        
        sb.append(")");
        return sb.toString();
    }

    /**
     * Gets the name of the file that was validated.
     * @return The file name.
     */
    public String getFileName() {
        return fileName;
    }

    public long getTotalLinesProcessed() {
        return totalLinesProcessed;
    }

    public Map<Character, Long> getLinesPerType() {
        return linesPerType;
    }

    public long getTotalErrors() {
        return errors.size();
    }

    public String getFirstDateSoins() {
        return firstDateSoins;
    }

    public String getLastDateSoins() {
        return lastDateSoins;
    }

    public BigDecimal getTotalHonorairesRemboursableAm() {
        return totalHonorairesRemboursableAm;
    }

    public BigDecimal getTotalRemboursableAmo() {
        return totalRemboursableAmo;
    }

    @Override
    public String toString() {
        if (!hasErrors()) {
            return "Validation successful for file: " + fileName;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Validation failed for file: ").append(fileName).append(" with ").append(errors.size()).append(" error(s):\n");
        int count = 1;
        for (RsfError error : errors) {
            sb.append("  ").append(count++).append(". ").append(error.toString()).append("\n");
        }
        return sb.toString();
    }
} 