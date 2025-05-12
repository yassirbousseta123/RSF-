package com.rsf.rsf.domain.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HoraireUpdateRecordTest {

    @Test
    public void testConstructorAndGetters() {
        // Create test data
        int sourceRowNum = 10;
        String numImmatriculation = "12345678901234567890123456789012";
        String dateNaissance = "20000101";
        String dateSoins = "20230505";
        String codeActe = "ABCD1";
        String horaire = "09:30";
        
        // Create object
        HoraireUpdateRecord record = new HoraireUpdateRecord(
                sourceRowNum, numImmatriculation, dateNaissance, dateSoins, codeActe, horaire);
        
        // Verify getters
        assertEquals(sourceRowNum, record.getSourceRowNum());
        assertEquals(numImmatriculation, record.getNumImmatriculation());
        assertEquals(dateNaissance, record.getDateNaissance());
        assertEquals(dateSoins, record.getDateSoins());
        assertEquals(codeActe, record.getCodeActe());
        assertEquals(horaire, record.getHoraire());
    }
} 