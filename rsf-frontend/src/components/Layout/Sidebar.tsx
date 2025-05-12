import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  Box,
  useTheme
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  Upload as UploadIcon,
  Settings as SettingsIcon,
  Logout as LogoutIcon,
  Check as ValidationIcon,
  Tune as OptimizationIcon
} from '@mui/icons-material';
import { useAuth } from '../../context/AuthContext';

interface SidebarProps {
  open: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ open }) => {
  const theme = useTheme();
  const { logout } = useAuth();
  const navigate = useNavigate();

  const menuItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/' },
    { text: 'Import Files', icon: <UploadIcon />, path: '/import' },
    { text: 'Validation', icon: <ValidationIcon />, path: '/validation-dashboard' },
    { text: 'Pre-Optimization', icon: <OptimizationIcon />, path: '/pre-optimization' },
    { text: 'Settings', icon: <SettingsIcon />, path: '/settings' }
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <Drawer
      variant="persistent"
      anchor="left"
      open={open}
      sx={{
        width: 240,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: 240,
          boxSizing: 'border-box',
          backgroundColor: theme.palette.background.default,
          borderRight: `1px solid ${theme.palette.divider}`
        }
      }}
    >
      <Box
        sx={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          padding: theme.spacing(0, 2),
          backgroundColor: theme.palette.primary.main,
          color: theme.palette.primary.contrastText
        }}
      >
        <Box
          component="img"
          sx={{ height: 30, mr: 1 }}
          alt="Logo"
          src="/logo.png"
        />
        <Box sx={{ fontWeight: 'bold', fontSize: '1.2rem' }}>RSF App</Box>
      </Box>
      <Divider />
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              component={NavLink}
              to={item.path}
              sx={{
                color: 'text.primary',
                '&.active': {
                  backgroundColor: theme.palette.action.selected,
                  '& .MuiListItemIcon-root': {
                    color: theme.palette.primary.main
                  }
                }
              }}
            >
              <ListItemIcon sx={{ color: 'inherit' }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Divider />
      <Box sx={{ flexGrow: 1 }} />
      <List>
        <ListItem disablePadding>
          <ListItemButton
            onClick={handleLogout}
            sx={{
              color: 'text.primary'
            }}
          >
            <ListItemIcon sx={{ color: 'inherit' }}>
              <LogoutIcon />
            </ListItemIcon>
            <ListItemText primary="Logout" />
          </ListItemButton>
        </ListItem>
      </List>
    </Drawer>
  );
};

export default Sidebar; 