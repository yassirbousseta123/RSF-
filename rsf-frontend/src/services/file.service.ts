import api from './api';

export interface FileResponse {
  id: string;
  originalName: string;
  storedName: string;
  type: string;
  status: 'QUEUED' | 'PROCESSING' | 'READY' | 'ERROR';
  uploadedAt: string;
  uploader: {
    id: number;
    username: string;
  };
}

export const FileService = {
  uploadFile: async (file: File): Promise<FileResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    
    // Set the content type to multipart/form-data
    const response = await api.post<FileResponse>('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    
    return response.data;
  },
  
  downloadFile: async (fileId: string): Promise<Blob> => {
    const response = await api.get(`/files/${fileId}`, {
      responseType: 'blob',
    });
    
    return response.data;
  },
  
  getFileMetadata: async (fileId: string): Promise<FileResponse> => {
    const response = await api.get<FileResponse>(`/files/${fileId}/meta`);
    return response.data;
  },
  
  getFileStatus: async (fileId: string): Promise<{ status: string }> => {
    const response = await api.get(`/files/${fileId}/status`);
    return response.data;
  },

  isValidFileType: (file: File): boolean => {
    // List of valid file extensions
    const validExtensions = ['.zip', '.txt', '.pdf', '.doc', '.docx', '.xls', '.xlsx'];
    const fileName = file.name.toLowerCase();
    
    return validExtensions.some(ext => fileName.endsWith(ext));
  },
  
  getFileIcon: (fileType: string): string => {
    switch (fileType) {
      case 'ZIP':
        return 'folder_zip';
      case 'PDF':
        return 'picture_as_pdf';
      case 'DOC':
      case 'DOCX':
        return 'description';
      case 'XLS':
      case 'XLSX':
        return 'table_chart';
      default:
        return 'insert_drive_file';
    }
  }
};

export default FileService; 