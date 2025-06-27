import React, { useState, useEffect } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Container,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Box,
  Chip,
  Paper,
  useTheme,
  useMediaQuery
} from '@mui/material';
import {
  Event as EventIcon,
  LocationOn as LocationIcon,
  AccessTime as TimeIcon,
  ArrowForward as ArrowIcon
} from '@mui/icons-material';
import { format } from 'date-fns';
import axios from 'axios';

const Home = () => {
  const [featuredEvents, setFeaturedEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  useEffect(() => {
    const fetchFeaturedEvents = async () => {
      try {
        const response = await axios.get('/api/events/public/upcoming');
        setFeaturedEvents(response.data.slice(0, 3)); // Show only 3 events
      } catch (error) {
        console.error('Failed to fetch featured events:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchFeaturedEvents();
  }, []);

  const renderHeroSection = () => (
    <Paper
      sx={{
        position: 'relative',
        backgroundColor: 'grey.800',
        color: 'white',
        mb: 4,
        backgroundSize: 'cover',
        backgroundRepeat: 'no-repeat',
        backgroundPosition: 'center',
        backgroundImage: 'url(https://images.unsplash.com/photo-1540575467063-178a50c2df87?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80)',
        minHeight: '60vh',
        display: 'flex',
        alignItems: 'center'
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          top: 0,
          bottom: 0,
          right: 0,
          left: 0,
          backgroundColor: 'rgba(0,0,0,.3)',
        }}
      />
      <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 1 }}>
        <Grid container spacing={4}>
          <Grid item xs={12} md={8}>
            <Typography
              component="h1"
              variant={isMobile ? "h3" : "h2"}
              color="inherit"
              gutterBottom
              sx={{ fontWeight: 'bold' }}
            >
              Discover Amazing Events
            </Typography>
            <Typography variant="h5" color="inherit" paragraph>
              Join thousands of people at the most exciting events in your area. 
              From concerts to conferences, find your next unforgettable experience.
            </Typography>
            <Box sx={{ mt: 4, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Button
                variant="contained"
                size="large"
                component={RouterLink}
                to="/events"
                startIcon={<EventIcon />}
                sx={{ 
                  bgcolor: 'primary.main',
                  '&:hover': { bgcolor: 'primary.dark' }
                }}
              >
                Browse Events
              </Button>
              <Button
                variant="outlined"
                size="large"
                component={RouterLink}
                to="/register"
                sx={{ 
                  color: 'white', 
                  borderColor: 'white',
                  '&:hover': { 
                    borderColor: 'white',
                    bgcolor: 'rgba(255,255,255,0.1)'
                  }
                }}
              >
                Get Started
              </Button>
            </Box>
          </Grid>
        </Grid>
      </Container>
    </Paper>
  );

  const renderFeatureSection = () => (
    <Container maxWidth="lg" sx={{ mb: 6 }}>
      <Typography variant="h4" component="h2" gutterBottom align="center" sx={{ mb: 4 }}>
        Why Choose Our Platform?
      </Typography>
      <Grid container spacing={4}>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', textAlign: 'center' }}>
            <CardContent>
              <EventIcon sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                Easy Registration
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Simple and secure ticket purchasing with instant confirmation and QR codes.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', textAlign: 'center' }}>
            <CardContent>
              <LocationIcon sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                Real-time Updates
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Get instant notifications about event changes and ticket availability.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', textAlign: 'center' }}>
            <CardContent>
              <TimeIcon sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                Secure Validation
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Advanced QR code system for quick and secure ticket validation at events.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );

  const renderFeaturedEvents = () => (
    <Container maxWidth="lg" sx={{ mb: 6 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h2">
          Featured Events
        </Typography>
        <Button
          component={RouterLink}
          to="/events"
          endIcon={<ArrowIcon />}
          sx={{ display: { xs: 'none', md: 'flex' } }}
        >
          View All Events
        </Button>
      </Box>
      
      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3].map((item) => (
            <Grid item xs={12} md={4} key={item}>
              <Card sx={{ height: 400 }}>
                <Box sx={{ height: 200, bgcolor: 'grey.200' }} />
                <CardContent>
                  <Box sx={{ height: 20, bgcolor: 'grey.200', mb: 1 }} />
                  <Box sx={{ height: 16, bgcolor: 'grey.200', mb: 1 }} />
                  <Box sx={{ height: 16, bgcolor: 'grey.200', width: '60%' }} />
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Grid container spacing={3}>
          {featuredEvents.map((event) => (
            <Grid item xs={12} md={4} key={event.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardMedia
                  component="img"
                  height="200"
                  image={event.imageUrl || 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?ixlib=rb-4.0.3&auto=format&fit=crop&w=500&q=80'}
                  alt={event.title}
                />
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" gutterBottom>
                    {event.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" paragraph>
                    {event.description?.substring(0, 100)}...
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <LocationIcon sx={{ fontSize: 16, mr: 0.5, color: 'text.secondary' }} />
                    <Typography variant="body2" color="text.secondary">
                      {event.location}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <TimeIcon sx={{ fontSize: 16, mr: 0.5, color: 'text.secondary' }} />
                    <Typography variant="body2" color="text.secondary">
                      {format(new Date(event.startDateTime), 'MMM dd, yyyy HH:mm')}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Chip 
                      label={`$${event.price}`} 
                      color="primary" 
                      size="small" 
                    />
                    <Chip 
                      label={`${event.availableTickets} tickets left`} 
                      color={event.availableTickets < 10 ? "error" : "success"}
                      size="small" 
                    />
                  </Box>
                </CardContent>
                <CardActions>
                  <Button 
                    size="small" 
                    component={RouterLink}
                    to={`/events/${event.id}`}
                    fullWidth
                  >
                    View Details
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
      
      {!isMobile && (
        <Box sx={{ textAlign: 'center', mt: 4 }}>
          <Button
            variant="outlined"
            size="large"
            component={RouterLink}
            to="/events"
            endIcon={<ArrowIcon />}
          >
            View All Events
          </Button>
        </Box>
      )}
    </Container>
  );

  return (
    <Box>
      {renderHeroSection()}
      {renderFeatureSection()}
      {renderFeaturedEvents()}
    </Box>
  );
};

export default Home; 