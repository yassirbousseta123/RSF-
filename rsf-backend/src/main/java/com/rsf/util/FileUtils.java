package com.rsf.util;

public class FileUtils {
    public static String detectType(String filename) {
        if (filename.startsWith("RSF_"))      return "RSF";
        if (filename.startsWith("HORAIRES_")) return "HORAIRE";
        if (filename.startsWith("LIGNES_"))   return "LIGNES";
        return "UNKNOWN";
    }
} 