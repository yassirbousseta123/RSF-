import React, { useState } from 'react';
import { 
  Box, 
  Typography, 
  Paper, 
  Button, 
  LinearProgress, 
  List, 
  ListItem,
  ListItemText, 
  ListItemIcon,
  IconButton,
  Chip,
  Divider,
  Alert,
  CircularProgress
} from '@mui/material';
import { 
  CloudUpload as CloudUploadIcon,
  InsertDriveFile as FileIcon,
  Delete as DeleteIcon
} from '@mui/icons-material';

interface UploadedFile {
  id: string;
  name: string;
  size: number;
  type: string;
  uploadedAt: Date;
}

const FileUpload: React.FC = () => {
  const [files, setFiles] = useState<File[]>([]);
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([]);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  
  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      const newFiles = Array.from(event.target.files);
      setFiles((prevFiles) => [...prevFiles, ...newFiles]);
    }
  };
  
  const handleRemoveFile = (index: number) => {
    setFiles((prevFiles) => prevFiles.filter((_, i) => i !== index));
  };
  
  const handleUpload = async () => {
    if (files.length === 0) {
      setError('Please select files to upload');
      return;
    }
    
    setUploading(true);
    setUploadProgress(0);
    setError(null);
    
    // Simulate file upload
    let progress = 0;
    const interval = setInterval(() => {
      progress += 10;
      setUploadProgress(progress);
      
      if (progress >= 100) {
        clearInterval(interval);
        
        // Create mock uploaded files
        const newUploadedFiles: UploadedFile[] = files.map((file) => ({
          id: Math.random().toString(36).substring(2, 9),
          name: file.name,
          size: file.size,
          type: file.type,
          uploadedAt: new Date(),
        }));
        
        setUploadedFiles((prev) => [...prev, ...newUploadedFiles]);
        setFiles([]);
        setUploading(false);
      }
    }, 500);
    
    // In a real application, you would use something like:
    /*
    try {
      const formData = new FormData();
      files.forEach((file) => {
        formData.append('files', file);
      });
      
      const response = await axios.post('/api/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round(
            (progressEvent.loaded * 100) / (progressEvent.total || 1)
          );
          setUploadProgress(percentCompleted);
        },
      });
      
      setUploadedFiles((prev) => [...prev, ...response.data]);
      setFiles([]);
    } catch (err: any) {
      setError(err.message || 'Failed to upload files');
    } finally {
      setUploading(false);
    }
    */
  };
  
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + ' ' + sizes[i];
  };
  
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        File Upload
      </Typography>
      
      <Paper elevation={2} sx={{ p: 3, mb: 4 }}>
        <Box
          sx={{
            border: '2px dashed',
            borderColor: 'grey.300',
            borderRadius: 2,
            p: 5,
            textAlign: 'center',
            bgcolor: 'background.default',
            mb: 3,
          }}
        >
          <input
            type="file"
            multiple
            onChange={handleFileChange}
            style={{ display: 'none' }}
            id="file-upload"
            disabled={uploading}
          />
          <label htmlFor="file-upload">
            <Button
              component="span"
              variant="contained"
              startIcon={<CloudUploadIcon />}
              disabled={uploading}
              sx={{ mb: 2 }}
            >
              Select Files
            </Button>
          </label>
          
          <Typography variant="body1" sx={{ mt: 1 }}>
            or drop files here
          </Typography>
        </Box>
        
        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}
        
        {files.length > 0 && (
          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle1" gutterBottom>
              Selected Files ({files.length})
            </Typography>
            
            <List>
              {files.map((file, index) => (
                <ListItem
                  key={index}
                  secondaryAction={
                    <IconButton
                      edge="end"
                      onClick={() => handleRemoveFile(index)}
                      disabled={uploading}
                    >
                      <DeleteIcon />
                    </IconButton>
                  }
                >
                  <ListItemIcon>
                    <FileIcon />
                  </ListItemIcon>
                  <ListItemText
                    primary={file.name}
                    secondary={formatFileSize(file.size)}
                  />
                </ListItem>
              ))}
            </List>
            
            <Box sx={{ mt: 2 }}>
              {uploading ? (
                <Box sx={{ width: '100%' }}>
                  <LinearProgress
                    variant="determinate"
                    value={uploadProgress}
                    sx={{ height: 10, borderRadius: 5 }}
                  />
                  <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                    <CircularProgress size={20} sx={{ mr: 1 }} />
                    <Typography variant="body2">
                      Uploading... {uploadProgress}%
                    </Typography>
                  </Box>
                </Box>
              ) : (
                <Button
                  variant="contained"
                  color="primary"
                  onClick={handleUpload}
                  startIcon={<CloudUploadIcon />}
                >
                  Upload Files
                </Button>
              )}
            </Box>
          </Box>
        )}
      </Paper>
      
      <Paper elevation={2} sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Uploaded Files
        </Typography>
        <Divider sx={{ mb: 2 }} />
        
        {uploadedFiles.length === 0 ? (
          <Typography variant="body1" color="text.secondary" sx={{ py: 2 }}>
            No files uploaded yet
          </Typography>
        ) : (
          <List>
            {uploadedFiles.map((file) => (
              <ListItem
                key={file.id}
                secondaryAction={
                  <Chip
                    size="small"
                    label={formatFileSize(file.size)}
                    color="primary"
                    variant="outlined"
                  />
                }
              >
                <ListItemIcon>
                  <FileIcon />
                </ListItemIcon>
                <ListItemText
                  primary={file.name}
                  secondary={`Uploaded on ${file.uploadedAt.toLocaleDateString()}`}
                />
              </ListItem>
            ))}
          </List>
        )}
      </Paper>
    </Box>
  );
};

export default FileUpload; 