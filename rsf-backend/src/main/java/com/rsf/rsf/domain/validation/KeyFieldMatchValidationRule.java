package com.rsf.rsf.domain.validation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Validates that key fields (N_IMMATRICULATION_ASSURE, N_ENTREE) in dependent lines (B, C, H, M, P, L)
 * match the corresponding fields in their parent 'A' line.
 */
public class KeyFieldMatchValidationRule implements RsfValidationRule {

    @Override
    public RsfValidationResult validate(Map<Character, List<Map<String, String>>> rsfData) {
        RsfValidationResult result = new RsfValidationResult();

        // TODO: This validation assumes a block structure where B, C, H, etc., lines
        //       belong to the immediately preceding 'A' line. The current Map structure
        //       doesn't preserve this order or relationship. This rule requires
        //       a different data structure or parsing approach that maintains the A-to-children link.

        // Placeholder logic assuming we can identify parent-child relationships:
        /*
        Map<String, String> currentParentA = null;
        int parentALineNum = -1;

        // Requires iterating through lines IN ORIGINAL ORDER
        List<Map<String, String>> allLines = ... // Get lines in original order

        for (int i = 0; i < allLines.size(); i++) {
            Map<String, String> line = allLines.get(i);
            int currentLineNumber = i + 1; // Or track original line number
            String lineTypeStr = line.get("TYPE_ENREGISTREMENT");
            if (lineTypeStr == null || lineTypeStr.isEmpty()) continue;
            char lineType = lineTypeStr.charAt(0);

            if (lineType == 'A') {
                currentParentA = line;
                parentALineNum = currentLineNumber;
            } else if (currentParentA != null && "BCHMPL".indexOf(lineType) != -1) {
                String parentImmat = currentParentA.get("N_IMMATRICULATION_ASSURE");
                String parentEntree = currentParentA.get("N_ENTREE");
                String childImmat = line.get("N_IMMATRICULATION_ASSURE");
                String childEntree = line.get("N_ENTREE");

                if (!Objects.equals(parentImmat, childImmat)) {
                    result.addError(new RsfError(currentLineNumber, "N_IMMATRICULATION_ASSURE", RsfErrorType.DEPENDENCY,
                            String.format("Mismatch N_IMMATRICULATION_ASSURE ('%s') with parent A line %d ('%s').",
                                    childImmat, parentALineNum, parentImmat)));
                }
                if (!Objects.equals(parentEntree, childEntree)) {
                    result.addError(new RsfError(currentLineNumber, "N_ENTREE", RsfErrorType.DEPENDENCY,
                            String.format("Mismatch N_ENTREE ('%s') with parent A line %d ('%s').",
                                    childEntree, parentALineNum, parentEntree)));
                }
            } else if (currentParentA == null && "BCHMPL".indexOf(lineType) != -1) {
                 result.addError(new RsfError(currentLineNumber, "TYPE_ENREGISTREMENT", RsfErrorType.STRUCTURAL,
                            String.format("Line type '%c' found at line %d without a preceding 'A' line.", lineType, currentLineNumber)));
            }
        }
        */

        result.addError(new RsfError(0, "SYSTEM", RsfErrorType.SYSTEM_ERROR, "KeyFieldMatchValidationRule requires original line sequence and A-to-children relationship, which is not available in the current rsfData structure."));

        return result;
    }

    @Override
    public RsfRuleType getRuleType() {
        return RsfRuleType.DEPENDENCY;
    }
} 