import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Container, 
  Grid, 
  Typography, 
  Paper, 
  CircularProgress,
  Alert,
  useTheme,
  Tabs,
  Tab,
  Backdrop
} from '@mui/material';
import WarningIcon from '@mui/icons-material/Warning';
import ErrorIcon from '@mui/icons-material/Error';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import DescriptionIcon from '@mui/icons-material/Description';
import BuildIcon from '@mui/icons-material/Build';

// Import services and type definitions
import { getValidationDashboardData, getAllErrors, fixValidationError } from '../services/validationService';
import { ValidationDashboardData } from '../types/validation';

// Import components
import ValidationSummaryCard from '../components/validation/ValidationSummaryCard';
import ValidationErrorTable from '../components/validation/ValidationErrorTable';
import ValidationStats from '../components/validation/ValidationStats';

const initialDashboardState: ValidationDashboardData = {
  summary: {
    totalFiles: 0,
    filesWithErrors: 0,
    totalErrors: 0,
    processingTime: 0,
    lastUpdated: new Date().toISOString()
  },
  stats: [],
  errorsByFile: {},
  errors: []
};

function DashboardPage() {
  const theme = useTheme();
  const [dashboardData, setDashboardData] = useState<ValidationDashboardData>(initialDashboardState);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentTab, setCurrentTab] = useState(0);
  const [errorPage, setErrorPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalErrorCount, setTotalErrorCount] = useState(0);
  const [errorsLoading, setErrorsLoading] = useState(false);
  const [fixingError, setFixingError] = useState<string | null>(null);

  // Fetch dashboard data
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const data = await getValidationDashboardData();
        setDashboardData(data);
        setTotalErrorCount(data.summary.totalErrors);
      } catch (err) {
        setError('Failed to load validation dashboard data');
        console.error('Error fetching validation data:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Fetch errors with pagination
  useEffect(() => {
    const fetchErrors = async () => {
      if (loading) return; // Skip if main data is still loading
      
      try {
        setErrorsLoading(true);
        const response = await getAllErrors({
          page: errorPage,
          size: rowsPerPage,
          sort: 'timestamp,desc'
        });
        
        setDashboardData(prev => ({
          ...prev,
          errors: response.errors
        }));
        setTotalErrorCount(response.totalItems);
      } catch (err) {
        console.error('Error fetching validation errors:', err);
      } finally {
        setErrorsLoading(false);
      }
    };

    fetchErrors();
  }, [errorPage, rowsPerPage, loading]);

  const handleFixError = async (errorId: string) => {
    try {
      setFixingError(errorId);
      await fixValidationError(errorId);
      
      // Refresh errors and dashboard data after fixing
      const data = await getValidationDashboardData();
      setDashboardData(data);
      setTotalErrorCount(data.summary.totalErrors);
    } catch (err) {
      console.error('Error fixing validation error:', err);
    } finally {
      setFixingError(null);
    }
  };

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  const handlePageChange = (newPage: number) => {
    setErrorPage(newPage);
  };

  const handleRowsPerPageChange = (newRowsPerPage: number) => {
    setRowsPerPage(newRowsPerPage);
    setErrorPage(0); // Reset to first page when changing rows per page
  };

  if (loading) {
    return (
      <Box 
        sx={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '80vh' 
        }}
      >
        <CircularProgress size={60} />
      </Box>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
      </Container>
    );
  }

  // Generate summary cards
  const summaryCards = [
    {
      title: 'Total Files',
      value: dashboardData.summary.totalFiles,
      icon: <DescriptionIcon />,
      tooltipText: 'Total number of files processed'
    },
    {
      title: 'Files with Errors',
      value: dashboardData.summary.filesWithErrors,
      icon: <WarningIcon />,
      tooltipText: 'Number of files that contain validation errors',
      change: dashboardData.summary.filesWithErrors > 0 
        ? (dashboardData.summary.filesWithErrors / dashboardData.summary.totalFiles) * 100 
        : 0
    },
    {
      title: 'Total Errors',
      value: dashboardData.summary.totalErrors,
      icon: <ErrorIcon />,
      tooltipText: 'Total number of validation errors found'
    },
    {
      title: 'Processing Time',
      value: `${(dashboardData.summary.processingTime / 1000).toFixed(2)}s`,
      icon: <CheckCircleIcon />,
      tooltipText: 'Time taken to process all files'
    }
  ];

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      {/* Backdrop to show when fixing an error */}
      <Backdrop
        sx={{ 
          color: '#fff', 
          zIndex: (theme) => theme.zIndex.drawer + 1,
          flexDirection: 'column',
          gap: 2 
        }}
        open={fixingError !== null}
      >
        <BuildIcon sx={{ fontSize: 40 }} />
        <Typography variant="h6">Fixing error...</Typography>
      </Backdrop>

      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          Validation Dashboard
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Overview of RSF file validation results
        </Typography>
      </Box>

      {/* Summary Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {summaryCards.map((card, index) => (
          <Grid sx={{ gridColumn: { xs: 'span 12', sm: 'span 6', md: 'span 3' } }} key={index}>
            <ValidationSummaryCard 
              title={card.title}
              value={card.value}
              icon={card.icon}
              tooltipText={card.tooltipText}
              change={card.change}
            />
          </Grid>
        ))}
      </Grid>

      {/* Tabs for Errors and Stats */}
      <Paper sx={{ mb: 4 }}>
        <Tabs
          value={currentTab}
          onChange={handleTabChange}
          textColor="primary"
          indicatorColor="primary"
          sx={{ borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab label="Validation Errors" />
          <Tab label="Error Statistics" />
        </Tabs>
        
        {/* Errors Tab */}
        {currentTab === 0 && (
          <Box sx={{ p: 2 }}>
            <ValidationErrorTable 
              errors={dashboardData.errors}
              loading={errorsLoading}
              onFixError={handleFixError}
              totalItems={totalErrorCount}
              page={errorPage}
              rowsPerPage={rowsPerPage}
              onPageChange={handlePageChange}
              onRowsPerPageChange={handleRowsPerPageChange}
            />
          </Box>
        )}
        
        {/* Stats Tab */}
        {currentTab === 1 && (
          <Box sx={{ p: 2 }}>
            <ValidationStats 
              stats={dashboardData.stats}
              loading={false}
            />
          </Box>
        )}
      </Paper>
    </Container>
  );
}

export default DashboardPage; 