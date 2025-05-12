package com.rsf.rsf.domain.validation;

import java.util.*;

/**
 * Validates inter-line dependencies in RSF data.
 * Ensures that dependent line types have their prerequisites.
 */
public class DependencyValidationRule implements RsfValidationRule {
    
    @Override
    public RsfValidationResult validate(Map<Character, List<Map<String, String>>> rsfData) {
        RsfValidationResult result = new RsfValidationResult();
        
        // Dependencies to check
        Set<Character> dependentTypes = Set.of('L', 'M'); // L and M lines require C lines
        
        // Early return if no dependent lines
        boolean hasAnyDependentLines = dependentTypes.stream()
            .anyMatch(rsfData::containsKey);
            
        if (!hasAnyDependentLines) {
            return result; // No errors
        }
        
        // Check if required C lines exist in the data
        if (!rsfData.containsKey('C')) {
            for (char dependentType : dependentTypes) {
                if (rsfData.containsKey(dependentType)) {
                    RsfError error = new RsfError(
                        0, 
                        null, 
                        RsfErrorType.DEPENDENCY_ERROR,
                        "Type " + dependentType + " lines exist without any type C lines"
                    );
                    result.addError(error);
                }
            }
            return result;
        }
        
        // Group lines by N_ENTREE for dependency checks within sequences
        Map<String, Set<Character>> lineTypesByEntree = new HashMap<>();
        
        // Collect all unique N_ENTREE values and their line types
        for (Map.Entry<Character, List<Map<String, String>>> entry : rsfData.entrySet()) {
            char lineType = entry.getKey();
            List<Map<String, String>> lines = entry.getValue();
            
            for (Map<String, String> line : lines) {
                String nEntree = line.get("N_ENTREE");
                if (nEntree != null && !nEntree.isEmpty()) {
                    lineTypesByEntree
                        .computeIfAbsent(nEntree, k -> new HashSet<>())
                        .add(lineType);
                }
            }
        }
        
        // Check dependencies for each sequence
        for (Map.Entry<String, Set<Character>> entry : lineTypesByEntree.entrySet()) {
            String nEntree = entry.getKey();
            Set<Character> lineTypes = entry.getValue();
            
            // For each dependent type, check if required type exists in the sequence
            for (char dependentType : dependentTypes) {
                if (lineTypes.contains(dependentType) && !lineTypes.contains('C')) {
                    RsfError error = new RsfError(
                        0,
                        "N_ENTREE",
                        RsfErrorType.DEPENDENCY_ERROR,
                        "Sequence " + nEntree + " has type " + dependentType + 
                        " lines without required type C lines"
                    );
                    result.addError(error);
                }
            }
        }
        
        return result;
    }
    
    @Override
    public RsfRuleType getRuleType() {
        return RsfRuleType.DEPENDENCY;
    }
} 