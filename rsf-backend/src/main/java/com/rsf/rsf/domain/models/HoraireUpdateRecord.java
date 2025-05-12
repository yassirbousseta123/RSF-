package com.rsf.rsf.domain.models;

/**
 * Represents a single update instruction extracted from a HORAIRE file.
 */
public class HoraireUpdateRecord {
    private final int sourceRowNum; // Original row number in the Excel file (for error reporting)
    private final String numImmatriculation; // N_IMMATRICULATION_ASSURE
    private final String dateNaissance; // DATE_NAISSANCE
    private final String dateSoins; // DATE_SOINS
    private final String codeActe; // CODE_ACTE
    private final String horaire; // Value to update
    
    /**
     * Creates a new horaire update record.
     */
    public HoraireUpdateRecord(int sourceRowNum, String numImmatriculation, String dateNaissance, 
                               String dateSoins, String codeActe, String horaire) {
        this.sourceRowNum = sourceRowNum;
        this.numImmatriculation = numImmatriculation;
        this.dateNaissance = dateNaissance;
        this.dateSoins = dateSoins;
        this.codeActe = codeActe;
        this.horaire = horaire;
    }
    
    /**
     * @return The original row number in the Excel file
     */
    public int getSourceRowNum() {
        return sourceRowNum;
    }
    
    /**
     * @return The patient's insurance number (N_IMMATRICULATION_ASSURE)
     */
    public String getNumImmatriculation() {
        return numImmatriculation;
    }
    
    /**
     * @return The patient's date of birth (DATE_NAISSANCE)
     */
    public String getDateNaissance() {
        return dateNaissance;
    }
    
    /**
     * @return The date of care (DATE_SOINS)
     */
    public String getDateSoins() {
        return dateSoins;
    }
    
    /**
     * @return The code for the medical act (CODE_ACTE)
     */
    public String getCodeActe() {
        return codeActe;
    }
    
    /**
     * @return The schedule value to update (horaire)
     */
    public String getHoraire() {
        return horaire;
    }
} 