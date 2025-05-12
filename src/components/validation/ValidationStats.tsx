import React from 'react';
import { Box, Paper, Typography, Grid } from '@mui/material';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { ValidationStat } from '../../types/validation';

interface ValidationStatsProps {
  stats: ValidationStat[];
  loading: boolean;
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#e91e63', '#4caf50'];

// Create a formatted label for the pie chart
const renderCustomizedLabel = ({
  cx,
  cy,
  midAngle,
  innerRadius,
  outerRadius,
  percent,
  index,
  name
}: any) => {
  const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
  const x = cx + radius * Math.cos(-midAngle * Math.PI / 180);
  const y = cy + radius * Math.sin(-midAngle * Math.PI / 180);

  return percent > 0.05 ? (
    <text 
      x={x} 
      y={y} 
      fill="white" 
      textAnchor={x > cx ? 'start' : 'end'} 
      dominantBaseline="central"
      fontSize={12}
    >
      {`${(percent * 100).toFixed(0)}%`}
    </text>
  ) : null;
};

// Custom tooltip for the pie chart
const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    return (
      <Paper sx={{ p: 1.5, boxShadow: 3 }}>
        <Typography variant="body2" color="textPrimary">
          <strong>{payload[0].name}</strong>
        </Typography>
        <Typography variant="body2" color="textSecondary">
          Count: {payload[0].value}
        </Typography>
        <Typography variant="body2" color="textSecondary">
          {`${(payload[0].payload.percent * 100).toFixed(2)}%`}
        </Typography>
      </Paper>
    );
  }
  return null;
};

const ValidationStats: React.FC<ValidationStatsProps> = ({ stats, loading }) => {
  if (loading) {
    return (
      <Paper sx={{ p: 3, height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Typography variant="body1" color="textSecondary">
          Loading statistics...
        </Typography>
      </Paper>
    );
  }

  if (!stats || stats.length === 0) {
    return (
      <Paper sx={{ p: 3, height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Typography variant="body1" color="textSecondary">
          No statistics available
        </Typography>
      </Paper>
    );
  }

  // Calculate total for percentages
  const total = stats.reduce((sum, stat) => sum + stat.count, 0);
  
  // Add percent field to each stat
  const chartData = stats.map(stat => ({
    ...stat,
    percent: stat.count / total
  }));

  return (
    <Paper sx={{ p: 3, height: '100%' }}>
      <Typography variant="h6" gutterBottom>
        Error Distribution
      </Typography>
      
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
        <Box sx={{ flex: '0 0 70%', height: 300, width: '100%' }}>
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomizedLabel}
                outerRadius={100}
                fill="#8884d8"
                dataKey="count"
                nameKey="name"
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </Box>
        
        <Box sx={{ flex: '1', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <Typography variant="body1" gutterBottom>
            Total Errors: <strong>{total}</strong>
          </Typography>
          
          {chartData.map((stat, index) => (
            <Box key={index} sx={{ mb: 1, display: 'flex', alignItems: 'center' }}>
              <Box
                sx={{
                  width: 14,
                  height: 14,
                  mr: 1,
                  borderRadius: '50%',
                  backgroundColor: COLORS[index % COLORS.length]
                }}
              />
              <Typography variant="body2">
                {stat.name}: <strong>{stat.count}</strong> ({(stat.percent * 100).toFixed(1)}%)
              </Typography>
            </Box>
          ))}
        </Box>
      </Box>
    </Paper>
  );
};

export default ValidationStats; 