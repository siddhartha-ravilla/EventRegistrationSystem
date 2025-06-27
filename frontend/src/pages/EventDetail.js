import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Button,
  Box,
  Grid,
  Chip,
  Divider,
  Alert,
  CircularProgress,
  Card,
  CardContent,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Stepper,
  Step,
  StepLabel
} from '@mui/material';
import {
  LocationOn,
  CalendarToday,
  AccessTime,
  People,
  AttachMoney,
  Bookmark,
  Share,
  QrCode
} from '@mui/icons-material';
import { format } from 'date-fns';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';

const EventDetail = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [bookingDialog, setBookingDialog] = useState(false);
  const [ticketQuantity, setTicketQuantity] = useState(1);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [activeStep, setActiveStep] = useState(0);
  const [ticket, setTicket] = useState(null);

  useEffect(() => {
    fetchEventDetails();
  }, [eventId]);

  const fetchEventDetails = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/api/events/${eventId}`);
      setEvent(response.data);
    } catch (err) {
      setError('Failed to load event details');
      console.error('Error fetching event:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleBookTicket = () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/events/${eventId}` } });
      return;
    }
    setBookingDialog(true);
  };

  const handleBookingSubmit = async () => {
    try {
      setBookingLoading(true);
      const response = await axios.post(`/api/tickets/book`, {
        eventId: event.id,
        quantity: ticketQuantity
      }, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      setTicket(response.data);
      setActiveStep(1);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to book ticket');
    } finally {
      setBookingLoading(false);
    }
  };

  const handleCloseDialog = () => {
    setBookingDialog(false);
    setActiveStep(0);
    setTicketQuantity(1);
    setTicket(null);
  };

  const steps = ['Select Tickets', 'Confirmation'];

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error || !event) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Alert severity="error">{error || 'Event not found'}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Grid container spacing={4}>
        {/* Event Image */}
        <Grid item xs={12} md={6}>
          <Paper
            sx={{
              height: 400,
              backgroundImage: `url(${event.imageUrl || 'https://via.placeholder.com/600x400?text=Event+Image'})`,
              backgroundSize: 'cover',
              backgroundPosition: 'center',
              borderRadius: 2
            }}
          />
        </Grid>

        {/* Event Details */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3, height: 'fit-content' }}>
            <Typography variant="h4" gutterBottom>
              {event.title}
            </Typography>
            
            <Box sx={{ mb: 2 }}>
              <Chip 
                label={event.category} 
                color="primary" 
                size="small" 
                sx={{ mr: 1 }}
              />
              <Chip 
                label={event.status} 
                color={event.status === 'ACTIVE' ? 'success' : 'default'}
                size="small"
              />
            </Box>

            <Typography variant="body1" color="text.secondary" paragraph>
              {event.description}
            </Typography>

            <Divider sx={{ my: 2 }} />

            <Box sx={{ mb: 2 }}>
              <Box display="flex" alignItems="center" mb={1}>
                <CalendarToday sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="body2">
                  {format(new Date(event.date), 'EEEE, MMMM do, yyyy')}
                </Typography>
              </Box>
              
              <Box display="flex" alignItems="center" mb={1}>
                <AccessTime sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="body2">
                  {format(new Date(event.date), 'h:mm a')}
                </Typography>
              </Box>
              
              <Box display="flex" alignItems="center" mb={1}>
                <LocationOn sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="body2">
                  {event.venue}
                </Typography>
              </Box>
              
              <Box display="flex" alignItems="center" mb={1}>
                <People sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="body2">
                  {event.availableTickets} tickets available
                </Typography>
              </Box>
              
              <Box display="flex" alignItems="center">
                <AttachMoney sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6" color="primary">
                  ${event.price}
                </Typography>
              </Box>
            </Box>

            <Box sx={{ mt: 3 }}>
              <Button
                variant="contained"
                size="large"
                fullWidth
                onClick={handleBookTicket}
                disabled={event.availableTickets === 0}
                sx={{ mb: 2 }}
              >
                {event.availableTickets === 0 ? 'Sold Out' : 'Book Tickets'}
              </Button>
              
              <Box display="flex" gap={1}>
                <Button
                  variant="outlined"
                  startIcon={<Bookmark />}
                  fullWidth
                >
                  Save
                </Button>
                <Button
                  variant="outlined"
                  startIcon={<Share />}
                  fullWidth
                >
                  Share
                </Button>
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>

      {/* Booking Dialog */}
      <Dialog 
        open={bookingDialog} 
        onClose={handleCloseDialog}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Book Tickets</DialogTitle>
        <DialogContent>
          <Stepper activeStep={activeStep} sx={{ mb: 3 }}>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>

          {activeStep === 0 && (
            <Box>
              <Typography variant="h6" gutterBottom>
                {event.title}
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                {format(new Date(event.date), 'EEEE, MMMM do, yyyy')} at {format(new Date(event.date), 'h:mm a')}
              </Typography>
              
              <TextField
                type="number"
                label="Number of Tickets"
                value={ticketQuantity}
                onChange={(e) => setTicketQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                inputProps={{ min: 1, max: event.availableTickets }}
                fullWidth
                sx={{ mb: 2 }}
              />
              
              <Typography variant="h6">
                Total: ${(event.price * ticketQuantity).toFixed(2)}
              </Typography>
            </Box>
          )}

          {activeStep === 1 && ticket && (
            <Box textAlign="center">
              <Typography variant="h6" gutterBottom>
                Booking Confirmed!
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                Your ticket has been booked successfully.
              </Typography>
              
              <Card sx={{ mb: 2 }}>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Ticket #{ticket.id}
                  </Typography>
                  <Typography variant="body2">
                    Quantity: {ticket.quantity}
                  </Typography>
                  <Typography variant="body2">
                    Total: ${ticket.totalAmount}
                  </Typography>
                </CardContent>
              </Card>
              
              <Box display="flex" justifyContent="center">
                <QrCode sx={{ fontSize: 100, color: 'primary.main' }} />
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          {activeStep === 0 && (
            <>
              <Button onClick={handleCloseDialog}>Cancel</Button>
              <Button 
                onClick={handleBookingSubmit}
                variant="contained"
                disabled={bookingLoading}
              >
                {bookingLoading ? <CircularProgress size={20} /> : 'Confirm Booking'}
              </Button>
            </>
          )}
          {activeStep === 1 && (
            <Button onClick={handleCloseDialog} variant="contained">
              Done
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default EventDetail; 