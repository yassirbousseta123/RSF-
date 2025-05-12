import React, { useState } from 'react';
import { Box, Typography, Container, Alert, Snackbar, Button } from '@mui/material';
import FileUpload from '../components/import/FileUpload';
import ImportProgress from '../components/import/ImportProgress';
import { uploadFile } from '../services/importService';
import { AxiosProgressEvent } from 'axios';

// Define a more specific type for file metadata
interface FileMetadata {
  id: string;
  originalName: string;
  storedName: string;
  type: string;
  status: string;
  uploadedAt: string;
  uploader: string | null;
}

const ImportPage: React.FC = () => {
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [fileId, setFileId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [uploadTimeout, setUploadTimeout] = useState<NodeJS.Timeout | null>(null);

  const resetUpload = () => {
    setUploading(false);
    setUploadProgress(0);
    setFileId(null);
    setError(null);
    if (uploadTimeout) {
      clearTimeout(uploadTimeout);
      setUploadTimeout(null);
    }
  };

  const handleUpload = async (file: File) => {
    resetUpload();
    setUploading(true);
    
    // Set a timeout to handle long-running uploads that might get stuck
    const timeout = setTimeout(() => {
      if (uploadProgress < 100) {
        setError("Upload timed out. The server might be taking too long to respond. Please try again.");
        setUploading(false);
      }
    }, 60000); // 1 minute timeout
    
    setUploadTimeout(timeout);
    
    try {
      // Handle file upload progress
      const onUploadProgressCallback = (progressEvent: AxiosProgressEvent) => {
        const total = progressEvent.total ?? file.size;
        const percentCompleted = Math.round((progressEvent.loaded * 100) / total);
        setUploadProgress(percentCompleted);
      };

      // Upload file and get metadata
      const fileMetadata = await uploadFile(file, onUploadProgressCallback) as FileMetadata;
      console.log('Upload successful:', fileMetadata);
      
      // Clear timeout since upload completed successfully
      if (uploadTimeout) {
        clearTimeout(uploadTimeout);
        setUploadTimeout(null);
      }
      
      // Store the file ID for tracking progress
      setFileId(fileMetadata.id);
      setSuccessMessage("File uploaded successfully. Processing has begun.");
      
    } catch (err: any) {
      console.error('Upload failed:', err);
      
      // Clear timeout since we've handled the error
      if (uploadTimeout) {
        clearTimeout(uploadTimeout);
        setUploadTimeout(null);
      }
      
      let errorMessage = 'Upload failed. Please check the file and try again.';
      
      // Extract error message from response if available
      if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
      } else if (err.message) {
        // Check for network error or abort
        if (err.message === 'Network Error') {
          errorMessage = 'Network error. Please check your connection and try again.';
        } else if (err.message.includes('timeout')) {
          errorMessage = 'Request timed out. The server might be busy.';
        } else {
          errorMessage = err.message;
        }
      }
      
      setError(errorMessage);
    } finally {
      setUploading(false);
    }
  };

  const handleProcessingComplete = (results: any) => {
    console.log('Processing complete:', results);
    setSuccessMessage(`Processing complete: ${results.recordsProcessed || 0} records processed.`);
  };
  
  const handleRetry = () => {
    resetUpload();
    setSuccessMessage(null);
  };

  return (
    <Container maxWidth="md">
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Import RSF/Excel File
        </Typography>
        
        <Box sx={{ mt: 3 }}>
          <FileUpload onFileUpload={handleUpload} disabled={uploading} />
        </Box>
        
        {uploading && (
          <Box sx={{ mt: 3 }}>
            <Alert severity="info">
              Uploading file: {uploadProgress}%
            </Alert>
          </Box>
        )}
        
        {error && (
          <Box sx={{ mt: 3 }}>
            <Alert 
              severity="error"
              action={
                <Button color="inherit" size="small" onClick={handleRetry}>
                  Retry
                </Button>
              }
            >
              {error}
            </Alert>
          </Box>
        )}
        
        {/* Import progress tracking and results */}
        {fileId && (
          <ImportProgress 
            fileId={fileId} 
            onComplete={handleProcessingComplete}
          />
        )}
        
        {/* Success message notification */}
        <Snackbar
          open={!!successMessage}
          autoHideDuration={6000}
          onClose={() => setSuccessMessage(null)}
          message={successMessage}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        />
      </Box>
    </Container>
  );
};

export default ImportPage; 