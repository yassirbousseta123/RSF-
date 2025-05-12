package com.rsf.rsf.service;

import com.rsf.rsf.domain.models.ExcelParsingResult;
import com.rsf.rsf.domain.models.HoraireUpdateRecord;
import com.rsf.rsf.domain.validation.RsfValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RsfIntegrationServiceTest {

    @InjectMocks
    private RsfIntegrationService rsfIntegrationService;

    private Map<Character, List<Map<String, String>>> rsfData;
    private ExcelParsingResult horaireResult;
    private ExcelParsingResult lignesResult;

    @BeforeEach
    public void setup() {
        // Initialize RSF data structure
        rsfData = new HashMap<>();
        rsfData.put('A', new ArrayList<>());
        rsfData.put('B', new ArrayList<>());
        rsfData.put('C', new ArrayList<>());

        // Create sample A-line (patient data)
        Map<String, String> aLine1 = new HashMap<>();
        aLine1.put("N_ENTREE", "123456789");
        aLine1.put("N_IMMATRICULATION_ASSURE", "12345678901234567890123456789012");
        aLine1.put("DATE_NAISSANCE", "20000101");
        aLine1.put("N_FINESS_EPMSI", "123456789");
        aLine1.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        rsfData.get('A').add(aLine1);

        // Create sample B-lines (medical acts)
        Map<String, String> bLine1 = new HashMap<>();
        bLine1.put("N_ENTREE", "123456789");
        bLine1.put("N_IMMATRICULATION_ASSURE", "12345678901234567890123456789012");
        bLine1.put("DATE_SOINS", "20230510");
        bLine1.put("CODE_ACTE", "ABCD1");
        bLine1.put("N_FINESS_EPMSI", "123456789");
        bLine1.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        rsfData.get('B').add(bLine1);

        Map<String, String> bLine2 = new HashMap<>();
        bLine2.put("N_ENTREE", "123456789");
        bLine2.put("N_IMMATRICULATION_ASSURE", "12345678901234567890123456789012");
        bLine2.put("DATE_SOINS", "20230511");
        bLine2.put("CODE_ACTE", "WXYZ9");
        bLine2.put("N_FINESS_EPMSI", "123456789");
        bLine2.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        rsfData.get('B').add(bLine2);

        // Create sample C-line
        Map<String, String> cLine1 = new HashMap<>();
        cLine1.put("N_ENTREE", "123456789");
        cLine1.put("N_IMMATRICULATION_ASSURE", "12345678901234567890123456789012");
        cLine1.put("DATE_SOINS", "20230512");
        cLine1.put("CODE_ACTE", "EFGH2");
        cLine1.put("N_FINESS_EPMSI", "123456789");
        cLine1.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        rsfData.get('C').add(cLine1);

        // Setup HORAIRE test data
        List<HoraireUpdateRecord> horaireUpdates = new ArrayList<>();
        horaireUpdates.add(new HoraireUpdateRecord(1, "12345678901234567890123456789012", "20000101", "20230510", "ABCD1", "09:30"));
        horaireUpdates.add(new HoraireUpdateRecord(2, "12345678901234567890123456789012", "20000101", "20230511", "WXYZ9", "14:45"));
        horaireUpdates.add(new HoraireUpdateRecord(3, "12345678901234567890123456789012", "20000101", "20230512", "EFGH2", "11:15"));
        horaireUpdates.add(new HoraireUpdateRecord(4, "98765432109876543210987654321098", "19900215", "20230515", "JKLM3", "16:00")); // No match
        
        horaireResult = ExcelParsingResult.horaireResult(horaireUpdates, new RsfValidationResult("test_horaire.xlsx"), 4);

        // Setup LIGNES test data (type 'A')
        List<Map<String, String>> lignesRecords = new ArrayList<>();
        Map<String, String> newALine = new HashMap<>();
        newALine.put("TYPE_ENREGISTREMENT", "A");
        newALine.put("N_FINESS_EPMSI", "123456789");
        newALine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        newALine.put("N_IMMATRICULATION_ASSURE", "98765432109876543210987654321098");
        newALine.put("N_ENTREE", "987654321");
        newALine.put("DATE_NAISSANCE", "19950315");
        newALine.put("DATE_ENTREE", "20230601");
        newALine.put("DATE_SORTIE", "20230610");
        lignesRecords.add(newALine);
        
        lignesResult = ExcelParsingResult.lignesResult(lignesRecords, new RsfValidationResult("test_lignes.xlsx"), 1, 'A');
    }

    @Test
    public void testIntegrateHoraireUpdates() {
        // When
        RsfIntegrationService.RsfIntegrationResult result = rsfIntegrationService.integrateHoraireUpdates(horaireResult, rsfData);
        
        // Then
        assertNotNull(result);
        assertEquals(4, result.getTotalProcessed());
        assertEquals(3, result.getUpdatedCount());
        assertEquals(1, result.getErrors().size()); // One record should fail to match
        
        // Verify updates applied
        assertEquals("09:30", rsfData.get('B').get(0).get("horaire"));
        assertEquals("14:45", rsfData.get('B').get(1).get("horaire"));
        assertEquals("11:15", rsfData.get('C').get(0).get("horaire"));
    }

    @Test
    public void testIntegrateHoraireUpdates_NoUpdates() {
        // When
        RsfIntegrationService.RsfIntegrationResult result = rsfIntegrationService.integrateHoraireUpdates(
                ExcelParsingResult.horaireResult(null, new RsfValidationResult("test.xlsx"), 0), rsfData);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalProcessed());
        assertEquals(0, result.getUpdatedCount());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("No HORAIRE updates to process"));
    }

    @Test
    public void testIntegrateLignesRecords_NewRecord() {
        // When
        RsfIntegrationService.RsfIntegrationResult result = rsfIntegrationService.integrateLignesRecords(lignesResult, rsfData);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalProcessed());
        assertEquals(1, result.getAddedCount());
        assertEquals(0, result.getUpdatedCount());
        assertEquals(0, result.getErrors().size());
        
        // Verify addition
        assertEquals(2, rsfData.get('A').size());
        Map<String, String> addedRecord = rsfData.get('A').get(1);
        assertEquals("98765432109876543210987654321098", addedRecord.get("N_IMMATRICULATION_ASSURE"));
        assertEquals("987654321", addedRecord.get("N_ENTREE"));
        assertEquals("19950315", addedRecord.get("DATE_NAISSANCE"));
    }

    @Test
    public void testIntegrateLignesRecords_UpdateExisting() {
        // Setup update to an existing record
        List<Map<String, String>> updateRecords = new ArrayList<>();
        Map<String, String> updatedALine = new HashMap<>();
        updatedALine.put("TYPE_ENREGISTREMENT", "A");
        updatedALine.put("N_FINESS_EPMSI", "123456789");
        updatedALine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        updatedALine.put("N_IMMATRICULATION_ASSURE", "12345678901234567890123456789012");
        updatedALine.put("N_ENTREE", "123456789");
        updatedALine.put("DATE_NAISSANCE", "20000101");
        updatedALine.put("DATE_ENTREE", "20230701"); // Updated value
        updatedALine.put("DATE_SORTIE", "20230715"); // Updated value
        updateRecords.add(updatedALine);
        
        ExcelParsingResult updateResult = ExcelParsingResult.lignesResult(
                updateRecords, new RsfValidationResult("test_update.xlsx"), 1, 'A');
        
        // When
        RsfIntegrationService.RsfIntegrationResult result = rsfIntegrationService.integrateLignesRecords(updateResult, rsfData);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalProcessed());
        assertEquals(0, result.getAddedCount());
        assertEquals(1, result.getUpdatedCount());
        assertEquals(0, result.getErrors().size());
        
        // Verify update
        assertEquals(1, rsfData.get('A').size());
        Map<String, String> updatedRecord = rsfData.get('A').get(0);
        assertEquals("20230701", updatedRecord.get("DATE_ENTREE"));
        assertEquals("20230715", updatedRecord.get("DATE_SORTIE"));
    }

    @Test
    public void testIntegrateLignesRecords_NoRecords() {
        // When
        RsfIntegrationService.RsfIntegrationResult result = rsfIntegrationService.integrateLignesRecords(
                ExcelParsingResult.lignesResult(null, new RsfValidationResult("test.xlsx"), 0, 'A'), rsfData);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalProcessed());
        assertEquals(0, result.getAddedCount());
        assertEquals(0, result.getUpdatedCount());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("No LIGNES records to process"));
    }

    @Test
    public void testIntegrateLignesRecords_InitializeLineType() {
        // Setup with a line type not in the data
        Map<Character, List<Map<String, String>>> emptyRsfData = new HashMap<>();
        
        List<Map<String, String>> mRecords = new ArrayList<>();
        Map<String, String> mLine = new HashMap<>();
        mLine.put("TYPE_ENREGISTREMENT", "M");
        mLine.put("N_FINESS_EPMSI", "123456789");
        mLine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        mLine.put("N_IMMATRICULATION_ASSURE", "12345678901234567890123456789012");
        mLine.put("N_ENTREE", "123456789");
        mLine.put("DATE_SOINS", "20230801");
        mLine.put("CODE_CCAM", "AAAA000");
        mRecords.add(mLine);
        
        ExcelParsingResult mResult = ExcelParsingResult.lignesResult(
                mRecords, new RsfValidationResult("test_m.xlsx"), 1, 'M');
        
        // When
        RsfIntegrationService.RsfIntegrationResult result = rsfIntegrationService.integrateLignesRecords(mResult, emptyRsfData);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalProcessed());
        assertEquals(1, result.getAddedCount());
        assertEquals(0, result.getUpdatedCount());
        
        // Verify line type was initialized
        assertTrue(emptyRsfData.containsKey('M'));
        assertEquals(1, emptyRsfData.get('M').size());
    }

    @Test
    public void testRsfIntegrationResult() {
        // Test constructor with just updated count
        RsfIntegrationService.RsfIntegrationResult result1 = new RsfIntegrationService.RsfIntegrationResult(
                10, 5, Arrays.asList("Error 1", "Error 2"));
        
        assertEquals(10, result1.getTotalProcessed());
        assertEquals(0, result1.getAddedCount());
        assertEquals(5, result1.getUpdatedCount());
        assertEquals(2, result1.getErrors().size());
        assertTrue(result1.hasErrors());
        
        // Test constructor with both added and updated counts
        RsfIntegrationService.RsfIntegrationResult result2 = new RsfIntegrationService.RsfIntegrationResult(
                20, 10, 5, Collections.emptyList());
        
        assertEquals(20, result2.getTotalProcessed());
        assertEquals(10, result2.getAddedCount());
        assertEquals(5, result2.getUpdatedCount());
        assertEquals(0, result2.getErrors().size());
        assertFalse(result2.hasErrors());
    }
} 