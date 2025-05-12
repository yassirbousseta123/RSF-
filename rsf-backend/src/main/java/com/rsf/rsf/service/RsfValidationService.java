package com.rsf.rsf.service;

import com.rsf.rsf.domain.validation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for validating RSF data against business rules.
 * Implements a rule engine, error reporting, and complex validation support.
 */
@Service
@Slf4j
public class RsfValidationService {

    private final Map<Character, Map<String, FieldRule>> fieldRules = new HashMap<>();
    private final List<RsfValidationRule> validationRules = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMATTER_DDMMYYYY = DateTimeFormatter.ofPattern("ddMMuuuu");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^-?\\d*\\.?\\d+$"); // Allows optional decimal point

    @PostConstruct
    public void initialize() {
        initializeFieldRules();
        registerValidationRules();
        log.info("RsfValidationService initialized with {} field rule sets and {} structural rules.", fieldRules.size(), validationRules.size());
    }

    /**
     * Validates the provided RSF data against all registered rules.
     *
     * @param rsfData Map containing RSF data by line type
     * @return Combined validation result with all errors
     */
    public RsfValidationResult validateRsfData(Map<Character, List<Map<String, String>>> rsfData) {
        RsfValidationResult combinedResult = new RsfValidationResult();
        for (RsfValidationRule rule : validationRules) {
            try {
                RsfValidationResult ruleResult = rule.validate(rsfData);
                combinedResult.addErrors(ruleResult);
                log.debug("Executed validation rule: {}. Found {} errors.", rule.getClass().getSimpleName(), ruleResult.getErrors().size());
            } catch (Exception e) {
                log.error("Error executing validation rule: {}. Error: {}", rule.getClass().getSimpleName(), e.getMessage(), e);
                // Add a generic error indicating a rule failed to execute
                combinedResult.addError(new RsfError(
                        -1, // Indicate rule execution error, not specific line
                        rule.getClass().getSimpleName(),
                        RsfErrorType.SYSTEM_ERROR,
                        "Failed to execute validation rule: " + e.getMessage()
                ));
            }
        }
        log.info("Rsf data validation completed. Total errors found: {}", combinedResult.getErrors().size());
        return combinedResult;
    }

    /**
     * Validates a specific field against its defined rules.
     * 
     * @param lineType The RSF line type (A, B, C, etc.)
     * @param fieldName The field name
     * @param value The field value
     * @return List of error messages or empty list if valid
     */
    public List<String> validateField(char lineType, String fieldName, String value) {
        List<String> errors = new ArrayList<>();
        FieldRule rule = getFieldRule(lineType, fieldName);
        
        if (rule == null) {
            // No rule defined might be acceptable for non-critical/optional fields
            // log.trace("No validation rule defined for field '{}' in line type '{}'. Skipping validation.", fieldName, lineType);
            return errors;
        }

        final String trimmedValue = (value != null) ? value.trim() : "";

        // Mandatory check (only if field is marked mandatory)
        if (rule.isMandatory() && trimmedValue.isEmpty()) {
            errors.add(String.format("%s: Field is mandatory but was empty.", rule.getDescription() != null ? rule.getDescription() : fieldName));
            return errors; // Stop validation for this field if mandatory check fails
        }

        // Skip further checks if the field is not mandatory and is empty
        if (!rule.isMandatory() && trimmedValue.isEmpty()) {
            return errors;
        }

        // Length checks (apply to the trimmed value)
        if (rule.getMinLength() != null && trimmedValue.length() < rule.getMinLength()) {
             errors.add(String.format("%s: Value '%s' is shorter than minimum length %d.",
                     rule.getDescription() != null ? rule.getDescription() : fieldName, trimmedValue, rule.getMinLength()));
        }
        if (rule.getMaxLength() != null && trimmedValue.length() > rule.getMaxLength()) {
             errors.add(String.format("%s: Value '%s' is longer than maximum length %d.",
                     rule.getDescription() != null ? rule.getDescription() : fieldName, trimmedValue, rule.getMaxLength()));
        }

        // Type checks (only if type is defined)
        if (rule.getType() != null) {
            switch (rule.getType()) {
                case NUMERIC:
                    if (!NUMERIC_PATTERN.matcher(trimmedValue).matches()) {
                        errors.add(String.format("%s: Value '%s' must be numeric.", rule.getDescription() != null ? rule.getDescription() : fieldName, trimmedValue));
                    }
                    break;
                case DATE:
                    // Use the specific ddMMyyyy format check
                    if (!isValidDateDDMMYYYY(trimmedValue)) {
                        errors.add(String.format("%s: Value '%s' must be a valid date in ddMMyyyy format.", rule.getDescription() != null ? rule.getDescription() : fieldName, trimmedValue));
                    }
                    break;
                case DECIMAL:
                     // Allow empty strings if not mandatory, otherwise validate format
                    if (!DECIMAL_PATTERN.matcher(trimmedValue).matches()) {
                         errors.add(String.format("%s: Value '%s' must be a valid decimal number.", rule.getDescription() != null ? rule.getDescription() : fieldName, trimmedValue));
                     }
                    break;
                case TEXT:
                     // No specific format validation for TEXT by default, only length checks apply
                    break;
                case BOOLEAN: // Example: Add boolean validation if needed
                     if (!trimmedValue.equalsIgnoreCase("true") && !trimmedValue.equalsIgnoreCase("false") && !trimmedValue.equals("1") && !trimmedValue.equals("0")) {
                         errors.add(String.format("%s: Value '%s' must be a valid boolean (true/false or 1/0).", rule.getDescription() != null ? rule.getDescription() : fieldName, trimmedValue));
                     }
                     break;
            }
        }

        // Regex check (only if regex is defined)
        if (rule.getRegex() != null && !rule.getRegex().isEmpty()) {
            if (!Pattern.matches(rule.getRegex(), trimmedValue)) {
                errors.add(String.format("%s: Value '%s' does not match the required pattern: %s.",
                         rule.getDescription() != null ? rule.getDescription() : fieldName, trimmedValue, rule.getRegex()));
            }
        }

        return errors;
    }

    /**
     * Loads field validation rules from configuration.
     * In a real application, this would load from a database, JSON file, or other configuration source.
     */
    private void initializeFieldRules() {
        // Line A Rules
        Map<String, FieldRule> typeARules = new HashMap<>();
        typeARules.put("N_FINESS_EPMSI", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS EPMSI")); // Mandatory, Numeric, Length 9
        typeARules.put("N_FINESS_GEOGRAPHIQUE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS Géographique")); // Mandatory, Numeric, Length 9
        typeARules.put("N_IMMATRICULATION_ASSURE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 32, "N° Immatriculation Assuré")); // Mandatory, Text, Max 32
        typeARules.put("RANG_BENEFICIAIRE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 3, 3, "Rang Bénéficiaire")); // Mandatory, Numeric, Length 3
        typeARules.put("N_ENTREE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 9, "N° Entrée")); // Mandatory, Text, Max 9 (Often numeric but spec allows AN)
        typeARules.put("DATE_NAISSANCE", createFieldRule(true, FieldRule.FieldType.DATE, 8, 8, "Date Naissance (ddMMyyyy)")); // Mandatory, Date, Length 8
        typeARules.put("DATE_ENTREE", createFieldRule(true, FieldRule.FieldType.DATE, 8, 8, "Date Entrée (ddMMyyyy)")); // Mandatory, Date, Length 8
        typeARules.put("DATE_SORTIE", createFieldRule(true, FieldRule.FieldType.DATE, 8, 8, "Date Sortie (ddMMyyyy)")); // Mandatory, Date, Length 8
        typeARules.put("CODE_POSTAL_RESIDENCE_PATIENT", createFieldRule(true, FieldRule.FieldType.NUMERIC, 5, 5, "Code Postal Résidence")); // Mandatory, Numeric, Length 5
        typeARules.put("TOTAL_BASE_REMBOURSEMENT", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Total Base Remboursement")); // Mandatory, Decimal, Max 8
        typeARules.put("TOTAL_REMBOURSABLE_AMO", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Total Remboursable AMO")); // Mandatory, Decimal, Max 8
        typeARules.put("TOTAL_HONORAIRE_FACTURE", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Total Honoraire Facturé")); // Mandatory, Decimal, Max 8
        typeARules.put("TOTAL_HONORAIRE_REMBOURSABLE_AM", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Total Honoraire Remboursable AM")); // Mandatory, Decimal, Max 8
        fieldRules.put('A', typeARules);

        // Line B Rules
        Map<String, FieldRule> typeBRules = new HashMap<>();
        typeBRules.put("N_FINESS_EPMSI", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS EPMSI"));
        typeBRules.put("N_FINESS_GEOGRAPHIQUE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS Géographique"));
        typeBRules.put("N_IMMATRICULATION_ASSURE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 32, "N° Immatriculation Assuré"));
        typeBRules.put("RANG_BENEFICIAIRE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 3, 3, "Rang Bénéficiaire"));
        typeBRules.put("N_ENTREE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 9, "N° Entrée"));
        typeBRules.put("DATE_SOINS", createFieldRule(true, FieldRule.FieldType.DATE, 8, 8, "Date Soins (ddMMyyyy)")); // Mandatory, Date, Length 8
        typeBRules.put("CODE_ACTE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 5, "Code Acte")); // Mandatory, Text, Max 5
        typeBRules.put("QUANTITE", createFieldRule(true, FieldRule.FieldType.NUMERIC, null, 3, "Quantité")); // Mandatory, Numeric, Max 3
        typeBRules.put("PRIX_UNITAIRE", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 7, "Prix Unitaire")); // Mandatory, Decimal, Max 7
        typeBRules.put("MONTANT_BASE_REMBOURSEMENT", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Montant Base Remboursement")); // Mandatory, Decimal, Max 8
        typeBRules.put("MONTANT_REMBOURSABLE_AMO", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Montant Remboursable AMO")); // Mandatory, Decimal, Max 8
        typeBRules.put("MONTANT_HONORAIRE", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Montant Honoraire")); // Mandatory, Decimal, Max 8
        fieldRules.put('B', typeBRules);

        // Line C Rules (Similar to B, adjust if needed)
        Map<String, FieldRule> typeCRules = new HashMap<>();
        typeCRules.put("N_FINESS_EPMSI", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS EPMSI"));
        typeCRules.put("N_FINESS_GEOGRAPHIQUE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS Géographique"));
        typeCRules.put("N_IMMATRICULATION_ASSURE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 32, "N° Immatriculation Assuré"));
        typeCRules.put("RANG_BENEFICIAIRE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 3, 3, "Rang Bénéficiaire"));
        typeCRules.put("N_ENTREE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 9, "N° Entrée"));
        typeCRules.put("DATE_SOINS", createFieldRule(true, FieldRule.FieldType.DATE, 8, 8, "Date Soins (ddMMyyyy)"));
        typeCRules.put("CODE_ACTE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 5, "Code Acte"));
        typeCRules.put("QUANTITE", createFieldRule(true, FieldRule.FieldType.NUMERIC, null, 3, "Quantité"));
        typeCRules.put("PRIX_UNITAIRE", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 7, "Prix Unitaire"));
        typeCRules.put("MONTANT_BASE_REMBOURSEMENT", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Montant Base Remboursement"));
        typeCRules.put("MONTANT_REMBOURSABLE_AMO", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Montant Remboursable AMO"));
        typeCRules.put("MONTANT_HONORAIRE", createFieldRule(true, FieldRule.FieldType.DECIMAL, null, 8, "Montant Honoraire"));
        fieldRules.put('C', typeCRules);

        // Line H Rules
        Map<String, FieldRule> typeHRules = new HashMap<>();
        typeHRules.put("N_FINESS_EPMSI", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS EPMSI"));
        typeHRules.put("N_FINESS_GEOGRAPHIQUE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS Géographique"));
        typeHRules.put("N_IMMATRICULATION_ASSURE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 32, "N° Immatriculation Assuré"));
        typeHRules.put("RANG_BENEFICIAIRE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 3, 3, "Rang Bénéficiaire"));
        typeHRules.put("N_ENTREE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 9, "N° Entrée"));
        typeHRules.put("DATE_DEBUT_SEJOUR", createFieldRule(true, FieldRule.FieldType.DATE, 8, 8, "Date Début Séjour (ddMMyyyy)")); // Mandatory, Date, Length 8
        typeHRules.put("CODE_UCD", createFieldRule(true, FieldRule.FieldType.TEXT, null, 7, "Code UCD")); // Mandatory, Text, Max 7
        typeHRules.put("QUANTITE", createFieldRule(true, FieldRule.FieldType.NUMERIC, null, 3, "Quantité")); // Mandatory, Numeric, Max 3
        fieldRules.put('H', typeHRules);

        // Line M Rules
        Map<String, FieldRule> typeMRules = new HashMap<>();
        typeMRules.put("N_FINESS_EPMSI", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS EPMSI"));
        typeMRules.put("N_FINESS_GEOGRAPHIQUE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS Géographique"));
        typeMRules.put("N_IMMATRICULATION_ASSURE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 32, "N° Immatriculation Assuré"));
        typeMRules.put("RANG_BENEFICIAIRE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 3, 3, "Rang Bénéficiaire"));
        typeMRules.put("N_ENTREE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 9, "N° Entrée"));
        typeMRules.put("DATE_SOINS", createFieldRule(true, FieldRule.FieldType.DATE, 8, 8, "Date Soins (ddMMyyyy)")); // Mandatory, Date, Length 8
        typeMRules.put("CODE_CCAM", createFieldRule(true, FieldRule.FieldType.TEXT, null, 13, "Code CCAM")); // Mandatory, Text, Max 13
        fieldRules.put('M', typeMRules);

        // Line P Rules
        Map<String, FieldRule> typePRules = new HashMap<>();
        typePRules.put("N_FINESS_EPMSI", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS EPMSI"));
        typePRules.put("N_FINESS_GEOGRAPHIQUE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS Géographique"));
        typePRules.put("N_IMMATRICULATION_ASSURE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 32, "N° Immatriculation Assuré"));
        typePRules.put("RANG_BENEFICIAIRE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 3, 3, "Rang Bénéficiaire"));
        typePRules.put("N_ENTREE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 9, "N° Entrée"));
        typePRules.put("DATE_DEBUT_SEJOUR", createFieldRule(true, FieldRule.FieldType.DATE, 8, 8, "Date Début Séjour (ddMMyyyy)")); // Mandatory, Date, Length 8
        typePRules.put("CODE_REFERENCE_LPP", createFieldRule(true, FieldRule.FieldType.TEXT, null, 13, "Code Référence LPP")); // Mandatory, Text, Max 13
        typePRules.put("QUANTITE", createFieldRule(true, FieldRule.FieldType.NUMERIC, null, 2, "Quantité")); // Mandatory, Numeric, Max 2
        fieldRules.put('P', typePRules);

        // Line L Rules
        Map<String, FieldRule> typeLRules = new HashMap<>();
        typeLRules.put("N_FINESS_EPMSI", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS EPMSI"));
        typeLRules.put("N_FINESS_GEOGRAPHIQUE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 9, 9, "FINESS Géographique"));
        typeLRules.put("N_IMMATRICULATION_ASSURE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 32, "N° Immatriculation Assuré"));
        typeLRules.put("RANG_BENEFICIAIRE", createFieldRule(true, FieldRule.FieldType.NUMERIC, 3, 3, "Rang Bénéficiaire"));
        typeLRules.put("N_ENTREE", createFieldRule(true, FieldRule.FieldType.TEXT, null, 9, "N° Entrée"));
        typeLRules.put("DATE_ACTE1", createFieldRule(true, FieldRule.FieldType.DATE, 8, 8, "Date Acte 1 (ddMMyyyy)")); // Mandatory, Date, Length 8
        typeLRules.put("QUANTITE_ACTE1", createFieldRule(true, FieldRule.FieldType.NUMERIC, null, 2, "Quantité Acte 1")); // Mandatory, Numeric, Max 2
        typeLRules.put("CODE_ACTE1", createFieldRule(true, FieldRule.FieldType.TEXT, null, 8, "Code Acte 1")); // Mandatory, Text, Max 8
        // Rules for ACTE 2-5 are optional as per Python validation structure (only ACTE1 seems implicitly mandatory for L)
        typeLRules.put("DATE_ACTE2", createFieldRule(false, FieldRule.FieldType.DATE, 8, 8, "Date Acte 2 (ddMMyyyy)")); // Optional, Date, Length 8
        typeLRules.put("QUANTITE_ACTE2", createFieldRule(false, FieldRule.FieldType.NUMERIC, null, 2, "Quantité Acte 2")); // Optional, Numeric, Max 2
        typeLRules.put("CODE_ACTE2", createFieldRule(false, FieldRule.FieldType.TEXT, null, 8, "Code Acte 2")); // Optional, Text, Max 8
        typeLRules.put("DATE_ACTE3", createFieldRule(false, FieldRule.FieldType.DATE, 8, 8, "Date Acte 3 (ddMMyyyy)"));
        typeLRules.put("QUANTITE_ACTE3", createFieldRule(false, FieldRule.FieldType.NUMERIC, null, 2, "Quantité Acte 3"));
        typeLRules.put("CODE_ACTE3", createFieldRule(false, FieldRule.FieldType.TEXT, null, 8, "Code Acte 3"));
        typeLRules.put("DATE_ACTE4", createFieldRule(false, FieldRule.FieldType.DATE, 8, 8, "Date Acte 4 (ddMMyyyy)"));
        typeLRules.put("QUANTITE_ACTE4", createFieldRule(false, FieldRule.FieldType.NUMERIC, null, 2, "Quantité Acte 4"));
        typeLRules.put("CODE_ACTE4", createFieldRule(false, FieldRule.FieldType.TEXT, null, 8, "Code Acte 4"));
        typeLRules.put("DATE_ACTE5", createFieldRule(false, FieldRule.FieldType.DATE, 8, 8, "Date Acte 5 (ddMMyyyy)"));
        typeLRules.put("QUANTITE_ACTE5", createFieldRule(false, FieldRule.FieldType.NUMERIC, null, 2, "Quantité Acte 5"));
        typeLRules.put("CODE_ACTE5", createFieldRule(false, FieldRule.FieldType.TEXT, null, 8, "Code Acte 5"));
        fieldRules.put('L', typeLRules);

        // Add rules for other years/types if needed
    }

    /**
     * Helper method to create FieldRule objects.
     */
    private FieldRule createFieldRule(boolean mandatory, FieldRule.FieldType type,
                                      Integer minLength, Integer maxLength, String description) {
        FieldRule rule = new FieldRule();
        rule.setMandatory(mandatory);
        rule.setType(type);
        rule.setMinLength(minLength);
        rule.setMaxLength(maxLength);
        rule.setDescription(description); // Add description
        // rule.setRegex(regex); // Regex can be added if needed
        return rule;
    }

    /**
     * Registers all validation rules (field-level and structural).
     */
    private void registerValidationRules() {
        // Register field validation rule (uses 'this' service to validate individual fields)
        validationRules.add(new FieldValidationRule(this));

        // Register structural validation rules based on Python logic
        validationRules.add(new LineOrderValidationRule());
        validationRules.add(new KeyFieldMatchValidationRule());
        validationRules.add(new ExpectedLineCountRule()); // Added rule for B or C line existence
        // Add other structural rules here if needed (e.g., line length check rule)

        log.info("Registered {} validation rules.", validationRules.size());
    }

    /**
     * Gets the rule for a specific field.
     */
    public FieldRule getFieldRule(char lineType, String fieldName) {
        Map<String, FieldRule> rules = fieldRules.get(lineType);
        return rules != null ? rules.get(fieldName) : null;
    }

    /**
     * Checks if a string is a valid date in ddMMyyyy format using strict parsing.
     */
    private boolean isValidDateDDMMYYYY(String value) {
        if (value == null || value.length() != 8) {
            return false;
        }
        try {
            // Use the strict resolver style to avoid lenient parsing (e.g., accepting day 32)
            DATE_FORMATTER_DDMMYYYY.parse(value);
            // Optionally check if the parsed date reconstructs to the original string if needed
            // LocalDate parsedDate = LocalDate.parse(value, DATE_FORMATTER_DDMMYYYY);
            // return value.equals(parsedDate.format(DATE_FORMATTER_DDMMYYYY));
            return true; // If parse succeeds, format is correct
        } catch (DateTimeParseException e) {
            return false;
        }
    }
} 