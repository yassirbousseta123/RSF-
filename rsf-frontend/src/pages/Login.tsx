import React from 'react';
import { Container, Box, Paper, Typography, useTheme } from '@mui/material';
import LoginForm from '../components/Auth/LoginForm';

const Login: React.FC = () => {
  const theme = useTheme();
  
  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          py: 8,
        }}
      >
        <Paper
          elevation={3}
          sx={{
            p: 4,
            borderRadius: 2,
            bgcolor: theme.palette.background.paper,
          }}
        >
          <Box sx={{ mb: 4, textAlign: 'center' }}>
            <Typography
              variant="h4"
              component="h1"
              gutterBottom
              color="primary"
              fontWeight="bold"
            >
              Resource Sharing Framework
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Login to access your resources
            </Typography>
          </Box>
          
          <LoginForm />
        </Paper>
      </Box>
    </Container>
  );
};

export default Login; 