import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  getAllPreOptimizations, 
  executePreOptimization, 
  PreOptimization 
} from '../../services/preOptimizationService';

const PreOptimizationExecution: React.FC = () => {
  const [preOptimizations, setPreOptimizations] = useState<PreOptimization[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [executingId, setExecutingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
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
  }, []);

  const fetchPreOptimizations = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const data = await getAllPreOptimizations();
      setPreOptimizations(data);
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
    try {
      setExecutingId(id);
      setError(null);
      
      await executePreOptimization(id);
      
      // Refresh the list after successful execution
      await fetchPreOptimizations();
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

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading && preOptimizations.length === 0) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div 
          className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"
          role="status"
          aria-label="Loading pre-optimizations"
        >
          <span className="sr-only">Loading pre-optimizations...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white shadow-md rounded-lg p-6">
      <h2 className="text-xl font-semibold mb-4">Pre-Optimization Execution</h2>
      
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}
      
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Last Executed</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Created At</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {preOptimizations.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-6 py-4 text-center text-sm text-gray-500">
                  No pre-optimizations found
                </td>
              </tr>
            ) : (
              preOptimizations.map((optimization) => (
                <tr key={optimization.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {optimization.name}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    {optimization.description}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                      ${optimization.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 
                        optimization.status === 'FAILED' ? 'bg-red-100 text-red-800' :
                        optimization.status === 'RUNNING' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-800'}`}>
                      {optimization.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {optimization.lastExecuted ? formatDate(optimization.lastExecuted) : 'Never'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatDate(optimization.createdAt)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button
                      onClick={() => handleExecutePreOptimization(optimization.id)}
                      disabled={executingId === optimization.id || optimization.status === 'RUNNING'}
                      className={`inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white 
                        ${executingId === optimization.id || optimization.status === 'RUNNING'
                          ? 'bg-gray-400 cursor-not-allowed'
                          : 'bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500'}`}
                    >
                      {executingId === optimization.id ? (
                        <>
                          <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                          </svg>
                          Executing...
                        </>
                      ) : 'Execute'}
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