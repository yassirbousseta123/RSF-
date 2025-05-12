package com.rsf.rsf.domain.validation;

import com.rsf.rsf.service.RsfValidationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates field-level rules for all RSF lines.
 * Checks each field against its defined rules (mandatory, type, length, format).
 */
public class FieldValidationRule implements RsfValidationRule {
    
    private final RsfValidationService validationService;
    
    /**
     * Constructor requiring the validation service.
     * 
     * @param validationService The service used to validate individual fields.
     */
    public FieldValidationRule(RsfValidationService validationService) {
        if (validationService == null) {
            throw new IllegalArgumentException("RsfValidationService cannot be null");
        }
        this.validationService = validationService;
    }
    
    @Override
    public RsfValidationResult validate(Map<Character, List<Map<String, String>>> rsfData) {
        RsfValidationResult result = new RsfValidationResult();
        
        // Validate each line type
        for (Map.Entry<Character, List<Map<String, String>>> entry : rsfData.entrySet()) {
            char lineType = entry.getKey();
            List<Map<String, String>> lines = entry.getValue();
            
            // Validate each line
            for (int i = 0; i < lines.size(); i++) {
                Map<String, String> line = lines.get(i);
                int lineNumber = i + 1; // 1-based line number
                
                // Validate each field
                for (Map.Entry<String, String> field : line.entrySet()) {
                    String fieldName = field.getKey();
                    String value = field.getValue();
                    
                    // Apply validation rules
                    List<String> fieldErrors = validationService.validateField(lineType, fieldName, value);
                    
                    // Add errors if any
                    for (String errorMessage : fieldErrors) {
                        result.addError(new RsfError(
                            lineNumber,
                            fieldName,
                            RsfErrorType.DATA_ERROR,
                            errorMessage
                        ));
                    }
                }
            }
        }
        
        return result;
    }
    
    @Override
    public RsfRuleType getRuleType() {
        return RsfRuleType.FIELD_VALIDATION;
    }
} 