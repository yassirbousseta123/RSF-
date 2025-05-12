// Types for validation data

export type ValidationErrorType = 
  'DATA_ERROR' | 
  'STRUCTURAL' | 
  'SEQUENCE_ERROR' | 
  'DEPENDENCY_ERROR' | 
  'FILE_NAME_ERROR' | 
  'STRUCTURAL_ERROR' | 
  'FORMAT_ERROR' | 
  'SYSTEM_ERROR';

export interface ValidationError {
  id: string;
  fileId: string;
  fileName: string;
  lineNumber: number;
  fieldName: string;
  errorType: ValidationErrorType;
  message: string;
  recordIdentifier?: string; // Optional identifier for the record (e.g., student ID)
  timestamp: string;
}

export interface ValidationSummary {
  totalFiles: number;
  filesWithErrors: number;
  totalErrors: number;
  processingTime: number; // in milliseconds
  lastUpdated: string;
}

export interface ValidationStat {
  name: string;
  value: number;
  description?: string;
}

export interface ErrorsByFile {
  [fileId: string]: ValidationError[];
}

export interface ValidationDashboardData {
  summary: ValidationSummary;
  stats: ValidationStat[];
  errorsByFile: ErrorsByFile;
  errors: ValidationError[];
}

export interface ValidationErrorResponse {
  errors: ValidationError[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
} 