import React, { useState } from 'react';
import {
  Container,
  Typography,
  Box,
  TextField,
  Button,
  Switch,
  FormControlLabel,
  Alert,
  Snackbar,
  Avatar,
  Card,
  CardContent,
  CardHeader,
  IconButton,
  FormGroup
} from '@mui/material';
import {
  Save as SaveIcon,
  Edit as EditIcon,
  PhotoCamera as PhotoCameraIcon,
  Security as SecurityIcon,
  Notifications as NotificationsIcon,
  Storage as StorageIcon
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';

// Add this interface to handle user type properly
interface ExtendedUser {
  id: string;
  name?: string;
  email?: string;
  profileImage?: string;
  roles: string[];
}

const Settings: React.FC = () => {
  const { user } = useAuth();
  useTheme(); // Keep the hook call if it has side effects or if other properties are needed later
  
  // Cast user to ExtendedUser to avoid type errors
  const extendedUser = user as unknown as ExtendedUser;
  
  // Profile state
  const [name, setName] = useState(extendedUser?.name || '');
  const [email, setEmail] = useState(extendedUser?.email || '');
  const [editing, setEditing] = useState(false);
  
  // Password state
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  // Notification settings
  const [emailNotifications, setEmailNotifications] = useState(true);
  const [pushNotifications, setPushNotifications] = useState(false);
  
  // Success/Error messages
  const [notification, setNotification] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error';
  }>({
    open: false,
    message: '',
    severity: 'success'
  });
  
  const handleProfileSave = () => {
    // Simulate API call to update profile
    setTimeout(() => {
      setEditing(false);
      showNotification('Profile updated successfully', 'success');
    }, 1000);
  };
  
  const handlePasswordChange = () => {
    if (newPassword !== confirmPassword) {
      showNotification('Passwords do not match', 'error');
      return;
    }
    
    if (newPassword.length < 8) {
      showNotification('Password must be at least 8 characters', 'error');
      return;
    }
    
    // Simulate API call to change password
    setTimeout(() => {
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      showNotification('Password changed successfully', 'success');
    }, 1000);
  };
  
  const showNotification = (message: string, severity: 'success' | 'error') => {
    setNotification({
      open: true,
      message,
      severity
    });
  };
  
  const closeNotification = () => {
    setNotification({ ...notification, open: false });
  };
  
  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Settings
      </Typography>
      
      <Box sx={{ mb: 4 }}>
        <Box sx={{ 
          display: 'flex', 
          flexWrap: 'wrap', 
          gap: 3 
        }}>
          {/* Profile Settings */}
          <Card elevation={3} sx={{ flex: '1 1 calc(50% - 12px)', minWidth: '300px' }}>
            <CardHeader 
              title="Profile Information" 
              action={
                <IconButton onClick={() => setEditing(!editing)}>
                  <EditIcon />
                </IconButton>
              }
            />
            <CardContent>
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
                <Avatar 
                  src={extendedUser?.profileImage || '/user-avatar.jpg'} 
                  sx={{ width: 100, height: 100, mb: 2 }}
                />
                <Button
                  variant="outlined"
                  startIcon={<PhotoCameraIcon />}
                  size="small"
                >
                  Change Photo
                </Button>
              </Box>
              
              <TextField
                label="Name"
                fullWidth
                margin="normal"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={!editing}
              />
              
              <TextField
                label="Email"
                fullWidth
                margin="normal"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={!editing}
              />
              
              {editing && (
                <Button
                  variant="contained"
                  color="primary"
                  startIcon={<SaveIcon />}
                  onClick={handleProfileSave}
                  sx={{ mt: 2 }}
                >
                  Save Changes
                </Button>
              )}
            </CardContent>
          </Card>
          
          {/* Security Settings */}
          <Card elevation={3} sx={{ flex: '1 1 calc(50% - 12px)', minWidth: '300px' }}>
            <CardHeader 
              title="Security" 
              avatar={<SecurityIcon color="primary" />}
            />
            <CardContent>
              <Typography variant="subtitle1" gutterBottom>
                Change Password
              </Typography>
              
              <TextField
                label="Current Password"
                type="password"
                fullWidth
                margin="normal"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
              />
              
              <TextField
                label="New Password"
                type="password"
                fullWidth
                margin="normal"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
              />
              
              <TextField
                label="Confirm New Password"
                type="password"
                fullWidth
                margin="normal"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              
              <Button
                variant="contained"
                color="primary"
                onClick={handlePasswordChange}
                sx={{ mt: 2 }}
              >
                Update Password
              </Button>
            </CardContent>
          </Card>
          
          {/* Notification Settings */}
          <Card elevation={3} sx={{ flex: '1 1 calc(50% - 12px)', minWidth: '300px', mt: 3 }}>
            <CardHeader 
              title="Notifications" 
              avatar={<NotificationsIcon color="primary" />}
            />
            <CardContent>
              <FormGroup>
                <FormControlLabel
                  control={
                    <Switch 
                      checked={emailNotifications} 
                      onChange={(e) => setEmailNotifications(e.target.checked)}
                    />
                  }
                  label="Email Notifications"
                />
                <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1, mb: 1 }}>
                  Receive notifications about file uploads, sharing, and system updates
                </Typography>
                
                <FormControlLabel
                  control={
                    <Switch 
                      checked={pushNotifications} 
                      onChange={(e) => setPushNotifications(e.target.checked)}
                    />
                  }
                  label="Push Notifications"
                />
                <Typography variant="body2" color="textSecondary" sx={{ ml: 4, mt: -1 }}>
                  Receive browser notifications when you're online
                </Typography>
              </FormGroup>
              <Button
                variant="contained"
                color="primary"
                sx={{ mt: 2 }}
                onClick={() => showNotification('Notification settings saved', 'success')}
              >
                Save Preferences
              </Button>
            </CardContent>
          </Card>
          
          {/* Storage Settings */}
          <Card elevation={3} sx={{ flex: '1 1 calc(50% - 12px)', minWidth: '300px', mt: 3 }}>
            <CardHeader 
              title="Storage" 
              avatar={<StorageIcon color="primary" />}
            />
            <CardContent>
              <Typography variant="body1" gutterBottom>
                Storage Used: 45 MB / 1 GB
              </Typography>
              <Box sx={{ 
                height: 10, 
                width: '100%', 
                bgcolor: 'grey.200', 
                borderRadius: 5,
                mb: 2
              }}>
                <Box 
                  sx={{ 
                    height: '100%', 
                    width: '4.5%',
                    bgcolor: 'primary.main',
                    borderRadius: 5
                  }} 
                />
              </Box>
              
              <Typography variant="subtitle2" gutterBottom>
                Storage Details
              </Typography>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">Documents</Typography>
                <Typography variant="body2">25 MB</Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">Images</Typography>
                <Typography variant="body2">15 MB</Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2">Other</Typography>
                <Typography variant="body2">5 MB</Typography>
              </Box>
              
              <Button
                variant="outlined"
                color="primary"
                sx={{ mt: 2 }}
                onClick={() => showNotification('This feature is not available in the demo', 'error')}
              >
                Manage Storage
              </Button>
            </CardContent>
          </Card>
        </Box>
      </Box>
      
      <Snackbar
        open={notification.open}
        autoHideDuration={6000}
        onClose={closeNotification}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={closeNotification} severity={notification.severity}>
          {notification.message}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default Settings; 