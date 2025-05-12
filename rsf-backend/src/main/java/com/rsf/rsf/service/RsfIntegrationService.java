package com.rsf.rsf.service;

import com.rsf.rsf.domain.models.ExcelParsingResult;
import com.rsf.rsf.domain.models.HoraireUpdateRecord;
import com.rsf.rsf.domain.validation.RsfError;
import com.rsf.rsf.domain.validation.RsfErrorType;
import com.rsf.rsf.domain.validation.RsfValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to integrate Excel parsed data (HORAIRE or LIGNES) with RSF data.
 * Handles updating RSF lines with horaire values and importing/updating RSF lines from LIGNES data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RsfIntegrationService {

    /**
     * Integrates HORAIRE updates with RSF data.
     * Matches records based on the quadruple (num_immatriculation, date_naissance, date_soins, code_acte)
     * and updates the horaire field in matching RSF type B or C lines.
     *
     * @param excelResult The parsed HORAIRE Excel file result
     * @param rsfData Map containing RSF data by line type (e.g., 'A', 'B', 'C')
     * @return A summary of the integration with success/failure counts
     */
    public RsfIntegrationResult integrateHoraireUpdates(ExcelParsingResult excelResult, Map<Character, List<Map<String, String>>> rsfData) {
        if (excelResult == null || excelResult.getHoraireUpdates() == null) {
            return new RsfIntegrationResult(0, 0, List.of("No HORAIRE updates to process"));
        }

        List<HoraireUpdateRecord> updates = excelResult.getHoraireUpdates();
        List<String> errors = new ArrayList<>();
        int updatedCount = 0;

        // Process each HORAIRE update record
        for (HoraireUpdateRecord update : updates) {
            boolean matchFound = false;

            // Try to match with type B lines
            if (rsfData.containsKey('B')) {
                for (Map<String, String> bLine : rsfData.get('B')) {
                    if (isMatchingLine(bLine, update, rsfData.get('A'))) {
                        bLine.put("horaire", update.getHoraire());
                        updatedCount++;
                        matchFound = true;
                        break;
                    }
                }
            }

            // Try to match with type C lines if not found in B
            if (!matchFound && rsfData.containsKey('C')) {
                for (Map<String, String> cLine : rsfData.get('C')) {
                    if (isMatchingLine(cLine, update, rsfData.get('A'))) {
                        cLine.put("horaire", update.getHoraire());
                        updatedCount++;
                        matchFound = true;
                        break;
                    }
                }
            }

            // Report if no match found
            if (!matchFound) {
                errors.add("Row " + update.getSourceRowNum() + ": No matching B or C line found for " +
                        "numImmatriculation=" + update.getNumImmatriculation() + 
                        ", dateNaissance=" + update.getDateNaissance() + 
                        ", dateSoins=" + update.getDateSoins() + 
                        ", codeActe=" + update.getCodeActe());
            }
        }

        return new RsfIntegrationResult(updates.size(), updatedCount, errors);
    }

    /**
     * Integrates LIGNES records with RSF data.
     * Either adds new lines or updates existing lines based on matching key fields.
     *
     * @param excelResult The parsed LIGNES Excel file result
     * @param rsfData Map containing RSF data by line type (e.g., 'A', 'B', 'C')
     * @return A summary of the integration with success/failure counts
     */
    public RsfIntegrationResult integrateLignesRecords(ExcelParsingResult excelResult, Map<Character, List<Map<String, String>>> rsfData) {
        if (excelResult == null || excelResult.getLignesRecords() == null) {
            return new RsfIntegrationResult(0, 0, List.of("No LIGNES records to process"));
        }

        List<Map<String, String>> records = excelResult.getLignesRecords();
        char lineType = excelResult.getParsedLineType();
        List<String> errors = new ArrayList<>();
        int addedCount = 0;
        int updatedCount = 0;

        // Initialize the line type list if it doesn't exist
        if (!rsfData.containsKey(lineType)) {
            rsfData.put(lineType, new ArrayList<>());
        }

        // Process each LIGNES record
        for (int i = 0; i < records.size(); i++) {
            Map<String, String> record = records.get(i);
            boolean isUpdate = false;

            // Check if it's an update (matching key fields)
            if (rsfData.containsKey(lineType)) {
                for (int j = 0; j < rsfData.get(lineType).size(); j++) {
                    Map<String, String> existingLine = rsfData.get(lineType).get(j);
                    if (isMatchingKeyFields(existingLine, record, lineType)) {
                        // Update existing line with new values
                        rsfData.get(lineType).set(j, record);
                        updatedCount++;
                        isUpdate = true;
                        break;
                    }
                }
            }

            // If it's not an update, add as a new line
            if (!isUpdate) {
                rsfData.get(lineType).add(record);
                addedCount++;
            }
        }

        return new RsfIntegrationResult(records.size(), addedCount, updatedCount, errors);
    }

    /**
     * Checks if an RSF line (type B or C) matches the HORAIRE update record.
     * Matching is based on N_IMMATRICULATION_ASSURE, DATE_SOINS, CODE_ACTE, and DATE_NAISSANCE 
     * from the parent A line.
     *
     * @param rsfLine RSF line (type B or C)
     * @param update HORAIRE update record
     * @param aLines List of A type lines to find parent for date_naissance
     * @return true if matches, false otherwise
     */
    private boolean isMatchingLine(Map<String, String> rsfLine, HoraireUpdateRecord update, List<Map<String, String>> aLines) {
        // Check direct fields match
        if (!rsfLine.get("N_IMMATRICULATION_ASSURE").equals(update.getNumImmatriculation()) ||
            !rsfLine.get("DATE_SOINS").equals(update.getDateSoins()) ||
            !rsfLine.get("CODE_ACTE").equals(update.getCodeActe())) {
            return false;
        }

        // Find parent A line to check DATE_NAISSANCE
        String nEntree = rsfLine.get("N_ENTREE");
        String nImmatriculation = rsfLine.get("N_IMMATRICULATION_ASSURE");
        
        for (Map<String, String> aLine : aLines) {
            if (aLine.get("N_ENTREE").equals(nEntree) && 
                aLine.get("N_IMMATRICULATION_ASSURE").equals(nImmatriculation) &&
                aLine.get("DATE_NAISSANCE").equals(update.getDateNaissance())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Checks if two RSF lines match based on key fields for the line type.
     *
     * @param line1 First RSF line
     * @param line2 Second RSF line
     * @param lineType Type of RSF line ('A', 'B', 'C', etc.)
     * @return true if key fields match, false otherwise
     */
    private boolean isMatchingKeyFields(Map<String, String> line1, Map<String, String> line2, char lineType) {
        // Common key fields for all line types
        if (!line1.get("N_FINESS_EPMSI").equals(line2.get("N_FINESS_EPMSI")) ||
            !line1.get("N_FINESS_GEOGRAPHIQUE").equals(line2.get("N_FINESS_GEOGRAPHIQUE")) ||
            !line1.get("N_IMMATRICULATION_ASSURE").equals(line2.get("N_IMMATRICULATION_ASSURE")) ||
            !line1.get("N_ENTREE").equals(line2.get("N_ENTREE"))) {
            return false;
        }

        // Additional key fields based on line type
        switch (lineType) {
            case 'A':
                return true; // Common fields are sufficient for type A
            case 'B':
            case 'C':
                return line1.get("DATE_SOINS").equals(line2.get("DATE_SOINS")) &&
                       line1.get("CODE_ACTE").equals(line2.get("CODE_ACTE"));
            case 'M':
                return line1.get("DATE_SOINS").equals(line2.get("DATE_SOINS")) &&
                       line1.get("CODE_CCAM").equals(line2.get("CODE_CCAM"));
            case 'P':
                return line1.get("DATE_DEBUT_SEJOUR").equals(line2.get("DATE_DEBUT_SEJOUR")) &&
                       line1.get("CODE_REFERENCE_LPP").equals(line2.get("CODE_REFERENCE_LPP"));
            case 'L':
                return line1.get("DATE_ACTE1").equals(line2.get("DATE_ACTE1")) &&
                       line1.get("CODE_ACTE1").equals(line2.get("CODE_ACTE1"));
            case 'H':
                return line1.get("DATE_DEBUT_SEJOUR").equals(line2.get("DATE_DEBUT_SEJOUR")) &&
                       line1.get("CODE_UCD").equals(line2.get("CODE_UCD"));
            default:
                return false;
        }
    }

    /**
     * Result class for RSF integration operations.
     */
    public static class RsfIntegrationResult {
        private final int totalProcessed;
        private final int addedCount;
        private final int updatedCount;
        private final List<String> errors;

        public RsfIntegrationResult(int totalProcessed, int updatedCount, List<String> errors) {
            this.totalProcessed = totalProcessed;
            this.addedCount = 0;
            this.updatedCount = updatedCount;
            this.errors = errors;
        }

        public RsfIntegrationResult(int totalProcessed, int addedCount, int updatedCount, List<String> errors) {
            this.totalProcessed = totalProcessed;
            this.addedCount = addedCount;
            this.updatedCount = updatedCount;
            this.errors = errors;
        }

        public int getTotalProcessed() {
            return totalProcessed;
        }

        public int getAddedCount() {
            return addedCount;
        }

        public int getUpdatedCount() {
            return updatedCount;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
} 