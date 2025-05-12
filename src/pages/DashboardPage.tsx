import React, { useEffect, useState } from 'react';
import { 
  Box, 
  Typography, 
  Container, 
  Paper, 
  Button, 
  Snackbar, 
  Alert, 
  useTheme, 
  CircularProgress
} from '@mui/material';
import { 
  Assessment as AssessmentIcon, 
  Error as ErrorIcon, 
  CheckCircle as CheckCircleIcon, 
  Warning as WarningIcon, 
  Refresh as RefreshIcon 
} from '@mui/icons-material';

import ValidationSummaryCard from '../components/validation/ValidationSummaryCard';
import ValidationErrorTable from '../components/validation/ValidationErrorTable';
import ValidationStats from '../components/validation/ValidationStats';

import { getValidationDashboardData, fixValidationError } from '../services/validationService';
import { ValidationDashboardData, ValidationSummary } from '../types/validation';

const DashboardPage: React.FC = () => {
  const theme = useTheme();
  const [dashboardData, setDashboardData] = useState<ValidationDashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' | 'info' | 'warning' }>({
    open: false,
    message: '',
    severity: 'info'
  });
  const [fixingError, setFixingError] = useState(false);

  // Fetch validation dashboard data
  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getValidationDashboardData();
      setDashboardData(data);
    } catch (err) {
      console.error('Error fetching validation data:', err);
      setError('Failed to load validation data. Please try again later.');
      setSnackbar({
        open: true,
        message: 'Failed to load validation data',
        severity: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  // Handle fixing an error
  const handleFixError = async (errorId: string) => {
    setFixingError(true);
    try {
      await fixValidationError(errorId);
      // Refresh data after fixing
      await fetchDashboardData();
      setSnackbar({
        open: true,
        message: 'Error fixed successfully',
        severity: 'success'
      });
    } catch (err) {
      console.error('Error fixing validation error:', err);
      setSnackbar({
        open: true,
        message: 'Failed to fix error',
        severity: 'error'
      });
    } finally {
      setFixingError(false);
    }
  };

  // Close snackbar
  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  // Fetch data on component mount
  useEffect(() => {
    fetchDashboardData();
  }, []);

  // Prepare summary stats
  const getSummaryData = (): ValidationSummary | null => {
    if (!dashboardData) return null;
    return dashboardData.summary;
  };

  // Prepare statistics
  const getStatsData = () => {
    if (!dashboardData || !dashboardData.errors) return [];
    
    // Create stats by error type
    const statsByType = new Map<string, number>();
    
    // Count errors by type
    dashboardData.errors.forEach(error => {
      const errorType = error.errorType;
      statsByType.set(errorType, (statsByType.get(errorType) || 0) + 1);
    });
    
    // Convert to array of stats
    return Array.from(statsByType.entries()).map(([name, count]) => ({
      name: name.replace('_', ' '),
      count
    }));
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Validation Dashboard
        </Typography>
        <Button 
          variant="outlined" 
          startIcon={<RefreshIcon />}
          onClick={fetchDashboardData}
          disabled={loading}
        >
          Refresh Data
        </Button>
      </Box>

      {/* Loading State */}
      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      )}

      {/* Error State */}
      {error && !loading && (
        <Paper sx={{ p: 3, mb: 3, bgcolor: theme.palette.error.light }}>
          <Typography color="error" variant="h6">
            {error}
          </Typography>
        </Paper>
      )}

      {/* Dashboard Content */}
      {!loading && !error && dashboardData && (
        <>
          {/* Summary Cards */}
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mb: 4 }}>
            <Box sx={{ flex: { xs: '1 1 100%', sm: '1 1 calc(50% - 1.5rem)', md: '1 1 calc(25% - 2.25rem)' } }}>
              <ValidationSummaryCard
                title="Total Files"
                value={getSummaryData()?.totalFiles.toString() || '0'}
                icon={<AssessmentIcon />}
                tooltipText="Total number of files processed"
              />
            </Box>
            <Box sx={{ flex: { xs: '1 1 100%', sm: '1 1 calc(50% - 1.5rem)', md: '1 1 calc(25% - 2.25rem)' } }}>
              <ValidationSummaryCard
                title="Files with Errors"
                value={getSummaryData()?.filesWithErrors.toString() || '0'}
                icon={<ErrorIcon />}
                tooltipText="Files that contain validation errors"
              />
            </Box>
            <Box sx={{ flex: { xs: '1 1 100%', sm: '1 1 calc(50% - 1.5rem)', md: '1 1 calc(25% - 2.25rem)' } }}>
              <ValidationSummaryCard
                title="Total Errors"
                value={dashboardData.errors.length.toString()}
                icon={<WarningIcon />}
                tooltipText="Total number of validation errors found"
              />
            </Box>
            <Box sx={{ flex: { xs: '1 1 100%', sm: '1 1 calc(50% - 1.5rem)', md: '1 1 calc(25% - 2.25rem)' } }}>
              <ValidationSummaryCard
                title="Processing Time"
                value={`${getSummaryData()?.processingTimeMs ? (getSummaryData()!.processingTimeMs / 1000).toFixed(2) : '0'} sec`}
                icon={<CheckCircleIcon />}
                tooltipText="Time taken to process and validate files"
              />
            </Box>
          </Box>

          {/* Statistics & Error Table */}
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
            <Box sx={{ flex: { xs: '1 1 100%', md: '1 1 calc(33.333% - 2rem)' } }}>
              <ValidationStats 
                stats={getStatsData()} 
                loading={loading} 
              />
            </Box>
            <Box sx={{ flex: { xs: '1 1 100%', md: '1 1 calc(66.666% - 1rem)' } }}>
              <Typography variant="h6" gutterBottom>
                Validation Errors
              </Typography>
              <ValidationErrorTable 
                errors={dashboardData.errors} 
                loading={loading}
                onFixError={handleFixError}
                onRefresh={fetchDashboardData}
              />
            </Box>
          </Box>
        </>
      )}

      {/* No Data State */}
      {!loading && !error && (!dashboardData || dashboardData.errors.length === 0) && (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <CheckCircleIcon sx={{ fontSize: 60, color: 'success.main', mb: 2 }} />
          <Typography variant="h5" gutterBottom>
            No Validation Errors
          </Typography>
          <Typography variant="body1" color="textSecondary">
            Great! All your files have passed validation.
          </Typography>
        </Paper>
      )}

      {/* Snackbar for notifications */}
      <Snackbar 
        open={snackbar.open} 
        autoHideDuration={6000} 
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default DashboardPage; 