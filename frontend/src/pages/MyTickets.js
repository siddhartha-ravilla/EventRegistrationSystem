import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Card,
  CardContent,
  Grid,
  Chip,
  Button,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Paper,
  Divider
} from '@mui/material';
import {
  QrCode,
  Event,
  LocationOn,
  CalendarToday,
  AccessTime,
  Download,
  Share
} from '@mui/icons-material';
import { format } from 'date-fns';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';

const MyTickets = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [qrDialog, setQrDialog] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    fetchTickets();
  }, [isAuthenticated, navigate]);

  const fetchTickets = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/tickets/my-tickets', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      setTickets(response.data);
    } catch (err) {
      setError('Failed to load tickets');
      console.error('Error fetching tickets:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleViewQR = (ticket) => {
    setSelectedTicket(ticket);
    setQrDialog(true);
  };

  const handleDownloadTicket = (ticket) => {
    // Generate PDF or image for download
    const ticketData = {
      id: ticket.id,
      eventTitle: ticket.event.title,
      date: format(new Date(ticket.event.date), 'EEEE, MMMM do, yyyy'),
      time: format(new Date(ticket.event.date), 'h:mm a'),
      venue: ticket.event.venue,
      quantity: ticket.quantity,
      totalAmount: ticket.totalAmount
    };
    
    // Create a simple text representation for now
    const ticketText = `
TICKET #${ticketData.id}
Event: ${ticketData.eventTitle}
Date: ${ticketData.date}
Time: ${ticketData.time}
Venue: ${ticketData.venue}
Quantity: ${ticketData.quantity}
Total: $${ticketData.totalAmount}
    `;
    
    const blob = new Blob([ticketText], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ticket-${ticket.id}.txt`;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  if (!isAuthenticated) {
    return null;
  }

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        My Tickets
      </Typography>
      
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        View and manage your booked tickets
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {tickets.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            No tickets found
          </Typography>
          <Typography variant="body2" color="text.secondary" paragraph>
            You haven't booked any tickets yet.
          </Typography>
          <Button
            variant="contained"
            onClick={() => navigate('/events')}
          >
            Browse Events
          </Button>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {tickets.map((ticket) => (
            <Grid item xs={12} md={6} lg={4} key={ticket.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                    <Typography variant="h6" component="h2" noWrap>
                      {ticket.event.title}
                    </Typography>
                    <Chip
                      label={ticket.status}
                      color={getStatusColor(ticket.status)}
                      size="small"
                    />
                  </Box>

                  <Box sx={{ mb: 2 }}>
                    <Box display="flex" alignItems="center" mb={1}>
                      <CalendarToday sx={{ mr: 1, fontSize: 16, color: 'text.secondary' }} />
                      <Typography variant="body2" color="text.secondary">
                        {format(new Date(ticket.event.date), 'MMM do, yyyy')}
                      </Typography>
                    </Box>
                    
                    <Box display="flex" alignItems="center" mb={1}>
                      <AccessTime sx={{ mr: 1, fontSize: 16, color: 'text.secondary' }} />
                      <Typography variant="body2" color="text.secondary">
                        {format(new Date(ticket.event.date), 'h:mm a')}
                      </Typography>
                    </Box>
                    
                    <Box display="flex" alignItems="center" mb={1}>
                      <LocationOn sx={{ mr: 1, fontSize: 16, color: 'text.secondary' }} />
                      <Typography variant="body2" color="text.secondary" noWrap>
                        {ticket.event.venue}
                      </Typography>
                    </Box>
                  </Box>

                  <Divider sx={{ my: 2 }} />

                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="body2">
                      Quantity: {ticket.quantity}
                    </Typography>
                    <Typography variant="h6" color="primary">
                      ${ticket.totalAmount}
                    </Typography>
                  </Box>

                  <Typography variant="caption" color="text.secondary">
                    Ticket ID: {ticket.id}
                  </Typography>
                </CardContent>

                <Box sx={{ p: 2, pt: 0 }}>
                  <Grid container spacing={1}>
                    <Grid item xs={4}>
                      <Button
                        size="small"
                        variant="outlined"
                        startIcon={<QrCode />}
                        onClick={() => handleViewQR(ticket)}
                        fullWidth
                      >
                        QR
                      </Button>
                    </Grid>
                    <Grid item xs={4}>
                      <Button
                        size="small"
                        variant="outlined"
                        startIcon={<Download />}
                        onClick={() => handleDownloadTicket(ticket)}
                        fullWidth
                      >
                        Download
                      </Button>
                    </Grid>
                    <Grid item xs={4}>
                      <Button
                        size="small"
                        variant="outlined"
                        startIcon={<Share />}
                        fullWidth
                      >
                        Share
                      </Button>
                    </Grid>
                  </Grid>
                </Box>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* QR Code Dialog */}
      <Dialog
        open={qrDialog}
        onClose={() => setQrDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          QR Code - {selectedTicket?.event?.title}
        </DialogTitle>
        <DialogContent>
          {selectedTicket && (
            <Box textAlign="center">
              <Paper sx={{ p: 3, mb: 2 }}>
                <QrCode sx={{ fontSize: 200, color: 'primary.main' }} />
              </Paper>
              <Typography variant="body2" color="text.secondary">
                Ticket ID: {selectedTicket.id}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Event: {selectedTicket.event.title}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Date: {format(new Date(selectedTicket.event.date), 'EEEE, MMMM do, yyyy')}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setQrDialog(false)}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default MyTickets; 