package com.rsf.rsf.service;

import com.rsf.rsf.domain.validation.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Direct tests for validation rules without Spring context.
 * These tests validate individual rule behavior in isolation.
 */
public class RsfValidationTest {

    /**
     * Test dependency validation rule - L or M lines should have preceding C lines.
     */
    @Test
    void testDependencyValidation() {
        // Setup data with L line but no C line
        Map<Character, List<Map<String, String>>> rsfData = new HashMap<>();
        
        // A line
        Map<String, String> aLine = new HashMap<>();
        aLine.put("N_ENTREE", "ENTRY001");
        aLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        List<Map<String, String>> aLines = new ArrayList<>();
        aLines.add(aLine);
        rsfData.put('A', aLines);
        
        // B line
        Map<String, String> bLine = new HashMap<>();
        bLine.put("N_ENTREE", "ENTRY001");
        bLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        List<Map<String, String>> bLines = new ArrayList<>();
        bLines.add(bLine);
        rsfData.put('B', bLines);
        
        // L line (without required C line)
        Map<String, String> lLine = new HashMap<>();
        lLine.put("N_ENTREE", "ENTRY001");
        lLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        List<Map<String, String>> lLines = new ArrayList<>();
        lLines.add(lLine);
        rsfData.put('L', lLines);
        
        // Execute validation
        DependencyValidationRule rule = new DependencyValidationRule();
        RsfValidationResult result = rule.validate(rsfData);
        
        // Verify results
        assertTrue(result.hasErrors(), "Should have detected L line with no C line");
        assertEquals(1, result.getErrorCount(), "Should have one error");
        assertEquals(RsfErrorType.DEPENDENCY_ERROR, result.getErrors().get(0).getErrorType());
    }
    
    /**
     * Test sequence validation rule - lines should have consistent identifiers.
     */
    @Test
    void testSequenceValidation() {
        // Setup data with inconsistent identifiers
        Map<Character, List<Map<String, String>>> rsfData = new HashMap<>();
        
        // A line
        Map<String, String> aLine = new HashMap<>();
        aLine.put("N_ENTREE", "ENTRY001");
        aLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        List<Map<String, String>> aLines = new ArrayList<>();
        aLines.add(aLine);
        rsfData.put('A', aLines);
        
        // B line with different N_IMMATRICULATION_ASSURE
        Map<String, String> bLine = new HashMap<>();
        bLine.put("N_ENTREE", "ENTRY001");
        bLine.put("N_IMMATRICULATION_ASSURE", "9876543210987"); // Different from A line
        List<Map<String, String>> bLines = new ArrayList<>();
        bLines.add(bLine);
        rsfData.put('B', bLines);
        
        // Execute validation
        SequenceValidationRule rule = new SequenceValidationRule();
        RsfValidationResult result = rule.validate(rsfData);
        
        // Verify results
        assertTrue(result.hasErrors(), "Should have detected inconsistent identifiers");
        assertEquals(RsfErrorType.SEQUENCE_ERROR, result.getErrors().get(0).getErrorType());
    }
    
    /**
     * Test structural validation - each sequence should have B or C lines.
     */
    @Test
    void testStructuralValidation() {
        // Setup data with A line but no B or C lines
        Map<Character, List<Map<String, String>>> rsfData = new HashMap<>();
        
        // A line only
        Map<String, String> aLine = new HashMap<>();
        aLine.put("N_ENTREE", "ENTRY001");
        aLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        List<Map<String, String>> aLines = new ArrayList<>();
        aLines.add(aLine);
        rsfData.put('A', aLines);
        
        // Execute validation through sequence rule which also checks structure
        SequenceValidationRule rule = new SequenceValidationRule();
        RsfValidationResult result = rule.validate(rsfData);
        
        // Verify results
        assertTrue(result.hasErrors(), "Should have detected missing B/C lines");
        boolean hasStructuralError = result.getErrors().stream()
            .anyMatch(e -> e.getErrorType() == RsfErrorType.STRUCTURAL);
        assertTrue(hasStructuralError, "Should have a STRUCTURAL error type");
    }
    
    /**
     * Test field validation rule with missing mandatory field.
     */
    @Test
    void testFieldValidationWithMissingMandatoryField() {
        // Create a validation service for testing field validation
        RsfValidationService service = createValidationService();
        
        // Create a field validation rule
        FieldValidationRule rule = new FieldValidationRule(service);
        
        // Setup test data with missing mandatory field
        Map<Character, List<Map<String, String>>> rsfData = new HashMap<>();
        
        // A line with missing N_FINESS_EPMSI (mandatory)
        Map<String, String> aLine = new HashMap<>();
        aLine.put("N_FINESS_EPMSI", "");  // Empty mandatory field
        aLine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        aLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        aLine.put("DATE_NAISSANCE", "19800101");
        aLine.put("N_ENTREE", "ENTRY001");
        
        List<Map<String, String>> aLines = new ArrayList<>();
        aLines.add(aLine);
        rsfData.put('A', aLines);
        
        // Execute validation
        RsfValidationResult result = rule.validate(rsfData);
        
        // Verify results
        assertTrue(result.hasErrors(), "Should have detected missing mandatory field");
        assertEquals(RsfErrorType.DATA_ERROR, result.getErrors().get(0).getErrorType());
    }
    
    /**
     * Create a validation service instance for testing.
     */
    private RsfValidationService createValidationService() {
        RsfValidationService service = new RsfValidationService();
        service.initialize(); // Initialize the service using the correct method name
        return service;
    }
} 