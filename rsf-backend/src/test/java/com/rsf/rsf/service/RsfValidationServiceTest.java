package com.rsf.rsf.service;

import com.rsf.rsf.domain.validation.RsfError;
import com.rsf.rsf.domain.validation.RsfErrorType;
import com.rsf.rsf.domain.validation.RsfValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RsfValidationServiceTest {

    @Autowired
    private RsfValidationService validationService;

    private Map<Character, List<Map<String, String>>> validRsfData;
    private Map<Character, List<Map<String, String>>> invalidRsfData;

    @BeforeEach
    void setUp() {
        // Initialize test data
        setupValidRsfData();
        setupInvalidRsfData();
    }

    /**
     * Test case: Validate completely valid RSF data
     * Expected: No validation errors
     */
    @Test
    void testValidRsfData() {
        RsfValidationResult result = validationService.validateRsfData(validRsfData);
        
        assertFalse(result.hasErrors(), "Valid RSF data should not have errors");
        assertEquals(0, result.getErrorCount(), "Error count should be 0");
    }
    
    /**
     * Test case: Validate RSF data with missing mandatory fields
     * Expected: DATA_ERROR validation errors
     */
    @Test
    void testMissingMandatoryFields() {
        // Create test data with missing N_IMMATRICULATION_ASSURE in type A line
        Map<Character, List<Map<String, String>>> testData = deepCopyRsfData(validRsfData);
        testData.get('A').get(0).put("N_IMMATRICULATION_ASSURE", "");
        
        RsfValidationResult result = validationService.validateRsfData(testData);
        
        assertTrue(result.hasErrors(), "Missing mandatory field should cause errors");
        assertErrorTypeExists(result, RsfErrorType.DATA_ERROR);
    }
    
    /**
     * Test case: Validate RSF data with invalid date format
     * Expected: DATA_ERROR validation errors
     */
    @Test
    void testInvalidDateFormat() {
        // Create test data with invalid DATE_NAISSANCE
        Map<Character, List<Map<String, String>>> testData = deepCopyRsfData(validRsfData);
        testData.get('A').get(0).put("DATE_NAISSANCE", "20220231"); // Invalid date (Feb 31)
        
        RsfValidationResult result = validationService.validateRsfData(testData);
        
        assertTrue(result.hasErrors(), "Invalid date format should cause errors");
        assertErrorTypeExists(result, RsfErrorType.DATA_ERROR);
    }
    
    /**
     * Test case: Validate RSF data with sequence errors (mismatched identifiers)
     * Expected: SEQUENCE_ERROR validation errors
     */
    @Test
    void testSequenceErrors() {
        // Create test data with mismatched N_IMMATRICULATION_ASSURE between A and B lines
        Map<Character, List<Map<String, String>>> testData = deepCopyRsfData(validRsfData);
        testData.get('B').get(0).put("N_IMMATRICULATION_ASSURE", "different_immatriculation");
        
        RsfValidationResult result = validationService.validateRsfData(testData);
        
        assertTrue(result.hasErrors(), "Mismatched identifiers should cause sequence errors");
        assertErrorTypeExists(result, RsfErrorType.SEQUENCE_ERROR);
    }
    
    /**
     * Test case: Validate RSF data with dependency errors (L line without C line)
     * Expected: DEPENDENCY_ERROR validation errors
     */
    @Test
    void testDependencyErrors() {
        // Create test data with L line but no C line
        Map<Character, List<Map<String, String>>> testData = deepCopyRsfData(validRsfData);
        
        // Remove any C lines
        testData.remove('C');
        
        // Add an L line with same identifiers as A line
        Map<String, String> aLine = testData.get('A').get(0);
        Map<String, String> lLine = new HashMap<>();
        lLine.put("N_FINESS_EPMSI", aLine.get("N_FINESS_EPMSI"));
        lLine.put("N_FINESS_GEOGRAPHIQUE", aLine.get("N_FINESS_GEOGRAPHIQUE"));
        lLine.put("N_IMMATRICULATION_ASSURE", aLine.get("N_IMMATRICULATION_ASSURE"));
        lLine.put("N_ENTREE", aLine.get("N_ENTREE"));
        lLine.put("DATE_ACTE1", "20220101");
        lLine.put("CODE_ACTE1", "ACTE1");
        
        List<Map<String, String>> lLines = new ArrayList<>();
        lLines.add(lLine);
        testData.put('L', lLines);
        
        RsfValidationResult result = validationService.validateRsfData(testData);
        
        assertTrue(result.hasErrors(), "L line without C line should cause dependency errors");
        assertErrorTypeExists(result, RsfErrorType.DEPENDENCY_ERROR);
    }
    
    /**
     * Test case: Validate RSF data with structural errors (missing required lines)
     * Expected: STRUCTURAL validation errors
     */
    @Test
    void testStructuralErrors() {
        // Create test data with A line but no B or C lines
        Map<Character, List<Map<String, String>>> testData = new HashMap<>();
        testData.put('A', validRsfData.get('A'));
        
        RsfValidationResult result = validationService.validateRsfData(testData);
        
        assertTrue(result.hasErrors(), "Missing B/C lines should cause structural errors");
        assertErrorTypeExists(result, RsfErrorType.STRUCTURAL);
    }
    
    /**
     * Test case: Validate RSF data with invalid field length
     * Expected: DATA_ERROR validation errors
     */
    @Test
    void testInvalidFieldLength() {
        // Create test data with too long N_FINESS_EPMSI
        Map<Character, List<Map<String, String>>> testData = deepCopyRsfData(validRsfData);
        testData.get('A').get(0).put("N_FINESS_EPMSI", "1234567890"); // 10 chars, max is 9
        
        RsfValidationResult result = validationService.validateRsfData(testData);
        
        assertTrue(result.hasErrors(), "Invalid field length should cause errors");
        assertErrorTypeExists(result, RsfErrorType.DATA_ERROR);
    }
    
    /**
     * Test case: Validate empty RSF data
     * Expected: STRUCTURAL validation errors
     */
    @Test
    void testEmptyRsfData() {
        Map<Character, List<Map<String, String>>> emptyData = new HashMap<>();
        
        RsfValidationResult result = validationService.validateRsfData(emptyData);
        
        assertTrue(result.hasErrors(), "Empty RSF data should cause errors");
        assertErrorTypeExists(result, RsfErrorType.STRUCTURAL);
    }

    /**
     * Set up valid RSF data for testing
     */
    private void setupValidRsfData() {
        validRsfData = new HashMap<>();
        
        // Create valid A line
        Map<String, String> aLine = new HashMap<>();
        aLine.put("N_FINESS_EPMSI", "123456789");
        aLine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        aLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        aLine.put("DATE_NAISSANCE", "19800101");
        aLine.put("N_ENTREE", "ENTRY001");
        
        List<Map<String, String>> aLines = new ArrayList<>();
        aLines.add(aLine);
        validRsfData.put('A', aLines);
        
        // Create valid B line
        Map<String, String> bLine = new HashMap<>();
        bLine.put("N_FINESS_EPMSI", "123456789");
        bLine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        bLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        bLine.put("N_ENTREE", "ENTRY001");
        bLine.put("DATE_SOINS", "20220101");
        bLine.put("CODE_ACTE", "ACTEB");
        bLine.put("horaire", "0900");
        
        List<Map<String, String>> bLines = new ArrayList<>();
        bLines.add(bLine);
        validRsfData.put('B', bLines);
        
        // Create valid C line
        Map<String, String> cLine = new HashMap<>();
        cLine.put("N_FINESS_EPMSI", "123456789");
        cLine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        cLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        cLine.put("N_ENTREE", "ENTRY001");
        cLine.put("DATE_SOINS", "20220102");
        cLine.put("CODE_ACTE", "ACTEC");
        cLine.put("horaire", "1000");
        
        List<Map<String, String>> cLines = new ArrayList<>();
        cLines.add(cLine);
        validRsfData.put('C', cLines);
    }
    
    /**
     * Set up invalid RSF data for testing
     */
    private void setupInvalidRsfData() {
        invalidRsfData = new HashMap<>();
        
        // Create A line with missing mandatory field
        Map<String, String> aLine = new HashMap<>();
        aLine.put("N_FINESS_EPMSI", "");  // Missing mandatory field
        aLine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        aLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        aLine.put("DATE_NAISSANCE", "19800101");
        aLine.put("N_ENTREE", "ENTRY001");
        
        List<Map<String, String>> aLines = new ArrayList<>();
        aLines.add(aLine);
        invalidRsfData.put('A', aLines);
        
        // Create B line with inconsistent N_ENTREE
        Map<String, String> bLine = new HashMap<>();
        bLine.put("N_FINESS_EPMSI", "123456789");
        bLine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        bLine.put("N_IMMATRICULATION_ASSURE", "1234567890123");
        bLine.put("N_ENTREE", "DIFFERENT_ENTRY");  // Different N_ENTREE causes sequence error
        bLine.put("DATE_SOINS", "20220101");
        bLine.put("CODE_ACTE", "ACTEB");
        
        List<Map<String, String>> bLines = new ArrayList<>();
        bLines.add(bLine);
        invalidRsfData.put('B', bLines);
    }
    
    /**
     * Helper method to create a deep copy of RSF data
     */
    private Map<Character, List<Map<String, String>>> deepCopyRsfData(Map<Character, List<Map<String, String>>> original) {
        Map<Character, List<Map<String, String>>> copy = new HashMap<>();
        
        for (Map.Entry<Character, List<Map<String, String>>> entry : original.entrySet()) {
            Character key = entry.getKey();
            List<Map<String, String>> linesCopy = new ArrayList<>();
            
            for (Map<String, String> line : entry.getValue()) {
                Map<String, String> lineCopy = new HashMap<>(line);
                linesCopy.add(lineCopy);
            }
            
            copy.put(key, linesCopy);
        }
        
        return copy;
    }
    
    /**
     * Helper method to assert that a specific error type exists in the validation result
     */
    private void assertErrorTypeExists(RsfValidationResult result, RsfErrorType errorType) {
        boolean found = false;
        for (RsfError error : result.getErrors()) {
            if (error.getErrorType() == errorType) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Expected to find error of type " + errorType);
    }
} 