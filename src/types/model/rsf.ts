export {}; // Make this file a module

export interface RsfFile {
  id: string;
  name: string;
  originalName: string;
  size: number;
  uploadDate: string;
  processed: boolean;
  status: FileStatus;
  extension: string;
  contentType: string;
  validationCompleted: boolean;
  errorCount?: number;
}

export type FileStatus = 
  | 'PENDING' 
  | 'UPLOADING' 
  | 'UPLOADED' 
  | 'PROCESSING' 
  | 'PROCESSED' 
  | 'ERROR' 
  | 'VALIDATING' 
  | 'VALIDATION_COMPLETE' 
  | 'VALIDATION_ERROR';

export interface RsfRecord {
  id: string;
  fileId: string;
  lineNumber: number;
  recordType: string;
  recordIdentifier?: string;
  fields: RsfField[];
}

export interface RsfField {
  id: string;
  recordId: string;
  name: string;
  value: string;
  position: number;
}

export interface FileProcessingResult {
  fileId: string;
  fileName: string;
  totalRecords: number;
  processedRecords: number;
  errorRecords: number;
  processingTimeMs: number;
  status: FileStatus;
  errors?: string[];
} 