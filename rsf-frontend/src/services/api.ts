import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // Default 30-second timeout
});

// Add a request interceptor to include the auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Add a response interceptor to handle common errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Network errors or timeouts
    if (!error.response) {
      if (error.code === 'ECONNABORTED') {
        console.error('Request timeout:', error);
        return Promise.reject(new Error('Request timed out. Please try again later.'));
      }
      
      if (axios.isCancel(error)) {
        console.error('Request cancelled:', error);
        return Promise.reject(new Error('Request was cancelled.'));
      }
      
      console.error('Network error:', error);
      return Promise.reject(new Error('Network error. Please check your connection and try again.'));
    }
    
    // Handle 401 Unauthorized - redirect to login
    if (error.response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
      return Promise.reject(new Error('Authentication required. Please log in.'));
    }
    
    // Handle other status codes
    if (error.response.status === 404) {
      return Promise.reject(new Error('Resource not found.'));
    }
    
    if (error.response.status === 500) {
      return Promise.reject(new Error('Server error. Please try again later.'));
    }
    
    // Pass through the original error for other cases
    return Promise.reject(error);
  }
);

export default apiClient; 