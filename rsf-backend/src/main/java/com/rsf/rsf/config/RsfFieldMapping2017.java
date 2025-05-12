package com.rsf.rsf.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds the RSF field definitions, validation rules, and expected headers for 2017 Excel imports.
 */
@Component
@Getter
public class RsfFieldMapping2017 {

    public static final Pattern YYYYMMDD_PATTERN = Pattern.compile("^(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])$"); // Stricter YYYYMMDD
    public static final Pattern NUMERIC_PATTERN = Pattern.compile("^\\d+$"); // Only digits
    public static final Pattern SIGNED_NUMERIC_PATTERN = Pattern.compile("^-?\\d+$"); // Optional sign, then digits
    public static final Pattern DECIMAL_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$"); // Optional sign, digits, optional decimal part

    // --- HORAIRE Specific ---
    public static final List<String> HORAIRE_EXPECTED_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "num_immatriculation", "date_naissance", "date_soins", "code_acte", "horaire"
    ));

    public static final int HORAIRE_NUM_IMMATRICULATION_LENGTH = 13;
    public static final int HORAIRE_CODE_ACTE_LENGTH = 5;


    // --- LIGNES Specific ---

    // Updated Headers based on user input
    public static final List<String> LIGNES_A_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "TYPE_ENREGISTREMENT", "N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "SEXE", "CODE_CIVILITE",
            "N_IMMATRICULATION_ASSURE", "CLE_N_IMMATRICULATION", "RANG_BENEFICIAIRE", "N_ENTREE",
            "N_IMMATRICULATION_INDIVIDUEL", "CLE_N_IMMATRICULATION_INDIVIDUEL", "INDICATEUR_PARCOURS_SOINS",
            "NATURE_OPERATION", "NATURE_ASSURANCE", "TYPE_CONTRAT_ORGANISME_COMPLEMENTAIRE", "JUSTIF_EXO_TM",
            "SEJOUR_FACTURABLE_ASSURANCE_MALADIE", "FILLER_1", "MOTIF_NON_FACTURATION_ASSURANCE_MALADIE",
            "CODE_GD_REGIME", "DATE_NAISSANCE", "RANG_NAISSANCE", "DATE_ENTREE", "DATE_SORTIE",
            "CODE_POSTAL_RESIDENCE_PATIENT", "TOTAL_BASE_REMBOURSEMENT", "TOTAL_REMBOURSABLE_AMO",
            "TOTAL_HONORAIRE_FACTURE", "TOTAL_HONORAIRE_REMBOURSABLE_AM", "TOTAL_PARTICIPATION_ASSURE_AVANT_OC",
            "TOTAL_REMBOURSABLE_OC_PH", "TOTAL_REMBOURSABLE_OC_HONORAIRES", "MONTANT_TOTAL_FACTUREPH", "NuméroA"
    ));

    public static final List<String> LIGNES_B_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "TYPE_ENREGISTREMENT", "N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE",
            "CLE_N_IMMATRICULATION", "RANG_BENEFICIAIRE", "N_ENTREE", "N_IMMATRICULATION_INDIVIDUEL",
            "CLE_N_IMMATRICULATION_INDIVIDUEL", "MODE_TRAITEMENT", "DISCIPLINE_PRESTATION",
            "JUSTIFICATION_EXOTM", "SPECIALITE_EXECUTANT", "DATE_SOINS", "CODE_ACTE", "QUANTITE",
            "COEFFICIENT", "TYPE_PRESTATION_INTERMEDIAIRE", "COEFFICIENT_MCO", "DENOMBREMENT",
            "PRIXUNITAIRE", "MONTANT_BASE_REMBOURSEMENT", "TAUX_APPLICABLE", "MONTANT_REMBOURSABLE_AMO",
            "MONTANT_HONORAIRE", "MONTANT_REMBOURSABLE_AMC", "FILLER", "NuméroB"
    ));

    public static final List<String> LIGNES_C_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "TYPE_ENREGISTREMENT", "N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE",
            "CLE_N_IMMATRICULATION", "RANG_BENEFICIAIRE", "N_ENTREE", "N_IMMATRICULATION_INDIVIDUEL",
            "CLE_N_IMMATRICULATION_INDIVIDUEL", "MODE_TRAITEMENT", "DISCIPLINE_PRESTATION",
            "JUSTIFICATION_EXOTM", "SPECIALITE_EXECUTANT", "DATE_SOINS", "CODE_ACTE", "QUANTITE",
            "COEFFICIENT", "DENOMBREMENT", "PRIXUNITAIRE", "MONTANT_REMBOURSABLE_AMO", // Name discrepancy noted in user spec comment, using this one.
            "TAUX_APPLICABLE", "MONTANT_REMBOURSABLE_AMO_1", // Name discrepancy noted
            "MONTANT_HONORAIRE", "MONTANT_REMBOURSABLE_AMC", "FILLER", "TYPE_UNITE_FONCTIONNELLE",
            "COEFFICIENT_MCO", "NuméroC"
    ));

    public static final List<String> LIGNES_H_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "TYPE_ENREGISTREMENT", "N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE",
            "CLE_N_IMMATRICULATION", "RANG_BENEFICIAIRE", "N_ENTREE", "N_IMMATRICULATION_INDIVIDUEL",
            "CLE_N_IMMATRICULATION_INDIVIDUEL", "DATE_DEBUT_SEJOUR", "CODE_UCD", "COEFF_FRACTIONNEMENT",
            "PRIX_ACHAT_UNITAIRE", "MONTANT_UNITAIRE_ECART", "MONTANT_TOTAL_ECART", "QUANTITE",
            "TOTAL_FACTURE", "Indication", "NuméroH"
    ));

    public static final List<String> LIGNES_M_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "TYPE_ENREGISTREMENT", "N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE",
            "CLE_N_IMMATRICULATION", "RANG_BENEFICIAIRE", "N_ENTREE", "N_IMMATRICULATION_INDIVIDUEL",
            "CLE_N_IMMATRICULATION_INDIVIDUEL", "MODE_TRAITEMENT", "DISCIPLINE_PRESTATION", "DATE_SOINS",
            "CODE_CCAM", "EXTENSION_DOCUMENTAIRE", "ACTIVITE", "PHASE", "MODIFICATEUR1", "MODIFICATEUR2",
            "MODIFICATEUR3", "MODIFICATEUR4", "CODE_ASSOCIATION_ACTE", "CODE_REMBOURSEMENT", "NUM_DENT1",
            "NUM_DENT2", "NUM_DENT3", "NUM_DENT4", "NUM_DENT5", "NUM_DENT6", "NUM_DENT7", "NUM_DENT8",
            "NUM_DENT9", "NUM_DENT10", "NUM_DENT11", "NUM_DENT12", "NUM_DENT13", "NUM_DENT14",
            "NUM_DENT15", "NUM_DENT16", "NUMÉRO_M"
    ));

    public static final List<String> LIGNES_P_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "TYPE_ENREGISTREMENT", "N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE",
            "CLE_N_IMMATRICULATION", "RANG_BENEFICIAIRE", "N_ENTREE", "N_IMMATRICULATION_INDIVIDUEL",
            "CLE_N_IMMATRICULATION_INDIVIDUEL", "DATE_DEBUT_SEJOUR", "CODE_REFERENCE_LPP", "QUANTITE",
            "TARIF_REFERENCE_LPP", "MONTANT_TOTAL_FACTURE", "PRIX_ACHAT_UNITAIRE",
            "MONTANT_UNITAIRE_ECART", "MONTANT_TOTAL_ECART", "NuméroP"
    ));

    public static final List<String> LIGNES_L_HEADERS = Collections.unmodifiableList(Arrays.asList(
            "TYPE_ENREGISTREMENT", "N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE",
            "CLE_N_IMMATRICULATION", "RANG_BENEFICIAIRE", "N_ENTREE", "N_IMMATRICULATION_INDIVIDUEL",
            "CLE_N_IMMATRICULATION_INDIVIDUEL", "MODE_TRAITEMENT", "DISCIPLINE_PRESTATION", "DATE_ACTE1",
            "QUANTITE_ACTE1", "CODE_ACTE1", "DATE_ACTE2", "QUANTITE_ACTE2", "CODE_ACTE2", "DATE_ACTE3",
            "QUANTITE_ACTE3", "CODE_ACTE3", "DATE_ACTE4", "QUANTITE_ACTE4", "CODE_ACTE4", "DATE_ACTE5",
            "QUANTITE_ACTE5", "CODE_ACTE5" // Missing NuméroL? Assuming it's not present based on list.
    ));


    // Map Line Type Char to Expected Headers
    public static final Map<Character, List<String>> LIGNES_HEADERS_MAP = Stream.of(
            Map.entry('A', LIGNES_A_HEADERS),
            Map.entry('B', LIGNES_B_HEADERS),
            Map.entry('C', LIGNES_C_HEADERS),
            Map.entry('H', LIGNES_H_HEADERS),
            Map.entry('M', LIGNES_M_HEADERS),
            Map.entry('P', LIGNES_P_HEADERS),
            Map.entry('L', LIGNES_L_HEADERS)
    ).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    // Define mandatory fields for each line type (Inferred - ADJUST AS NEEDED based on actual requirements)
    public static final Map<Character, Set<String>> LIGNES_MANDATORY_FIELDS_MAP = Map.ofEntries(
            Map.entry('A', Set.of("N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE", "N_ENTREE", "DATE_NAISSANCE", "DATE_ENTREE", "DATE_SORTIE")),
            Map.entry('B', Set.of("N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE", "N_ENTREE", "DATE_SOINS", "CODE_ACTE")),
            Map.entry('C', Set.of("N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE", "N_ENTREE", "DATE_SOINS", "CODE_ACTE")),
            Map.entry('H', Set.of("N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE", "N_ENTREE", "DATE_DEBUT_SEJOUR", "CODE_UCD", "QUANTITE")), // Added H mandatory fields
            Map.entry('M', Set.of("N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE", "N_ENTREE", "DATE_SOINS", "CODE_CCAM")),
            Map.entry('P', Set.of("N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE", "N_ENTREE", "DATE_DEBUT_SEJOUR", "CODE_REFERENCE_LPP", "QUANTITE")), // Added P mandatory field
            Map.entry('L', Set.of("N_FINESS_EPMSI", "N_FINESS_GEOGRAPHIQUE", "N_IMMATRICULATION_ASSURE", "N_ENTREE", "DATE_ACTE1", "CODE_ACTE1", "QUANTITE_ACTE1")) // Added L mandatory fields
    );

    // Define field specifications (length, type) - Translated from user's fixed-width spec
    // Length 0 means variable length / no specific length check needed for Excel content.
    // Type validation (Date, Numeric) is applied.
     public static final Map<Character, Map<String, FieldSpec>> LIGNES_FIELD_SPECS_MAP = Map.ofEntries(
        Map.entry('A', Map.ofEntries(
            Map.entry("TYPE_ENREGISTREMENT", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_EPMSI", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_GEOGRAPHIQUE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("SEXE", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("CODE_CIVILITE", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("N_IMMATRICULATION_ASSURE", new FieldSpec(32, FieldType.ALPHANUMERIC)),
            Map.entry("CLE_N_IMMATRICULATION", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("RANG_BENEFICIAIRE", new FieldSpec(3, FieldType.NUMERIC)),
            Map.entry("N_ENTREE", new FieldSpec(9, FieldType.ALPHANUMERIC)), // Often numeric, but spec says AN
            Map.entry("N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(32, FieldType.ALPHANUMERIC)),
            Map.entry("CLE_N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("INDICATEUR_PARCOURS_SOINS", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("NATURE_OPERATION", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("NATURE_ASSURANCE", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("TYPE_CONTRAT_ORGANISME_COMPLEMENTAIRE", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("JUSTIF_EXO_TM", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("SEJOUR_FACTURABLE_ASSURANCE_MALADIE", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("FILLER_1", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("MOTIF_NON_FACTURATION_ASSURANCE_MALADIE", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("CODE_GD_REGIME", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("DATE_NAISSANCE", new FieldSpec(8, FieldType.DATE)),
            Map.entry("RANG_NAISSANCE", new FieldSpec(1, FieldType.NUMERIC)),
            Map.entry("DATE_ENTREE", new FieldSpec(8, FieldType.DATE)),
            Map.entry("DATE_SORTIE", new FieldSpec(8, FieldType.DATE)),
            Map.entry("CODE_POSTAL_RESIDENCE_PATIENT", new FieldSpec(5, FieldType.NUMERIC)),
            Map.entry("TOTAL_BASE_REMBOURSEMENT", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)), // Amounts are likely decimal
            Map.entry("TOTAL_REMBOURSABLE_AMO", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("TOTAL_HONORAIRE_FACTURE", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("TOTAL_HONORAIRE_REMBOURSABLE_AM", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("TOTAL_PARTICIPATION_ASSURE_AVANT_OC", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("TOTAL_REMBOURSABLE_OC_PH", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("TOTAL_REMBOURSABLE_OC_HONORAIRES", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("MONTANT_TOTAL_FACTUREPH", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("NuméroA", new FieldSpec(5, FieldType.ALPHANUMERIC))
        )),
        Map.entry('B', Map.ofEntries(
            Map.entry("TYPE_ENREGISTREMENT", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_EPMSI", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_GEOGRAPHIQUE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_IMMATRICULATION_ASSURE", new FieldSpec(32, FieldType.ALPHANUMERIC)),
            Map.entry("CLE_N_IMMATRICULATION", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("RANG_BENEFICIAIRE", new FieldSpec(3, FieldType.NUMERIC)),
            Map.entry("N_ENTREE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(32, FieldType.ALPHANUMERIC)),
            Map.entry("CLE_N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("MODE_TRAITEMENT", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("DISCIPLINE_PRESTATION", new FieldSpec(3, FieldType.ALPHANUMERIC)),
            Map.entry("JUSTIFICATION_EXOTM", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("SPECIALITE_EXECUTANT", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("DATE_SOINS", new FieldSpec(8, FieldType.DATE)),
            Map.entry("CODE_ACTE", new FieldSpec(5, FieldType.ALPHANUMERIC)),
            Map.entry("QUANTITE", new FieldSpec(3, FieldType.NUMERIC)),
            Map.entry("COEFFICIENT", new FieldSpec(6, FieldType.NUMERIC_DECIMAL)), // Format 4+2 implies decimal
            Map.entry("TYPE_PRESTATION_INTERMEDIAIRE", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("COEFFICIENT_MCO", new FieldSpec(5, FieldType.NUMERIC_DECIMAL)), // Assuming decimal
            Map.entry("DENOMBREMENT", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("PRIXUNITAIRE", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
            Map.entry("MONTANT_BASE_REMBOURSEMENT", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("TAUX_APPLICABLE", new FieldSpec(3, FieldType.NUMERIC_DECIMAL)), // Often a percentage
            Map.entry("MONTANT_REMBOURSABLE_AMO", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("MONTANT_HONORAIRE", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("MONTANT_REMBOURSABLE_AMC", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
            Map.entry("FILLER", new FieldSpec(15, FieldType.ALPHANUMERIC)),
            Map.entry("NuméroB", new FieldSpec(5, FieldType.ALPHANUMERIC))
        )),
         Map.entry('C', Map.ofEntries(
            Map.entry("TYPE_ENREGISTREMENT", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_EPMSI", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_GEOGRAPHIQUE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_IMMATRICULATION_ASSURE", new FieldSpec(32, FieldType.ALPHANUMERIC)), // Spec inconsistency noted (13 vs 32), using 32 from header list context
            Map.entry("CLE_N_IMMATRICULATION", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("RANG_BENEFICIAIRE", new FieldSpec(3, FieldType.NUMERIC)),
            Map.entry("N_ENTREE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(32, FieldType.ALPHANUMERIC)),
            Map.entry("CLE_N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("MODE_TRAITEMENT", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("DISCIPLINE_PRESTATION", new FieldSpec(3, FieldType.ALPHANUMERIC)),
            Map.entry("JUSTIFICATION_EXOTM", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("SPECIALITE_EXECUTANT", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("DATE_SOINS", new FieldSpec(8, FieldType.DATE)),
            Map.entry("CODE_ACTE", new FieldSpec(5, FieldType.ALPHANUMERIC)),
            Map.entry("QUANTITE", new FieldSpec(3, FieldType.NUMERIC)),
            Map.entry("COEFFICIENT", new FieldSpec(6, FieldType.NUMERIC_DECIMAL)),
            Map.entry("DENOMBREMENT", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("PRIXUNITAIRE", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
            Map.entry("MONTANT_REMBOURSABLE_AMO", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)), // Doc: MONTANT_BASE_REMBOURSEMENT
            Map.entry("TAUX_APPLICABLE", new FieldSpec(3, FieldType.NUMERIC_DECIMAL)),
            Map.entry("MONTANT_REMBOURSABLE_AMO_1", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)), // Doc: Duplicate name MONTANT_REMBOURSABLE_AMO
            Map.entry("MONTANT_HONORAIRE", new FieldSpec(8, FieldType.NUMERIC_DECIMAL)),
            Map.entry("MONTANT_REMBOURSABLE_AMC", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
            Map.entry("FILLER", new FieldSpec(11, FieldType.ALPHANUMERIC)),
            Map.entry("TYPE_UNITE_FONCTIONNELLE", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("COEFFICIENT_MCO", new FieldSpec(5, FieldType.NUMERIC_DECIMAL)),
            Map.entry("NuméroC", new FieldSpec(5, FieldType.ALPHANUMERIC))
         )),
          Map.entry('H', Map.ofEntries(
            Map.entry("TYPE_ENREGISTREMENT", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_EPMSI", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_GEOGRAPHIQUE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_IMMATRICULATION_ASSURE", new FieldSpec(32, FieldType.ALPHANUMERIC)),
            Map.entry("CLE_N_IMMATRICULATION", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("RANG_BENEFICIAIRE", new FieldSpec(3, FieldType.NUMERIC)),
            Map.entry("N_ENTREE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(32, FieldType.ALPHANUMERIC)),
            Map.entry("CLE_N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("DATE_DEBUT_SEJOUR", new FieldSpec(8, FieldType.DATE)),
            Map.entry("CODE_UCD", new FieldSpec(7, FieldType.ALPHANUMERIC)), // Often numeric but can have letters
            Map.entry("COEFF_FRACTIONNEMENT", new FieldSpec(5, FieldType.NUMERIC_DECIMAL)),
            Map.entry("PRIX_ACHAT_UNITAIRE", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
            Map.entry("MONTANT_UNITAIRE_ECART", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
            Map.entry("MONTANT_TOTAL_ECART", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
            Map.entry("QUANTITE", new FieldSpec(3, FieldType.NUMERIC)),
            Map.entry("TOTAL_FACTURE", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
            Map.entry("Indication", new FieldSpec(7, FieldType.ALPHANUMERIC)), // Length assumed from spec
            Map.entry("NuméroH", new FieldSpec(5, FieldType.ALPHANUMERIC))
         )),
         Map.entry('M', Map.ofEntries(
            Map.entry("TYPE_ENREGISTREMENT", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_EPMSI", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_FINESS_GEOGRAPHIQUE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_IMMATRICULATION_ASSURE", new FieldSpec(32, FieldType.ALPHANUMERIC)),
            Map.entry("CLE_N_IMMATRICULATION", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("RANG_BENEFICIAIRE", new FieldSpec(3, FieldType.NUMERIC)),
            Map.entry("N_ENTREE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
            Map.entry("N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(32, FieldType.ALPHANUMERIC)),
            Map.entry("CLE_N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("MODE_TRAITEMENT", new FieldSpec(2, FieldType.ALPHANUMERIC)),
            Map.entry("DISCIPLINE_PRESTATION", new FieldSpec(3, FieldType.ALPHANUMERIC)),
            Map.entry("DATE_SOINS", new FieldSpec(8, FieldType.DATE)),
            Map.entry("CODE_CCAM", new FieldSpec(13, FieldType.ALPHANUMERIC)),
            Map.entry("EXTENSION_DOCUMENTAIRE", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("ACTIVITE", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("PHASE", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("MODIFICATEUR1", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("MODIFICATEUR2", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("MODIFICATEUR3", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("MODIFICATEUR4", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("CODE_ASSOCIATION_ACTE", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("CODE_REMBOURSEMENT", new FieldSpec(1, FieldType.ALPHANUMERIC)),
            Map.entry("NUM_DENT1", new FieldSpec(2, FieldType.NUMERIC)), Map.entry("NUM_DENT2", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("NUM_DENT3", new FieldSpec(2, FieldType.NUMERIC)), Map.entry("NUM_DENT4", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("NUM_DENT5", new FieldSpec(2, FieldType.NUMERIC)), Map.entry("NUM_DENT6", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("NUM_DENT7", new FieldSpec(2, FieldType.NUMERIC)), Map.entry("NUM_DENT8", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("NUM_DENT9", new FieldSpec(2, FieldType.NUMERIC)), Map.entry("NUM_DENT10", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("NUM_DENT11", new FieldSpec(2, FieldType.NUMERIC)), Map.entry("NUM_DENT12", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("NUM_DENT13", new FieldSpec(2, FieldType.NUMERIC)), Map.entry("NUM_DENT14", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("NUM_DENT15", new FieldSpec(2, FieldType.NUMERIC)), Map.entry("NUM_DENT16", new FieldSpec(2, FieldType.NUMERIC)),
            Map.entry("NUMÉRO_M", new FieldSpec(5, FieldType.ALPHANUMERIC))
         )),
          Map.entry('P', Map.ofEntries(
             Map.entry("TYPE_ENREGISTREMENT", new FieldSpec(1, FieldType.ALPHANUMERIC)),
             Map.entry("N_FINESS_EPMSI", new FieldSpec(9, FieldType.ALPHANUMERIC)),
             Map.entry("N_FINESS_GEOGRAPHIQUE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
             Map.entry("N_IMMATRICULATION_ASSURE", new FieldSpec(32, FieldType.ALPHANUMERIC)),
             Map.entry("CLE_N_IMMATRICULATION", new FieldSpec(2, FieldType.ALPHANUMERIC)),
             Map.entry("RANG_BENEFICIAIRE", new FieldSpec(3, FieldType.NUMERIC)),
             Map.entry("N_ENTREE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
             Map.entry("N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(32, FieldType.ALPHANUMERIC)),
             Map.entry("CLE_N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(2, FieldType.ALPHANUMERIC)),
             Map.entry("DATE_DEBUT_SEJOUR", new FieldSpec(8, FieldType.DATE)),
             Map.entry("CODE_REFERENCE_LPP", new FieldSpec(13, FieldType.ALPHANUMERIC)),
             Map.entry("QUANTITE", new FieldSpec(2, FieldType.NUMERIC)),
             Map.entry("TARIF_REFERENCE_LPP", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
             Map.entry("MONTANT_TOTAL_FACTURE", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
             Map.entry("PRIX_ACHAT_UNITAIRE", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
             Map.entry("MONTANT_UNITAIRE_ECART", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
             Map.entry("MONTANT_TOTAL_ECART", new FieldSpec(7, FieldType.NUMERIC_DECIMAL)),
             Map.entry("NuméroP", new FieldSpec(5, FieldType.ALPHANUMERIC))
         )),
         Map.entry('L', Map.ofEntries(
             Map.entry("TYPE_ENREGISTREMENT", new FieldSpec(1, FieldType.ALPHANUMERIC)),
             Map.entry("N_FINESS_EPMSI", new FieldSpec(9, FieldType.ALPHANUMERIC)),
             Map.entry("N_FINESS_GEOGRAPHIQUE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
             Map.entry("N_IMMATRICULATION_ASSURE", new FieldSpec(32, FieldType.ALPHANUMERIC)),
             Map.entry("CLE_N_IMMATRICULATION", new FieldSpec(2, FieldType.ALPHANUMERIC)),
             Map.entry("RANG_BENEFICIAIRE", new FieldSpec(3, FieldType.NUMERIC)),
             Map.entry("N_ENTREE", new FieldSpec(9, FieldType.ALPHANUMERIC)),
             Map.entry("N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(32, FieldType.ALPHANUMERIC)),
             Map.entry("CLE_N_IMMATRICULATION_INDIVIDUEL", new FieldSpec(2, FieldType.ALPHANUMERIC)),
             Map.entry("MODE_TRAITEMENT", new FieldSpec(2, FieldType.ALPHANUMERIC)),
             Map.entry("DISCIPLINE_PRESTATION", new FieldSpec(3, FieldType.ALPHANUMERIC)),
             Map.entry("DATE_ACTE1", new FieldSpec(8, FieldType.DATE)),
             Map.entry("QUANTITE_ACTE1", new FieldSpec(2, FieldType.NUMERIC)),
             Map.entry("CODE_ACTE1", new FieldSpec(8, FieldType.ALPHANUMERIC)), // Length from spec
             Map.entry("DATE_ACTE2", new FieldSpec(8, FieldType.DATE)),
             Map.entry("QUANTITE_ACTE2", new FieldSpec(2, FieldType.NUMERIC)),
             Map.entry("CODE_ACTE2", new FieldSpec(8, FieldType.ALPHANUMERIC)),
             Map.entry("DATE_ACTE3", new FieldSpec(8, FieldType.DATE)),
             Map.entry("QUANTITE_ACTE3", new FieldSpec(2, FieldType.NUMERIC)),
             Map.entry("CODE_ACTE3", new FieldSpec(8, FieldType.ALPHANUMERIC)),
             Map.entry("DATE_ACTE4", new FieldSpec(8, FieldType.DATE)),
             Map.entry("QUANTITE_ACTE4", new FieldSpec(2, FieldType.NUMERIC)),
             Map.entry("CODE_ACTE4", new FieldSpec(8, FieldType.ALPHANUMERIC)),
             Map.entry("DATE_ACTE5", new FieldSpec(8, FieldType.DATE)),
             Map.entry("QUANTITE_ACTE5", new FieldSpec(2, FieldType.NUMERIC)),
             Map.entry("CODE_ACTE5", new FieldSpec(8, FieldType.ALPHANUMERIC))
             // Missing NuméroL?
         ))
    );


    /**
     * Defines validation rules for a field.
     * Length 0 means variable length (no length check).
     */
    public record FieldSpec(int length, FieldType type) {}

    public enum FieldType {
        ALPHANUMERIC, // Any characters allowed
        NUMERIC,      // Only digits [0-9]
        NUMERIC_DECIMAL, // Digits, potentially with a decimal point and sign
        DATE          // Must match YYYYMMDD pattern
    }
} 