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
import { useAuth } from '../context/AuthContext';

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

  // ... rest of the component
}; 