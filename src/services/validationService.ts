import { apiClient } from './api';
import { ValidationDashboardData, ValidationSummary, ValidationError } from '../types/validation';

/**
 * Fetch the full validation dashboard data
 * @returns ValidationDashboardData containing summary, errors and errors by file
 */
export const getValidationDashboardData = async (): Promise<ValidationDashboardData> => {
  try {
    const response = await apiClient.get<ValidationDashboardData>('/validation/dashboard');
    return response.data;
  } catch (error) {
    console.error('Error fetching validation dashboard data:', error);
    throw error;
  }
};

/**
 * Fetch just the validation summary
 * @returns ValidationSummary with key metrics
 */
export const getValidationSummary = async (): Promise<ValidationSummary> => {
  try {
    const response = await apiClient.get<ValidationSummary>('/validation/summary');
    return response.data;
  } catch (error) {
    console.error('Error fetching validation summary:', error);
    throw error;
  }
};

/**
 * Fetch validation errors for a specific file
 * @param fileId The ID of the file to get errors for
 * @returns Array of ValidationError objects
 */
export const getErrorsForFile = async (fileId: string): Promise<ValidationError[]> => {
  try {
    const response = await apiClient.get<ValidationError[]>(`/validation/errors/${fileId}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching errors for file ${fileId}:`, error);
    throw error;
  }
};

/**
 * Fetch all validation errors with optional filtering
 * @param params Filter parameters (optional)
 * @returns Array of ValidationError objects
 */
export const getAllErrors = async (params?: {
  errorType?: string;
  fileId?: string;
  fieldName?: string;
  page?: number;
  size?: number;
  sort?: string;
}): Promise<ValidationError[]> => {
  try {
    const response = await apiClient.get<ValidationError[]>('/validation/errors', { params });
    return response.data;
  } catch (error) {
    console.error('Error fetching all validation errors:', error);
    throw error;
  }
};

/**
 * Attempt to fix a validation error
 * @param errorId The ID of the error to fix
 * @returns The result of the fix operation
 */
export const fixValidationError = async (errorId: string): Promise<any> => {
  try {
    const response = await apiClient.post(`/validation/fix/${errorId}`);
    return response.data;
  } catch (error) {
    console.error(`Error fixing validation error ${errorId}:`, error);
    throw error;
  }
}; 