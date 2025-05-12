package com.rsf.rsf.service;

import com.rsf.rsf.config.RsfFieldMapping2017;
import com.rsf.rsf.domain.models.ExcelParsingResult;
import com.rsf.rsf.domain.models.HoraireUpdateRecord;
import com.rsf.rsf.domain.validation.RsfValidationResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExcelParserServiceTest {

    @Mock
    private RsfIntegrationService rsfIntegrationService;

    @Spy
    private RsfFieldMapping2017 fieldMapping = new RsfFieldMapping2017();

    @InjectMocks
    private ExcelParserService excelParserService;

    // Store the zip bytes to create fresh streams for each test
    private byte[] horaireZipBytes;
    private byte[] lignesZipBytes;
    private byte[] multipleFilesZipBytes;
    private byte[] invalidHeaderZipBytes;
    private byte[] invalidLignesTypeZipBytes;
    private byte[] emptyZipBytes;
    private byte[] dataValidationErrorZipBytes;

    @BeforeEach
    public void setup() throws IOException {
        // Initialize all the ZIP file bytes
        horaireZipBytes = createTestHoraireZipBytes();
        lignesZipBytes = createTestLignesZipBytes('A');
        multipleFilesZipBytes = createMultipleFilesZipBytes();
        invalidHeaderZipBytes = createInvalidHeaderZipBytes();
        invalidLignesTypeZipBytes = createInvalidLignesTypeZipBytes();
        emptyZipBytes = createEmptyZipBytes();
        dataValidationErrorZipBytes = createDataValidationErrorZipBytes();
    }

    // Create byte arrays for each zip file
    private byte[] createTestHoraireZipBytes() throws IOException {
        // Create a sample HORAIRE Excel file
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"num_immatriculation", "date_naissance", "date_soins", "code_acte", "horaire"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create data rows
            Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("12345678901234567890123456789012"); // 32 chars
            dataRow1.createCell(1).setCellValue("20170101"); // YYYYMMDD
            dataRow1.createCell(2).setCellValue("20171015"); // YYYYMMDD
            dataRow1.createCell(3).setCellValue("ABCD1"); // 5 chars
            dataRow1.createCell(4).setCellValue("09:30");

            Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("98765432109876543210987654321098"); // 32 chars
            dataRow2.createCell(1).setCellValue("20000215"); // YYYYMMDD
            dataRow2.createCell(2).setCellValue("20171020"); // YYYYMMDD
            dataRow2.createCell(3).setCellValue("WXYZ9"); // 5 chars
            dataRow2.createCell(4).setCellValue("14:45");
            
            // Save workbook to ByteArrayOutputStream
            ByteArrayOutputStream workbookOut = new ByteArrayOutputStream();
            workbook.write(workbookOut);
            
            // Create ZIP containing the Excel file
            ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
                // Add entry to ZIP
                ZipEntry entry = new ZipEntry("HORAIRS_2017.xlsx");
                zos.putNextEntry(entry);
                zos.write(workbookOut.toByteArray());
                zos.closeEntry();
            }
            
            return zipOut.toByteArray();
        }
    }
    
    private byte[] createTestLignesZipBytes(char lineType) throws IOException {
        // Create a sample LIGNES Excel file
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            
            // Get the full list of headers for type A from RsfFieldMapping2017
            List<String> headers = Arrays.asList(
                "TYPE_ENREGISTREMENT", "N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "SEXE", "CODE_CIVILITE",
                "N_IMMATRICULATION_ASSURE", "CLE_N_IMMATRICULATION", "RANG_BENEFICIAIRE", "N_ENTREE",
                "N_IMMATRICULATION_INDIVIDUEL", "CLE_N_IMMATRICULATION_INDIVIDUEL", "INDICATEUR_PARCOURS_SOINS",
                "NATURE_OPERATION", "NATURE_ASSURANCE", "TYPE_CONTRAT_ORGANISME_COMPLEMENTAIRE", "JUSTIF_EXO_TM",
                "SEJOUR_FACTURABLE_ASSURANCE_MALADIE", "FILLER_1", "MOTIF_NON_FACTURATION_ASSURANCE_MALADIE",
                "CODE_GD_REGIME", "DATE_NAISSANCE", "RANG_NAISSANCE", "DATE_ENTREE", "DATE_SORTIE",
                "CODE_POSTAL_RESIDENCE_PATIENT", "TOTAL_BASE_REMBOURSEMENT", "TOTAL_REMBOURSABLE_AMO",
                "TOTAL_HONORAIRE_FACTURE", "TOTAL_HONORAIRE_REMBOURSABLE_AM", "TOTAL_PARTICIPATION_ASSURE_AVANT_OC",
                "TOTAL_REMBOURSABLE_OC_PH", "TOTAL_REMBOURSABLE_OC_HONORAIRES", "MONTANT_TOTAL_FACTUREPH", "NuméroA"
            );
            
            // Create header row with all required headers
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }
            
            // Create data row with sample data
            Row dataRow = sheet.createRow(1);
            Map<String, String> sampleData = createSampleLignesData(lineType);
            
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                Cell cell = dataRow.createCell(i);
                cell.setCellValue(sampleData.getOrDefault(header, ""));
            }
            
            // Save workbook to ByteArrayOutputStream
            ByteArrayOutputStream workbookOut = new ByteArrayOutputStream();
            workbook.write(workbookOut);
            
            // Create ZIP containing the Excel file
            ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
                // Add entry to ZIP with proper naming format
                ZipEntry entry = new ZipEntry("LIGNES_" + lineType + "_2017.xlsx");
                zos.putNextEntry(entry);
                zos.write(workbookOut.toByteArray());
                zos.closeEntry();
            }
            
            return zipOut.toByteArray();
        }
    }

    private byte[] createMultipleFilesZipBytes() throws IOException {
        // Create two Excel files and add them to a single ZIP
        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
            // Create first Excel file (HORAIRE)
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Sheet1");
                
                // Header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"num_immatriculation", "date_naissance", "date_soins", "code_acte", "horaire"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }
                
                // Data row
                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue("12345678901234567890123456789012");
                dataRow.createCell(1).setCellValue("20170101");
                dataRow.createCell(2).setCellValue("20171015");
                dataRow.createCell(3).setCellValue("ABCD1");
                dataRow.createCell(4).setCellValue("10:00");
                
                // Save first workbook to ByteArrayOutputStream
                ByteArrayOutputStream workbookOut = new ByteArrayOutputStream();
                workbook.write(workbookOut);
                
                // Add first file to ZIP
                ZipEntry entry = new ZipEntry("HORAIRS_2017_1.xlsx");
                zos.putNextEntry(entry);
                zos.write(workbookOut.toByteArray());
                zos.closeEntry();
            }
            
            // Create second Excel file (HORAIRE)
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Sheet1");
                
                // Header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"num_immatriculation", "date_naissance", "date_soins", "code_acte", "horaire"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }
                
                // Data row
                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue("98765432109876543210987654321098");
                dataRow.createCell(1).setCellValue("20170202");
                dataRow.createCell(2).setCellValue("20171016");
                dataRow.createCell(3).setCellValue("WXYZ9");
                dataRow.createCell(4).setCellValue("11:30");
                
                // Save second workbook to ByteArrayOutputStream
                ByteArrayOutputStream workbookOut = new ByteArrayOutputStream();
                workbook.write(workbookOut);
                
                // Add second file to ZIP
                ZipEntry entry = new ZipEntry("HORAIRS_2017_2.xlsx");
                zos.putNextEntry(entry);
                zos.write(workbookOut.toByteArray());
                zos.closeEntry();
            }
        }
        
        return zipOut.toByteArray();
    }

    private byte[] createInvalidHeaderZipBytes() throws IOException {
        // Create a sample Excel file with invalid headers
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            
            // Create header row with invalid headers
            Row headerRow = sheet.createRow(0);
            String[] invalidHeaders = {"wrong_column1", "wrong_column2", "wrong_column3"};
            for (int i = 0; i < invalidHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(invalidHeaders[i]);
            }
            
            // Create data row
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Some data 1");
            dataRow.createCell(1).setCellValue("Some data 2");
            dataRow.createCell(2).setCellValue("Some data 3");
            
            // Save workbook to ByteArrayOutputStream
            ByteArrayOutputStream workbookOut = new ByteArrayOutputStream();
            workbook.write(workbookOut);
            
            // Create ZIP containing the Excel file
            ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
                // Add entry to ZIP with the expected naming format
                ZipEntry entry = new ZipEntry("HORAIRS_2017.xlsx");
                zos.putNextEntry(entry);
                zos.write(workbookOut.toByteArray());
                zos.closeEntry();
            }
            
            return zipOut.toByteArray();
        }
    }

    private byte[] createInvalidLignesTypeZipBytes() throws IOException {
        // Create a LIGNES file with an invalid type
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"TYPE_ENREGISTREMENT", "N_FINESS_EPMSI", "DATE_NAISSANCE", "DATE_ENTREE", "DATE_SORTIE"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create data row with invalid type (X is not supported)
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("X");
            dataRow.createCell(1).setCellValue("123456789");
            dataRow.createCell(2).setCellValue("20000101");
            dataRow.createCell(3).setCellValue("20170101");
            dataRow.createCell(4).setCellValue("20170110");
            
            // Save workbook to ByteArrayOutputStream
            ByteArrayOutputStream workbookOut = new ByteArrayOutputStream();
            workbook.write(workbookOut);
            
            // Create ZIP containing the Excel file
            ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
                // Add entry to ZIP with expected format
                ZipEntry entry = new ZipEntry("LIGNES_X_2017.xlsx");
                zos.putNextEntry(entry);
                zos.write(workbookOut.toByteArray());
                zos.closeEntry();
            }
            
            return zipOut.toByteArray();
        }
    }

    private byte[] createEmptyZipBytes() throws IOException {
        // Create an empty ZIP file
        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
            // Create an empty ZIP (no entries)
        }
        return zipOut.toByteArray();
    }

    private byte[] createDataValidationErrorZipBytes() throws IOException {
        // Create a sample HORAIRE Excel file with some validation errors
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"num_immatriculation", "date_naissance", "date_soins", "code_acte", "horaire"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Valid data row
            Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("12345678901234567890123456789012"); // 32 chars
            dataRow1.createCell(1).setCellValue("20170101"); // YYYYMMDD
            dataRow1.createCell(2).setCellValue("20171015"); // YYYYMMDD
            dataRow1.createCell(3).setCellValue("ABCD1"); // 5 chars
            dataRow1.createCell(4).setCellValue("09:30"); // Valid time
            
            // Invalid data row - invalid date format
            Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("98765432109876543210987654321098");
            dataRow2.createCell(1).setCellValue("01/02/2000"); // Wrong date format
            dataRow2.createCell(2).setCellValue("20171020");
            dataRow2.createCell(3).setCellValue("WXYZ9");
            dataRow2.createCell(4).setCellValue("14:45");
            
            // Invalid data row - invalid time format
            Row dataRow3 = sheet.createRow(3);
            dataRow3.createCell(0).setCellValue("12345678901234567890123456789012");
            dataRow3.createCell(1).setCellValue("20170101");
            dataRow3.createCell(2).setCellValue("20171015");
            dataRow3.createCell(3).setCellValue("ABCD1");
            dataRow3.createCell(4).setCellValue("945"); // Invalid time format
            
            // Save workbook to ByteArrayOutputStream
            ByteArrayOutputStream workbookOut = new ByteArrayOutputStream();
            workbook.write(workbookOut);
            
            // Create ZIP containing the Excel file
            ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
                // Add entry to ZIP
                ZipEntry entry = new ZipEntry("HORAIRS_2017.xlsx");
                zos.putNextEntry(entry);
                zos.write(workbookOut.toByteArray());
                zos.closeEntry();
            }
            
            return zipOut.toByteArray();
        }
    }

    private Map<String, String> createSampleLignesData(char lineType) {
        Map<String, String> data = new HashMap<>();
        
        // Common fields for all line types
        data.put("TYPE_ENREGISTREMENT", String.valueOf(lineType));
        data.put("N_FINESS_EPMSI", "123456789");
        data.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        data.put("N_IMMATRICULATION_ASSURE", "12345678901234567890123456789012");
        data.put("CLE_N_IMMATRICULATION", "12");
        data.put("RANG_BENEFICIAIRE", "001");
        data.put("N_ENTREE", "123456789");
        data.put("N_IMMATRICULATION_INDIVIDUEL", "98765432109876543210987654321098");
        data.put("CLE_N_IMMATRICULATION_INDIVIDUEL", "98");
        
        // Type-specific fields
        switch (lineType) {
            case 'A':
                data.put("SEXE", "1");
                data.put("CODE_CIVILITE", "1");
                data.put("INDICATEUR_PARCOURS_SOINS", "1");
                data.put("NATURE_OPERATION", "1");
                data.put("NATURE_ASSURANCE", "10");
                data.put("TYPE_CONTRAT_ORGANISME_COMPLEMENTAIRE", "01");
                data.put("JUSTIF_EXO_TM", "1");
                data.put("SEJOUR_FACTURABLE_ASSURANCE_MALADIE", "1");
                data.put("FILLER_1", "0");
                data.put("MOTIF_NON_FACTURATION_ASSURANCE_MALADIE", "0");
                data.put("CODE_GD_REGIME", "01");
                data.put("DATE_NAISSANCE", "20000101");
                data.put("RANG_NAISSANCE", "1");
                data.put("DATE_ENTREE", "20170101");
                data.put("DATE_SORTIE", "20170110");
                data.put("CODE_POSTAL_RESIDENCE_PATIENT", "75001");
                data.put("TOTAL_BASE_REMBOURSEMENT", "01000.00"); // Proper 8-char format
                data.put("TOTAL_REMBOURSABLE_AMO", "00800.00"); // Proper 8-char format
                data.put("TOTAL_HONORAIRE_FACTURE", "01200.00"); // Proper 8-char format
                data.put("TOTAL_HONORAIRE_REMBOURSABLE_AM", "00900.00"); // Proper 8-char format
                data.put("TOTAL_PARTICIPATION_ASSURE_AVANT_OC", "00200.00"); // Proper 8-char format
                data.put("TOTAL_REMBOURSABLE_OC_PH", "00100.00"); // Proper 8-char format
                data.put("TOTAL_REMBOURSABLE_OC_HONORAIRES", "00100.00"); // Proper 8-char format
                data.put("MONTANT_TOTAL_FACTUREPH", "01300.00"); // Proper 8-char format
                data.put("NuméroA", "A0001");
                break;
            case 'B':
            case 'C':
                data.put("MODE_TRAITEMENT", "01");
                data.put("DISCIPLINE_PRESTATION", "001");
                data.put("JUSTIFICATION_EXOTM", "1");
                data.put("SPECIALITE_EXECUTANT", "01");
                data.put("DATE_SOINS", "20170105");
                data.put("CODE_ACTE", "ABCD1");
                data.put("QUANTITE", "001");
                data.put("COEFFICIENT", "001000");
                data.put("TYPE_PRESTATION_INTERMEDIAIRE", "0");
                data.put("COEFFICIENT_MCO", "00100");
                data.put("DENOMBREMENT", "01");
                data.put("PRIXUNITAIRE", "0100.00");
                data.put("MONTANT_BASE_REMBOURSEMENT", "0100.00");
                data.put("TAUX_APPLICABLE", "100");
                data.put("MONTANT_REMBOURSABLE_AMO", "0100.00");
                data.put("MONTANT_HONORAIRE", "0100.00");
                data.put("MONTANT_REMBOURSABLE_AMC", "0100.00");
                data.put("FILLER", "");
                if (lineType == 'B') {
                    data.put("NuméroB", "B0001");
                } else {
                    data.put("MONTANT_REMBOURSABLE_AMO_1", "0100.00");
                    data.put("TYPE_UNITE_FONCTIONNELLE", "01");
                    data.put("NuméroC", "C0001");
                }
                break;
            case 'M':
                data.put("MODE_TRAITEMENT", "01");
                data.put("DISCIPLINE_PRESTATION", "001");
                data.put("DATE_SOINS", "20170105");
                data.put("CODE_CCAM", "AAAA000");
                data.put("EXTENSION_DOCUMENTAIRE", "1");
                data.put("ACTIVITE", "1");
                data.put("PHASE", "1");
                data.put("MODIFICATEUR1", "0");
                data.put("MODIFICATEUR2", "0");
                data.put("MODIFICATEUR3", "0");
                data.put("MODIFICATEUR4", "0");
                data.put("CODE_ASSOCIATION_ACTE", "0");
                data.put("CODE_REMBOURSEMENT", "0");
                for (int i = 1; i <= 16; i++) {
                    data.put("NUM_DENT" + i, "00");
                }
                data.put("NUMÉRO_M", "M0001");
                break;
            case 'P':
                data.put("DATE_DEBUT_SEJOUR", "20170101");
                data.put("CODE_REFERENCE_LPP", "1234567890123");
                data.put("QUANTITE", "01");
                data.put("TARIF_REFERENCE_LPP", "0100.00");
                data.put("MONTANT_TOTAL_FACTURE", "0100.00");
                data.put("PRIX_ACHAT_UNITAIRE", "0100.00");
                data.put("MONTANT_UNITAIRE_ECART", "0000.00");
                data.put("MONTANT_TOTAL_ECART", "0000.00");
                data.put("NuméroP", "P0001");
                break;
            case 'L':
                data.put("MODE_TRAITEMENT", "01");
                data.put("DISCIPLINE_PRESTATION", "001");
                data.put("DATE_ACTE1", "20170101");
                data.put("QUANTITE_ACTE1", "01");
                data.put("CODE_ACTE1", "ABCDEFGH");
                data.put("DATE_ACTE2", "");
                data.put("QUANTITE_ACTE2", "");
                data.put("CODE_ACTE2", "");
                data.put("DATE_ACTE3", "");
                data.put("QUANTITE_ACTE3", "");
                data.put("CODE_ACTE3", "");
                data.put("DATE_ACTE4", "");
                data.put("QUANTITE_ACTE4", "");
                data.put("CODE_ACTE4", "");
                data.put("DATE_ACTE5", "");
                data.put("QUANTITE_ACTE5", "");
                data.put("CODE_ACTE5", "");
                break;
            case 'H':
                data.put("DATE_DEBUT_SEJOUR", "20170101");
                data.put("CODE_UCD", "1234567");
                data.put("COEFF_FRACTIONNEMENT", "00100");
                data.put("PRIX_ACHAT_UNITAIRE", "0100.00");
                data.put("MONTANT_UNITAIRE_ECART", "0000.00");
                data.put("MONTANT_TOTAL_ECART", "0000.00");
                data.put("QUANTITE", "001");
                data.put("TOTAL_FACTURE", "0100.00");
                data.put("Indication", "IND0001");
                data.put("NuméroH", "H0001");
                break;
        }
        
        return data;
    }

    // Helper method to create a new input stream from byte array
    private ByteArrayInputStream createInputStreamFromBytes(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    @Test
    public void testParseHoraireFile() {
        // Given - Create a new ByteArrayInputStream for each test
        ByteArrayInputStream zipStream = createInputStreamFromBytes(horaireZipBytes);
        
        // When
        ExcelParsingResult result = excelParserService.parseExcelFromZip(zipStream, "test_horaire.zip");
        
        // Then
        assertNotNull(result);
        assertFalse(result.getValidationResult().hasErrors(), 
                "Validation errors: " + (result.getValidationResult().hasErrors() ? 
                        result.getValidationResult().getErrors().toString() : "None"));
        assertNotNull(result.getHoraireUpdates());
        assertEquals(2, result.getHoraireUpdates().size());
        assertEquals(2, result.getTotalRowsProcessed());
        
        // Verify first record
        HoraireUpdateRecord record1 = result.getHoraireUpdates().get(0);
        assertEquals("12345678901234567890123456789012", record1.getNumImmatriculation());
        assertEquals("20170101", record1.getDateNaissance());
        assertEquals("20171015", record1.getDateSoins());
        assertEquals("ABCD1", record1.getCodeActe());
        assertEquals("09:30", record1.getHoraire());
        
        // Verify second record
        HoraireUpdateRecord record2 = result.getHoraireUpdates().get(1);
        assertEquals("98765432109876543210987654321098", record2.getNumImmatriculation());
        assertEquals("20000215", record2.getDateNaissance());
        assertEquals("20171020", record2.getDateSoins());
        assertEquals("WXYZ9", record2.getCodeActe());
        assertEquals("14:45", record2.getHoraire());
    }

    @Test
    public void testParseLignesFile() {
        // Given - Create a new ByteArrayInputStream for each test
        char lineType = 'A';
        ByteArrayInputStream zipStream = createInputStreamFromBytes(lignesZipBytes);
        
        // When
        ExcelParsingResult result = excelParserService.parseExcelFromZip(zipStream, "test_lignes_a.zip");
        
        // Then
        assertNotNull(result);
        assertFalse(result.getValidationResult().hasErrors(),
                "Validation errors: " + (result.getValidationResult().hasErrors() ? 
                        result.getValidationResult().getErrors().toString() : "None"));
        assertNotNull(result.getLignesRecords());
        assertEquals(1, result.getLignesRecords().size());
        assertEquals(1, result.getTotalRowsProcessed());
        assertEquals(lineType, result.getParsedLineType());
        
        // Verify record fields
        Map<String, String> record = result.getLignesRecords().get(0);
        assertEquals(lineType + "", record.get("TYPE_ENREGISTREMENT"));
        assertEquals("123456789", record.get("N_FINESS_EPMSI"));
        assertEquals("20000101", record.get("DATE_NAISSANCE"));
        assertEquals("20170101", record.get("DATE_ENTREE"));
        assertEquals("20170110", record.get("DATE_SORTIE"));
    }

    @Test
    public void testInvalidHoraireHeader() {
        // Create a new ByteArrayInputStream for each test
        ByteArrayInputStream zipStream = createInputStreamFromBytes(invalidHeaderZipBytes);
        
        // Parse zip
        ExcelParsingResult result = excelParserService.parseExcelFromZip(zipStream, "test_invalid_header.zip");
        
        // Verify that validation fails because of invalid header
        assertNotNull(result);
        assertTrue(result.getValidationResult().hasErrors());
        assertTrue(result.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getMessage().contains("Invalid headers")));
    }

    @Test
    public void testApplyExcelUpdatesToRsfData() {
        // Given - Create a new ByteArrayInputStream for each test
        ByteArrayInputStream horaireZipStream = createInputStreamFromBytes(horaireZipBytes);
        ExcelParsingResult horaireResult = excelParserService.parseExcelFromZip(horaireZipStream, "test_horaire.zip");
        
        Map<Character, List<Map<String, String>>> rsfData = new HashMap<>();
        rsfData.put('A', new ArrayList<>());
        rsfData.put('B', new ArrayList<>());
        rsfData.put('C', new ArrayList<>());
        
        // Create sample RSF data with matching records
        Map<String, String> aLine = new HashMap<>();
        aLine.put("N_ENTREE", "123456789");
        aLine.put("N_IMMATRICULATION_ASSURE", "12345678901234567890123456789012");
        aLine.put("DATE_NAISSANCE", "20170101");
        aLine.put("N_FINESS_EPMSI", "123456789");
        aLine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        rsfData.get('A').add(aLine);
        
        Map<String, String> bLine = new HashMap<>();
        bLine.put("N_ENTREE", "123456789");
        bLine.put("N_IMMATRICULATION_ASSURE", "12345678901234567890123456789012");
        bLine.put("DATE_SOINS", "20171015");
        bLine.put("CODE_ACTE", "ABCD1");
        bLine.put("N_FINESS_EPMSI", "123456789");
        bLine.put("N_FINESS_GEOGRAPHIQUE", "987654321");
        rsfData.get('B').add(bLine);
        
        // Mock integration service response
        when(rsfIntegrationService.integrateHoraireUpdates(eq(horaireResult), any())).thenReturn(
                new RsfIntegrationService.RsfIntegrationResult(2, 1, Collections.emptyList()));
        
        // When
        RsfIntegrationService.RsfIntegrationResult integrationResult = 
                excelParserService.applyExcelUpdatesToRsfData(horaireResult, rsfData);
        
        // Then
        assertNotNull(integrationResult);
        assertEquals(2, integrationResult.getTotalProcessed());
        assertEquals(1, integrationResult.getUpdatedCount());
        assertFalse(integrationResult.hasErrors());
        
        // Verify the integration service was called correctly
        verify(rsfIntegrationService, times(1)).integrateHoraireUpdates(eq(horaireResult), eq(rsfData));
    }

    @Test
    public void testEmptyZipFile() {
        // Create a new ByteArrayInputStream for each test
        ByteArrayInputStream zipStream = createInputStreamFromBytes(emptyZipBytes);
        
        // Parse empty ZIP
        ExcelParsingResult result = excelParserService.parseExcelFromZip(zipStream, "empty.zip");
        
        // Verify error handling
        assertNotNull(result);
        assertTrue(result.getValidationResult().hasErrors());
        assertTrue(result.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getMessage().contains("No valid Excel file found")));
    }
    
    @Test
    public void testInvalidZipFile() {
        // Create an invalid ZIP file (just random bytes)
        byte[] invalidZipContent = {0x50, 0x4B, 0x05, 0x06, 0x00, 0x00, 0x00};
        ByteArrayInputStream zipStream = new ByteArrayInputStream(invalidZipContent);
        
        // Parse invalid ZIP
        ExcelParsingResult result = excelParserService.parseExcelFromZip(zipStream, "invalid.zip");
        
        // Verify error handling - expect errors but not checking for specific error type as it may vary
        assertNotNull(result);
        assertTrue(result.getValidationResult().hasErrors());
    }

    @Test
    public void testMultipleExcelFilesInZip() {
        // Create a new ByteArrayInputStream for each test
        ByteArrayInputStream zipStream = createInputStreamFromBytes(multipleFilesZipBytes);
        
        // Parse ZIP with multiple Excel files
        ExcelParsingResult result = excelParserService.parseExcelFromZip(zipStream, "multiple_files.zip");
        
        // Verify that only the first file was processed
        assertNotNull(result);
        assertFalse(result.getValidationResult().hasErrors());
        assertEquals(1, result.getHoraireUpdates().size());
        assertEquals("12345678901234567890123456789012", result.getHoraireUpdates().get(0).getNumImmatriculation());
    }
    
    @Test
    public void testInvalidLignesFileType() {
        // Create a new ByteArrayInputStream for each test
        ByteArrayInputStream zipStream = createInputStreamFromBytes(invalidLignesTypeZipBytes);
        
        // Parse ZIP with invalid LIGNES type
        ExcelParsingResult result = excelParserService.parseExcelFromZip(zipStream, "invalid_lignes_type.zip");
        
        // Verify error handling
        assertNotNull(result);
        assertTrue(result.getValidationResult().hasErrors());
        assertTrue(result.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.getMessage().contains("Unsupported LIGNES type")));
    }
    
    @Test
    public void testDataValidationErrors() {
        // Create a new ByteArrayInputStream for each test
        ByteArrayInputStream zipStream = createInputStreamFromBytes(dataValidationErrorZipBytes);
        
        // Parse ZIP
        ExcelParsingResult result = excelParserService.parseExcelFromZip(zipStream, "validation_errors.zip");
        
        // Verify validation results
        assertNotNull(result);
        assertTrue(result.getValidationResult().hasErrors()); // Should have validation errors
        assertTrue(result.getValidationResult().getErrors().size() >= 1); // At least one error
        assertEquals(2, result.getHoraireUpdates().size()); // Two valid records (could vary based on implementation)
        assertEquals(3, result.getTotalRowsProcessed()); // Three rows processed
    }
} 