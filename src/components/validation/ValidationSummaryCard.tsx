import React from 'react';
import { 
  Card, 
  CardContent, 
  Typography, 
  Box, 
  Tooltip, 
  IconButton,
  CircularProgress
} from '@mui/material';
import { Info as InfoIcon, ArrowUpward, ArrowDownward } from '@mui/icons-material';

interface ValidationSummaryCardProps {
  title: string;
  value: string;
  icon?: React.ReactNode;
  tooltipText?: string;
  change?: {
    value: number;
    isPositive: boolean;
  };
  loading?: boolean;
}

const ValidationSummaryCard: React.FC<ValidationSummaryCardProps> = ({
  title,
  value,
  icon,
  tooltipText,
  change,
  loading = false
}) => {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
          <Typography variant="subtitle1" color="text.secondary">
            {title}
            {tooltipText && (
              <Tooltip title={tooltipText} arrow placement="top">
                <IconButton size="small" sx={{ ml: 0.5, p: 0 }}>
                  <InfoIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}
          </Typography>
          {icon && <Box sx={{ color: 'primary.main' }}>{icon}</Box>}
        </Box>
        
        <Box sx={{ mt: 2, display: 'flex', alignItems: 'center' }}>
          {loading ? (
            <CircularProgress size={24} />
          ) : (
            <>
              <Typography variant="h4" component="div" sx={{ flexGrow: 1 }}>
                {value}
              </Typography>
              
              {change && (
                <Box 
                  sx={{ 
                    display: 'flex', 
                    alignItems: 'center',
                    color: change.isPositive ? 'success.main' : 'error.main' 
                  }}
                >
                  {change.isPositive ? <ArrowUpward fontSize="small" /> : <ArrowDownward fontSize="small" />}
                  <Typography variant="body2" sx={{ ml: 0.5 }}>
                    {change.value}%
                  </Typography>
                </Box>
              )}
            </>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};

export default ValidationSummaryCard; 