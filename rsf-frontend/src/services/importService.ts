import apiClient from './api';

/**
 * Uploads a file (ZIP, XLS, XLSX) to the backend.
 * 
 * @param file The file to upload.
 * @param onUploadProgress Optional callback function to track upload progress.
 * @returns The backend response (structure depends on the API).
 */
export const uploadFile = async (file: File, onUploadProgress?: (progressEvent: any) => void) => {
  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await apiClient.post('/import/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: onUploadProgress, // Pass the progress callback to Axios
      timeout: 30000, // 30 second timeout for upload request
    });
    return response.data;
  } catch (error) {
    console.error("Error uploading file:", error);
    // Rethrow or handle error as needed
    // Example: return error.response?.data || { error: 'Upload failed' };
    throw error; 
  }
};

/**
 * Get the current progress of a file import
 * 
 * @param fileId The ID of the uploaded file
 * @returns Progress information including percent complete and any errors
 */
export const getImportProgress = async (fileId: string) => {
  try {
    const response = await apiClient.get(`/import/progress/${fileId}`, {
      timeout: 10000, // 10 second timeout for progress checks
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching import progress:", error);
    throw error;
  }
};

/**
 * Get the results of a completed import
 * 
 * @param fileId The ID of the uploaded file
 * @returns Detailed results of the import process
 */
export const getImportResults = async (fileId: string) => {
  try {
    const response = await apiClient.get(`/import/results/${fileId}`, {
      timeout: 15000, // 15 second timeout for fetching results
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching import results:", error);
    throw error;
  }
};

/**
 * Get validation information for a specific file
 * 
 * @param fileId The ID of the uploaded file
 * @returns Validation details for the file
 */
export const getFileValidation = async (fileId: string) => {
  try {
    const response = await apiClient.get(`/import/validate/${fileId}`, {
      timeout: 10000, // 10 second timeout
    });
    return response.data;
  } catch (error) {
    console.error("Error fetching validation information:", error);
    throw error;
  }
};

/**
 * Check the status of the import service
 * 
 * @returns Status information about the import service
 */
export const getImportStatus = async () => {
  try {
    const response = await apiClient.get('/import/status', {
      timeout: 5000, // 5 second timeout for status check
    });
    return response.data;
  } catch (error) {
    console.error("Error checking import service status:", error);
    throw error;
  }
};

// TODO: Add functions for checking import status if needed
// export const getImportStatus = async (fileId: string) => { ... }; 