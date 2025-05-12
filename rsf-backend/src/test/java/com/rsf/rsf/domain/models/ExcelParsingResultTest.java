package com.rsf.rsf.domain.models;

import com.rsf.rsf.domain.validation.RsfError;
import com.rsf.rsf.domain.validation.RsfErrorType;
import com.rsf.rsf.domain.validation.RsfValidationResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelParsingResultTest {

    @Test
    public void testHoraireResultFactory() {
        // Create test data
        List<HoraireUpdateRecord> updates = new ArrayList<>();
        updates.add(new HoraireUpdateRecord(1, "12345678901234567890123456789012", "20230101", "20230501", "ABCD1", "09:30"));
        RsfValidationResult validationResult = new RsfValidationResult("test.xlsx");
        
        // Use factory method
        ExcelParsingResult result = ExcelParsingResult.horaireResult(updates, validationResult, 1);
        
        // Verify result
        assertNotNull(result);
        assertSame(updates, result.getHoraireUpdates());
        assertNull(result.getLignesRecords());
        assertSame(validationResult, result.getValidationResult());
        assertEquals(1, result.getTotalRowsProcessed());
        assertEquals('\0', result.getParsedLineType());
    }

    @Test
    public void testLignesResultFactory() {
        // Create test data
        List<Map<String, String>> records = new ArrayList<>();
        Map<String, String> record = new HashMap<>();
        record.put("TYPE_ENREGISTREMENT", "A");
        record.put("N_FINESS_EPMSI", "123456789");
        records.add(record);
        RsfValidationResult validationResult = new RsfValidationResult("test.xlsx");
        
        // Use factory method
        ExcelParsingResult result = ExcelParsingResult.lignesResult(records, validationResult, 1, 'A');
        
        // Verify result
        assertNotNull(result);
        assertNull(result.getHoraireUpdates());
        assertSame(records, result.getLignesRecords());
        assertSame(validationResult, result.getValidationResult());
        assertEquals(1, result.getTotalRowsProcessed());
        assertEquals('A', result.getParsedLineType());
    }

    @Test
    public void testErrorResultFactory() {
        // Create validation result with errors
        RsfValidationResult validationResult = new RsfValidationResult("test.xlsx");
        validationResult.addError(new RsfError(0, "", RsfErrorType.DATA_ERROR, "Test error"));
        
        // Use factory method
        ExcelParsingResult result = ExcelParsingResult.errorResult(validationResult);
        
        // Verify result
        assertNotNull(result);
        assertNull(result.getHoraireUpdates());
        assertNull(result.getLignesRecords());
        assertSame(validationResult, result.getValidationResult());
        assertEquals(0, result.getTotalRowsProcessed());
        assertEquals('\0', result.getParsedLineType());
        assertTrue(result.getValidationResult().hasErrors());
    }

    @Test
    public void testGetters() {
        // Create a result with all fields populated
        List<HoraireUpdateRecord> updates = Collections.singletonList(
                new HoraireUpdateRecord(1, "12345678901234567890123456789012", "20230101", "20230501", "ABCD1", "09:30"));
        List<Map<String, String>> records = Collections.singletonList(new HashMap<>());
        RsfValidationResult validationResult = new RsfValidationResult("test.xlsx");
        int totalRows = 5;
        char lineType = 'B';
        
        ExcelParsingResult result = new ExcelParsingResult(updates, records, validationResult, totalRows, lineType);
        
        // Verify getters
        assertSame(updates, result.getHoraireUpdates());
        assertSame(records, result.getLignesRecords());
        assertSame(validationResult, result.getValidationResult());
        assertEquals(totalRows, result.getTotalRowsProcessed());
        assertEquals(lineType, result.getParsedLineType());
    }
} 