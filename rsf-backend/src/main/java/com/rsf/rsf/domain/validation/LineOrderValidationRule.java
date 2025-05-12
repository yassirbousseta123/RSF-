package com.rsf.rsf.domain.validation;

import java.util.List;
import java.util.Map;

/**
 * Validates the structural order of lines in RSF data.
 * Rules:
 * - First line must be type 'A'.
 * - 'M' or 'L' lines must be preceded by a 'C' line within the same 'A' block.
 */
public class LineOrderValidationRule implements RsfValidationRule {

    @Override
    public RsfValidationResult validate(Map<Character, List<Map<String, String>>> rsfData) {
        RsfValidationResult result = new RsfValidationResult();
        List<Map<String, String>> allLines = rsfData.values().stream()
                                                  .flatMap(List::stream)
                                                  .toList(); // Need a way to preserve original order

        // TODO: This validation requires the original line order, which is lost
        //       when grouping by line type in RsfParsingService. The parsing or
        //       validation approach needs adjustment to access original sequence.
        //       For now, this rule cannot be fully implemented with the current data structure.

        // Placeholder logic assuming we have ordered lines:
        /*
        if (allLines.isEmpty()) {
            // Empty file validation might be handled elsewhere
            return result;
        }

        // Check first line type
        Map<String, String> firstLine = allLines.get(0);
        // Assuming TYPE_ENREGISTREMENT field exists and holds the line type
        if (!"A".equals(firstLine.get("TYPE_ENREGISTREMENT"))) {
             result.addError(new RsfError(1, "TYPE_ENREGISTREMENT", RsfErrorType.STRUCTURAL, "First line must be of type A."));
        }

        boolean lastLineWasC = false;
        boolean inABlock = false;
        for (int i = 0; i < allLines.size(); i++) {
             Map<String, String> currentLine = allLines.get(i);
             String lineTypeStr = currentLine.get("TYPE_ENREGISTREMENT");
             if (lineTypeStr == null || lineTypeStr.isEmpty()) continue; // Skip if type missing
             char lineType = lineTypeStr.charAt(0);
             int currentLineNumber = i + 1; // 1-based line number

             if (lineType == 'A') {
                 inABlock = true;
                 lastLineWasC = false; // Reset C flag for new A block
             }

             if (inABlock) {
                  if (lineType == 'C') {
                      lastLineWasC = true;
                  } else if (lineType == 'M' || lineType == 'L') {
                      if (!lastLineWasC) {
                          result.addError(new RsfError(currentLineNumber, "TYPE_ENREGISTREMENT", RsfErrorType.STRUCTURAL,
                           String.format("Line type '%c' at line %d must be preceded by a 'C' line within the same 'A' block.", lineType, currentLineNumber)));
                      }
                      // M/L doesn't reset the C flag; multiple M/L can follow one C
                  } else if (lineType != 'A' && lineType != 'B' && lineType != 'H' && lineType != 'P') {
                       // If other types reset the C flag, add logic here.
                       // Assuming B, H, P don't affect the M/L following C rule.
                       // lastLineWasC = false; // Reset if B/H/P break the C->M/L sequence possibility
                  }
             }
        }
        */

        result.addError(new RsfError(0, "SYSTEM", RsfErrorType.SYSTEM_ERROR, "LineOrderValidationRule requires original line sequence, which is not available in the current rsfData structure."));


        return result;
    }

    @Override
    public RsfRuleType getRuleType() {
        return RsfRuleType.STRUCTURAL;
    }
} 