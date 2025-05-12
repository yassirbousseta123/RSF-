package com.rsf.rsf.service;

import com.rsf.rsf.config.RsfFieldMapping2017;
import com.rsf.rsf.domain.models.ExcelParsingResult;
import com.rsf.rsf.domain.models.HoraireUpdateRecord;
import com.rsf.rsf.domain.validation.RsfError;
import com.rsf.rsf.domain.validation.RsfErrorType;
import com.rsf.rsf.domain.validation.RsfValidationResult;
import com.rsf.rsf.utils.ExcelParsingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelParserService {

    private static final String HORAIRE_PREFIX = "HORAIRES_";
    private static final String LIGNES_PREFIX = "LIGNES_";
    private static final Pattern LIGNES_FILENAME_PATTERN = Pattern.compile(LIGNES_PREFIX + "([A-Z])_.*\\.xlsx?$"); // LIGNES_[TYPE]_.xlsx or .xls
    private static final Logger log = LoggerFactory.getLogger(ExcelParserService.class);

    private final RsfFieldMapping2017 fieldMapping;
    private final RsfIntegrationService rsfIntegrationService;

    public ExcelParsingResult parseExcelFromZip(InputStream zipInputStream, String zipFileName) {
        RsfValidationResult validationResult = new RsfValidationResult(zipFileName);
        try {
            // Copy input stream to byte array to avoid stream closed issues
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zipInputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            
            // Create new input stream from the bytes
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            
            try (ZipInputStream zis = new ZipInputStream(bais)) {
                ZipEntry entry;
                boolean horaireFound = false;
                boolean lignesFound = false;
                ExcelParsingResult result = null;

                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        String entryName = entry.getName().substring(entry.getName().lastIndexOf('/') + 1); // Get base filename
                        log.info("Processing entry: {}", entryName);

                        if (entryName.startsWith(HORAIRE_PREFIX) && (entryName.endsWith(".xlsx") || entryName.endsWith(".xls"))) {
                            if (horaireFound || lignesFound) {
                                log.warn("Multiple HORAIRE/LIGNES files found in ZIP {}. Processing the first one encountered.", zipFileName);
                                zis.closeEntry();
                                continue; // Skip additional files
                            }
                            horaireFound = true;
                            
                            // Copy zip entry content to byte array for processing
                            ByteArrayOutputStream entryBytes = new ByteArrayOutputStream();
                            byte[] entryBuffer = new byte[1024];
                            int entryLen;
                            while ((entryLen = zis.read(entryBuffer)) > 0) {
                                entryBytes.write(entryBuffer, 0, entryLen);
                            }
                            entryBytes.flush();
                            
                            // Parse using in-memory bytes
                            result = parseHoraireFile(new ByteArrayInputStream(entryBytes.toByteArray()), entryName, validationResult);

                        } else if (entryName.startsWith(LIGNES_PREFIX) && (entryName.endsWith(".xlsx") || entryName.endsWith(".xls"))) {
                            Matcher matcher = LIGNES_FILENAME_PATTERN.matcher(entryName);
                            if (matcher.matches()) {
                                 if (horaireFound || lignesFound) {
                                    log.warn("Multiple HORAIRE/LIGNES files found in ZIP {}. Processing the first one encountered.", zipFileName);
                                    zis.closeEntry();
                                    continue; // Skip additional files
                                }
                                lignesFound = true;
                                char lineType = matcher.group(1).charAt(0);
                                if (RsfFieldMapping2017.LIGNES_HEADERS_MAP.containsKey(lineType)) {
                                    // Copy zip entry content to byte array for processing
                                    ByteArrayOutputStream entryBytes = new ByteArrayOutputStream();
                                    byte[] entryBuffer = new byte[1024];
                                    int entryLen;
                                    while ((entryLen = zis.read(entryBuffer)) > 0) {
                                        entryBytes.write(entryBuffer, 0, entryLen);
                                    }
                                    entryBytes.flush();
                                    
                                    // Parse using in-memory bytes
                                    result = parseLignesFile(new ByteArrayInputStream(entryBytes.toByteArray()), entryName, lineType, validationResult);
                                } else {
                                    validationResult.addError(new RsfError(0, entryName, RsfErrorType.FILE_NAME_ERROR, "Unsupported LIGNES type '/" + lineType + "/' in filename."));
                                    return ExcelParsingResult.errorResult(validationResult);
                                }
                            } else {
                                validationResult.addError(new RsfError(0, entryName, RsfErrorType.FILE_NAME_ERROR, "Invalid LIGNES filename format. Expected LIGNES_[A|B|C|H|M|P|L]_*.xlsx"));
                                // Don't return immediately, maybe another valid file exists
                            }
                        }
                    }
                    zis.closeEntry();
                    // Only process the first valid HORAIRE or LIGNES file found
                    if (result != null) {
                        break;
                    }
                }

                if (result == null) {
                    // No valid HORAIRE or LIGNES file found after checking all entries
                    validationResult.addError(new RsfError(0, zipFileName, RsfErrorType.FILE_NAME_ERROR, "No valid Excel file found starting with '" + HORAIRE_PREFIX + "' or '" + LIGNES_PREFIX + "' in ZIP archive."));
                    return ExcelParsingResult.errorResult(validationResult);
                }

                return result;

            } catch (Exception e) {
                log.error("Error processing ZIP file {}: {}", zipFileName, e.getMessage(), e);
                validationResult.addError(new RsfError(0, zipFileName, RsfErrorType.STRUCTURAL_ERROR, "Failed to process ZIP file: " + e.getMessage()));
                return ExcelParsingResult.errorResult(validationResult);
            }
        } catch (Exception e) {
            log.error("Error reading ZIP file {}: {}", zipFileName, e.getMessage(), e);
            validationResult.addError(new RsfError(0, zipFileName, RsfErrorType.STRUCTURAL_ERROR, "Failed to read ZIP file: " + e.getMessage()));
            return ExcelParsingResult.errorResult(validationResult);
        }
    }

    private ExcelParsingResult parseHoraireFile(InputStream fileInputStream, String fileName, RsfValidationResult validationResult) {
        List<HoraireUpdateRecord> updates = new ArrayList<>();
        int totalRowsProcessed = 0;
        try (Workbook workbook = WorkbookFactory.create(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is on the first sheet

            // 1. Header Validation
            List<String> expectedHeaders = RsfFieldMapping2017.HORAIRE_EXPECTED_HEADERS;
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                validationResult.addError(new RsfError(1, "", RsfErrorType.STRUCTURAL_ERROR, "Missing header row in HORAIRE file."));
                return ExcelParsingResult.errorResult(validationResult);
            }

            List<String> actualHeaders = new ArrayList<>();
            for (int i = 0; i < expectedHeaders.size(); i++) { // Check only up to expected header count
                Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                actualHeaders.add(ExcelParsingUtils.getCellStringValue(cell));
            }

            // Check for exact match and count
            if (headerRow.getLastCellNum() != expectedHeaders.size() || !actualHeaders.equals(expectedHeaders)) {
                 String headersFound = actualHeaders.size() > expectedHeaders.size() ?
                    String.join(", ", actualHeaders) : // If more headers than expected, show all
                    String.join(", ", getActualHeaderValues(headerRow)); // Show actual content

                validationResult.addError(new RsfError(1, String.join("|", getActualHeaderValues(headerRow)), RsfErrorType.FORMAT_ERROR,
                        "Invalid headers in HORAIRE file. Expected: [" + String.join(", ", expectedHeaders) +
                                "], Found: [" + headersFound + "]"));
                return ExcelParsingResult.errorResult(validationResult);
            }

            // 2. Data Extraction & Validation
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next(); // Skip header row

            int rowNum = 1; // Start from 1 for user-facing error messages (header is row 1)
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;
                totalRowsProcessed++;
                boolean rowHasError = false;

                String numImmat = ExcelParsingUtils.getCellStringValue(row.getCell(0));
                String dateNaiss = ExcelParsingUtils.getCellStringValue(row.getCell(1));
                String dateSoins = ExcelParsingUtils.getCellStringValue(row.getCell(2));
                String codeActe = ExcelParsingUtils.getCellStringValue(row.getCell(3));
                String horaire = ExcelParsingUtils.getCellStringValue(row.getCell(4));
                String rawLineContent = formatRawLine(row, expectedHeaders.size());
                
                log.info("HORAIRE ROW {}: numImmat={}, dateNaiss={}, dateSoins={}, codeActe={}, horaire={}", 
                    rowNum, numImmat, dateNaiss, dateSoins, codeActe, horaire);

                // --- Field Validations ---
                if (numImmat.isEmpty()) {
                    log.info("VALIDATION FAILED for row {}: num_immatriculation is empty", rowNum);
                    validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR, "num_immatriculation cannot be empty.", "num_immatriculation"));
                    rowHasError = true;
                } else if (numImmat.length() != RsfFieldMapping2017.HORAIRE_NUM_IMMATRICULATION_LENGTH) {
                    log.info("VALIDATION FAILED for row {}: num_immatriculation has invalid length {}", rowNum, numImmat.length());
                    validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR,
                            "Invalid length for num_immatriculation. Expected " + RsfFieldMapping2017.HORAIRE_NUM_IMMATRICULATION_LENGTH + ", got " + numImmat.length() + ".", "num_immatriculation"));
                    rowHasError = true;
                }

                // Normalize and validate date_naissance
                String normalizedDateNaiss = ExcelParsingUtils.normalizeDate(dateNaiss);
                if (dateNaiss.isEmpty()) {
                    log.info("VALIDATION FAILED for row {}: date_naissance is empty", rowNum);
                    validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR, "date_naissance cannot be empty.", "date_naissance"));
                    rowHasError = true;
                } else if (!ExcelParsingUtils.isValidDate(dateNaiss)) {
                    log.info("VALIDATION FAILED for row {}: date_naissance has invalid format: {}", rowNum, dateNaiss);
                    validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR, 
                        "Invalid format for date_naissance. Expected YYYYMMDD, YYYY-MM-DD, or DD/MM/YYYY, got '" + dateNaiss + "'.", "date_naissance"));
                    rowHasError = true;
                } else {
                    // Replace with normalized date
                    dateNaiss = normalizedDateNaiss;
                }

                // Normalize and validate date_soins
                String normalizedDateSoins = ExcelParsingUtils.normalizeDate(dateSoins);
                if (dateSoins.isEmpty()) {
                    log.info("VALIDATION FAILED for row {}: date_soins is empty", rowNum);
                    validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR, "date_soins cannot be empty.", "date_soins"));
                    rowHasError = true;
                } else if (!ExcelParsingUtils.isValidDate(dateSoins)) {
                    log.info("VALIDATION FAILED for row {}: date_soins has invalid format: {}", rowNum, dateSoins);
                    validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR, 
                        "Invalid format for date_soins. Expected YYYYMMDD, YYYY-MM-DD, or DD/MM/YYYY, got '" + dateSoins + "'.", "date_soins"));
                    rowHasError = true;
                } else {
                    // Replace with normalized date
                    dateSoins = normalizedDateSoins;
                }

                if (codeActe.isEmpty()) {
                    log.info("VALIDATION FAILED for row {}: code_acte is empty", rowNum);
                    validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR, "code_acte cannot be empty.", "code_acte"));
                    rowHasError = true;
                } else if (codeActe.length() != RsfFieldMapping2017.HORAIRE_CODE_ACTE_LENGTH) {
                    log.info("VALIDATION FAILED for row {}: code_acte has invalid length {}", rowNum, codeActe.length());
                    validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR,
                            "Invalid length for code_acte. Expected " + RsfFieldMapping2017.HORAIRE_CODE_ACTE_LENGTH + ", got " + codeActe.length() + ".", "code_acte"));
                    rowHasError = true;
                }

                 if (horaire.isEmpty()) {
                     log.info("VALIDATION FAILED for row {}: horaire is empty", rowNum);
                     validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR, "horaire cannot be empty.", "horaire"));
                     rowHasError = true;
                 }
                // --- End Field Validations ---

                if (!rowHasError) {
                    log.info("ADDING row {} to updates list", rowNum);
                    updates.add(new HoraireUpdateRecord(rowNum, numImmat, dateNaiss, dateSoins, codeActe, horaire));
                } else {
                    log.info("SKIPPING row {} due to validation errors", rowNum);
                }
            }

            return ExcelParsingResult.horaireResult(updates, validationResult, totalRowsProcessed);

        } catch (Exception e) {
            log.error("Error parsing HORAIRE file {}: {}", fileName, e.getMessage(), e);
            validationResult.addError(new RsfError(0, fileName, RsfErrorType.STRUCTURAL_ERROR, "Failed to parse HORAIRE Excel file: " + e.getMessage()));
            return ExcelParsingResult.errorResult(validationResult);
        }
    }

    private ExcelParsingResult parseLignesFile(InputStream fileInputStream, String fileName, char lineType, RsfValidationResult validationResult) {
        List<Map<String, String>> records = new ArrayList<>();
        int totalRowsProcessed = 0;
        try (Workbook workbook = WorkbookFactory.create(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            // 1. Header Validation
            List<String> expectedHeaders = RsfFieldMapping2017.LIGNES_HEADERS_MAP.get(lineType);
            if (expectedHeaders == null) { // Should not happen due to check in caller, but defensive
                 validationResult.addError(new RsfError(0, fileName, RsfErrorType.FILE_NAME_ERROR, "Internal error: No header definition found for LIGNES type '" + lineType + "'."));
                return ExcelParsingResult.errorResult(validationResult);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                validationResult.addError(new RsfError(1, "", RsfErrorType.STRUCTURAL_ERROR, "Missing header row in LIGNES file type '" + lineType + "'."));
                return ExcelParsingResult.errorResult(validationResult);
            }

            List<String> actualHeaders = getActualHeaderValues(headerRow);

            // Check for exact match (order and name) and count
            if (actualHeaders.size() != expectedHeaders.size() || !actualHeaders.equals(expectedHeaders)) {
                 validationResult.addError(new RsfError(1, String.join("|", actualHeaders), RsfErrorType.FORMAT_ERROR,
                        "Invalid headers for LIGNES file type '" + lineType + "'. Expected: [" +
                                String.join(", ", expectedHeaders) + "], Found: [" +
                                String.join(", ", actualHeaders) + "]"));
                return ExcelParsingResult.errorResult(validationResult);
            }

            // 2. Data Extraction & Validation
            Map<String, RsfFieldMapping2017.FieldSpec> fieldSpecs = RsfFieldMapping2017.LIGNES_FIELD_SPECS_MAP.getOrDefault(lineType, Collections.emptyMap());
            Set<String> mandatoryFields = RsfFieldMapping2017.LIGNES_MANDATORY_FIELDS_MAP.getOrDefault(lineType, Collections.emptySet());

            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next(); // Skip header row

            int rowNum = 1; // Start from 1 for user-facing error messages
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;
                totalRowsProcessed++;
                boolean rowHasError = false;
                Map<String, String> rowData = new LinkedHashMap<>(); // Preserve column order
                String rawLineContent = formatRawLine(row, expectedHeaders.size());

                for (int i = 0; i < expectedHeaders.size(); i++) {
                    String headerName = expectedHeaders.get(i);
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String cellValue = ExcelParsingUtils.getCellStringValue(cell);
                    rowData.put(headerName, cellValue);

                    // --- Field Validations ---
                    // Mandatory check
                    if (mandatoryFields.contains(headerName) && cellValue.isEmpty()) {
                        validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR, "Mandatory field '" + headerName + "' cannot be empty.", headerName));
                        rowHasError = true;
                        continue; // Skip further validation for this field
                    }

                    // Format/Type/Length check based on spec (if value is not empty)
                    if (!cellValue.isEmpty() && fieldSpecs.containsKey(headerName)) {
                        RsfFieldMapping2017.FieldSpec spec = fieldSpecs.get(headerName);
                        boolean valid = true;
                        String errorMsg = null;

                        switch (spec.type()) {
                            case DATE:
                                if (!ExcelParsingUtils.isValidDate(cellValue)) {
                                    valid = false;
                                    errorMsg = "Invalid format for date field '" + headerName + "'. Expected YYYYMMDD, got '" + cellValue + "'.";
                                }
                                break;
                            case NUMERIC:
                                if (!ExcelParsingUtils.isNumeric(cellValue)) {
                                    valid = false;
                                    errorMsg = "Invalid format for numeric field '" + headerName + "'. Expected only digits, got '" + cellValue + "'.";
                                }
                                break;
                            case NUMERIC_DECIMAL:
                                // Allow more flexible decimal/integer format from Excel
                                if (!RsfFieldMapping2017.DECIMAL_PATTERN.matcher(cellValue.replace(',' ,'.')).matches()) {
                                     valid = false;
                                     errorMsg = "Invalid format for numeric/decimal field '" + headerName + "'. Expected number, got '" + cellValue + "'.";
                                }
                                break;
                            case ALPHANUMERIC:
                                // No specific format check, but length check applies below
                                break;
                        }

                        // Length check (if length > 0 in spec)
                        if (valid && spec.length() > 0 && cellValue.length() != spec.length()) {
                            valid = false;
                            errorMsg = "Invalid length for field '" + headerName + "'. Expected " + spec.length() + " characters, got " + cellValue.length() + ".";
                        }

                        if (!valid) {
                            validationResult.addError(new RsfError(rowNum, rawLineContent, RsfErrorType.DATA_ERROR, errorMsg, headerName));
                            rowHasError = true;
                        }
                    }
                     // --- End Field Validations ---
                }

                if (!rowHasError) {
                    records.add(rowData);
                }
            }

            return ExcelParsingResult.lignesResult(records, validationResult, totalRowsProcessed, lineType);

        } catch (Exception e) {
            log.error("Error parsing LIGNES file {} (Type {}): {}", fileName, lineType, e.getMessage(), e);
            validationResult.addError(new RsfError(0, fileName, RsfErrorType.STRUCTURAL_ERROR, "Failed to parse LIGNES Excel file (Type '" + lineType + "'): " + e.getMessage()));
            return ExcelParsingResult.errorResult(validationResult);
        }
    }

    private List<String> getActualHeaderValues(Row headerRow) {
        List<String> actualHeaders = new ArrayList<>();
         if (headerRow != null) {
             short lastCellNum = headerRow.getLastCellNum();
             for (int i = 0; i < lastCellNum; i++) {
                 Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                 actualHeaders.add(ExcelParsingUtils.getCellStringValue(cell));
             }
         }
        return actualHeaders;
    }

    // Helper to get a string representation of the row for error messages
    private String formatRawLine(Row row, int maxCells) {
        if (row == null) return "[EMPTY ROW]";
        List<String> cellValues = new ArrayList<>();
        int lastCell = Math.min(maxCells, row.getLastCellNum()); // Limit to expected number of cells
        for (int i = 0; i < lastCell; i++) {
            cellValues.add(ExcelParsingUtils.getCellStringValue(row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)));
        }
        return String.join("|", cellValues);
    }

    /**
     * Applies parsed Excel data (HORAIRE updates or LIGNES records) to RSF data.
     * 
     * @param excelResult The parsed Excel data (from parseExcelFromZip)
     * @param rsfData The RSF data to update, map of line type to list of line records
     * @return Integration result with counts of affected records and any errors
     */
    public RsfIntegrationService.RsfIntegrationResult applyExcelUpdatesToRsfData(
            ExcelParsingResult excelResult, 
            Map<Character, List<Map<String, String>>> rsfData) {
        
        if (excelResult == null) {
            return new RsfIntegrationService.RsfIntegrationResult(0, 0, List.of("Null Excel result provided"));
        }
        
        if (excelResult.getHoraireUpdates() != null) {
            // This is a HORAIRE file, apply updates to RSF B and C lines
            return rsfIntegrationService.integrateHoraireUpdates(excelResult, rsfData);
        } else if (excelResult.getLignesRecords() != null) {
            // This is a LIGNES file, add or update RSF lines of the specified type
            return rsfIntegrationService.integrateLignesRecords(excelResult, rsfData);
        } else {
            // Empty or error result
            return new RsfIntegrationService.RsfIntegrationResult(0, 0, 
                    List.of("Excel parsing result contains no data to apply"));
        }
    }

} 