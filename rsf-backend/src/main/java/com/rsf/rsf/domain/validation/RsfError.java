package com.rsf.rsf.domain.validation;

/**
 * Represents a validation error in RSF data.
 */
public class RsfError {
    private int lineNumber;         // 1-based line number (0 for file-level errors)
    private String field;           // Field with the error (null for line/file level errors)
    private RsfErrorType errorType; // Category of error
    private String message;         // Descriptive error message
    private String lineContent;     // Content of the line with the error
    
    /**
     * Default constructor.
     */
    public RsfError() {
    }
    
    /**
     * Constructor with 4 parameters.
     * 
     * @param lineNumber Line number where error occurred
     * @param field Field with the error
     * @param errorType Category of error
     * @param message Descriptive error message
     */
    public RsfError(int lineNumber, String field, RsfErrorType errorType, String message) {
        this.lineNumber = lineNumber;
        this.field = field;
        this.errorType = errorType;
        this.message = message;
        this.lineContent = "";
    }

    /**
     * Constructor with 5 parameters.
     * 
     * @param lineNumber Line number where error occurred
     * @param lineContent Content of the line
     * @param errorType Category of error
     * @param message Descriptive error message
     * @param field Field with the error
     */
    public RsfError(int lineNumber, String lineContent, RsfErrorType errorType, String message, String field) {
        this.lineNumber = lineNumber;
        this.field = field;
        this.errorType = errorType;
        this.message = message;
        this.lineContent = lineContent;
    }
    
    // Getters and setters

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }

    public RsfErrorType getErrorType() {
        return errorType;
    }
    
    public void setErrorType(RsfErrorType errorType) {
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getLineContent() {
        return lineContent;
    }
    
    public void setLineContent(String lineContent) {
        this.lineContent = lineContent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Error [Line ").append(lineNumber);
        sb.append(", Type: ").append(errorType);
        if (field != null) {
            sb.append(", Field: ").append(field);
        }
        sb.append("]: ").append(message);
        if (lineContent != null && !lineContent.isEmpty()) {
            sb.append(" | Line Content: '").append(lineContent).append("'");
        }
        return sb.toString();
    }
} 