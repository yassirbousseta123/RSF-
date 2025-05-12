package com.rsf.rsf.domain.validation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Validates that the RSF data contains at least one 'B' or 'C' line.
 */
public class ExpectedLineCountRule implements RsfValidationRule {

    @Override
    public RsfValidationResult validate(Map<Character, List<Map<String, String>>> rsfData) {
        RsfValidationResult result = new RsfValidationResult();

        boolean hasBLine = !rsfData.getOrDefault('B', Collections.emptyList()).isEmpty();
        boolean hasCLine = !rsfData.getOrDefault('C', Collections.emptyList()).isEmpty();

        if (!hasBLine && !hasCLine) {
            result.addError(new RsfError(
                    0, // Error applies to the whole file
                    "File Structure",
                    RsfErrorType.STRUCTURAL,
                    "File must contain at least one B or C line."
            ));
        }

        return result;
    }

    @Override
    public RsfRuleType getRuleType() {
        return RsfRuleType.STRUCTURAL;
    }
} 