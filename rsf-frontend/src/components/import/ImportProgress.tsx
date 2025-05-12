// Placeholder for ImportProgress component
import React, { useEffect, useState, useRef } from 'react';
import { getImportProgress, getImportResults } from '../../services/importService';
import { Alert, Box, Card, CircularProgress, Typography, List, ListItem } from '@mui/material';
import WarningIcon from '@mui/icons-material/Warning';
import ImportResults from './ImportResults'; // Import the results component

interface ValidationData {
  valid: boolean;
  fileType: string;
  details: string[];
}

interface ProgressData {
  progress: number;
  errors: string[];
  complete: boolean;
  validation?: ValidationData;
}

interface ImportProgressProps {
  fileId: string | null;
  onComplete?: (results: any) => void;
}

const ImportProgress: React.FC<ImportProgressProps> = ({ fileId, onComplete }) => {
  const [progress, setProgress] = useState<number>(0);
  const [errors, setErrors] = useState<string[]>([]);
  const [isComplete, setIsComplete] = useState<boolean>(false);
  const [results, setResults] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [progressData, setProgressData] = useState<ProgressData | null>(null);
  const isMountedRef = useRef<boolean>(true);
  const intervalIdRef = useRef<NodeJS.Timeout | null>(null);
  const consecutiveErrorsRef = useRef<number>(0);

  useEffect(() => {
    return () => {
      // Clean up on component unmount
      isMountedRef.current = false;
      if (intervalIdRef.current) {
        clearInterval(intervalIdRef.current);
        intervalIdRef.current = null;
      }
    };
  }, []);

  useEffect(() => {
    if (!fileId) return;

    // Reset state when fileId changes
    setProgress(0);
    setErrors([]);
    setIsComplete(false);
    setResults(null);
    setError(null);
    setProgressData(null);
    consecutiveErrorsRef.current = 0;

    // Clean up any existing interval
    if (intervalIdRef.current) {
      clearInterval(intervalIdRef.current);
      intervalIdRef.current = null;
    }

    const checkProgress = async () => {
      if (!isMountedRef.current) return;
      
      try {
        const data = await getImportProgress(fileId);
        if (!isMountedRef.current) return;

        // Reset consecutive errors on successful API call
        consecutiveErrorsRef.current = 0;

        setProgressData(data);
        setProgress(data.progress);
        setErrors(data.errors || []);
        
        if (data.complete) {
          // Stop polling once complete
          if (intervalIdRef.current) {
            clearInterval(intervalIdRef.current);
            intervalIdRef.current = null;
          }

          if (!isComplete) {
            setIsComplete(true);
            
            try {
              // Fetch final results
              const resultsData = await getImportResults(fileId);
              if (isMountedRef.current) {
                setResults(resultsData);
                if (onComplete) {
                  onComplete(resultsData);
                }
              }
            } catch (err) {
              console.error('Error fetching import results:', err);
              if (isMountedRef.current) {
                setError('Failed to retrieve import results');
              }
            }
          }
        }
      } catch (err) {
        console.error('Error checking import progress:', err);
        
        if (isMountedRef.current) {
          // Count consecutive errors
          consecutiveErrorsRef.current++;
          
          // After 5 consecutive errors, show error and stop polling
          if (consecutiveErrorsRef.current >= 5) {
            setError('Failed to check import progress after multiple attempts');
            if (intervalIdRef.current) {
              clearInterval(intervalIdRef.current);
              intervalIdRef.current = null;
            }
          }
        }
      }
    };

    // Initial check
    checkProgress();

    // Set up polling if not already complete
    if (!isComplete && !intervalIdRef.current) {
      intervalIdRef.current = setInterval(checkProgress, 2000);
    }
  }, [fileId, isComplete, onComplete]);

  if (error) {
    return (
      <Alert severity="error" sx={{ mt: 3 }}>
        {error}
      </Alert>
    );
  }

  if (!fileId) {
    return null;
  }

  return (
    <Box sx={{ mt: 3 }}>
      {!isComplete ? (
        <Card sx={{ p: 3, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <Box sx={{ position: 'relative', display: 'inline-flex', mb: 2 }}>
            <CircularProgress variant="determinate" value={progress} size={80} />
            <Box
              sx={{
                top: 0,
                left: 0,
                bottom: 0,
                right: 0,
                position: 'absolute',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Typography variant="caption" component="div" color="text.secondary">
                {`${Math.round(progress)}%`}
              </Typography>
            </Box>
          </Box>
          <Typography variant="h6" align="center" gutterBottom>
            Processing Import
          </Typography>
          <Typography variant="body2" color="text.secondary" align="center">
            Please wait while your file is being processed...
          </Typography>

          {errors.length > 0 && (
            <Box sx={{ mt: 2, width: '100%' }}>
              <Typography variant="subtitle2" color="warning.main" gutterBottom>
                <WarningIcon fontSize="small" sx={{ verticalAlign: 'middle', mr: 1 }} />
                Warnings:
              </Typography>
              <List dense>
                {errors.map((error, index) => (
                  <ListItem key={index} sx={{ color: 'warning.main' }}>
                    • {error}
                  </ListItem>
                ))}
              </List>
            </Box>
          )}
          
          {/* Display validation details if available */}
          {progressData?.validation && (
            <Box sx={{ mt: 2, width: '100%', textAlign: 'left' }}>
              <Typography variant="subtitle2" gutterBottom>
                File validation:
              </Typography>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2" color="text.secondary">File type:</Typography>
                <Typography variant="body2" sx={{ fontWeight: 'medium' }}>
                  {progressData.validation.fileType.toUpperCase()}
                </Typography>
              </Box>
              
              {/* Add RSF-specific message for RSF files */}
              {fileId && progressData.validation.fileType === 'txt' && 
               results?.filename && results.filename.startsWith('RSF_') && (
                <Alert severity="info" sx={{ mt: 1, mb: 2 }}>
                  <Typography variant="body2">
                    Processing RSF file - Analyzing line types, dates, and validating format
                  </Typography>
                </Alert>
              )}
              
              {progressData.validation.details && progressData.validation.details.length > 0 && (
                <>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Processing steps:
                  </Typography>
                  <List dense sx={{ bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider', borderRadius: 1 }}>
                    {progressData.validation.details.map((detail: string, index: number) => (
                      <ListItem key={index} sx={{ py: 0.5 }}>
                        <Typography variant="body2">
                          • {detail}
                        </Typography>
                      </ListItem>
                    ))}
                  </List>
                </>
              )}
            </Box>
          )}
        </Card>
      ) : (
        // Render the ImportResults component when complete
        results ? <ImportResults results={results} /> : 
        <Alert severity="info">Loading results...</Alert> // Show loading while results are fetched
      )}
    </Box>
  );
};

export default ImportProgress; 