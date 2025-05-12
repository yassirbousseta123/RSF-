package com.rsf.controller;

import com.rsf.domain.FileEntity;
import com.rsf.repo.FileRepo;
import com.rsf.rsf.domain.validation.RsfError;
import com.rsf.rsf.domain.validation.RsfErrorType;
import com.rsf.rsf.domain.validation.RsfValidationResult;
import com.rsf.rsf.service.RsfValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/validation")
public class ValidationController {
    private static final Logger log = LoggerFactory.getLogger(ValidationController.class);

    private final RsfValidationService validationService;
    private final FileRepo fileRepo;
    
    @Autowired
    public ValidationController(RsfValidationService validationService, FileRepo fileRepo) {
        this.validationService = validationService;
        this.fileRepo = fileRepo;
    }

    /**
     * Get full validation dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        try {
            log.info("Fetching validation dashboard data");
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Retrieve all files with validation errors
            List<FileEntity> files = fileRepo.findAll();
            
            // Calculate summary statistics
            int totalFiles = files.size();
            int filesWithErrors = 0;
            int totalErrors = 0;
            
            // Prepare error stats by type
            Map<RsfErrorType, Integer> errorsByType = new HashMap<>();
            for (RsfErrorType type : RsfErrorType.values()) {
                errorsByType.put(type, 0);
            }
            
            // Initialize error collection
            List<Map<String, Object>> errors = new ArrayList<>();
            Map<String, List<Map<String, Object>>> errorsByFile = new HashMap<>();
            
            // Process each file's validation results
            for (FileEntity file : files) {
                // In a real implementation, retrieve actual validation results from a database
                // For this example, we'll generate some sample data
                String fileIdStr = String.valueOf(file.hashCode()); // Use hashCode as a fallback
                List<RsfError> fileErrors = getMockValidationErrors(fileIdStr);
                
                if (!fileErrors.isEmpty()) {
                    filesWithErrors++;
                    totalErrors += fileErrors.size();
                    
                    // Group errors by type for statistics
                    for (RsfError error : fileErrors) {
                        RsfErrorType errorType = error.getErrorType();
                        errorsByType.put(errorType, errorsByType.getOrDefault(errorType, 0) + 1);
                        
                        // Convert to map for JSON response
                        Map<String, Object> errorMap = convertErrorToMap(error, file);
                        errors.add(errorMap);
                        
                        // Group by file
                        if (!errorsByFile.containsKey(fileIdStr)) {
                            errorsByFile.put(fileIdStr, new ArrayList<>());
                        }
                        errorsByFile.get(fileIdStr).add(errorMap);
                    }
                }
            }
            
            // Create summary object
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalFiles", totalFiles);
            summary.put("filesWithErrors", filesWithErrors);
            summary.put("totalErrors", totalErrors);
            summary.put("processingTime", 1500); // Mock processing time in ms
            summary.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            
            // Create stats array for chart data
            List<Map<String, Object>> stats = new ArrayList<>();
            for (Map.Entry<RsfErrorType, Integer> entry : errorsByType.entrySet()) {
                if (entry.getValue() > 0) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("name", entry.getKey().toString());
                    stat.put("value", entry.getValue());
                    stat.put("description", getErrorTypeDescription(entry.getKey()));
                    stats.add(stat);
                }
            }
            
            // Sort errors by timestamp (newest first)
            errors.sort((e1, e2) -> {
                String t1 = (String) e1.get("timestamp");
                String t2 = (String) e2.get("timestamp");
                return t2.compareTo(t1);
            });
            
            // Limit to most recent errors for the dashboard view
            List<Map<String, Object>> recentErrors = errors.stream()
                .limit(25)
                .collect(Collectors.toList());
            
            // Assemble the complete dashboard data
            dashboardData.put("summary", summary);
            dashboardData.put("stats", stats);
            dashboardData.put("errorsByFile", errorsByFile);
            dashboardData.put("errors", recentErrors);
            
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("Error retrieving dashboard data", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error retrieving dashboard data: " + e.getMessage()));
        }
    }
    
    /**
     * Get validation summary without detailed errors
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getValidationSummary() {
        try {
            log.info("Fetching validation summary");
            
            // In a real implementation, retrieve actual validation results
            // For now, return simplified version of the dashboard data
            List<FileEntity> files = fileRepo.findAll();
            
            // Calculate summary statistics
            int totalFiles = files.size();
            int filesWithErrors = 0;
            int totalErrors = 0;
            
            // Process each file's validation results
            for (FileEntity file : files) {
                String fileIdStr = String.valueOf(file.hashCode());
                List<RsfError> fileErrors = getMockValidationErrors(fileIdStr);
                if (!fileErrors.isEmpty()) {
                    filesWithErrors++;
                    totalErrors += fileErrors.size();
                }
            }
            
            // Create summary object
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalFiles", totalFiles);
            summary.put("filesWithErrors", filesWithErrors);
            summary.put("totalErrors", totalErrors);
            summary.put("processingTime", 1500); // Mock processing time in ms
            summary.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error retrieving validation summary", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error retrieving validation summary: " + e.getMessage()));
        }
    }
    
    /**
     * Get errors for a specific file
     */
    @GetMapping("/errors/{fileId}")
    public ResponseEntity<List<Map<String, Object>>> getErrorsForFile(@PathVariable String fileId) {
        try {
            log.info("Fetching errors for file: {}", fileId);
            
            Optional<FileEntity> fileOpt = fileRepo.findById(UUID.fromString(fileId));
            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            FileEntity file = fileOpt.get();
            List<RsfError> errors = getMockValidationErrors(fileId);
            
            List<Map<String, Object>> errorMaps = errors.stream()
                .map(error -> convertErrorToMap(error, file))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(errorMaps);
        } catch (Exception e) {
            log.error("Error retrieving errors for file {}", fileId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all errors with pagination and optional filtering
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getAllErrors(
            @RequestParam(required = false) String errorType,
            @RequestParam(required = false) String fileId,
            @RequestParam(required = false) String fieldName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {
        
        try {
            log.info("Fetching all errors with filters: type={}, fileId={}, field={}, page={}, size={}",
                    errorType, fileId, fieldName, page, size);
            
            List<Map<String, Object>> allErrors = new ArrayList<>();
            int totalItems = 0;
            
            // In a real implementation, this would use repository methods with proper pagination
            // For this mock implementation, we'll get all errors and filter in memory
            List<FileEntity> files;
            if (fileId != null && !fileId.isEmpty()) {
                Optional<FileEntity> fileOpt = fileRepo.findById(UUID.fromString(fileId));
                files = fileOpt.map(List::of).orElse(List.of());
            } else {
                files = fileRepo.findAll();
            }
            
            for (FileEntity file : files) {
                String fileIdStr = String.valueOf(file.hashCode());
                List<RsfError> errors = getMockValidationErrors(fileIdStr);
                
                // Apply filters
                if (errorType != null && !errorType.isEmpty()) {
                    try {
                        RsfErrorType type = RsfErrorType.valueOf(errorType);
                        errors = errors.stream()
                                .filter(e -> e.getErrorType() == type)
                                .collect(Collectors.toList());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid error type filter: {}", errorType);
                    }
                }
                
                if (fieldName != null && !fieldName.isEmpty()) {
                    errors = errors.stream()
                            .filter(e -> fieldName.equals(e.getField()))
                            .collect(Collectors.toList());
                }
                
                // Convert to maps
                List<Map<String, Object>> errorMaps = errors.stream()
                        .map(error -> convertErrorToMap(error, file))
                        .collect(Collectors.toList());
                
                allErrors.addAll(errorMaps);
                totalItems += errorMaps.size();
            }
            
            // Sort the results
            String[] sortParts = sort.split(",");
            String sortField = sortParts[0];
            boolean ascending = sortParts.length < 2 || !sortParts[1].equalsIgnoreCase("desc");
            
            allErrors.sort((e1, e2) -> {
                Object v1 = e1.get(sortField);
                Object v2 = e2.get(sortField);
                int result = 0;
                
                if (v1 instanceof String && v2 instanceof String) {
                    result = ((String) v1).compareTo((String) v2);
                } else if (v1 instanceof Number && v2 instanceof Number) {
                    result = Double.compare(((Number) v1).doubleValue(), ((Number) v2).doubleValue());
                }
                
                return ascending ? result : -result;
            });
            
            // Apply pagination
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, allErrors.size());
            
            List<Map<String, Object>> pagedErrors;
            if (fromIndex < allErrors.size()) {
                pagedErrors = allErrors.subList(fromIndex, toIndex);
            } else {
                pagedErrors = List.of();
            }
            
            int totalPages = (int) Math.ceil((double) totalItems / size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("errors", pagedErrors);
            response.put("totalItems", totalItems);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving all errors", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error retrieving errors: " + e.getMessage()));
        }
    }
    
    /**
     * Attempt to fix a validation error
     */
    @PostMapping("/fix/{errorId}")
    public ResponseEntity<Map<String, Object>> fixError(@PathVariable String errorId) {
        try {
            log.info("Attempting to fix error: {}", errorId);
            // In a real implementation, this would identify the error and attempt to fix it
            // For now, just return a simulated success response
            boolean success = true;
            String message = "Error fixed successfully";
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", message);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fixing validation error {}", errorId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Error fixing validation error: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Helper method to convert RsfError to a map for JSON responses
     */
    private Map<String, Object> convertErrorToMap(RsfError error, FileEntity file) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", UUID.randomUUID().toString()); // Generate a unique ID for this error
        map.put("fileId", String.valueOf(file.hashCode()));
        // Use safe fallbacks for fields that may not be accessible
        map.put("fileName", "File-" + file.hashCode());
        map.put("lineNumber", error.getLineNumber());
        map.put("fieldName", error.getField());
        map.put("errorType", error.getErrorType().toString());
        map.put("message", error.getMessage());
        map.put("recordIdentifier", error.getLineNumber() + "-" + error.getField()); // Simulate record ID
        map.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return map;
    }
    
    /**
     * Helper method to get a description for an error type
     */
    private String getErrorTypeDescription(RsfErrorType type) {
        switch (type) {
            case DATA_ERROR:
                return "Data-level validation errors";
            case STRUCTURAL:
            case STRUCTURAL_ERROR:
                return "Errors in file structure";
            case SEQUENCE_ERROR:
                return "Problems with record sequencing";
            case DEPENDENCY_ERROR:
                return "Inter-record dependency issues";
            case FILE_NAME_ERROR:
                return "Issues with filename format";
            case FORMAT_ERROR:
                return "Data format problems";
            case SYSTEM_ERROR:
                return "System-level validation failures";
            default:
                return "Other validation errors";
        }
    }
    
    /**
     * Helper method to generate mock validation errors for testing
     * In a real implementation, these would come from the database
     */
    private List<RsfError> getMockValidationErrors(String fileId) {
        // To make results deterministic based on fileId
        Random random = new Random(fileId.hashCode());
        int errorCount = random.nextInt(10); // 0-9 errors per file
        
        List<RsfError> errors = new ArrayList<>();
        for (int i = 0; i < errorCount; i++) {
            RsfErrorType[] types = RsfErrorType.values();
            RsfErrorType type = types[random.nextInt(types.length)];
            
            String fieldName;
            switch (random.nextInt(5)) {
                case 0: fieldName = "N_FINESS_EPMSI"; break;
                case 1: fieldName = "N_IMMATRICULATION_ASSURE"; break;
                case 2: fieldName = "DATE_NAISSANCE"; break;
                case 3: fieldName = "CODE_ACTE"; break;
                default: fieldName = "MONTANT_BASE_REMBOURSEMENT"; break;
            }
            
            String message;
            switch (type) {
                case DATA_ERROR:
                    message = "Invalid data format for " + fieldName;
                    break;
                case STRUCTURAL:
                case STRUCTURAL_ERROR:
                    message = "Missing required line or structure issue";
                    break;
                case SEQUENCE_ERROR:
                    message = "Invalid sequence for records";
                    break;
                case DEPENDENCY_ERROR:
                    message = fieldName + " references a non-existent record";
                    break;
                case FILE_NAME_ERROR:
                    message = "Invalid file naming format";
                    break;
                case FORMAT_ERROR:
                    message = "Format error in " + fieldName;
                    break;
                case SYSTEM_ERROR:
                    message = "System error during validation";
                    break;
                default:
                    message = "Unknown error type";
            }
            
            errors.add(new RsfError(random.nextInt(100) + 1, fieldName, type, message));
        }
        
        return errors;
    }
} 