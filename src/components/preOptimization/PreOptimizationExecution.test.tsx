import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { BrowserRouter } from 'react-router-dom';
import PreOptimizationExecution from './PreOptimizationExecution';
import * as preOptimizationService from '../../services/preOptimizationService';

// Mock the service
jest.mock('../../services/preOptimizationService');

// Mock navigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('PreOptimizationExecution Component', () => {
  const mockPreOptimizations = [
    {
      id: '1',
      name: 'Test Optimization 1',
      description: 'This is a test optimization',
      status: 'COMPLETED',
      lastExecuted: '2023-09-15T10:30:00',
      createdAt: '2023-09-14T08:00:00'
    },
    {
      id: '2',
      name: 'Test Optimization 2',
      description: 'Another test optimization',
      status: 'PENDING',
      createdAt: '2023-09-14T09:00:00'
    }
  ];

  beforeEach(() => {
    // Mock localStorage
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: jest.fn(() => 'fake-token'),
        setItem: jest.fn(),
        removeItem: jest.fn()
      },
      writable: true
    });

    // Reset mocks
    jest.clearAllMocks();
    
    // Mock service implementation
    (preOptimizationService.getAllPreOptimizations as jest.Mock).mockResolvedValue(mockPreOptimizations);
    (preOptimizationService.executePreOptimization as jest.Mock).mockResolvedValue({ status: 'success' });
  });

  test('renders loading state initially', () => {
    render(
      <BrowserRouter>
        <PreOptimizationExecution />
      </BrowserRouter>
    );
    
    expect(screen.getByRole('status')).toBeInTheDocument();
  });

  test('renders pre-optimizations after loading', async () => {
    render(
      <BrowserRouter>
        <PreOptimizationExecution />
      </BrowserRouter>
    );
    
    // Wait for the loading state to disappear
    await waitFor(() => {
      expect(preOptimizationService.getAllPreOptimizations).toHaveBeenCalledTimes(1);
    });
    
    // Check if pre-optimizations are displayed
    expect(screen.getByText('Test Optimization 1')).toBeInTheDocument();
    expect(screen.getByText('This is a test optimization')).toBeInTheDocument();
    expect(screen.getByText('Test Optimization 2')).toBeInTheDocument();
  });

  test('executes a pre-optimization when button is clicked', async () => {
    render(
      <BrowserRouter>
        <PreOptimizationExecution />
      </BrowserRouter>
    );
    
    // Wait for the component to load
    await waitFor(() => {
      expect(screen.getByText('Test Optimization 2')).toBeInTheDocument();
    });
    
    // Find the execute button for the second pre-optimization
    const executeButtons = screen.getAllByText('Execute');
    fireEvent.click(executeButtons[1]); // Second button for "Test Optimization 2"
    
    // Check if the execution was called with the correct ID
    expect(preOptimizationService.executePreOptimization).toHaveBeenCalledWith('2');
    
    // Check that the service was called to refresh the list
    await waitFor(() => {
      expect(preOptimizationService.getAllPreOptimizations).toHaveBeenCalledTimes(2);
    });
  });

  test('redirects to login if no token is found', () => {
    // Mock localStorage to return null for token
    (window.localStorage.getItem as jest.Mock).mockReturnValueOnce(null);
    
    render(
      <BrowserRouter>
        <PreOptimizationExecution />
      </BrowserRouter>
    );
    
    // Check if navigate was called with the login route
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });
}); 