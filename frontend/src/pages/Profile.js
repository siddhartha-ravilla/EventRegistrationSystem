import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Box,
  Grid,
  Avatar,
  Alert,
  CircularProgress,
  Divider,
  Card,
  CardContent,
  Chip
} from '@mui/material';
import {
  Person,
  Email,
  Phone,
  LocationOn,
  Edit,
  Save,
  Cancel,
  Security
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';

const Profile = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, updateUser } = useAuth();
  
  const [profile, setProfile] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    bio: ''
  });
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [originalProfile, setOriginalProfile] = useState({});

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    fetchProfile();
  }, [isAuthenticated, navigate]);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/users/profile', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      const userProfile = response.data;
      setProfile({
        firstName: userProfile.firstName || '',
        lastName: userProfile.lastName || '',
        email: userProfile.email || '',
        phone: userProfile.phone || '',
        address: userProfile.address || '',
        bio: userProfile.bio || ''
      });
      setOriginalProfile({
        firstName: userProfile.firstName || '',
        lastName: userProfile.lastName || '',
        email: userProfile.email || '',
        phone: userProfile.phone || '',
        address: userProfile.address || '',
        bio: userProfile.bio || ''
      });
    } catch (err) {
      setError('Failed to load profile');
      console.error('Error fetching profile:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile(prev => ({
      ...prev,
      [name]: value
    }));
    setError('');
    setSuccess('');
  };

  const handleEdit = () => {
    setIsEditing(true);
    setError('');
    setSuccess('');
  };

  const handleCancel = () => {
    setProfile(originalProfile);
    setIsEditing(false);
    setError('');
    setSuccess('');
  };

  const handleSave = async () => {
    try {
      setLoading(true);
      setError('');
      setSuccess('');

      const response = await axios.put('/api/users/profile', profile, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      setOriginalProfile(profile);
      setIsEditing(false);
      setSuccess('Profile updated successfully!');
      
      // Update user context if needed
      if (updateUser) {
        updateUser(response.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const getInitials = () => {
    const firstName = profile.firstName || user?.firstName || '';
    const lastName = profile.lastName || user?.lastName || '';
    return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();
  };

  if (!isAuthenticated) {
    return null;
  }

  if (loading && !isEditing) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Profile
      </Typography>
      
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Manage your account information and preferences
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          {success}
        </Alert>
      )}

      <Grid container spacing={4}>
        {/* Profile Picture and Basic Info */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Avatar
              sx={{
                width: 120,
                height: 120,
                fontSize: 48,
                margin: '0 auto 16px',
                bgcolor: 'primary.main'
              }}
            >
              {getInitials()}
            </Avatar>
            
            <Typography variant="h6" gutterBottom>
              {profile.firstName} {profile.lastName}
            </Typography>
            
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {user?.username}
            </Typography>
            
            <Chip
              label={user?.role || 'USER'}
              color={user?.role === 'ADMIN' ? 'error' : 'default'}
              size="small"
              sx={{ mt: 1 }}
            />

            <Box sx={{ mt: 3 }}>
              <Button
                variant={isEditing ? "outlined" : "contained"}
                startIcon={isEditing ? <Cancel /> : <Edit />}
                onClick={isEditing ? handleCancel : handleEdit}
                fullWidth
                sx={{ mb: 1 }}
              >
                {isEditing ? 'Cancel' : 'Edit Profile'}
              </Button>
              
              {isEditing && (
                <Button
                  variant="contained"
                  startIcon={<Save />}
                  onClick={handleSave}
                  disabled={loading}
                  fullWidth
                >
                  {loading ? <CircularProgress size={20} /> : 'Save Changes'}
                </Button>
              )}
            </Box>
          </Paper>
        </Grid>

        {/* Profile Details */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Personal Information
            </Typography>
            
            <Grid container spacing={3}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="First Name"
                  name="firstName"
                  value={profile.firstName}
                  onChange={handleChange}
                  disabled={!isEditing}
                  InputProps={{
                    startAdornment: (
                      <Person sx={{ mr: 1, color: 'text.secondary' }} />
                    ),
                  }}
                />
              </Grid>
              
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Last Name"
                  name="lastName"
                  value={profile.lastName}
                  onChange={handleChange}
                  disabled={!isEditing}
                />
              </Grid>
              
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Email"
                  name="email"
                  type="email"
                  value={profile.email}
                  onChange={handleChange}
                  disabled={!isEditing}
                  InputProps={{
                    startAdornment: (
                      <Email sx={{ mr: 1, color: 'text.secondary' }} />
                    ),
                  }}
                />
              </Grid>
              
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Phone"
                  name="phone"
                  value={profile.phone}
                  onChange={handleChange}
                  disabled={!isEditing}
                  InputProps={{
                    startAdornment: (
                      <Phone sx={{ mr: 1, color: 'text.secondary' }} />
                    ),
                  }}
                />
              </Grid>
              
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Address"
                  name="address"
                  value={profile.address}
                  onChange={handleChange}
                  disabled={!isEditing}
                  multiline
                  rows={2}
                  InputProps={{
                    startAdornment: (
                      <LocationOn sx={{ mr: 1, color: 'text.secondary' }} />
                    ),
                  }}
                />
              </Grid>
              
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Bio"
                  name="bio"
                  value={profile.bio}
                  onChange={handleChange}
                  disabled={!isEditing}
                  multiline
                  rows={4}
                  placeholder="Tell us about yourself..."
                />
              </Grid>
            </Grid>
          </Paper>

          {/* Account Statistics */}
          <Paper sx={{ p: 3, mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Account Statistics
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent sx={{ textAlign: 'center' }}>
                    <Typography variant="h4" color="primary">
                      {user?.ticketsCount || 0}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Tickets Booked
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent sx={{ textAlign: 'center' }}>
                    <Typography variant="h4" color="success.main">
                      {user?.eventsAttended || 0}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Events Attended
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
              
              <Grid item xs={12} sm={4}>
                <Card>
                  <CardContent sx={{ textAlign: 'center' }}>
                    <Typography variant="h4" color="warning.main">
                      {user?.memberSince ? 
                        new Date(user.memberSince).getFullYear() : 
                        new Date().getFullYear()
                      }
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Member Since
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Paper>

          {/* Security Section */}
          <Paper sx={{ p: 3, mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Security
            </Typography>
            
            <Box display="flex" alignItems="center" justifyContent="space-between">
              <Box display="flex" alignItems="center">
                <Security sx={{ mr: 2, color: 'text.secondary' }} />
                <Box>
                  <Typography variant="body1">
                    Change Password
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Update your account password
                  </Typography>
                </Box>
              </Box>
              
              <Button
                variant="outlined"
                onClick={() => navigate('/change-password')}
              >
                Change
              </Button>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Profile; 