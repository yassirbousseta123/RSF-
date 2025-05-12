package com.rsf.rsf.domain.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RsfValidationResultTest {

    private RsfValidationResult validationResult;
    private final String testFileName = "test_file.rsf";

    @BeforeEach
    void setUp() {
        validationResult = new RsfValidationResult(testFileName);
    }

    @Test
    void testValidationResultCreationValidName() {
        assertEquals(testFileName, validationResult.getFileName());
        assertFalse(validationResult.hasErrors());
        assertTrue(validationResult.getErrors().isEmpty());
    }

    @Test
    void testValidationResultCreationWithErrors() {
        List<RsfError> errors = new ArrayList<>();
        errors.add(new RsfError(1, "LINE1", RsfErrorType.FORMAT_ERROR, "Msg1"));
        RsfValidationResult result = new RsfValidationResult((List<RsfError>) errors);
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertNull(result.getFileName(), "Filename should be null when created from errors list");
    }

    @Test
    void testValidationResultCreationNullName() {
        assertThrows(IllegalArgumentException.class, () -> new RsfValidationResult((String) null), 
            "Constructor should throw IllegalArgumentException for null filename");
    }

    @Test
    void testValidationResultCreationEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> new RsfValidationResult(""),
            "Constructor should throw IllegalArgumentException for empty filename");
    }

     @Test
    void testValidationResultCreationBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new RsfValidationResult("   "),
            "Constructor should throw IllegalArgumentException for blank filename");
    }

    @Test
    void testAddError() {
        RsfError error1 = new RsfError(1, "LINE1", RsfErrorType.FORMAT_ERROR, "Msg1");
        validationResult.addError(error1);
        assertTrue(validationResult.hasErrors());
        assertEquals(1, validationResult.getErrors().size());
        assertEquals(error1, validationResult.getErrors().get(0));

        RsfError error2 = new RsfError(2, "LINE2", RsfErrorType.DATA_ERROR, "Msg2", "FieldX");
        validationResult.addError(error2);
        assertEquals(2, validationResult.getErrors().size());
        assertTrue(validationResult.getErrors().contains(error1));
        assertTrue(validationResult.getErrors().contains(error2));
    }

    @Test
    void testAddNullError() {
        // Test adding null error - should not throw, just ignore
        int currentSize = validationResult.getErrors().size();
        validationResult.addError(null);
        assertEquals(currentSize, validationResult.getErrors().size());
    }

    @Test
    void testGetErrorsImmutability() {
        RsfError error1 = new RsfError(1, "LINE1", RsfErrorType.FORMAT_ERROR, "Msg1");
        validationResult.addError(error1);
        List<RsfError> errors = validationResult.getErrors();

        // Attempt to modify the returned list
        RsfError error2 = new RsfError(2, "LINE2", RsfErrorType.DATA_ERROR, "Msg2");
        assertThrows(UnsupportedOperationException.class, () -> errors.add(error2));
        assertThrows(UnsupportedOperationException.class, () -> errors.remove(0));
        assertThrows(UnsupportedOperationException.class, errors::clear);

        assertEquals(1, validationResult.getErrors().size(), "Original list should remain unchanged");
    }

    @Test
    void testToStringSuccess() {
        String expected = "Validation successful for file: " + testFileName;
        assertEquals(expected, validationResult.toString());
    }

    @Test
    void testToStringFailure() {
        RsfError error1 = new RsfError(10, "AAA", RsfErrorType.FORMAT_ERROR, "Bad format", null);
        RsfError error2 = new RsfError(25, "BBB", RsfErrorType.DATA_ERROR, "Bad data", "FIELD_Y");
        validationResult.addError(error1);
        validationResult.addError(error2);

        String expected = "Validation failed for file: " + testFileName + " with 2 error(s):\n"
                        + "  1. Error [Line 10, Type: FORMAT_ERROR]: Bad format | Line Content: 'AAA'\n"
                        + "  2. Error [Line 25, Type: DATA_ERROR, Field: FIELD_Y]: Bad data | Line Content: 'BBB'\n";
        assertEquals(expected, validationResult.toString());
    }
} 