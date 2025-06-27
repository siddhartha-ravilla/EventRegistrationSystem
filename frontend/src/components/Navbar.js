import React, { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Menu,
  MenuItem,
  Box,
  Avatar,
  Chip,
  useTheme,
  useMediaQuery
} from '@mui/material';
import {
  Menu as MenuIcon,
  Event as EventIcon,
  Person as PersonIcon,
  AdminPanelSettings as AdminIcon,
  ExitToApp as LogoutIcon
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  
  const [mobileMenuAnchor, setMobileMenuAnchor] = useState(null);
  const [userMenuAnchor, setUserMenuAnchor] = useState(null);

  const handleMobileMenuOpen = (event) => {
    setMobileMenuAnchor(event.currentTarget);
  };

  const handleMobileMenuClose = () => {
    setMobileMenuAnchor(null);
  };

  const handleUserMenuOpen = (event) => {
    setUserMenuAnchor(event.currentTarget);
  };

  const handleUserMenuClose = () => {
    setUserMenuAnchor(null);
  };

  const handleLogout = () => {
    logout();
    handleUserMenuClose();
    navigate('/');
  };

  const handleNavigation = (path) => {
    navigate(path);
    handleMobileMenuClose();
    handleUserMenuClose();
  };

  const renderDesktopMenu = () => (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
      <Button
        color="inherit"
        component={RouterLink}
        to="/events"
        startIcon={<EventIcon />}
      >
        Events
      </Button>
      
      {isAuthenticated && (
        <>
          <Button
            color="inherit"
            component={RouterLink}
            to="/create-event"
            startIcon={<EventIcon />}
          >
            Create Event
          </Button>
          <Button
            color="inherit"
            component={RouterLink}
            to="/my-tickets"
            startIcon={<EventIcon />}
          >
            My Tickets
          </Button>
          {user?.role === 'ADMIN' && (
            <Button
              color="inherit"
              component={RouterLink}
              to="/admin"
              startIcon={<AdminIcon />}
            >
              Admin
            </Button>
          )}
        </>
      )}
    </Box>
  );

  const renderMobileMenu = () => (
    <>
      <IconButton
        color="inherit"
        onClick={handleMobileMenuOpen}
        sx={{ display: { md: 'none' } }}
      >
        <MenuIcon />
      </IconButton>
      <Menu
        anchorEl={mobileMenuAnchor}
        open={Boolean(mobileMenuAnchor)}
        onClose={handleMobileMenuClose}
        sx={{ display: { md: 'none' } }}
      >
        <MenuItem onClick={() => handleNavigation('/events')}>
          <EventIcon sx={{ mr: 1 }} />
          Events
        </MenuItem>
        {isAuthenticated && (
          <>
            <MenuItem onClick={() => handleNavigation('/create-event')}>
              <EventIcon sx={{ mr: 1 }} />
              Create Event
            </MenuItem>
            <MenuItem onClick={() => handleNavigation('/my-tickets')}>
              <EventIcon sx={{ mr: 1 }} />
              My Tickets
            </MenuItem>
            {user?.role === 'ADMIN' && (
              <MenuItem onClick={() => handleNavigation('/admin')}>
                <AdminIcon sx={{ mr: 1 }} />
                Admin
              </MenuItem>
            )}
          </>
        )}
      </Menu>
    </>
  );

  const renderAuthButtons = () => {
    if (!isAuthenticated) {
      return (
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            color="inherit"
            component={RouterLink}
            to="/login"
            variant="outlined"
            sx={{ color: 'white', borderColor: 'white' }}
          >
            Login
          </Button>
          <Button
            color="secondary"
            component={RouterLink}
            to="/register"
            variant="contained"
          >
            Register
          </Button>
        </Box>
      );
    }

    return (
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <Chip
          label={user?.role}
          color={user?.role === 'ADMIN' ? 'error' : 'primary'}
          size="small"
          variant="outlined"
        />
        <IconButton
          onClick={handleUserMenuOpen}
          sx={{ color: 'white' }}
        >
          <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main' }}>
            <PersonIcon />
          </Avatar>
        </IconButton>
        <Menu
          anchorEl={userMenuAnchor}
          open={Boolean(userMenuAnchor)}
          onClose={handleUserMenuClose}
        >
          <MenuItem onClick={() => handleNavigation('/profile')}>
            <PersonIcon sx={{ mr: 1 }} />
            Profile
          </MenuItem>
          <MenuItem onClick={handleLogout}>
            <LogoutIcon sx={{ mr: 1 }} />
            Logout
          </MenuItem>
        </Menu>
      </Box>
    );
  };

  return (
    <AppBar position="static" elevation={2}>
      <Toolbar>
        <Typography
          variant="h6"
          component={RouterLink}
          to="/"
          sx={{
            flexGrow: 1,
            textDecoration: 'none',
            color: 'inherit',
            fontWeight: 'bold'
          }}
        >
          Event Registration
        </Typography>

        {!isMobile && renderDesktopMenu()}
        {renderMobileMenu()}
        {renderAuthButtons()}
      </Toolbar>
    </AppBar>
  );
};

export default Navbar; 