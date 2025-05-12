import React from 'react';
import {
  Card,
  CardContent,
  Typography,
  Box,
  CircularProgress,
  Tooltip,
  IconButton,
  useTheme
} from '@mui/material';
import InfoIcon from '@mui/icons-material/Info';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

interface ValidationSummaryCardProps {
  title: string;
  value: number | string;
  icon?: React.ReactNode;
  tooltipText?: string;
  change?: number;
  loading?: boolean;
}

function ValidationSummaryCard({
  title,
  value,
  icon,
  tooltipText,
  change,
  loading = false
}: ValidationSummaryCardProps) {
  const theme = useTheme();

  return (
    <Card sx={{ height: '100%', position: 'relative' }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <Typography variant="subtitle2" color="text.secondary">
            {title}
            {tooltipText && (
              <Tooltip title={tooltipText}>
                <IconButton size="small" sx={{ ml: 0.5, p: 0 }}>
                  <InfoIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}
          </Typography>
          {icon && (
            <Box 
              sx={{ 
                color: theme.palette.primary.main,
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center' 
              }}
            >
              {icon}
            </Box>
          )}
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
            <CircularProgress size={24} />
          </Box>
        ) : (
          <>
            <Typography variant="h4" component="div" sx={{ mt: 1 }}>
              {value}
            </Typography>
            
            {change !== undefined && (
              <Box 
                sx={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  mt: 1,
                  color: change > 0 
                    ? theme.palette.error.main 
                    : change < 0 
                      ? theme.palette.success.main 
                      : theme.palette.text.secondary
                }}
              >
                {change > 0 ? (
                  <ArrowUpwardIcon fontSize="small" sx={{ mr: 0.5 }} />
                ) : change < 0 ? (
                  <ArrowDownwardIcon fontSize="small" sx={{ mr: 0.5 }} />
                ) : null}
                <Typography variant="body2">
                  {Math.abs(change).toFixed(1)}%
                </Typography>
              </Box>
            )}
          </>
        )}
      </CardContent>
    </Card>
  );
}

export default ValidationSummaryCard; 