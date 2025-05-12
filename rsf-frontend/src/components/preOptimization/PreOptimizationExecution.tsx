import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
// Keep import but we're not using it with mock data
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import axios from 'axios';
import { CheckCircleIcon, ExclamationCircleIcon } from '@heroicons/react/24/solid'; // Assuming Heroicons

// Define types
interface PreOptimization {
  id: string;
  name: string;
  description: string;
  status: string;
  lastExecuted?: string;
  createdAt: string;
}

// Mock data for development
const mockPreOptimizations: PreOptimization[] = [
  {
    id: '1',
    name: 'Format Validation',
    description: 'Validates and fixes formatting issues in RSF files',
    status: 'COMPLETED',
    lastExecuted: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(), // 1 day ago
    createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString() // 7 days ago
  },
  {
    id: '2',
    name: 'Reference Data Check',
    description: 'Validates and fixes reference data in RSF files',
    status: 'PENDING',
    createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString() // 5 days ago
  },
  {
    id: '3',
    name: 'Data Cleanup',
    description: 'Removes duplicate and inconsistent data',
    status: 'FAILED',
    lastExecuted: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString(), // 12 hours ago
    createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString() // 3 days ago
  }
];

const PreOptimizationExecution: React.FC = () => {
  const [preOptimizations, setPreOptimizations] = useState<PreOptimization[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [executingId, setExecutingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [showSuccessIcon, setShowSuccessIcon] = useState<boolean>(false);
  const navigate = useNavigate();

  // Check if user is authenticated
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
    }
  }, [navigate]);

  // Fetch pre-optimizations on component mount
  useEffect(() => {
    fetchPreOptimizations();
  }, []);  // eslint-disable-line react-hooks/exhaustive-deps

  const fetchPreOptimizations = async () => {
    try {
      setLoading(true);
      setError(null);
      
      console.log("Fetching pre-optimizations...");
      
      // Comment out the real API call
      // const token = localStorage.getItem('token');
      // const response = await axios.get('/api/pre-optimizations', {
      //   headers: {
      //     Authorization: `Bearer ${token}`
      //   }
      // });
      // setPreOptimizations(response.data);
      
      // Use mock data instead
      await new Promise(resolve => setTimeout(resolve, 1000));
      setPreOptimizations(mockPreOptimizations);
      console.log("Pre-optimizations loaded:", mockPreOptimizations);
    } catch (err: any) {
      console.error('Error fetching pre-optimizations:', err);
      setError(err.response?.data?.message || 'Failed to load pre-optimizations');
      
      // Redirect to login if unauthorized
      if (err.response?.status === 401) {
        navigate('/login');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleExecutePreOptimization = async (id: string) => {
    console.log(`Executing pre-optimization with ID: ${id}`);
    setSuccessMessage(null);
    setShowSuccessIcon(false);
    try {
      setExecutingId(id);
      setError(null);
      
      // Comment out the real API call
      // const token = localStorage.getItem('token');
      // await axios.post(`/api/pre-optimizations/${id}/execute`, {}, {
      //   headers: {
      //     Authorization: `Bearer ${token}`
      //   }
      // });
      
      // Simulate execution with mock data
      console.log("Simulating execution delay...");
      await new Promise(resolve => setTimeout(resolve, 2000)); // Simulate delay
      
      // Update mock data
      console.log("Updating pre-optimization status...");
      
      // Find the optimization being executed to use its name in the success message
      const optimization = preOptimizations.find(opt => opt.id === id);
      
      setPreOptimizations(prevOptimizations => {
        const updated = prevOptimizations.map(opt => 
          opt.id === id 
            ? {
                ...opt, 
                status: 'COMPLETED',
                lastExecuted: new Date().toISOString()
              } 
            : opt
        );
        console.log("Updated pre-optimizations:", updated);
        return updated;
      });
      
      // Set success message
      setSuccessMessage(`Successfully executed ${optimization?.name || 'optimization'}`);
      setShowSuccessIcon(true);
      
      // Clear success message after 5 seconds
      setTimeout(() => {
        setSuccessMessage(null);
        setShowSuccessIcon(false);
      }, 5000);
    } catch (err: any) {
      console.error(`Error executing pre-optimization ${id}:`, err);
      setError(err.response?.data?.message || 'Execution failed');
      
      // Redirect to login if unauthorized
      if (err.response?.status === 401) {
        navigate('/login');
      }
    } finally {
      setExecutingId(null);
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Never';
    try {
      return new Date(dateString).toLocaleString(undefined, { 
        year: 'numeric', month: 'numeric', day: 'numeric', 
        hour: 'numeric', minute: 'numeric' 
      });
    } catch (e) {
      console.error("Error formatting date:", dateString, e);
      return 'Invalid Date';
    }
  };

  if (loading && preOptimizations.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div 
          className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-indigo-500"
          role="status"
          aria-label="Loading pre-optimizations"
        >
          <span className="sr-only">Loading...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white shadow-md rounded-lg p-6 border border-gray-200/75 relative">
      {/* Success overlay - fixed size and position */}
      {showSuccessIcon && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50" onClick={() => setShowSuccessIcon(false)}>
          <div className="bg-white rounded-full p-4 shadow-lg w-24 h-24 flex items-center justify-center">
            <CheckCircleIcon className="h-16 w-16 text-green-500" />
          </div>
        </div>
      )}
      
      <h2 className="text-xl font-semibold mb-5 text-gray-700">Pre-Optimization Execution</h2>
      
      {error && (
        <div className="flex items-start bg-red-50 border-l-4 border-red-400 text-red-700 px-4 py-3 rounded mb-4 shadow-sm">
          <ExclamationCircleIcon className="h-5 w-5 mr-3 mt-0.5 text-red-400" />
          <div>
            <p className="font-medium">Error</p>
            <p className="text-sm">{error}</p>
          </div>
        </div>
      )}
      
      {successMessage && (
        <div className="flex items-start bg-green-50 border-l-4 border-green-400 text-green-700 px-4 py-3 rounded mb-4 shadow-sm">
           <CheckCircleIcon className="h-5 w-5 mr-3 mt-0.5 text-green-400" />
           <div>
             <p className="font-medium">Success</p>
             <p className="text-sm">{successMessage}</p>
           </div>
        </div>
      )}
      
      <div className="overflow-x-auto rounded border border-gray-200/75">
        <table className="min-w-full divide-y divide-gray-200/75 bg-white">
          <thead className="bg-gray-50">
            <tr className="divide-x divide-gray-200/75">
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Last Executed</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Created At</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200/75">
            {preOptimizations.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-4 text-center text-sm text-gray-500 italic">
                  No pre-optimizations available.
                </td>
              </tr>
            ) : (
              preOptimizations.map((optimization, index) => (
                <tr key={optimization.id} className={`divide-x divide-gray-200/75 ${index % 2 !== 0 ? 'bg-gray-50/50' : 'bg-white'} hover:bg-indigo-50/30 transition-colors duration-150`}>
                  <td className="px-4 py-3 whitespace-nowrap text-sm font-medium text-gray-900">
                    {optimization.name}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600 max-w-xs truncate" title={optimization.description}>
                    {optimization.description}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm">
                    <span className={`inline-block px-2.5 py-0.5 text-xs font-semibold rounded-full 
                      ${optimization.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 
                        optimization.status === 'FAILED' ? 'bg-red-100 text-red-800' :
                        optimization.status === 'RUNNING' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-700'}`}>
                      {optimization.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                    {formatDate(optimization.lastExecuted)}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                    {formatDate(optimization.createdAt)}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap text-sm font-medium">
                    <button
                      type="button"
                      onClick={() => handleExecutePreOptimization(optimization.id)}
                      disabled={executingId === optimization.id || optimization.status === 'RUNNING' || optimization.status === 'COMPLETED'}
                      className={`inline-flex items-center justify-center px-3 py-1.5 border border-transparent rounded-md shadow-sm text-xs font-medium text-white transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 
                        ${executingId === optimization.id || optimization.status === 'RUNNING' || optimization.status === 'COMPLETED'
                          ? 'bg-gray-300 cursor-not-allowed'
                          : 'bg-indigo-600 hover:bg-indigo-700'}`}
                    >
                      {executingId === optimization.id ? (
                        <>
                          <svg className="animate-spin -ml-0.5 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                          </svg>
                          Executing...
                        </>
                      ) : (optimization.status === 'COMPLETED' ? 'Completed' : 'Execute')}
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PreOptimizationExecution; 