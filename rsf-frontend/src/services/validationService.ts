import apiClient from './api';
import {
  ValidationDashboardData,
  ValidationError,
  ValidationSummary,
  ValidationErrorResponse
} from '../types/validation';

/**
 * Fetches the complete validation dashboard data including summary, stats, and latest errors
 */
export async function getValidationDashboardData(): Promise<ValidationDashboardData> {
  try {
    const response = await apiClient.get<ValidationDashboardData>('/validation/dashboard');
    return response.data;
  } catch (error) {
    console.error('Error fetching validation dashboard data:', error);
    throw error;
  }
}

/**
 * Fetches just the validation summary information
 */
export async function getValidationSummary(): Promise<ValidationSummary> {
  try {
    const response = await apiClient.get<ValidationSummary>('/validation/summary');
    return response.data;
  } catch (error) {
    console.error('Error fetching validation summary:', error);
    throw error;
  }
}

/**
 * Fetches validation errors for a specific file
 * @param fileId - The ID of the file to fetch errors for
 */
export async function getErrorsForFile(fileId: string): Promise<ValidationError[]> {
  try {
    const response = await apiClient.get<ValidationError[]>(`/validation/errors/${fileId}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching errors for file ${fileId}:`, error);
    throw error;
  }
}

/**
 * Fetch all validation errors with optional filtering
 * @param params - Optional filter parameters
 */
export async function getAllErrors(params?: {
  errorType?: string;
  fileId?: string;
  fieldName?: string;
  page?: number;
  size?: number;
  sort?: string;
}): Promise<ValidationErrorResponse> {
  try {
    const response = await apiClient.get<ValidationErrorResponse>('/validation/errors', {
      params
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching validation errors:', error);
    throw error;
  }
}

/**
 * Attempts to fix a specific validation error
 * @param errorId - ID of the error to fix
 */
export async function fixValidationError(errorId: string): Promise<any> {
  try {
    const response = await apiClient.post(`/validation/fix/${errorId}`);
    return response.data;
  } catch (error) {
    console.error(`Error fixing validation error ${errorId}:`, error);
    throw error;
  }
} 