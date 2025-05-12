import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Box, Button, TextField, Typography, Alert, CircularProgress } from '@mui/material';
import AuthService from '../../services/auth.service';

const LoginForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  
  const navigate = useNavigate();
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !password) {
      setError('Email and password are required');
      return;
    }
    
    try {
      setIsLoading(true);
      setError(null);
      
      // Use AuthService instead of context
      // Backend expects username field, but we use email in the UI
      const response = await AuthService.login({
        username: email,
        password: password
      });
      
      // Store token in localStorage
      localStorage.setItem('token', response.token);
      
      navigate('/dashboard');
    } catch (err: any) {
      console.error('Login error:', err);
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };
  
  return (
    <Box
      component="form"
      onSubmit={handleSubmit}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        maxWidth: 400,
        mx: 'auto',
        p: 3,
        borderRadius: 2,
        boxShadow: 3,
        bgcolor: 'background.paper'
      }}
    >
      <Typography variant="h5" component="h1" gutterBottom align="center">
        Log In
      </Typography>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      <TextField
        label="Email"
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        required
        fullWidth
      />
      
      <TextField
        label="Password"
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
        fullWidth
      />
      
      <Button
        type="submit"
        variant="contained"
        color="primary"
        size="large"
        fullWidth
        disabled={isLoading}
        sx={{ mt: 2 }}
      >
        {isLoading ? <CircularProgress size={24} /> : 'Log In'}
      </Button>
      
      <Typography align="center" sx={{ mt: 2 }}>
        Don't have an account?{' '}
        <Link to="/register" style={{ textDecoration: 'none', color: 'primary.main' }}>
          Register
        </Link>
      </Typography>
    </Box>
  );
};

export default LoginForm; 