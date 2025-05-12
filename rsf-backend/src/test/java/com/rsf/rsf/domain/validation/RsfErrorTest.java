package com.rsf.rsf.domain.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class RsfErrorTest {

    @Test
    void testRsfErrorCreationGeneral() {
        RsfError error = new RsfError(10, "ABC", RsfErrorType.FORMAT_ERROR, "Invalid format", null);
        assertEquals(10, error.getLineNumber());
        assertEquals("ABC", error.getLineContent());
        assertEquals(RsfErrorType.FORMAT_ERROR, error.getErrorType());
        assertEquals("Invalid format", error.getMessage());
        assertNull(error.getField());
    }

    @Test
    void testRsfErrorCreationFieldSpecific() {
        RsfError error = new RsfError(25, "123XYZ", RsfErrorType.DATA_ERROR, "Invalid date", "DATE_SOINS");
        assertEquals(25, error.getLineNumber());
        assertEquals("123XYZ", error.getLineContent());
        assertEquals(RsfErrorType.DATA_ERROR, error.getErrorType());
        assertEquals("Invalid date", error.getMessage());
        assertEquals("DATE_SOINS", error.getField());
    }

    @Test
    void testRsfErrorNullLineContent() {
        RsfError error = new RsfError(5, null, RsfErrorType.STRUCTURAL_ERROR, "Missing line", null);
        assertNull(error.getLineContent(), "Line content should be null if null is passed");
    }

    @Test
    void testRsfErrorToStringGeneral() {
        RsfError error = new RsfError(15, "DEF456", RsfErrorType.SEQUENCE_ERROR, "Sequence mismatch", null);
        String expected = "Error [Line 15, Type: SEQUENCE_ERROR]: Sequence mismatch | Line Content: 'DEF456'";
        assertEquals(expected, error.toString());
    }

    @Test
    void testRsfErrorToStringFieldSpecific() {
        RsfError error = new RsfError(30, "GHI789", RsfErrorType.DEPENDENCY_ERROR, "Missing C line", "N_ENTREE");
        String expected = "Error [Line 30, Type: DEPENDENCY_ERROR, Field: N_ENTREE]: Missing C line | Line Content: 'GHI789'";
        assertEquals(expected, error.toString());
    }

    @Test
    void testRsfErrorToStringNoLineContent() {
        RsfError error = new RsfError(5, null, RsfErrorType.FILE_NAME_ERROR, "Invalid file name pattern", null);
        String expected = "Error [Line 5, Type: FILE_NAME_ERROR]: Invalid file name pattern"; // No line content part
        assertEquals(expected, error.toString());
    }
} 