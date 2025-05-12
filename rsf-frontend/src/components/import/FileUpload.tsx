// Placeholder for FileUpload component
import React, { useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { Box, Typography, Paper } from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';

interface FileUploadProps {
  onFileUpload: (file: File) => void;
  disabled?: boolean;
}

const FileUpload: React.FC<FileUploadProps> = ({ onFileUpload, disabled }) => {
  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles && acceptedFiles.length > 0) {
      // Handle only the first file if multiple are dropped
      onFileUpload(acceptedFiles[0]);
    }
  }, [onFileUpload]);

  const { getRootProps, getInputProps, isDragActive, acceptedFiles } = useDropzone({
    onDrop,
    accept: {
      'application/zip': ['.zip'],
      'application/vnd.ms-excel': ['.xls'],
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
      'text/plain': ['.txt'],
    },
    maxFiles: 1,
    disabled: disabled,
  });

  const acceptedFileItems = acceptedFiles.map(file => (
    <Box key={file.name} sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
        <InsertDriveFileIcon sx={{ mr: 1 }} />
        <Typography variant="body2">{file.name} - {(file.size / 1024).toFixed(2)} KB</Typography>
    </Box>
  ));

  return (
    <Paper 
      variant="outlined" 
      sx={{
        p: 3,
        border: `2px dashed ${isDragActive ? 'primary.main' : 'grey.500'}`,
        backgroundColor: isDragActive ? 'action.hover' : 'transparent',
        textAlign: 'center',
        cursor: disabled ? 'default' : 'pointer',
        opacity: disabled ? 0.6 : 1,
      }}
      {...getRootProps()}
    >
      <input {...getInputProps()} />
      <UploadFileIcon sx={{ fontSize: 40, color: 'grey.700' }} />
      {isDragActive ? (
        <Typography sx={{ mt: 1 }}>Drop the file here ...</Typography>
      ) : (
        <Typography sx={{ mt: 1 }}>
          Drag 'n' drop a RSF file (TXT), ZIP, XLS, or XLSX file here, or click to select file
        </Typography>
      )}
       {acceptedFileItems.length > 0 && (
           <Box sx={{ mt: 2 }}>
                {acceptedFileItems}
            </Box>
       )}
       {/* Optional: Add a button for non-drag-drop scenarios, though clicking the area works */} 
       {/* <Button variant="contained" sx={{mt: 2}} disabled={disabled}>Select File</Button> */} 
    </Paper>
  );
};

export default FileUpload; 