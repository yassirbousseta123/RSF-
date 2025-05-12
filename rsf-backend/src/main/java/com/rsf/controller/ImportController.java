package com.rsf.controller;

import com.rsf.domain.FileEntity;
import com.rsf.domain.User;
import com.rsf.repo.FileRepo;
import com.rsf.repo.UserRepo;
import com.rsf.service.StorageService;
import com.rsf.util.ApplicationContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/v1/import")
@RequiredArgsConstructor
@Slf4j
public class ImportController {
    private static final Logger log = LoggerFactory.getLogger(ImportController.class);
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("zip", "xls", "xlsx", "csv", "txt");

    private final StorageService storage;
    private final UserRepo users;
    private final FileRepo files;
    
    @Value("${file.storage-path:uploads}") 
    private String storagePath;
    
    // Thread-safe storage for import progress and results (in production, use Redis or another persistent store)
    private final Map<String, Integer> importProgress = new ConcurrentHashMap<>();
    private final Map<String, List<String>> importErrors = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> importResults = new ConcurrentHashMap<>();
    private final Map<String, ValidationResult> validationResults = new ConcurrentHashMap<>();
    private final Map<String, List<String>> processedDataLines = new ConcurrentHashMap<>();

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile file,
                              @AuthenticationPrincipal UserDetails auth) {
        try {
            log.info("File upload request received: {}, size: {}", file.getOriginalFilename(), file.getSize());
            
            // Validate file 
            ValidationResult validationResult = validateFile(file);
            if (!validationResult.isValid()) {
                log.warn("File validation failed: {}", validationResult.getErrors());
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "errors", validationResult.getErrors()
                ));
            }
            
            User uploader = null;
            if (auth != null) {
                uploader = users.findByUsername(auth.getUsername())
                        .orElse(null);
                log.info("Upload by user: {}", auth.getUsername());
            } else {
                log.info("Anonymous upload (no authentication)");
            }
            
            FileEntity savedFile = storage.store(file, uploader);
            String fileId = savedFile.getId().toString(); // Get UUID directly from field
            log.info("File saved successfully with ID: {}", fileId);
            
            // Initialize progress tracking
            importProgress.put(fileId, 0);
            importErrors.put(fileId, new ArrayList<>());
            validationResults.put(fileId, validationResult);
            processedDataLines.put(fileId, new ArrayList<>());
            
            // Start processing in background (real processing)
            new Thread(() -> {
                try {
                    processImportInBackground(fileId, file.getOriginalFilename(), savedFile);
                } catch (Throwable t) {
                    // Catch any error that might be killing the background thread
                    log.error("CRITICAL ERROR in background processing thread: {}", t.getMessage(), t);
                    // Ensure progress is marked as completed with error state
                    importProgress.put(fileId, 100);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", true);
                    errorResult.put("message", "Processing failed with critical error: " + t.getMessage());
                    errorResult.put("filename", file.getOriginalFilename());
                    importResults.put(fileId, errorResult);
                }
            }).start();
            
            return ResponseEntity.ok(savedFile);
        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "File upload failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/progress/{fileId}")
    public ResponseEntity<Map<String, Object>> getImportProgress(@PathVariable String fileId) {
        log.debug("Getting progress for file: {}", fileId);
        if (!importProgress.containsKey(fileId)) {
            log.warn("No progress found for file ID: {}", fileId);
            return ResponseEntity.ok(
                Map.of(
                    "fileId", fileId,
                    "progress", 0,
                    "errors", Collections.emptyList(),
                    "complete", false,
                    "message", "Processing not started or file not found"
                )
            );
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("fileId", fileId);
        response.put("progress", importProgress.get(fileId));
        response.put("errors", importErrors.get(fileId));
        response.put("complete", importProgress.get(fileId) >= 100);
        
        // Include validation information if available
        if (validationResults.containsKey(fileId)) {
            ValidationResult validation = validationResults.get(fileId);
            response.put("validation", Map.of(
                "valid", validation.isValid(),
                "fileType", validation.getFileType(),
                "details", validation.getDetails()
            ));
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/results/{fileId}")
    public ResponseEntity<Map<String, Object>> getImportResults(@PathVariable String fileId) {
        log.debug("Getting results for file: {}", fileId);
        if (!importResults.containsKey(fileId)) {
            log.warn("No results found for file ID: {}", fileId);
            return ResponseEntity.ok(
                Map.of(
                    "fileId", fileId,
                    "status", "Not completed",
                    "message", "Import results not available"
                )
            );
        }
        
        Map<String, Object> results = new HashMap<>(importResults.get(fileId));
        
        // Include errors list for detailed error reporting
        if (importErrors.containsKey(fileId)) {
            results.put("errors", importErrors.get(fileId));
        }
        
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/validate/{fileId}")
    public ResponseEntity<ValidationResult> getValidationResult(@PathVariable String fileId) {
        log.debug("Getting validation results for file: {}", fileId);
        if (!validationResults.containsKey(fileId)) {
            log.warn("No validation results found for file ID: {}", fileId);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(validationResults.get(fileId));
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of("status", "Import service is running"));
    }
    
    /**
     * Validates the uploaded file
     */
    private ValidationResult validateFile(MultipartFile file) {
        ValidationResult result = new ValidationResult();
        
        // Check if file is empty
        if (file.isEmpty()) {
            result.addError("The file is empty");
            return result;
        }
        
        // Check file size (max 70MB as configured in application.yml)
        if (file.getSize() > 70 * 1024 * 1024) { 
            result.addError("File size exceeds maximum limit (70MB)");
            return result;
        }
        
        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            result.setFileType(extension);
            
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                result.addError("Invalid file format. Allowed formats: " + String.join(", ", ALLOWED_EXTENSIONS));
                return result;
            }
            
            // Additional validation based on file type
            if ("zip".equals(extension)) {
                result.addDetail("ZIP file detected - will be extracted");
                // We could add ZIP specific validation here
            } else if ("xls".equals(extension) || "xlsx".equals(extension)) {
                result.addDetail("Excel file detected - will process spreadsheet");
                // We could add Excel specific validation here
            } else if ("csv".equals(extension)) {
                result.addDetail("CSV file detected - will process data");
            }
        } else {
            result.addError("Filename is missing");
            return result;
        }
        
        result.setValid(true);
        return result;
    }
    
    // Real file processing
    private void processImportInBackground(String fileId, String filename, FileEntity fileEntity) {
        try {
            // Report initial progress
            importProgress.put(fileId, 10);
            log.info("Starting background processing for file: {}, ID: {}", filename, fileId);
            Thread.sleep(200); // Small delay for UI
            
            // Validate the file based on extension
            String extension = "";
            if (filename != null && filename.contains(".")) {
                extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            }
            
            // Get the file from storage - Fix for FileEntity ID access
            log.info("Loading file resource from storage for ID: {}", fileId);
            Resource fileResource = storage.load(UUID.fromString(fileId));
            File physicalFile = fileResource.getFile();
            
            log.info("Processing file: {}, type: {}", filename, extension);
            addValidationDetail(fileId, "Starting file processing");
            
            // Process different file types
            List<String> processedRecords = new ArrayList<>();
            int totalRecordCount = 0;
            boolean isRsfFile = filename != null && filename.startsWith("RSF_");
            boolean isHoraireFile = filename != null && filename.startsWith("HORAIRES_");
            
            log.info("File identified as: RSF={}, HORAIRE={}", isRsfFile, isHoraireFile);
            
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("filename", filename);
            
            // Specific processing based on file type
            try {
                if ("zip".equals(extension)) {
                    log.info("Starting ZIP file processing");
                    Map<String, Object> zipResults = processZipFile(physicalFile, fileId, isRsfFile);
                    result.putAll(zipResults);
                    
                    // Add processed records for display
                    if (zipResults.containsKey("sampleRecords")) {
                        List<String> samples = (List<String>) zipResults.get("sampleRecords");
                        processedRecords.addAll(samples);
                    }
                    
                    // Add RSF stats
                    if (zipResults.containsKey("rsfStats")) {
                        Map<String, Object> stats = (Map<String, Object>) zipResults.get("rsfStats");
                        result.putAll(stats);
                    }
                    
                    if (zipResults.containsKey("processedLines")) {
                        List<String> lines = (List<String>) zipResults.get("processedLines");
                        processedDataLines.put(fileId, lines);
                    }
                    log.info("ZIP processing complete");
                } else if ("xls".equals(extension) || "xlsx".equals(extension)) {
                    log.info("Starting Excel file processing");
                    Map<String, Object> excelResults = processExcelFile(physicalFile, extension, fileId, isRsfFile);
                    result.putAll(excelResults);
                    
                    // Add processed records for display
                    if (excelResults.containsKey("sampleRecords")) {
                        List<String> samples = (List<String>) excelResults.get("sampleRecords");
                        processedRecords.addAll(samples);
                    }
                    
                    // Add RSF stats
                    if (excelResults.containsKey("rsfStats")) {
                        Map<String, Object> stats = (Map<String, Object>) excelResults.get("rsfStats");
                        result.putAll(stats);
                    }
                    
                    // Add HORAIRE data if available
                    if (isHoraireFile && excelResults.containsKey("horaireUpdates")) {
                        log.info("Adding HORAIRE updates to results. Count: {}", 
                                ((List<?>)excelResults.get("horaireUpdates")).size());
                        result.put("horaireUpdates", excelResults.get("horaireUpdates"));
                        result.put("totalRowsProcessed", excelResults.getOrDefault("totalRowsProcessed", 0));
                        result.put("updatedCount", excelResults.getOrDefault("updatedCount", 0));
                    }
                    log.info("Excel processing complete");
                } else if ("csv".equals(extension) || "txt".equals(extension)) {
                    log.info("Starting CSV/TXT file processing");
                    Map<String, Object> csvResults = processCsvFile(physicalFile, fileId, isRsfFile);
                    result.putAll(csvResults);
                    
                    // Add processed records for display
                    if (csvResults.containsKey("sampleRecords")) {
                        List<String> samples = (List<String>) csvResults.get("sampleRecords");
                        processedRecords.addAll(samples);
                    }
                    
                    // Add RSF stats
                    if (csvResults.containsKey("rsfStats")) {
                        Map<String, Object> stats = (Map<String, Object>) csvResults.get("rsfStats");
                        result.putAll(stats);
                    }
                    log.info("CSV/TXT processing complete");
                }
            } catch (Exception e) {
                log.error("Error processing file: {}", e.getMessage(), e);
                importErrors.get(fileId).add("Error processing file: " + e.getMessage());
                result.put("error", true);
                result.put("errorMessage", "File processing failed: " + e.getMessage());
            }
            
            // Add processed records to results
            result.put("processedRecords", processedRecords);
            log.info("Added {} processed records to results", processedRecords.size());
            
            // Add errors if any
            if (importErrors.containsKey(fileId) && !importErrors.get(fileId).isEmpty()) {
                result.put("errors", importErrors.get(fileId));
                log.info("Added {} errors to results", importErrors.get(fileId).size());
            }

            // Mark as completed and store results
            log.info("Setting progress to 100% and storing results for fileId: {}", fileId);
            importProgress.put(fileId, 100);
            importResults.put(fileId, result);
            
            log.info("Completed processing file: {}, fileId: {}", filename, fileId);
        } catch (Exception e) {
            log.error("Error in import process", e);
            importErrors.get(fileId).add("Processing failed: " + e.getMessage());
            
            Map<String, Object> result = new HashMap<>();
            result.put("error", true);
            result.put("message", "Processing failed: " + e.getMessage());
            if (importErrors.containsKey(fileId)) {
                result.put("errors", importErrors.get(fileId));
            }
            
            importProgress.put(fileId, 100); // Mark as completed, even though it failed
            importResults.put(fileId, result);
        }
    }
    
    private Map<String, Object> processZipFile(File zipFile, String fileId, boolean isRsfFile) throws IOException {
        List<String> sampleRecords = new ArrayList<>();
        int recordCount = 0;
        Map<Character, Integer> lineTypeCounts = new HashMap<>();
        int errorCount = 0;
        String firstDateSoins = null;
        String lastDateSoins = null;
        List<String> errorDetails = new ArrayList<>();

        try (ZipFile zip = new ZipFile(zipFile)) {
            int totalEntries = zip.size();
            int processedEntries = 0;
            
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".txt")) {
                    // Process each text file inside the ZIP
                    try (InputStream is = zip.getInputStream(entry);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                        
                        String line;
                        int lineNumber = 0;
                        while ((line = reader.readLine()) != null) {
                            recordCount++;
                            lineNumber++;
                            
                            // RSF specific processing
                            if (isRsfFile && line.length() > 0) {
                                char lineType = line.charAt(0);
                                lineTypeCounts.put(lineType, lineTypeCounts.getOrDefault(lineType, 0) + 1);
                                
                                // Check for dates in B, C, or M lines
                                if ((lineType == 'B' || lineType == 'C') && line.length() >= 116) {
                                    String dateSoins = extractDateSoins(line, lineType);
                                    if (dateSoins != null) {
                                        if (firstDateSoins == null || dateSoins.compareTo(firstDateSoins) < 0) {
                                            firstDateSoins = dateSoins;
                                        }
                                        if (lastDateSoins == null || dateSoins.compareTo(lastDateSoins) > 0) {
                                            lastDateSoins = dateSoins;
                                        }
                                    }
                                } else if (lineType == 'M' && line.length() >= 113) {
                                    String dateSoins = extractDateSoins(line, lineType);
                                    if (dateSoins != null) {
                                        if (firstDateSoins == null || dateSoins.compareTo(firstDateSoins) < 0) {
                                            firstDateSoins = dateSoins;
                                        }
                                        if (lastDateSoins == null || dateSoins.compareTo(lastDateSoins) > 0) {
                                            lastDateSoins = dateSoins;
                                        }
                                    }
                                }
                                
                                // Validate the line and count errors
                                boolean isValidLine = validateRsfLine(line, lineType);
                                if (!isValidLine) {
                                    errorCount++;
                                    // Add detailed error information
                                    String errorMessage = getRsfLineErrorMessage(line, lineType);
                                    String linePreview = line.length() > 50 ? line.substring(0, 47) + "..." : line;
                                    errorDetails.add("File: " + entry.getName() + " - Line " + lineNumber + 
                                                    " (" + lineType + "): " + errorMessage + " - " + linePreview);
                                    
                                    // Limit the number of detailed errors to prevent excessive memory usage
                                    if (errorDetails.size() >= 100) {
                                        errorDetails.add("Additional errors omitted (too many to display)");
                                        break;
                                    }
                                }
                            }
                            
                            // Save a sample of the first records found
                            if (sampleRecords.size() < 5) {
                                sampleRecords.add("File: " + entry.getName() + " - Record: " + line);
                            }
                        }
                    }
                }
                
                processedEntries++;
                // Update progress proportionally
                int progress = 20 + (int)((float)processedEntries / totalEntries * 70);
                importProgress.put(fileId, Math.min(progress, 89)); // Cap at 89%
                
                addValidationDetail(fileId, "Processed " + processedEntries + " of " + totalEntries + " ZIP entries");
            }
        }
        
        Map<String, Object> rsfStats = new HashMap<>();
        if (isRsfFile) {
            rsfStats.put("totalLines", recordCount);
            rsfStats.put("lineTypeCounts", lineTypeCounts);
            rsfStats.put("errorCount", errorCount);
            rsfStats.put("firstDateSoins", firstDateSoins);
            rsfStats.put("lastDateSoins", lastDateSoins);
        }
        
        // Use a LinkedHashMap since Map.of has a fixed size
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordCount", recordCount);
        result.put("sampleRecords", sampleRecords);
        result.put("rsfStats", rsfStats);
        result.put("errorDetails", errorDetails);
        
        return result;
    }
    
    private String extractDateSoins(String line, char lineType) {
        try {
            int startPos;
            if (lineType == 'B' || lineType == 'C') {
                startPos = 108 - 1; // -1 because index is 0-based
            } else if (lineType == 'M') {
                startPos = 105 - 1; // -1 because index is 0-based
            } else {
                return null;
            }
            
            // Ensure the line is long enough
            if (line.length() < startPos + 8) {
                return null;
            }
            
            return line.substring(startPos, startPos + 8);
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean validateRsfLine(String line, char lineType) {
        // Basic validation - real implementation would be more complex
        if (line == null || line.isEmpty()) {
            return false;
        }
        
        // Check for minimum line length based on line type
        // Updated to match the 2017 mapping document lengths
        switch (lineType) {
            case 'A': return line.length() >= 207;
            case 'B': return line.length() >= 193;
            case 'C': return line.length() >= 190;
            case 'H': return line.length() >= 157;
            case 'M': return line.length() >= 166;
            case 'P': return line.length() >= 157;
            case 'L': return line.length() >= 194;
            default: return line.length() > 1; // At minimum, should have more than just the type marker
        }
    }
    
    /**
     * Get a detailed error message for an invalid RSF line
     */
    private String getRsfLineErrorMessage(String line, char lineType) {
        if (line == null || line.isEmpty()) {
            return "Empty line found";
        }
        
        // Updated expected lengths to match the 2017 mapping document
        int expectedLength;
        switch (lineType) {
            case 'A': expectedLength = 207; break;
            case 'B': expectedLength = 193; break;
            case 'C': expectedLength = 190; break;
            case 'H': expectedLength = 157; break;
            case 'M': expectedLength = 166; break;
            case 'P': expectedLength = 157; break;
            case 'L': expectedLength = 194; break;
            default: return "Unknown line type: " + lineType;
        }
        
        if (line.length() < expectedLength) {
            return "Line type " + lineType + " is too short: " + line.length() + " chars (expected " + expectedLength + ")";
        }
        
        return "Invalid format for line type " + lineType;
    }
    
    private Map<String, Object> processExcelFile(File excelFile, String extension, String fileId, boolean isRsfFile) throws IOException {
        List<String> sampleRecords = new ArrayList<>();
        int recordCount = 0;
        Map<Character, Integer> lineTypeCounts = new HashMap<>();
        int errorCount = 0;
        String firstDateSoins = null;
        String lastDateSoins = null;
        List<String> errorDetails = new ArrayList<>();
        
        // Check if it's a HORAIRE file
        boolean isHoraireFile = excelFile.getName().startsWith("HORAIRES_");
        List<Map<String, Object>> horaireUpdates = new ArrayList<>();
        int totalRowsProcessed = 0;
        int updatedCount = 0;
        
        // Create result map
        Map<String, Object> result = new HashMap<>();
        
        try (InputStream is = Files.newInputStream(excelFile.toPath());
             Workbook workbook = "xlsx".equals(extension) ? new XSSFWorkbook(is) : new HSSFWorkbook(is)) {
            
            int totalSheets = workbook.getNumberOfSheets();
            
            // Handle HORAIRE files using the RSF module if available
            if (isHoraireFile) {
                try {
                    log.info("Starting HORAIRE file processing for file: {}", excelFile.getName());
                    
                    // Use reflection to access RSF module classes dynamically (to avoid direct dependencies)
                    Class<?> excelParserServiceClass;
                    try {
                        excelParserServiceClass = Class.forName("com.rsf.rsf.service.ExcelParserService");
                        log.info("Found ExcelParserService class");
                    } catch (ClassNotFoundException e) {
                        log.error("ExcelParserService class not found: {}", e.getMessage());
                        errorDetails.add("Could not find ExcelParserService class: " + e.getMessage());
                        throw new RuntimeException("ExcelParserService class not found", e);
                    }
                    
                    Object excelParserService = getBean(excelParserServiceClass);
                    if (excelParserService == null) {
                        log.error("Failed to get ExcelParserService bean from Spring context");
                        errorDetails.add("ExcelParserService bean not available");
                        throw new RuntimeException("ExcelParserService bean not available");
                    }
                    log.info("Got ExcelParserService bean: {}", excelParserService.getClass().getName());
                    
                    // Parse HORAIRE file
                    Method parseHoraireMethod;
                    try {
                        parseHoraireMethod = excelParserServiceClass.getDeclaredMethod("parseHoraireFile", 
                                InputStream.class, String.class, Object.class);
                        parseHoraireMethod.setAccessible(true);
                        log.info("Found parseHoraireFile method");
                    } catch (NoSuchMethodException e) {
                        log.error("parseHoraireFile method not found: {}", e.getMessage());
                        errorDetails.add("Method not found in ExcelParserService: parseHoraireFile");
                        throw new RuntimeException("parseHoraireFile method not found", e);
                    }
                    
                    // Create RsfValidationResult
                    Class<?> validationResultClass;
                    Object validationResult;
                    try {
                        validationResultClass = Class.forName("com.rsf.rsf.domain.validation.RsfValidationResult");
                        validationResult = validationResultClass.getDeclaredConstructor(String.class).newInstance(excelFile.getName());
                        log.info("Created RsfValidationResult");
                    } catch (Exception e) {
                        log.error("Failed to create RsfValidationResult: {}", e.getMessage());
                        errorDetails.add("Could not create validation result: " + e.getMessage());
                        throw new RuntimeException("Failed to create RsfValidationResult", e);
                    }
                    
                    // Reopen input stream for parsing
                    try (InputStream parseIs = Files.newInputStream(excelFile.toPath())) {
                        log.info("Invoking parseHoraireFile method");
                        Object excelResult = parseHoraireMethod.invoke(excelParserService, parseIs, excelFile.getName(), validationResult);
                        
                        if (excelResult == null) {
                            log.error("parseHoraireFile returned null result");
                            errorDetails.add("HORAIRE parser returned null result");
                            throw new RuntimeException("HORAIRE parser returned null result");
                        }
                        log.info("parseHoraireFile returned result: {}", excelResult.getClass().getName());
                        
                        // Extract HORAIRE updates
                        Method getHoraireUpdatesMethod;
                        Method getTotalRowsProcessedMethod;
                        Method getValidationResultMethod;
                        
                        try {
                            getHoraireUpdatesMethod = excelResult.getClass().getMethod("getHoraireUpdates");
                            getTotalRowsProcessedMethod = excelResult.getClass().getMethod("getTotalRowsProcessed");
                            getValidationResultMethod = excelResult.getClass().getMethod("getValidationResult");
                            log.info("Found all required methods on ExcelParsingResult");
                        } catch (NoSuchMethodException e) {
                            log.error("Required method not found on ExcelParsingResult: {}", e.getMessage());
                            errorDetails.add("Required method not found: " + e.getMessage());
                            throw new RuntimeException("Missing required method on ExcelParsingResult", e);
                        }
                        
                        List<?> updates;
                        try {
                            updates = (List<?>) getHoraireUpdatesMethod.invoke(excelResult);
                            totalRowsProcessed = (int) getTotalRowsProcessedMethod.invoke(excelResult);
                            log.info("Got {} HORAIRE updates, totalRowsProcessed={}", 
                                    updates != null ? updates.size() : 0, totalRowsProcessed);
                        } catch (Exception e) {
                            log.error("Failed to get updates from result: {}", e.getMessage());
                            errorDetails.add("Could not extract updates: " + e.getMessage());
                            throw new RuntimeException("Failed to extract updates", e);
                        }
                                
                        // Copy updates to our local list
                        if (updates != null) {
                            for (Object update : updates) {
                                // For each HoraireUpdateRecord, convert to a Map for JSON serialization
                                Map<String, Object> updateMap = new HashMap<>();
                                
                                try {
                                    // Get properties using reflection
                                    Method getSourceRowNumMethod = update.getClass().getMethod("getSourceRowNum");
                                    Method getNumImmatriculationMethod = update.getClass().getMethod("getNumImmatriculation");
                                    Method getDateNaissanceMethod = update.getClass().getMethod("getDateNaissance");
                                    Method getDateSoinsMethod = update.getClass().getMethod("getDateSoins");
                                    Method getCodeActeMethod = update.getClass().getMethod("getCodeActe");
                                    Method getHoraireMethod = update.getClass().getMethod("getHoraire");
                                    
                                    updateMap.put("sourceRowNum", getSourceRowNumMethod.invoke(update));
                                    updateMap.put("numImmatriculation", getNumImmatriculationMethod.invoke(update));
                                    updateMap.put("dateNaissance", getDateNaissanceMethod.invoke(update));
                                    updateMap.put("dateSoins", getDateSoinsMethod.invoke(update));
                                    updateMap.put("codeActe", getCodeActeMethod.invoke(update));
                                    updateMap.put("horaire", getHoraireMethod.invoke(update));
                                    
                                    horaireUpdates.add(updateMap);
                                } catch (Exception e) {
                                    log.error("Error extracting property from HORAIRE update: {}", e.getMessage(), e);
                                    errorDetails.add("Error extracting property: " + e.getMessage());
                                }
                            }
                            
                            try {
                                // Get validation errors
                                Object validation = getValidationResultMethod.invoke(excelResult);
                                Method hasErrorsMethod = validation.getClass().getMethod("hasErrors");
                                boolean hasErrors = (boolean) hasErrorsMethod.invoke(validation);
                                
                                if (hasErrors) {
                                    Method getErrorsMethod = validation.getClass().getMethod("getErrors");
                                    List<?> errors = (List<?>) getErrorsMethod.invoke(validation);
                                    
                                    for (Object error : errors) {
                                        Method getMessageMethod = error.getClass().getMethod("getMessage");
                                        String errorMessage = (String) getMessageMethod.invoke(error);
                                        errorDetails.add(errorMessage);
                                    }
                                }
                            } catch (Exception e) {
                                log.error("Error extracting validation errors: {}", e.getMessage(), e);
                                errorDetails.add("Error extracting validation errors: " + e.getMessage());
                            }
                        }
                        
                        // Add all HORAIRE updates to the result map for the frontend to display
                        result.put("totalRowsProcessed", totalRowsProcessed);
                        result.put("updatedCount", horaireUpdates.size());
                        result.put("horaireUpdates", horaireUpdates);
                        
                        // Add some sample records for display
                        recordCount = totalRowsProcessed;
                        
                        // Create sample records for display
                        sampleRecords.add("HORAIRE File: " + excelFile.getName() + " - Total Records: " + totalRowsProcessed);
                        
                        // Add ALL records to the processed data lines collection for frontend access
                        String fileIdKey = fileId;
                        if (!processedDataLines.containsKey(fileIdKey)) {
                            processedDataLines.put(fileIdKey, new ArrayList<>());
                        }
                        
                        // Add a header row first
                        processedDataLines.get(fileIdKey).add("num_immatriculation | date_naissance | date_soins | code_acte | horaire");
                        
                        // Add all HORAIRE records as processed lines
                        for (Map<String, Object> updateMap : horaireUpdates) {
                            processedDataLines.get(fileIdKey).add(String.format("Row: %s | %s | %s | %s | %s | %s",
                                    updateMap.get("sourceRowNum"),
                                    updateMap.get("numImmatriculation"),
                                    updateMap.get("dateNaissance"),
                                    updateMap.get("dateSoins"),
                                    updateMap.get("codeActe"),
                                    updateMap.get("horaire")));
                        }
                        
                        // Add a few samples for the result itself
                        for (int i = 0; i < Math.min(5, horaireUpdates.size()); i++) {
                            Map<String, Object> updateMap = horaireUpdates.get(i);
                            sampleRecords.add(String.format("Row %s: Num Immat=%s, Date Naissance=%s, Date Soins=%s, Code Acte=%s, Horaire=%s",
                                    updateMap.get("sourceRowNum"),
                                    updateMap.get("numImmatriculation"),
                                    updateMap.get("dateNaissance"),
                                    updateMap.get("dateSoins"),
                                    updateMap.get("codeActe"),
                                    updateMap.get("horaire")));
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing HORAIRE file using RSF module: {}", e.getMessage(), e);
                    errorDetails.add("Error processing HORAIRE file: " + e.getMessage());
                }
            } else {
                // Process regular Excel file
                for (int i = 0; i < totalSheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    
                    // Update progress for each sheet
                    importProgress.put(fileId, 20 + (i * 70 / totalSheets));
                    addValidationDetail(fileId, "Processing sheet " + (i+1) + " of " + totalSheets);
                    
                    for (Row row : sheet) {
                        recordCount++;
                        
                        // Process RSF data if applicable
                        if (isRsfFile) {
                            Cell firstCell = row.getCell(0);
                            if (firstCell != null && firstCell.getCellType() == CellType.STRING) {
                                String cellValue = firstCell.getStringCellValue();
                                if (cellValue != null && cellValue.length() > 0) {
                                    char lineType = cellValue.charAt(0);
                                    if (Character.isLetter(lineType)) {
                                        lineTypeCounts.put(lineType, lineTypeCounts.getOrDefault(lineType, 0) + 1);
                                        
                                        // Look for date fields in specific columns
                                        if (lineType == 'B' || lineType == 'C' || lineType == 'M') {
                                            int dateColumn = (lineType == 'M') ? 5 : 7; // Adjust based on your Excel structure
                                            Cell dateCell = row.getCell(dateColumn);
                                            if (dateCell != null) {
                                                try {
                                                    String dateSoins = null;
                                                    if (dateCell.getCellType() == CellType.NUMERIC) {
                                                        dateSoins = new SimpleDateFormat("ddMMyyyy").format(dateCell.getDateCellValue());
                                                    } else if (dateCell.getCellType() == CellType.STRING) {
                                                        dateSoins = dateCell.getStringCellValue();
                                                    }
                                                    
                                                    if (dateSoins != null && dateSoins.length() == 8) {
                                                        if (firstDateSoins == null || dateSoins.compareTo(firstDateSoins) < 0) {
                                                            firstDateSoins = dateSoins;
                                                        }
                                                        if (lastDateSoins == null || dateSoins.compareTo(lastDateSoins) > 0) {
                                                            lastDateSoins = dateSoins;
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    // Continue even if date extraction fails
                                                }
                                            }
                                        }
                                        
                                        // Simple validation check
                                        boolean isValid = validateRsfLine(cellValue, lineType);
                                        if (!isValid) {
                                            errorCount++;
                                            String errorMsg = getRsfLineErrorMessage(cellValue, lineType);
                                            errorDetails.add("Line type " + lineType + ": " + errorMsg);
                                            
                                            // Only add a few errors to the list
                                            if (errorDetails.size() > 10) {
                                                errorDetails.add("... and more errors (showing first 10 only)");
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Create a string representation of the row for the sample
                        if (sampleRecords.size() < 5) {
                            StringBuilder rowData = new StringBuilder();
                            rowData.append("Sheet: ").append(sheet.getSheetName()).append(" - Row: ").append(row.getRowNum()).append(" - ");
                            
                            for (Cell cell : row) {
                                String cellValue = "";
                                switch (cell.getCellType()) {
                                    case STRING:
                                        cellValue = cell.getStringCellValue();
                                        break;
                                    case NUMERIC:
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                        break;
                                    case BOOLEAN:
                                        cellValue = String.valueOf(cell.getBooleanCellValue());
                                        break;
                                    case FORMULA:
                                        cellValue = cell.getCellFormula();
                                        break;
                                    default:
                                        cellValue = "[EMPTY]";
                                }
                                rowData.append(" | ").append(cellValue);
                            }
                            
                            sampleRecords.add(rowData.toString());
                        }
                        
                        // Check for potential errors in the row
                        if (recordCount % 100 == 0) {
                            importProgress.put(fileId, 20 + (i * 70 / totalSheets) + (70 / totalSheets) * (int)((float)recordCount / sheet.getLastRowNum()));
                        }
                    }
                }
            }
        }
        
        Map<String, Object> rsfStats = new HashMap<>();
        if (isRsfFile) {
            rsfStats.put("totalLines", recordCount);
            rsfStats.put("lineTypeCounts", lineTypeCounts);
            rsfStats.put("errorCount", errorCount);
            rsfStats.put("firstDateSoins", firstDateSoins);
            rsfStats.put("lastDateSoins", lastDateSoins);
        }
        
        // Add RSF stats to result
        result.put("recordCount", recordCount);
        result.put("sampleRecords", sampleRecords);
        result.put("rsfStats", rsfStats);
        result.put("errorDetails", errorDetails);
        
        // Add HORAIRE specific data
        if (isHoraireFile) {
            result.put("horaireUpdates", horaireUpdates);
            result.put("totalRowsProcessed", totalRowsProcessed);
            result.put("updatedCount", updatedCount);
        }
        
        return result;
    }
    
    private Map<String, Object> processCsvFile(File csvFile, String fileId, boolean isRsfFile) throws IOException {
        List<String> sampleRecords = new ArrayList<>();
        int recordCount = 0;
        Map<Character, Integer> lineTypeCounts = new HashMap<>();
        int errorCount = 0;
        String firstDateSoins = null;
        String lastDateSoins = null;
        List<String> errorDetails = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(csvFile.toPath())) {
            // Estimate total lines for progress tracking
            long totalLines = Files.lines(csvFile.toPath()).count();
            String line;
            
            while ((line = reader.readLine()) != null) {
                recordCount++;
                
                // RSF specific processing
                if (isRsfFile && line.length() > 0) {
                    char lineType = line.charAt(0);
                    lineTypeCounts.put(lineType, lineTypeCounts.getOrDefault(lineType, 0) + 1);
                    
                    // Check for dates in B, C, or M lines
                    if ((lineType == 'B' || lineType == 'C') && line.length() >= 116) {
                        String dateSoins = extractDateSoins(line, lineType);
                        if (dateSoins != null) {
                            if (firstDateSoins == null || dateSoins.compareTo(firstDateSoins) < 0) {
                                firstDateSoins = dateSoins;
                            }
                            if (lastDateSoins == null || dateSoins.compareTo(lastDateSoins) > 0) {
                                lastDateSoins = dateSoins;
                            }
                        }
                    } else if (lineType == 'M' && line.length() >= 113) {
                        String dateSoins = extractDateSoins(line, lineType);
                        if (dateSoins != null) {
                            if (firstDateSoins == null || dateSoins.compareTo(firstDateSoins) < 0) {
                                firstDateSoins = dateSoins;
                            }
                            if (lastDateSoins == null || dateSoins.compareTo(lastDateSoins) > 0) {
                                lastDateSoins = dateSoins;
                            }
                        }
                    }
                    
                    // Validate the line and count errors
                    boolean isValidLine = validateRsfLine(line, lineType);
                    if (!isValidLine) {
                        errorCount++;
                        // Add detailed error information
                        String errorMessage = getRsfLineErrorMessage(line, lineType);
                        String linePreview = line.length() > 50 ? line.substring(0, 47) + "..." : line;
                        errorDetails.add("Line " + recordCount + " (" + lineType + "): " + errorMessage + " - " + linePreview);
                        
                        // Limit the number of detailed errors to prevent excessive memory usage
                        if (errorDetails.size() >= 100) {
                            errorDetails.add("Additional errors omitted (too many to display)");
                            break;
                        }
                    }
                }
                
                // Save sample records
                if (sampleRecords.size() < 5) {
                    sampleRecords.add("Record " + recordCount + ": " + line);
                }
                
                // Update progress periodically
                if (recordCount % 100 == 0 || recordCount == 1) {
                    int progress = 20 + (int)((float)recordCount / totalLines * 70);
                    importProgress.put(fileId, Math.min(progress, 89));
                    addValidationDetail(fileId, "Processed " + recordCount + " of ~" + totalLines + " records");
                }
            }
        }
        
        Map<String, Object> rsfStats = new HashMap<>();
        if (isRsfFile) {
            rsfStats.put("totalLines", recordCount);
            rsfStats.put("lineTypeCounts", lineTypeCounts);
            rsfStats.put("errorCount", errorCount);
            rsfStats.put("firstDateSoins", firstDateSoins);
            rsfStats.put("lastDateSoins", lastDateSoins);
        }
        
        // Use a LinkedHashMap since Map.of has a fixed size
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordCount", recordCount);
        result.put("sampleRecords", sampleRecords);
        result.put("rsfStats", rsfStats);
        result.put("errorDetails", errorDetails);
        
        return result;
    }
    
    private void addValidationDetail(String fileId, String detail) {
        ValidationResult validation = validationResults.getOrDefault(fileId, new ValidationResult());
        validation.addDetail(detail);
        validationResults.put(fileId, validation);
    }
    
    /**
     * Model class for file validation results
     */
    public static class ValidationResult {
        private boolean valid = false;
        private String fileType = "unknown";
        private final List<String> errors = new ArrayList<>();
        private final List<String> details = new ArrayList<>();
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public void addError(String error) {
            this.errors.add(error);
        }
        
        public List<String> getDetails() {
            return details;
        }
        
        public void addDetail(String detail) {
            this.details.add(detail);
        }
        
        public String getFileType() {
            return fileType;
        }
        
        public void setFileType(String fileType) {
            this.fileType = fileType;
        }
    }
    
    /**
     * Helper method to get a Spring bean by class
     */
    private <T> T getBean(Class<T> clazz) {
        try {
            return ApplicationContextProvider.getApplicationContext().getBean(clazz);
        } catch (Exception e) {
            log.error("Error retrieving bean of class {}: {}", clazz.getName(), e.getMessage());
            return null;
        }
    }
} 