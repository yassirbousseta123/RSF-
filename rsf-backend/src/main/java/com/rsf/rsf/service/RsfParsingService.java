package com.rsf.rsf.service;

import com.rsf.rsf.config.RsfMappingConfig;
import com.rsf.rsf.domain.models.FieldDefinition;
import com.rsf.rsf.exception.RsfParsingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RsfParsingService {

    private final RsfMappingConfig rsfMappingConfig;

    public RsfParsingService(RsfMappingConfig rsfMappingConfig) {
        this.rsfMappingConfig = rsfMappingConfig;
    }

    /**
     * Parses an RSF text file based on the provided year's mapping configuration.
     *
     * @param inputStream The input stream of the RSF file content.
     * @param year The year for which to retrieve the mapping configuration.
     * @return A Map where keys are line type characters and values are lists of maps,
     *         each map representing a parsed line with field names as keys and extracted values as strings.
     * @throws RsfParsingException if parsing fails due to I/O errors or configuration issues.
     */
    public Map<Character, List<Map<String, String>>> parseRsfFile(InputStream inputStream, int year) throws RsfParsingException {
        Map<Character, List<FieldDefinition>> yearMappings = rsfMappingConfig.rsfFieldMappings().get(year);
        if (yearMappings == null) {
            log.error("No RSF mapping configuration found for year: {}", year);
            throw new RsfParsingException("No RSF mapping configuration found for year: " + year);
        }

        Map<Character, List<Map<String, String>>> parsedData = new HashMap<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    log.debug("Skipping empty line at line number: {}", lineNumber);
                    continue; // Skip empty lines
                }

                if (line.length() < 1) {
                     log.warn("Line {} is too short to determine line type: '{}'", lineNumber, line);
                     // Decide how to handle this: skip, error, default type? Skipping for now.
                     continue;
                }

                char lineType = line.charAt(0);
                List<FieldDefinition> fieldDefinitions = yearMappings.get(lineType);

                if (fieldDefinitions == null) {
                    log.warn("No field definitions found for line type '{}' in year {} at line {}. Skipping line.", lineType, year, lineNumber);
                    continue; // Skip lines with unmapped types for the given year
                }

                Map<String, String> lineData = new HashMap<>();
                for (FieldDefinition field : fieldDefinitions) {
                    try {
                         int start = field.getStartIndex(); // 0-based start
                         int end = field.getEndIndex(); // 0-based end (exclusive)

                         if (start >= line.length()) {
                             // Field start position is beyond the line length, store as empty
                             log.trace("Field '{}' start index {} is out of bounds for line {} (length {}). Setting empty value.", field.getName(), start, lineNumber, line.length());
                             lineData.put(field.getName(), "");
                         } else {
                             // Ensure end index doesn't exceed line length
                             int actualEnd = Math.min(end, line.length());
                             String value = line.substring(start, actualEnd).trim();
                             lineData.put(field.getName(), value);
                             log.trace("Parsed field '{}' for line type '{}', line {}: '{}'", field.getName(), lineType, lineNumber, value);
                         }
                    } catch (IndexOutOfBoundsException e) {
                         // This catch block might be redundant due to the checks above, but serves as a safeguard.
                         log.error("Error parsing field '{}' on line {} (type {}). Line content: '{}'. Start: {}, End: {}. Error: {}",
                                 field.getName(), lineNumber, lineType, line, field.getStartIndex(), field.getEndIndex(), e.getMessage());
                         // Store empty string or handle error differently (e.g., throw exception, add error marker)
                         lineData.put(field.getName(), "");
                    } catch (Exception e) {
                         log.error("Unexpected error parsing field '{}' on line {} (type {}). Error: {}",
                                 field.getName(), lineNumber, lineType, e.getMessage(), e);
                         // Decide on error handling: skip field, skip line, throw exception? Storing empty for now.
                         lineData.put(field.getName(), "");
                    }
                }

                parsedData.computeIfAbsent(lineType, k -> new ArrayList<>()).add(lineData);

            }
        } catch (IOException e) {
            log.error("Failed to read RSF file content. Error at line {}: {}", lineNumber, e.getMessage(), e);
            throw new RsfParsingException("Failed to read RSF file content", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred during RSF parsing at line {}: {}", lineNumber, e.getMessage(), e);
            throw new RsfParsingException("An unexpected error occurred during parsing", e);
        }

        if (parsedData.isEmpty() && lineNumber > 0) {
             log.warn("RSF parsing finished, but no data was extracted. Check file content and mappings for year {}.", year);
        } else {
            log.info("Successfully parsed RSF file for year {}. Processed {} lines. Found data for line types: {}",
                    year, lineNumber, parsedData.keySet());
        }


        return parsedData;
    }
} 