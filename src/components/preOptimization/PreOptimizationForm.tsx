import React, { useState } from 'react';
import axios from 'axios';

interface PreOptimizationFormProps {
  onSuccess?: () => void; // Optional callback for successful submission
}

const PreOptimizationForm: React.FC<PreOptimizationFormProps> = ({ onSuccess }) => {
  const [type, setType] = useState<'FIDES'>('FIDES'); // Default to FIDES
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const validateDates = (): boolean => {
    if (!startDate || !endDate) {
      setError('Both start and end dates are required.');
      return false;
    }
    const start = new Date(startDate);
    const end = new Date(endDate);
    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
        setError('Invalid date format.');
        return false;
    }
    if (end <= start) {
      setError('End date must be after start date.');
      return false;
    }
    setError(null); // Clear previous errors
    return true;
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null); // Clear previous errors

    if (!validateDates()) {
      return;
    }

    setLoading(true);

    try {
      const response = await axios.post('/api/pre-optimizations', {
        type,
        startDate,
        endDate,
      });

      // Assuming a successful response means the creation was successful
      console.log('Pre-optimization created:', response.data);
      // Reset form or provide feedback
      setStartDate('');
      setEndDate('');
      if (onSuccess) {
        onSuccess(); // Call the success callback if provided
      }
      // Optionally, add a success message state and display it
    } catch (err) {
      console.error('Error creating pre-optimization:', err);
      if (axios.isAxiosError(err) && err.response) {
        // Handle specific server errors (like overlapping dates)
        setError(err.response.data.message || 'An error occurred on the server.');
      } else {
        setError('An unexpected error occurred.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 p-6 bg-white shadow-md rounded-lg max-w-md mx-auto">
      <h2 className="text-xl font-semibold text-gray-700 mb-4">Create Pre-optimization</h2>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
          <span className="block sm:inline">{error}</span>
        </div>
      )}

      <div>
        <label htmlFor="type" className="block text-sm font-medium text-gray-700">
          Type
        </label>
        <select
          id="type"
          name="type"
          value={type}
          onChange={(e) => setType(e.target.value as 'FIDES')}
          className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md shadow-sm"
          disabled // Only FIDES is available for now
        >
          <option value="FIDES">FIDES</option>
          {/* Add other types here if needed in the future */}
        </select>
      </div>

      <div>
        <label htmlFor="startDate" className="block text-sm font-medium text-gray-700">
          Start Date
        </label>
        <input
          type="date"
          id="startDate"
          name="startDate"
          value={startDate}
          onChange={(e) => {
              setStartDate(e.target.value);
              setError(null); // Clear error on change
          }}
          required
          className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
        />
      </div>

      <div>
        <label htmlFor="endDate" className="block text-sm font-medium text-gray-700">
          End Date
        </label>
        <input
          type="date"
          id="endDate"
          name="endDate"
          value={endDate}
          onChange={(e) => {
              setEndDate(e.target.value);
              setError(null); // Clear error on change
          }}
          required
          className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
        />
      </div>

      <div>
        <button
          type="submit"
          disabled={loading}
          className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white ${
            loading ? 'bg-indigo-400 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500'
          }`}
        >
          {loading ? 'Creating...' : 'Create Pre-optimization'}
        </button>
      </div>
    </form>
  );
};

export default PreOptimizationForm; 