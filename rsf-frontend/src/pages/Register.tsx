import React from 'react';
import { Container, Box, Paper, Typography, useTheme } from '@mui/material';
import RegisterForm from '../components/Auth/RegisterForm';

const Register: React.FC = () => {
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
              Create an Account
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Join our resource sharing platform
            </Typography>
          </Box>
          
          <RegisterForm />
        </Paper>
      </Box>
    </Container>
  );
};

export default Register; 