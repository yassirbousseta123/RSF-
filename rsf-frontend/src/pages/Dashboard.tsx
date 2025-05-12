import React from 'react';
import { 
  Container, 
  Typography, 
  Box, 
  Paper, 
  Card, 
  CardContent,
  Stack
} from '@mui/material';
import { useAuth } from '../context/AuthContext';

const Dashboard: React.FC = () => {
  const { user } = useAuth();

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Dashboard
      </Typography>
      
      <Paper elevation={3} sx={{ p: 3, mb: 4 }}>
        <Typography variant="h5" gutterBottom>
          Welcome, {user?.name || user?.email || 'User'}!
        </Typography>
        <Typography variant="body1">
          You've successfully logged in to the Resource Sharing Framework.
        </Typography>
      </Paper>
      
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" gutterBottom>Stats</Typography>
        <Box sx={{ 
          display: 'flex', 
          flexWrap: 'wrap', 
          gap: 2 
        }}>
          {[
            { label: 'Uploaded Files', value: '12' },
            { label: 'Storage Used', value: '45 MB' },
            { label: 'Shared Resources', value: '3' },
            { label: 'Downloads', value: '27' }
          ].map((stat, index) => (
            <Card 
              key={index} 
              sx={{ 
                minWidth: { xs: '100%', sm: 'calc(50% - 8px)', md: 'calc(25% - 12px)' },
                bgcolor: 'primary.light',
                color: 'primary.contrastText'
              }}
            >
              <CardContent sx={{ textAlign: 'center' }}>
                <Typography variant="h3" component="div">
                  {stat.value}
                </Typography>
                <Typography variant="body1">
                  {stat.label}
                </Typography>
              </CardContent>
            </Card>
          ))}
        </Box>
      </Box>
      
      <Paper elevation={3} sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>Recent Files</Typography>
        <Stack spacing={2}>
          {['Quarterly Report.pdf', 'Project Proposal.docx', 'Budget.xlsx', 'Meeting Notes.txt']
            .map((file, index) => (
              <Box 
                key={index}
                sx={{ 
                  p: 2, 
                  bgcolor: 'background.default',
                  borderRadius: 1,
                  border: '1px solid',
                  borderColor: 'divider'
                }}
              >
                <Typography>{file}</Typography>
              </Box>
            ))}
        </Stack>
      </Paper>
    </Container>
  );
};

export default Dashboard; 