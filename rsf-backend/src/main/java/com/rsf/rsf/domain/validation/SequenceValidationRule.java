package com.rsf.rsf.domain.validation;

import java.util.*;

/**
 * Validates sequence integrity in RSF data.
 * Ensures that lines within a sequence share the same identifiers and follow proper structure.
 */
public class SequenceValidationRule implements RsfValidationRule {
    
    @Override
    public RsfValidationResult validate(Map<Character, List<Map<String, String>>> rsfData) {
        RsfValidationResult result = new RsfValidationResult();
        
        if (!rsfData.containsKey('A')) {
            result.addError(new RsfError(0, null, RsfErrorType.STRUCTURAL, "Missing type A lines"));
            return result;
        }
        
        // Group lines by N_ENTREE to identify sequences
        Map<String, Map<Character, List<Map<String, String>>>> sequences = new HashMap<>();
        
        // First, collect all A lines (each represents a sequence start)
        List<Map<String, String>> aLines = rsfData.get('A');
        for (int i = 0; i < aLines.size(); i++) {
            Map<String, String> aLine = aLines.get(i);
            String nEntree = aLine.get("N_ENTREE");
            
            if (nEntree == null || nEntree.isEmpty()) {
                result.addError(new RsfError(i + 1, "N_ENTREE", RsfErrorType.DATA_ERROR, 
                    "Missing N_ENTREE in type A line"));
                continue;
            }
            
            // Initialize sequence data structure
            Map<Character, List<Map<String, String>>> sequenceData = new HashMap<>();
            sequenceData.put('A', new ArrayList<>(Collections.singletonList(aLine)));
            sequences.put(nEntree, sequenceData);
        }
        
        // Process other line types and associate with sequences
        for (char lineType : rsfData.keySet()) {
            if (lineType == 'A') continue; // Already processed A lines
            
            List<Map<String, String>> lines = rsfData.get(lineType);
            for (int i = 0; i < lines.size(); i++) {
                Map<String, String> line = lines.get(i);
                String nEntree = line.get("N_ENTREE");
                
                if (nEntree == null || nEntree.isEmpty()) {
                    result.addError(new RsfError(i + 1, "N_ENTREE", RsfErrorType.DATA_ERROR, 
                        "Missing N_ENTREE in type " + lineType + " line"));
                    continue;
                }
                
                // Check if this line's N_ENTREE matches any sequence
                if (!sequences.containsKey(nEntree)) {
                    result.addError(new RsfError(i + 1, "N_ENTREE", RsfErrorType.SEQUENCE_ERROR, 
                        "Type " + lineType + " line refers to non-existent N_ENTREE: " + nEntree));
                    continue;
                }
                
                // Add line to its sequence data
                Map<Character, List<Map<String, String>>> sequenceData = sequences.get(nEntree);
                sequenceData.computeIfAbsent(lineType, k -> new ArrayList<>()).add(line);
            }
        }
        
        // Now validate each sequence
        for (Map.Entry<String, Map<Character, List<Map<String, String>>>> entry : sequences.entrySet()) {
            String nEntree = entry.getKey();
            Map<Character, List<Map<String, String>>> sequenceData = entry.getValue();
            
            validateSequence(nEntree, sequenceData, result);
        }
        
        return result;
    }
    
    /**
     * Validates a single sequence of related lines.
     */
    private void validateSequence(String nEntree, Map<Character, List<Map<String, String>>> sequenceData, 
                                  RsfValidationResult result) {
        // Get the A line (parent) for this sequence
        List<Map<String, String>> aLines = sequenceData.get('A');
        if (aLines == null || aLines.isEmpty()) {
            // Should never happen due to how we build sequences
            return;
        }
        
        Map<String, String> parentLine = aLines.get(0);
        String nImmatriculation = parentLine.get("N_IMMATRICULATION_ASSURE");
        
        // Check that sequence has at least one B or C line
        boolean hasBOrC = sequenceData.containsKey('B') || sequenceData.containsKey('C');
        if (!hasBOrC) {
            result.addError(new RsfError(0, null, RsfErrorType.STRUCTURAL, 
                "Sequence with N_ENTREE " + nEntree + " has no B or C lines"));
        }
        
        // Validate each line type in the sequence
        for (char lineType : sequenceData.keySet()) {
            if (lineType == 'A') continue; // Skip parent line
            
            List<Map<String, String>> lines = sequenceData.get(lineType);
            for (Map<String, String> line : lines) {
                // Check consistent N_IMMATRICULATION_ASSURE
                String lineImmatriculation = line.get("N_IMMATRICULATION_ASSURE");
                if (!Objects.equals(nImmatriculation, lineImmatriculation)) {
                    result.addError(new RsfError(0, "N_IMMATRICULATION_ASSURE", RsfErrorType.SEQUENCE_ERROR, 
                        "Inconsistent N_IMMATRICULATION_ASSURE in sequence " + nEntree + 
                        ": parent=" + nImmatriculation + ", line=" + lineImmatriculation));
                }
                
                // For L and M lines, ensure C lines exist in the sequence
                if ((lineType == 'L' || lineType == 'M') && !sequenceData.containsKey('C')) {
                    result.addError(new RsfError(0, null, RsfErrorType.DEPENDENCY_ERROR, 
                        "Type " + lineType + " line exists in sequence " + nEntree + 
                        " without any C line"));
                }
            }
        }
    }
    
    @Override
    public RsfRuleType getRuleType() {
        return RsfRuleType.SEQUENCE;
    }
} 