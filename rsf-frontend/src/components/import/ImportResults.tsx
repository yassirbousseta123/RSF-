// Placeholder for ImportResults component
import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { Box, Typography, Paper, Alert, Divider, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Card, List, ListItem, ListItemIcon, ListItemText, Accordion, AccordionSummary, AccordionDetails, Button } from '@mui/material';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import InfoIcon from '@mui/icons-material/Info';
import MapIcon from '@mui/icons-material/Map';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import DateRangeIcon from '@mui/icons-material/DateRange';
import BugReportIcon from '@mui/icons-material/BugReport';
import AccessTimeIcon from '@mui/icons-material/AccessTime';

interface ImportResultsProps {
  results: any; // Replace 'any' with a specific type for backend results
}

const ImportResults: React.FC<ImportResultsProps> = ({ results }) => {
  const [selectedLineType, setSelectedLineType] = useState<string | null>(null);
  const [showDetailedMapping, setShowDetailedMapping] = useState(false);
  const [showDebugInfo, setShowDebugInfo] = useState(false);
  const [debugInfo, setDebugInfo] = useState<{[key: string]: any}>({});
  const [forceRender, setForceRender] = useState(0); // Add a counter to force re-renders
  
  // Basic display - enhance based on actual backend response structure
  const message = results?.message || 'Processing complete.';
  const severity = results?.error ? 'error' : 'success'; // Example logic
  
  // Check file type
  const isRsfFile = results?.filename?.startsWith('RSF_');
  const isHoraireFile = results?.filename?.toUpperCase().includes('HORAIRES_');
  console.log("File type detection:", { 
    filename: results?.filename, 
    isRsfFile, 
    isHoraireFile, 
    hasHoraireUpdates: results?.horaireUpdates?.length > 0
  });
  
  // RSF specific data
  const lineTypeCounts: Record<string, number> = results?.lineTypeCounts || {};
  const totalLines = results?.totalLines || 0;
  const errorCount = results?.errorCount || 0;
  const firstDateSoins = results?.firstDateSoins;
  const lastDateSoins = results?.lastDateSoins;
  
  // Get sample records to extract error information
  const processedRecords = useMemo(() => results?.processedRecords || [], [results?.processedRecords]);
  const errors = results?.errors || [];
  
  // Horaire specific data
  const horaireUpdatesRef = useRef<any[]>([]);
  const totalRowsProcessed = results?.totalRowsProcessed || 0;
  const updatedCount = results?.updatedCount || 0;
  
  // Initialize with backend data if available
  useEffect(() => {
    if (results?.horaireUpdates && results.horaireUpdates.length > 0) {
      horaireUpdatesRef.current = results.horaireUpdates;
      setForceRender(count => count + 1); // Force a re-render when backend data arrives
    }
  }, [results?.horaireUpdates]);
  
  // Fallback: Extract HORAIRE records from sample records if none are provided directly
  useEffect(() => {
    if (isHoraireFile && horaireUpdatesRef.current.length === 0 && processedRecords.length > 0) {
      // Try to extract HORAIRE records from the sample records
      const extractedRecords = [];
      
      console.log("Attempting to extract HORAIRE records from samples:", processedRecords);
      
      // Skip header row only (should contain column names)
      let headerSkipped = false;
      
      // Log the total records to process
      console.log(`Total records to process: ${processedRecords.length}`);
      
      for (const record of processedRecords) {
        console.log(`Processing record: ${record}`);
        
        // Skip only the header row (first row with column names)
        if (!headerSkipped && record.includes('num_immatriculation')) {
          headerSkipped = true;
          console.log(`Skipping header row: ${record}`);
          continue;
        }
        
        // Skip summary rows and empty rows
        if (record.includes('Total Records') || record.trim() === '') {
          console.log(`Skipping summary/empty row: ${record}`);
          continue;
        }
        
        try {
          // Extract row number and data from the format "Sheet: XXX - Row: N - | val1 | val2 | val3..."
          let rowNum = 0;
          const rowMatch = record.match(/Row: (\d+)/);
          if (rowMatch) {
            rowNum = parseInt(rowMatch[1]);
          } else {
            // Try alternative format
            const altMatch = record.match(/Row (\d+):/);
            if (altMatch) {
              rowNum = parseInt(altMatch[1]);
            }
          }
          
          console.log(`Row number extracted: ${rowNum}`);
          
          // Extract the values from the record
          const parts = record.split('|').map((p: string) => p.trim());
          
          console.log(`Parts after splitting: ${JSON.stringify(parts)}`);
          
          // Skip if we don't have enough parts
          if (parts.length < 5) {
            console.log(`Skipping record with insufficient parts (${parts.length}): ${record}`);
            continue;
          }
          
          // Find the value parts (after the initial metadata)
          let values = parts;
          
          // If there's a metadata section, remove it
          if (parts[0].includes('Row:') || parts[0].includes('Sheet:')) {
            values = parts.slice(1);
            console.log(`Removed metadata, values: ${JSON.stringify(values)}`);
          }
          
          // Try to map the values to the expected fields
          // The expected order is: num_immatriculation, date_naissance, date_soins, code_acte, horaire
          
          let numImmat = values[0] || "";
          let dateNaissance = values[1] || "";
          let dateSoins = values[2] || "";
          let codeActe = values[3] || "";
          let horaire = values[4] || "";
          
          console.log(`Before processing - numImmat: ${numImmat}, dateNaissance: ${dateNaissance}, dateSoins: ${dateSoins}, codeActe: ${codeActe}, horaire: ${horaire}`);
          
          // Attempt to clean up dates if they're in numeric format
          if (dateNaissance.includes('.') && !dateNaissance.includes('-')) {
            const dateNum = parseFloat(dateNaissance);
            const oldDateNaissance = dateNaissance;
            dateNaissance = new Date((dateNum - 25569) * 86400 * 1000).toISOString().split('T')[0].replace(/-/g, '');
            console.log(`Converted dateNaissance from ${oldDateNaissance} to ${dateNaissance}`);
          }
          
          if (dateSoins.includes('.') && !dateSoins.includes('-')) {
            const dateNum = parseFloat(dateSoins);
            const oldDateSoins = dateSoins;
            dateSoins = new Date((dateNum - 25569) * 86400 * 1000).toISOString().split('T')[0].replace(/-/g, '');
            console.log(`Converted dateSoins from ${oldDateSoins} to ${dateSoins}`);
          }
          
          // Log the values before creating the record
          console.log(`After processing - numImmat: ${numImmat}, dateNaissance: ${dateNaissance}, dateSoins: ${dateSoins}, codeActe: ${codeActe}, horaire: ${horaire}`);
          
          // Create a HORAIRE update record with original values (less strict cleaning)
          const horaireRecord: {
            sourceRowNum: number,
            numImmatriculation: string,
            dateNaissance: string,
            dateSoins: string,
            codeActe: string,
            horaire: string
          } = {
            sourceRowNum: rowNum || extractedRecords.length + 1, // Use counter if no row number found
            numImmatriculation: numImmat,
            dateNaissance: dateNaissance,
            dateSoins: dateSoins,
            codeActe: codeActe,
            horaire: horaire
          };
          
          console.log(`Adding record: ${JSON.stringify(horaireRecord)}`);
          extractedRecords.push(horaireRecord);
        } catch (e) {
          console.error("Error parsing record:", record, e);
        }
      }
      
      console.log("Extracted HORAIRE records:", extractedRecords);
      if (extractedRecords.length > 0) {
        horaireUpdatesRef.current = extractedRecords;
        // Force a re-render
        setForceRender(count => count + 1);
        setDebugInfo(prev => ({...prev}));
      }
    }
  }, [isHoraireFile, processedRecords]);

  // Helper function to format a date from JJMMAAAA (DD/MM/YYYY) to a readable format
  const formatDate = (dateString: string | null | undefined) => {
    if (!dateString || dateString.length !== 8) return 'N/A';
    return `${dateString.substring(0, 2)}/${dateString.substring(2, 4)}/${dateString.substring(4, 8)}`;
  };

  // Function to extract field value from a sample record based on position
  const extractFieldValue = (record: string, startPos: number, endPos: number): string => {
    if (!record || record.length < endPos) return 'N/A';
    
    // Direct processing for raw RSF data (no formatting/prefixes)
    const validTypes = ['A', 'B', 'C', 'H', 'M', 'P', 'L'];
    if (record.length > 0 && validTypes.includes(record[0])) {
      if (record.length >= endPos) {
        return record.substring(startPos - 1, endPos);
      }
    }
    
    // Extract the actual data part from the record for formatted records
    let dataPart = record;
    
    // Handle different record formats
    if (record.includes(' - Record: ')) {
      // Format: "File: XXX - Record: DATA"
      dataPart = record.split(' - Record: ')[1];
    } else if (record.includes('Record ')) {
      // Format: "Record N: DATA"
      const parts = record.split(': ');
      if (parts.length > 1) {
        dataPart = parts[parts.length - 1];
      }
    }
    
    // Ensure we have enough characters
    if (!dataPart || dataPart.length < endPos) return 'N/A';
    
    return dataPart.substring(startPos - 1, endPos);
  };
  
  // Find a sample record for a specific line type
  const getSampleRecordForType = useCallback((lineType: string): string | null => {
    if (!processedRecords || processedRecords.length === 0) return null;
    
    // First try to find a record that directly starts with the line type (raw RSF format)
    for (const record of processedRecords) {
      if (record.startsWith(lineType)) {
        return record;
      }
    }
    
    // If no direct match, try other record formats with prefixes
    for (const record of processedRecords) {
      // Try common record format patterns
      if ((record.includes(' - Record: ') && record.split(' - Record: ')[1]?.startsWith(lineType)) ||
          (record.includes(': ') && record.split(': ').pop()?.startsWith(lineType))) {
        return record;
      }
    }
    
    // If no exact match, check if any record contains the line type character anywhere
    for (const record of processedRecords) {
      const cleanRecord = record.replace(/Record \d+: /g, '').replace(/File: .* - Record: /g, '');
      if (cleanRecord.startsWith(lineType)) {
        return record;
      }
    }
    
    return null; // Default if no suitable record is found
  }, [processedRecords]);
  
  // Function to format a field value, with special handling for dates
  const formatFieldValue = (value: string, isDate: boolean = false): string => {
    if (value === 'N/A') return value;
    
    if (isDate && value.length === 8) {
      return `${value} (${formatDate(value)})`;
    }
    
    return value;
  };
  
  // Generate debug info whenever the component renders
  useEffect(() => {
    const debug: {[key: string]: any} = {};
    
    // Log the sample records
    debug.processedRecords = processedRecords;
    
    if (isRsfFile) {
      // Find sample records for each line type
      const lineTypes = ['A', 'B', 'C', 'H', 'M', 'P', 'L'];
      debug.samplesByType = {};
      
      lineTypes.forEach(type => {
        const sample = getSampleRecordForType(type);
        if (sample) {
          // Extract a few example fields for verification
          const typeCode = extractFieldValue(sample, 1, 1);
          const finess = extractFieldValue(sample, 2, 10);
          
          debug.samplesByType[type] = {
            record: sample,
            extractedType: typeCode,
            extractedFiness: finess
          };
        }
      });
    } else if (isHoraireFile) {
      debug.horaireUpdates = horaireUpdatesRef.current;
    }
    
    setDebugInfo(debug);
  }, [results, processedRecords, isRsfFile, isHoraireFile, getSampleRecordForType]);
  
  // RSF Line Type descriptions
  const lineTypeDescriptions: Record<string, string> = {
    'A': 'Patient Information (207 chars) - Patient and Billing Summary',
    'B': 'Hospital Stay (193 chars) - Hospital Service Details',
    'C': 'Healthcare Act (190 chars) - Consultation Details',
    'H': 'Homogeneous Patient Group (157 chars) - Medication Details',
    'M': 'Medical Procedures (166 chars) - Dental and CCAM Acts',
    'P': 'Pricing Information (157 chars) - LPP Device Details',
    'L': 'Link Information (194 chars) - Multiple Act Details'
  };
  
  // Required lengths for each line type
  const requiredLineLengths: Record<string, number> = {
    'A': 207,
    'B': 193,
    'C': 190, 
    'H': 157,
    'M': 166,
    'P': 157,
    'L': 194
  };

  // Key fields for each line type for the detailed mapping display
  const getKeyFields = (lineType: string): { field: string, position: string, description: string, value: string, isDate: boolean }[] => {
    const sampleRecord = getSampleRecordForType(lineType);
    
    const makeField = (field: string, position: string, description: string, isDate: boolean = false) => {
      const [start, end] = position.split('-').map(pos => parseInt(pos, 10));
      const value = sampleRecord ? extractFieldValue(sampleRecord, start, end) : 'N/A';
      return { field, position, description, value, isDate };
    };
    
    switch(lineType) {
      case 'A':
        return [
          makeField('TYPE_ENREGISTREMENT', '1-1', 'Record type, always "A"'),
          makeField('N_FINESS_EPMSI', '2-10', 'FINESS registration number'),
          makeField('SEXE', '20-20', 'Patient sex'),
          makeField('DATE_NAISSANCE', '114-121', 'Birth date (JJMMAAAA)', true),
          makeField('DATE_ENTREE', '123-130', 'Entry date (JJMMAAAA)', true),
          makeField('DATE_SORTIE', '131-138', 'Exit date (JJMMAAAA)', true)
        ];
      case 'B':
        return [
          makeField('TYPE_ENREGISTREMENT', '1-1', 'Record type, always "B"'),
          makeField('N_FINESS_EPMSI', '2-10', 'FINESS registration number'),
          makeField('MODE_TRAITEMENT', '100-101', 'Treatment mode'),
          makeField('DATE_SOINS', '108-115', 'Care date (JJMMAAAA)', true),
          makeField('CODE_ACTE', '116-120', 'Act code (4+1 format)'),
          makeField('QUANTITE', '121-123', 'Quantity (with leading zeros)')
        ];
      case 'C':
        return [
          makeField('TYPE_ENREGISTREMENT', '1-1', 'Record type, always "C"'),
          makeField('N_FINESS_EPMSI', '2-10', 'FINESS registration number'),
          makeField('MODE_TRAITEMENT', '100-101', 'Treatment mode'),
          makeField('DATE_SOINS', '108-115', 'Care date (JJMMAAAA)', true),
          makeField('CODE_ACTE', '116-120', 'Act code')
        ];
      case 'H':
        return [
          makeField('TYPE_ENREGISTREMENT', '1-1', 'Record type, always "H"'),
          makeField('N_FINESS_EPMSI', '2-10', 'FINESS registration number'),
          makeField('DATE_DEBUT_SEJOUR', '100-107', 'Stay start date (JJMMAAAA)', true),
          makeField('CODE_UCD', '108-114', 'UCD code'),
          makeField('QUANTITE', '141-143', 'Quantity')
        ];
      case 'M':
        return [
          makeField('TYPE_ENREGISTREMENT', '1-1', 'Record type, always "M"'),
          makeField('N_FINESS_EPMSI', '2-10', 'FINESS registration number'),
          makeField('MODE_TRAITEMENT', '100-101', 'Treatment mode'),
          makeField('DATE_SOINS', '105-112', 'Act date (JJMMAAAA)', true),
          makeField('CODE_CCAM', '113-125', 'CCAM code')
        ];
      case 'P':
        return [
          makeField('TYPE_ENREGISTREMENT', '1-1', 'Record type, always "P"'),
          makeField('N_FINESS_EPMSI', '2-10', 'FINESS registration number'),
          makeField('DATE_DEBUT_SEJOUR', '100-107', 'Stay start date (JJMMAAAA)', true),
          makeField('CODE_REFERENCE_LPP', '108-120', 'LPP reference code'),
          makeField('QUANTITE', '121-122', 'Quantity')
        ];
      case 'L':
        return [
          makeField('TYPE_ENREGISTREMENT', '1-1', 'Record type, always "L"'),
          makeField('N_FINESS_EPMSI', '2-10', 'FINESS registration number'),
          makeField('MODE_TRAITEMENT', '100-101', 'Treatment mode'),
          makeField('DATE_ACTE1', '105-112', 'Date of act 1 (JJMMAAAA)', true),
          makeField('CODE_ACTE1', '115-122', 'Code of act 1'),
          makeField('DATE_ACTE2', '123-130', 'Date of act 2 (JJMMAAAA)', true),
          makeField('CODE_ACTE2', '133-140', 'Code of act 2')
        ];
      default:
        return [];
    }
  };

  // Render the debugging information section
  const renderDebugInfo = () => {
    return (
      <Box sx={{ mt: 3, mb: 3 }}>
        <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
          <BugReportIcon sx={{ mr: 1 }} /> 
          Debug Information
        </Typography>
        
        <Paper variant="outlined" sx={{ p: 2 }}>
          <Typography variant="subtitle1" gutterBottom>Sample Records</Typography>
          <Box sx={{ maxHeight: '300px', overflow: 'auto', mb: 2 }}>
            <pre style={{ whiteSpace: 'pre-wrap', fontSize: '0.8rem' }}>
              {JSON.stringify(processedRecords, null, 2)}
            </pre>
          </Box>
          
          {isRsfFile && (
            <>
              <Typography variant="subtitle1" gutterBottom>Record Extraction Test</Typography>
              <TableContainer sx={{ mb: 2 }}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Line Type</TableCell>
                      <TableCell>Found Record</TableCell>
                      <TableCell>Extracted Type</TableCell>
                      <TableCell>Extracted FINESS</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {Object.entries(debugInfo.samplesByType || {}).map(([type, data]: [string, any]) => (
                      <TableRow key={type}>
                        <TableCell>{type}</TableCell>
                        <TableCell>
                          <Typography variant="body2" sx={{ 
                            fontFamily: 'monospace', 
                            fontSize: '0.7rem',
                            maxWidth: '300px',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap'
                          }}>
                            {data.record}
                          </Typography>
                        </TableCell>
                        <TableCell>{data.extractedType}</TableCell>
                        <TableCell>{data.extractedFiness}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </>
          )}
          
          {isHoraireFile && (
            <>
              <Typography variant="subtitle1" gutterBottom>HORAIRE Updates</Typography>
              <Box sx={{ maxHeight: '300px', overflow: 'auto', mb: 2 }}>
                <pre style={{ whiteSpace: 'pre-wrap', fontSize: '0.8rem' }}>
                  {JSON.stringify(debugInfo.horaireUpdates, null, 2)}
                </pre>
              </Box>
            </>
          )}
        </Paper>
      </Box>
    );
  };

  const renderDetailedMapping = () => {
    return (
      <Box sx={{ mt: 3, mb: 3 }}>
        <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
          <InfoIcon sx={{ mr: 1 }} /> 
          Detailed RSF Mapping Reference
        </Typography>

        <Paper variant="outlined" sx={{ p: 2 }}>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Click on a line type to view key field positions, descriptions, and actual values according to the 2017 mapping guidelines.
            {processedRecords.length === 0 && (
              <Alert severity="info" sx={{ mt: 1 }}>
                No sample records available to extract field values
              </Alert>
            )}
          </Typography>
          
          {Object.keys(lineTypeDescriptions).map((type) => (
            <Accordion 
              key={type} 
              expanded={selectedLineType === type}
              onChange={() => setSelectedLineType(selectedLineType === type ? null : type)}
              sx={{ mb: 1 }}
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography>
                  <strong>Type {type}:</strong> {lineTypeDescriptions[type]}
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow sx={{ bgcolor: 'primary.light' }}>
                        <TableCell>Field</TableCell>
                        <TableCell>Position</TableCell>
                        <TableCell>Description</TableCell>
                        <TableCell>Actual Value</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {getKeyFields(type).map((field, index) => (
                        <TableRow key={index} hover>
                          <TableCell><code>{field.field}</code></TableCell>
                          <TableCell>{field.position}</TableCell>
                          <TableCell>{field.description}</TableCell>
                          <TableCell>
                            {field.isDate ? (
                              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                <DateRangeIcon fontSize="small" sx={{ mr: 0.5, color: 'primary.main' }} />
                                {formatFieldValue(field.value, true)}
                              </Box>
                            ) : (
                              <code>{field.value}</code>
                            )}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </AccordionDetails>
            </Accordion>
          ))}
        </Paper>
      </Box>
    );
  };
  
  // Render HORAIRE data
  const renderHoraireData = () => {
    console.log("Rendering HORAIRE data:", { isHoraireFile, horaireUpdates: horaireUpdatesRef.current, totalRowsProcessed, renderCount: forceRender });
    
    if (!isHoraireFile) return null;
    
    // If we have no records but processedRecords has data, show a message
    if (horaireUpdatesRef.current.length === 0) {
      return (
        <Alert severity="info" sx={{ mb: 2 }}>
          <InfoIcon sx={{ mr: 1 }} />
          No HORAIRE records available. The file may be empty or could not be processed correctly.
        </Alert>
      );
    }
    
    // Calculate success rate
    const successRate = totalRowsProcessed > 0 
      ? ((horaireUpdatesRef.current.length / totalRowsProcessed) * 100).toFixed(1) 
      : 'N/A';
    
    // Format dates in readable format
    const formatDate = (dateStr: string) => {
      if (!dateStr || dateStr === 'N/A') return dateStr;
      
      // Try different formats
      if (dateStr.length === 8) {
        // Format YYYYMMDD
        try {
          const year = dateStr.substring(0, 4);
          const month = dateStr.substring(4, 6);
          const day = dateStr.substring(6, 8);
          return `${day}/${month}/${year}`;
        } catch (e) {
          return dateStr;
        }
      } else if (dateStr.length === 10 && dateStr.includes('-')) {
        // Format YYYY-MM-DD
        try {
          const [year, month, day] = dateStr.split('-');
          return `${day}/${month}/${year}`;
        } catch (e) {
          return dateStr;
        }
      }
      
      return dateStr;
    };
    
    return (
      <Box>
        <Typography variant="h6" component="h2" gutterBottom>
          <AccessTimeIcon sx={{ mr: 1 }} /> HORAIRE Records
        </Typography>
        
        {/* Stats Dashboard */}
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, mb: 2 }}>
          <Box sx={{ flex: '1 1 calc(33% - 16px)', minWidth: '150px' }}>
            <Paper elevation={1} sx={{ p: 2 }}>
              <Typography variant="subtitle2" color="text.secondary">Total Rows</Typography>
              <Typography variant="h5">{totalRowsProcessed || horaireUpdatesRef.current.length}</Typography>
            </Paper>
          </Box>
          <Box sx={{ flex: '1 1 calc(33% - 16px)', minWidth: '150px' }}>
            <Paper elevation={1} sx={{ p: 2 }}>
              <Typography variant="subtitle2" color="text.secondary">Records Updated</Typography>
              <Typography variant="h5">{updatedCount || horaireUpdatesRef.current.length}</Typography>
            </Paper>
          </Box>
          <Box sx={{ flex: '1 1 calc(33% - 16px)', minWidth: '150px' }}>
            <Paper elevation={1} sx={{ p: 2 }}>
              <Typography variant="subtitle2" color="text.secondary">Success Rate</Typography>
              <Typography variant="h5">{successRate}%</Typography>
            </Paper>
          </Box>
        </Box>
        
        {/* HORAIRE Updates Table */}
        <TableContainer component={Paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Row</TableCell>
                <TableCell>Num. Immatriculation</TableCell>
                <TableCell>Date Naissance</TableCell>
                <TableCell>Date Soins</TableCell>
                <TableCell>Code Acte</TableCell>
                <TableCell>Horaire</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {horaireUpdatesRef.current.map((update: any, index: number) => (
                <TableRow key={index}>
                  <TableCell>{update.sourceRowNum}</TableCell>
                  <TableCell>{update.numImmatriculation}</TableCell>
                  <TableCell>{formatDate(update.dateNaissance)}</TableCell>
                  <TableCell>{formatDate(update.dateSoins)}</TableCell>
                  <TableCell>{update.codeActe}</TableCell>
                  <TableCell>{update.horaire}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Box>
    );
  };

  return (
    <Paper variant="outlined" sx={{ p: 2, mt: 2 }}>
      <Alert severity={severity}>
        <Typography variant="body1">{message}</Typography>
      </Alert>
      
      {isRsfFile && (
        <Box sx={{ mt: 3 }}>
          <Typography variant="h6" gutterBottom>
            RSF File Analysis
          </Typography>
          
          <Box sx={{ 
            display: 'flex', 
            flexWrap: 'wrap', 
            gap: 2, 
            mb: 2 
          }}>
            <Box sx={{ 
              flex: { xs: '1 1 100%', sm: '1 1 calc(50% - 16px)', md: '1 1 calc(25% - 16px)' },
              p: 1, 
              border: '1px solid', 
              borderColor: 'divider', 
              borderRadius: 1 
            }}>
              <Typography variant="subtitle2" color="text.secondary">Total Lines</Typography>
              <Typography variant="h5" fontWeight="bold">{totalLines}</Typography>
            </Box>
            
            <Box sx={{ 
              flex: { xs: '1 1 100%', sm: '1 1 calc(50% - 16px)', md: '1 1 calc(25% - 16px)' },
              p: 1, 
              border: '1px solid', 
              borderColor: 'divider', 
              borderRadius: 1,
              bgcolor: errorCount > 0 ? 'error.light' : 'inherit'
            }}>
              <Typography variant="subtitle2" color={errorCount > 0 ? 'white' : 'text.secondary'}>Error Lines</Typography>
              <Typography variant="h5" fontWeight="bold" color={errorCount > 0 ? 'white' : 'inherit'}>
                {errorCount}
              </Typography>
            </Box>
            
            <Box sx={{ 
              flex: { xs: '1 1 100%', sm: '1 1 calc(100% - 16px)', md: '1 1 calc(50% - 16px)' },
              p: 1, 
              border: '1px solid', 
              borderColor: 'divider', 
              borderRadius: 1 
            }}>
              <Typography variant="subtitle2" color="text.secondary">Care Period</Typography>
              <Typography variant="body1">
                <strong>From:</strong> {formatDate(firstDateSoins)} <strong>To:</strong> {formatDate(lastDateSoins)}
              </Typography>
            </Box>
          </Box>
          
          <Divider sx={{ my: 2 }} />
          
          {/* RSF Mapping Section */}
          <Box sx={{ mb: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center' }}>
                <MapIcon sx={{ mr: 1 }} /> 
                RSF Mapping
              </Typography>
              
              <Box>
                <Button 
                  size="small" 
                  startIcon={<HelpOutlineIcon />} 
                  onClick={() => setShowDetailedMapping(!showDetailedMapping)}
                  sx={{ mr: 1 }}
                >
                  {showDetailedMapping ? 'Hide Detailed Mapping' : 'Show Detailed Mapping'}
                </Button>
                
                <Button 
                  size="small" 
                  startIcon={<BugReportIcon />} 
                  onClick={() => setShowDebugInfo(!showDebugInfo)}
                  color="secondary"
                >
                  {showDebugInfo ? 'Hide Debug Info' : 'Show Debug Info'}
                </Button>
              </Box>
            </Box>
            
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ bgcolor: 'primary.main' }}>
                    <TableCell sx={{ color: 'white' }}>Line Type</TableCell>
                    <TableCell sx={{ color: 'white' }}>Description</TableCell>
                    <TableCell sx={{ color: 'white' }}>Count</TableCell>
                    <TableCell sx={{ color: 'white' }}>Required Length</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {Object.entries(lineTypeCounts).map(([type, count]) => (
                    <TableRow key={type} hover>
                      <TableCell><strong>{type}</strong></TableCell>
                      <TableCell>{lineTypeDescriptions[type] || 'Unknown Type'}</TableCell>
                      <TableCell>{count}</TableCell>
                      <TableCell>{requiredLineLengths[type] || 'N/A'}</TableCell>
                    </TableRow>
                  ))}
                  {Object.keys(lineTypeCounts).length === 0 && (
                    <TableRow>
                      <TableCell colSpan={4} align="center">No line type data available</TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
          
          {/* Debug Info */}
          {showDebugInfo && renderDebugInfo()}
          
          {/* Detailed Mapping Info */}
          {showDetailedMapping && renderDetailedMapping()}
          
          {/* Error Display Section */}
          {errorCount > 0 && (
            <Box sx={{ mb: 3 }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', color: 'error.main', mb: 1 }}>
                <ErrorOutlineIcon sx={{ mr: 1 }} /> 
                Errors Detected
              </Typography>
              
              <Card variant="outlined" sx={{ bgcolor: 'error.light', color: 'white' }}>
                <Box sx={{ p: 2 }}>
                  <Typography variant="body1" sx={{ mb: 1 }}>
                    The file contains {errorCount} lines with validation errors:
                  </Typography>
                  
                  <List dense>
                    {/* Common RSF errors based on line type validation */}
                    <ListItem>
                      <ListItemIcon>
                        <ErrorOutlineIcon sx={{ color: 'white' }} />
                      </ListItemIcon>
                      <ListItemText 
                        primary="Line Format Errors" 
                        secondary={
                          <span style={{ color: 'rgba(255, 255, 255, 0.7)' }}>
                            Some lines do not meet required format specifications according to the 2017 mapping
                          </span>
                        }
                      />
                    </ListItem>
                    
                    {/* Display other errors if available */}
                    {errors.map((error: string, index: number) => (
                      <ListItem key={index}>
                        <ListItemIcon>
                          <ErrorOutlineIcon sx={{ color: 'white' }} />
                        </ListItemIcon>
                        <ListItemText 
                          primary={`Error ${index + 1}`}
                          secondary={
                            <span style={{ color: 'rgba(255, 255, 255, 0.7)' }}>{error}</span>
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              </Card>
            </Box>
          )}
          
          {/* Sample Records Section */}
          {processedRecords.length > 0 && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <InfoIcon sx={{ mr: 1 }} /> 
                Sample Records
              </Typography>
              
              <Card variant="outlined">
                <List dense>
                  {processedRecords.map((record: string, index: number) => (
                    <ListItem key={index} divider={index < processedRecords.length - 1}>
                      <ListItemText 
                        primary={`Record ${index + 1}`}
                        secondary={record}
                        secondaryTypographyProps={{ 
                          component: 'div',
                          sx: { 
                            fontFamily: 'monospace', 
                            fontSize: '0.8rem',
                            overflowX: 'auto',
                            whiteSpace: 'nowrap'
                          } 
                        }}
                      />
                    </ListItem>
                  ))}
                </List>
              </Card>
            </Box>
          )}
        </Box>
      )}
      
      {/* HORAIRE File Analysis */}
      {isHoraireFile && (
        <Box sx={{ mt: 3 }}>
          <Typography variant="h6" gutterBottom>
            HORAIRE File Analysis
          </Typography>
          
          <Box>
            <Button 
              size="small" 
              startIcon={<BugReportIcon />} 
              onClick={() => setShowDebugInfo(!showDebugInfo)}
              color="secondary"
              sx={{ mb: 2 }}
            >
              {showDebugInfo ? 'Hide Debug Info' : 'Show Debug Info'}
            </Button>
          </Box>
          
          {/* HORAIRE Data Display */}
          {renderHoraireData()}
          
          {/* Debug Info */}
          {showDebugInfo && renderDebugInfo()}
          
          {/* Error Display Section */}
          {errors.length > 0 && (
            <Box sx={{ mb: 3, mt: 3 }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', color: 'error.main', mb: 1 }}>
                <ErrorOutlineIcon sx={{ mr: 1 }} /> 
                Errors Detected
              </Typography>
              
              <Card variant="outlined" sx={{ bgcolor: 'error.light', color: 'white' }}>
                <Box sx={{ p: 2 }}>
                  <Typography variant="body1" sx={{ mb: 1 }}>
                    The file contains {errors.length} validation errors:
                  </Typography>
                  
                  <List dense>
                    {errors.map((error: string, index: number) => (
                      <ListItem key={index}>
                        <ListItemIcon>
                          <ErrorOutlineIcon sx={{ color: 'white' }} />
                        </ListItemIcon>
                        <ListItemText 
                          primary={`Error ${index + 1}`}
                          secondary={
                            <span style={{ color: 'rgba(255, 255, 255, 0.7)' }}>{error}</span>
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              </Card>
            </Box>
          )}
          
          {/* Sample Records Section */}
          {processedRecords.length > 0 && (
            <Box sx={{ mb: 2, mt: 3 }}>
              <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <InfoIcon sx={{ mr: 1 }} /> 
                Sample Records
              </Typography>
              
              <Card variant="outlined">
                <List dense>
                  {processedRecords.map((record: string, index: number) => (
                    <ListItem key={index} divider={index < processedRecords.length - 1}>
                      <ListItemText 
                        primary={`Record ${index + 1}`}
                        secondary={record}
                        secondaryTypographyProps={{ 
                          component: 'div',
                          sx: { 
                            fontFamily: 'monospace', 
                            fontSize: '0.8rem',
                            overflowX: 'auto',
                            whiteSpace: 'nowrap'
                          } 
                        }}
                      />
                    </ListItem>
                  ))}
                </List>
              </Card>
            </Box>
          )}
        </Box>
      )}
    </Paper>
  );
};

export default ImportResults; 