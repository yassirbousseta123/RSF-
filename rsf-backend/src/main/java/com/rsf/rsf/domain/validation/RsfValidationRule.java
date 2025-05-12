package com.rsf.rsf.domain.validation;

import java.util.List;
import java.util.Map;

/**
 * Interface for RSF validation rules.
 * Each rule validates a specific aspect of RSF data and produces errors if validation fails.
 */
public interface RsfValidationRule {
    /**
     * Validates the RSF data against this rule.
     * 
     * @param rsfData Map containing RSF data by line type (e.g., 'A', 'B', 'C')
     * @return Validation result containing any errors found
     */
    RsfValidationResult validate(Map<Character, List<Map<String, String>>> rsfData);
    
    /**
     * Gets the type of this validation rule.
     * 
     * @return The rule type
     */
    RsfRuleType getRuleType();
} 