import React from 'react';
import { 
  Box, 
  Grid, 
  Paper, 
  Typography, 
  Divider,
  CircularProgress,
  useTheme
} from '@mui/material';
import { ValidationStat } from '../../types/validation';

// Add these after installing recharts package
// npm install recharts
// If not installed, consider replacing with a simpler representation

interface ValidationStatsProps {
  stats: ValidationStat[];
  loading?: boolean;
}

function ValidationStats({ stats, loading = false }: ValidationStatsProps) {
  const theme = useTheme();
  
  // Colors for chart/display
  const COLORS = [
    theme.palette.error.main,
    theme.palette.warning.main,
    theme.palette.info.main,
    theme.palette.success.main,
    theme.palette.primary.main,
    theme.palette.secondary.main,
    // Add more colors as needed
  ];

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Paper sx={{ p: 2, height: '100%' }}>
      <Typography variant="h6" gutterBottom>
        Error Distribution
      </Typography>
      <Divider sx={{ mb: 2 }} />

      {stats.length === 0 ? (
        <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 4 }}>
          No statistics available
        </Typography>
      ) : (
        <Grid container spacing={2}>
          {/* For now, showing simple stat cards instead of chart until recharts is installed */}
          {stats.map((stat, index) => (
            <Grid sx={{ gridColumn: { xs: 'span 12', sm: 'span 6', md: 'span 4' } }} key={index}>
              <Box sx={{ 
                p: 2, 
                borderLeft: `4px solid ${COLORS[index % COLORS.length]}`,
                height: '100%',
                display: 'flex',
                flexDirection: 'column'
              }}>
                <Typography variant="body2" color="text.secondary">
                  {stat.name}
                </Typography>
                <Typography variant="h6">
                  {stat.value}
                </Typography>
                {stat.description && (
                  <Typography variant="caption" color="text.secondary">
                    {stat.description}
                  </Typography>
                )}
              </Box>
            </Grid>
          ))}
        </Grid>
      )}
    </Paper>
  );
}

export default ValidationStats; 