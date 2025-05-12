package com.rsf.rsf.config;

import com.rsf.rsf.domain.models.FieldDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class RsfMappingConfig {

    // Structure: Map<Year, Map<LineType, List<FieldDefinition>>>
    @Bean
    public Map<Integer, Map<Character, List<FieldDefinition>>> rsfFieldMappings() {
        return Map.of(
                2017, create2017Mappings()
                // Add mappings for other years here if needed
        );
    }

    private Map<Character, List<FieldDefinition>> create2017Mappings() {
        return Map.of(
                'A', createLineAMapping2017(),
                'B', createLineBMapping2017(),
                'C', createLineCMapping2017(),
                'H', createLineHMapping2017(),
                'M', createLineMMapping2017(),
                'P', createLinePMapping2017(),
                'L', createLineLMapping2017()
        );
    }

    // Mappings based on Python RSF_2017_MAPPINGS

    private List<FieldDefinition> createLineAMapping2017() {
        return List.of(
                new FieldDefinition("TYPE_ENREGISTREMENT", 1, 1),
                new FieldDefinition("N_FINESS_EPMSI", 2, 9),
                new FieldDefinition("N_FINESS_GEOGRAPHIQUE", 11, 9),
                new FieldDefinition("SEXE", 20, 1),
                new FieldDefinition("CODE_CIVILITE", 21, 1),
                new FieldDefinition("N_IMMATRICULATION_ASSURE", 22, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION", 54, 2),
                new FieldDefinition("RANG_BENEFICIAIRE", 56, 3),
                new FieldDefinition("N_ENTREE", 59, 9),
                new FieldDefinition("N_IMMATRICULATION_INDIVIDUEL", 68, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION_INDIVIDUEL", 100, 2),
                new FieldDefinition("INDICATEUR_PARCOURS_SOINS", 102, 1),
                new FieldDefinition("NATURE_OPERATION", 103, 1),
                new FieldDefinition("NATURE_ASSURANCE", 104, 2),
                new FieldDefinition("TYPE_CONTRAT_ORGANISME_COMPLEMENTAIRE", 106, 2),
                new FieldDefinition("JUSTIF_EXO_TM", 108, 1),
                new FieldDefinition("SEJOUR_FACTURABLE_ASSURANCE_MALADIE", 109, 1),
                new FieldDefinition("FILLER_1", 110, 1),
                new FieldDefinition("MOTIF_NON_FACTURATION", 111, 1),
                new FieldDefinition("CODE_GD_REGIME", 112, 2),
                new FieldDefinition("DATE_NAISSANCE", 114, 8),
                new FieldDefinition("RANG_NAISSANCE", 122, 1),
                new FieldDefinition("DATE_ENTREE", 123, 8),
                new FieldDefinition("DATE_SORTIE", 131, 8),
                new FieldDefinition("CODE_POSTAL_RESIDENCE_PATIENT", 139, 5),
                new FieldDefinition("TOTAL_BASE_REMBOURSEMENT", 144, 8),
                new FieldDefinition("TOTAL_REMBOURSABLE_AMO", 152, 8),
                new FieldDefinition("TOTAL_HONORAIRE_FACTURE", 160, 8),
                new FieldDefinition("TOTAL_HONORAIRE_REMBOURSABLE_AM", 168, 8),
                new FieldDefinition("TOTAL_PARTICIPATION_ASSURE_AVANT_OC", 176, 8),
                new FieldDefinition("TOTAL_REMBOURSABLE_OC_PH", 184, 8),
                new FieldDefinition("TOTAL_REMBOURSABLE_OC_HONORAIRES", 192, 8),
                new FieldDefinition("MONTANT_TOTAL_FACTUREPH", 200, 8),
                new FieldDefinition("NUMERO_A", 208, 5)
        );
    }

    private List<FieldDefinition> createLineBMapping2017() {
        return List.of(
                new FieldDefinition("TYPE_ENREGISTREMENT", 1, 1),
                new FieldDefinition("N_FINESS_EPMSI", 2, 9),
                new FieldDefinition("N_FINESS_GEOGRAPHIQUE", 11, 9),
                new FieldDefinition("N_IMMATRICULATION_ASSURE", 20, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION", 52, 2),
                new FieldDefinition("RANG_BENEFICIAIRE", 54, 3),
                new FieldDefinition("N_ENTREE", 57, 9),
                new FieldDefinition("N_IMMATRICULATION_INDIVIDUEL", 66, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION_INDIVIDUEL", 98, 2),
                new FieldDefinition("MODE_TRAITEMENT", 100, 2),
                new FieldDefinition("DISCIPLINE_PRESTATION", 102, 3),
                new FieldDefinition("JUSTIFICATION_EXOTM", 105, 1),
                new FieldDefinition("SPECIALITE_EXECUTANT", 106, 2),
                new FieldDefinition("DATE_SOINS", 108, 8),
                new FieldDefinition("CODE_ACTE", 116, 5),
                new FieldDefinition("QUANTITE", 121, 3),
                new FieldDefinition("COEFFICIENT", 124, 6),
                new FieldDefinition("TYPE_PRESTATION_INTERMEDIAIRE", 130, 1),
                new FieldDefinition("COEFFICIENT_MCO", 131, 5),
                new FieldDefinition("DENOMBREMENT", 136, 2),
                new FieldDefinition("PRIX_UNITAIRE", 138, 7),
                new FieldDefinition("MONTANT_BASE_REMBOURSEMENT", 145, 8),
                new FieldDefinition("TAUX_APPLICABLE", 153, 3),
                new FieldDefinition("MONTANT_REMBOURSABLE_AMO", 156, 8),
                new FieldDefinition("MONTANT_HONORAIRE", 164, 8),
                new FieldDefinition("MONTANT_REMBOURSABLE_AMC", 172, 7),
                new FieldDefinition("FILLER", 179, 15),
                new FieldDefinition("NUMERO_B", 194, 5)
        );
    }

    private List<FieldDefinition> createLineCMapping2017() {
        return List.of(
                new FieldDefinition("TYPE_ENREGISTREMENT", 1, 1),
                new FieldDefinition("N_FINESS_EPMSI", 2, 9),
                new FieldDefinition("N_FINESS_GEOGRAPHIQUE", 11, 9),
                new FieldDefinition("N_IMMATRICULATION_ASSURE", 20, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION", 52, 2),
                new FieldDefinition("RANG_BENEFICIAIRE", 54, 3),
                new FieldDefinition("N_ENTREE", 57, 9),
                new FieldDefinition("N_IMMATRICULATION_INDIVIDUEL", 66, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION_INDIVIDUEL", 98, 2),
                new FieldDefinition("MODE_TRAITEMENT", 100, 2),
                new FieldDefinition("DISCIPLINE_PRESTATION", 102, 3),
                new FieldDefinition("JUSTIFICATION_EXOTM", 105, 1),
                new FieldDefinition("SPECIALITE_EXECUTANT", 106, 2),
                new FieldDefinition("DATE_SOINS", 108, 8),
                new FieldDefinition("CODE_ACTE", 116, 5),
                new FieldDefinition("QUANTITE", 121, 3),
                new FieldDefinition("COEFFICIENT", 124, 6),
                new FieldDefinition("DENOMBREMENT", 130, 2),
                new FieldDefinition("PRIX_UNITAIRE", 132, 7),
                new FieldDefinition("MONTANT_BASE_REMBOURSEMENT", 139, 8),
                new FieldDefinition("TAUX_APPLICABLE", 147, 3),
                new FieldDefinition("MONTANT_REMBOURSABLE_AMO", 150, 8),
                new FieldDefinition("MONTANT_HONORAIRE", 158, 8),
                new FieldDefinition("MONTANT_REMBOURSABLE_AMC", 166, 7),
                new FieldDefinition("FILLER", 173, 11),
                new FieldDefinition("TYPE_UNITE_FONCTIONNELLE", 184, 2),
                new FieldDefinition("COEFFICIENT_MCO", 186, 5),
                new FieldDefinition("NUMERO_C", 191, 5)
        );
    }

    private List<FieldDefinition> createLineHMapping2017() {
        return List.of(
                new FieldDefinition("TYPE_ENREGISTREMENT", 1, 1),
                new FieldDefinition("N_FINESS_EPMSI", 2, 9),
                new FieldDefinition("N_FINESS_GEOGRAPHIQUE", 11, 9),
                new FieldDefinition("N_IMMATRICULATION_ASSURE", 20, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION", 52, 2),
                new FieldDefinition("RANG_BENEFICIAIRE", 54, 3),
                new FieldDefinition("N_ENTREE", 57, 9),
                new FieldDefinition("N_IMMATRICULATION_INDIVIDUEL", 66, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION_INDIVIDUEL", 98, 2),
                new FieldDefinition("DATE_DEBUT_SEJOUR", 100, 8),
                new FieldDefinition("CODE_UCD", 108, 7),
                new FieldDefinition("COEFF_FRACTIONNEMENT", 115, 5),
                new FieldDefinition("PRIX_ACHAT_UNITAIRE", 120, 7),
                new FieldDefinition("MONTANT_UNITAIRE_ECART", 127, 7),
                new FieldDefinition("MONTANT_TOTAL_ECART", 134, 7),
                new FieldDefinition("QUANTITE", 141, 3),
                new FieldDefinition("TOTAL_FACTURE", 144, 7),
                new FieldDefinition("INDICATION", 151, 7),
                new FieldDefinition("NUMERO_H", 158, 5)
        );
    }

    private List<FieldDefinition> createLineMMapping2017() {
        return List.of(
                new FieldDefinition("TYPE_ENREGISTREMENT", 1, 1),
                new FieldDefinition("N_FINESS_EPMSI", 2, 9),
                new FieldDefinition("N_FINESS_GEOGRAPHIQUE", 11, 9),
                new FieldDefinition("N_IMMATRICULATION_ASSURE", 20, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION", 52, 2),
                new FieldDefinition("RANG_BENEFICIAIRE", 54, 3),
                new FieldDefinition("N_ENTREE", 57, 9),
                new FieldDefinition("N_IMMATRICULATION_INDIVIDUEL", 66, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION_INDIVIDUEL", 98, 2),
                new FieldDefinition("MODE_TRAITEMENT", 100, 2),
                new FieldDefinition("DISCIPLINE_PRESTATION", 102, 3),
                new FieldDefinition("DATE_SOINS", 105, 8),
                new FieldDefinition("CODE_CCAM", 113, 13),
                new FieldDefinition("EXTENSION_DOCUMENTAIRE", 126, 1),
                new FieldDefinition("ACTIVITE", 127, 1),
                new FieldDefinition("PHASE", 128, 1),
                new FieldDefinition("MODIFICATEUR1", 129, 1),
                new FieldDefinition("MODIFICATEUR2", 130, 1),
                new FieldDefinition("MODIFICATEUR3", 131, 1),
                new FieldDefinition("MODIFICATEUR4", 132, 1),
                new FieldDefinition("CODE_ASSOCIATION_ACTE", 133, 1),
                new FieldDefinition("CODE_REMBOURSEMENT", 134, 1),
                new FieldDefinition("NUM_DENT1", 135, 2),
                new FieldDefinition("NUM_DENT2", 137, 2),
                new FieldDefinition("NUM_DENT3", 139, 2),
                new FieldDefinition("NUM_DENT4", 141, 2),
                new FieldDefinition("NUM_DENT5", 143, 2),
                new FieldDefinition("NUM_DENT6", 145, 2),
                new FieldDefinition("NUM_DENT7", 147, 2),
                new FieldDefinition("NUM_DENT8", 149, 2),
                new FieldDefinition("NUM_DENT9", 151, 2),
                new FieldDefinition("NUM_DENT10", 153, 2),
                new FieldDefinition("NUM_DENT11", 155, 2),
                new FieldDefinition("NUM_DENT12", 157, 2),
                new FieldDefinition("NUM_DENT13", 159, 2),
                new FieldDefinition("NUM_DENT14", 161, 2),
                new FieldDefinition("NUM_DENT15", 163, 2),
                new FieldDefinition("NUM_DENT16", 165, 2),
                new FieldDefinition("NUMERO_M", 167, 5)
        );
    }

    private List<FieldDefinition> createLinePMapping2017() {
        return List.of(
                new FieldDefinition("TYPE_ENREGISTREMENT", 1, 1),
                new FieldDefinition("N_FINESS_EPMSI", 2, 9),
                new FieldDefinition("N_FINESS_GEOGRAPHIQUE", 11, 9),
                new FieldDefinition("N_IMMATRICULATION_ASSURE", 20, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION", 52, 2),
                new FieldDefinition("RANG_BENEFICIAIRE", 54, 3),
                new FieldDefinition("N_ENTREE", 57, 9),
                new FieldDefinition("N_IMMATRICULATION_INDIVIDUEL", 66, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION_INDIVIDUEL", 98, 2),
                new FieldDefinition("DATE_DEBUT_SEJOUR", 100, 8),
                new FieldDefinition("CODE_REFERENCE_LPP", 108, 13),
                new FieldDefinition("QUANTITE", 121, 2),
                new FieldDefinition("TARIF_REFERENCE_LPP", 123, 7),
                new FieldDefinition("MONTANT_TOTAL_FACTURE", 130, 7),
                new FieldDefinition("PRIX_ACHAT_UNITAIRE", 137, 7),
                new FieldDefinition("MONTANT_UNITAIRE_ECART", 144, 7),
                new FieldDefinition("MONTANT_TOTAL_ECART", 151, 7)
        );
    }

    private List<FieldDefinition> createLineLMapping2017() {
        return List.of(
                new FieldDefinition("TYPE_ENREGISTREMENT", 1, 1),
                new FieldDefinition("N_FINESS_EPMSI", 2, 9),
                new FieldDefinition("N_FINESS_GEOGRAPHIQUE", 11, 9),
                new FieldDefinition("N_IMMATRICULATION_ASSURE", 20, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION", 52, 2),
                new FieldDefinition("RANG_BENEFICIAIRE", 54, 3),
                new FieldDefinition("N_ENTREE", 57, 9),
                new FieldDefinition("N_IMMATRICULATION_INDIVIDUEL", 66, 32),
                new FieldDefinition("CLE_N_IMMATRICULATION_INDIVIDUEL", 98, 2),
                new FieldDefinition("MODE_TRAITEMENT", 100, 2),
                new FieldDefinition("DISCIPLINE_PRESTATION", 102, 3),
                new FieldDefinition("DATE_ACTE1", 105, 8),
                new FieldDefinition("QUANTITE_ACTE1", 113, 2),
                new FieldDefinition("CODE_ACTE1", 115, 8),
                new FieldDefinition("DATE_ACTE2", 123, 8),
                new FieldDefinition("QUANTITE_ACTE2", 131, 2),
                new FieldDefinition("CODE_ACTE2", 133, 8),
                new FieldDefinition("DATE_ACTE3", 141, 8),
                new FieldDefinition("QUANTITE_ACTE3", 149, 2),
                new FieldDefinition("CODE_ACTE3", 151, 8),
                new FieldDefinition("DATE_ACTE4", 159, 8),
                new FieldDefinition("QUANTITE_ACTE4", 167, 2),
                new FieldDefinition("CODE_ACTE4", 169, 8),
                new FieldDefinition("DATE_ACTE5", 177, 8),
                new FieldDefinition("QUANTITE_ACTE5", 185, 2),
                new FieldDefinition("CODE_ACTE5", 187, 8)
        );
    }
} 