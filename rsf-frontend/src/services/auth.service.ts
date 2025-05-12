import api from './api';
import { LoginRequest, LoginResponse, RegisterRequest } from '../types/auth';

export const AuthService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    console.log('Login request:', { username: credentials.username, hasPassword: !!credentials.password });
    try {
      const response = await api.post<LoginResponse>('/auth/login', credentials);
      console.log('Login response status:', response.status);
      return response.data;
    } catch (error: any) {
      console.error('Login error:', error);
      console.error('Login error details:', { 
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      });
      throw error;
    }
  },

  register: async (userData: RegisterRequest): Promise<void> => {
    console.log('Register request:', { username: userData.username, hasPassword: !!userData.password });
    try {
      const response = await api.post('/auth/register', userData);
      console.log('Register response status:', response.status);
      return;
    } catch (error: any) {
      console.error('Register error:', error);
      console.error('Register error details:', { 
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      });
      throw error;
    }
  },

  logout: (): void => {
    localStorage.removeItem('token');
  },

  getCurrentUser: async (): Promise<any> => {
    try {
      const response = await api.get('/auth/whoami');
      console.log('GetCurrentUser response:', response.data);
      return response.data;
    } catch (error) {
      console.error('GetCurrentUser error:', error);
      throw error;
    }
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token');
  }
};

export default AuthService; 