// Define error types
export type ValidationErrorType = 
  | 'DATA_ERROR' 
  | 'STRUCTURAL' 
  | 'SEQUENCE_ERROR' 
  | 'REFERENCE_ERROR' 
  | 'CONSISTENCY_ERROR' 
  | 'FORMAT_ERROR' 
  | 'OTHER';

// Define individual validation error
export interface ValidationError {
  id: string;
  fileId: string;
  fileName: string;
  lineNumber: number;
  fieldName: string;
  errorType: ValidationErrorType;
  message: string;
  recordIdentifier?: string;
}

// Define validation summary
export interface ValidationSummary {
  totalFiles: number;
  filesWithErrors: number;
  processingTimeMs: number;
}

// Define validation stats for charts
export interface ValidationStat {
  name: string;
  count: number;
}

// Define errors by file
export interface ErrorsByFile {
  [fileId: string]: ValidationError[];
}

// Define main validation dashboard data structure
export interface ValidationDashboardData {
  summary: ValidationSummary;
  errors: ValidationError[];
  errorsByFile: ErrorsByFile;
} 