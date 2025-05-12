import apiClient from '../services/api';

// Type definitions
export interface PreOptimization {
  id: string;
  name: string;
  description: string;
  status: string;
  lastExecuted?: string;
  createdAt: string;
}

/**
 * Fetches all pre-optimizations available to the user
 */
export const getAllPreOptimizations = async (): Promise<PreOptimization[]> => {
  try {
    const response = await apiClient.get<PreOptimization[]>('/api/pre-optimizations');
    return response.data;
  } catch (error) {
    console.error('Error fetching pre-optimizations:', error);
    throw error;
  }
};

/**
 * Get details of a specific pre-optimization
 * @param id - Pre-optimization ID
 */
export const getPreOptimization = async (id: string): Promise<PreOptimization> => {
  try {
    const response = await apiClient.get<PreOptimization>(`/api/pre-optimizations/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching pre-optimization ${id}:`, error);
    throw error;
  }
};

/**
 * Execute a pre-optimization process
 * @param id - Pre-optimization ID to execute
 */
export const executePreOptimization = async (id: string): Promise<any> => {
  try {
    const response = await apiClient.post(`/api/pre-optimizations/${id}/execute`);
    return response.data;
  } catch (error) {
    console.error(`Error executing pre-optimization ${id}:`, error);
    throw error;
  }
};

/**
 * Cancel an executing pre-optimization if possible
 * @param id - Pre-optimization ID to cancel
 */
export const cancelPreOptimization = async (id: string): Promise<any> => {
  try {
    const response = await apiClient.post(`/api/pre-optimizations/${id}/cancel`);
    return response.data;
  } catch (error) {
    console.error(`Error canceling pre-optimization ${id}:`, error);
    throw error;
  }
}; 