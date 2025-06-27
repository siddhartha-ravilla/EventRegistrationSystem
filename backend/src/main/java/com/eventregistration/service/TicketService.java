package com.eventregistration.service;

import com.eventregistration.model.Event;
import com.eventregistration.model.Ticket;
import com.eventregistration.model.User;
import com.eventregistration.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private QRCodeService qrCodeService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public Ticket purchaseTicket(Long eventId, User user) {
        // Check if event is available
        if (!eventService.isEventAvailable(eventId)) {
            throw new RuntimeException("Event is not available or sold out");
        }
        
        // Check if user already has a ticket for this event
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        List<Ticket> existingTickets = ticketRepository.findTicketsByEventAndUser(event, user);
        if (!existingTickets.isEmpty()) {
            throw new RuntimeException("User already has a ticket for this event");
        }
        
        // Reserve ticket
        if (!eventService.reserveTicket(eventId)) {
            throw new RuntimeException("Failed to reserve ticket - event may be sold out");
        }
        
        try {
            // Create ticket
            Ticket ticket = new Ticket(event, user);
            ticket = ticketRepository.save(ticket);
            
            // Generate QR code
            String qrCodeData = qrCodeService.generateTicketQRCode(
                ticket.getTicketNumber(), 
                event.getId().toString(), 
                user.getId().toString()
            );
            
            // Send confirmation email with QR code
            notificationService.sendTicketConfirmationEmail(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                event.getTitle(),
                ticket.getTicketNumber(),
                qrCodeData
            );
            
            return ticket;
            
        } catch (Exception e) {
            // Release the reserved ticket if something goes wrong
            eventService.releaseTicket(eventId);
            throw new RuntimeException("Failed to create ticket", e);
        }
    }
    
    @Transactional
    public Ticket validateTicket(String qrCode, String validatorName) {
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Invalid QR code"));
        
        if (ticket.getStatus() != Ticket.TicketStatus.ACTIVE) {
            throw new RuntimeException("Ticket is not active");
        }
        
        // Check if event has started
        Event event = ticket.getEvent();
        if (LocalDateTime.now().isBefore(event.getStartDateTime())) {
            throw new RuntimeException("Event has not started yet");
        }
        
        if (LocalDateTime.now().isAfter(event.getEndDateTime())) {
            throw new RuntimeException("Event has already ended");
        }
        
        // Validate ticket
        ticket.setStatus(Ticket.TicketStatus.VALIDATED);
        ticket.setValidatedAt(LocalDateTime.now());
        ticket.setValidatedBy(validatorName);
        
        return ticketRepository.save(ticket);
    }
    
    @Transactional
    public Ticket cancelTicket(Long ticketId, User user) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        // Check if user owns the ticket or is admin
        if (!ticket.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Not authorized to cancel this ticket");
        }
        
        if (ticket.getStatus() != Ticket.TicketStatus.ACTIVE) {
            throw new RuntimeException("Ticket cannot be cancelled");
        }
        
        // Check if event has started
        Event event = ticket.getEvent();
        if (LocalDateTime.now().isAfter(event.getStartDateTime())) {
            throw new RuntimeException("Cannot cancel ticket after event has started");
        }
        
        // Cancel ticket and release capacity
        ticket.setStatus(Ticket.TicketStatus.CANCELLED);
        ticketRepository.save(ticket);
        eventService.releaseTicket(event.getId());
        
        return ticket;
    }
    
    public Optional<Ticket> findByTicketNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }
    
    public Optional<Ticket> findByQrCode(String qrCode) {
        return ticketRepository.findByQrCode(qrCode);
    }
    
    public List<Ticket> findByUser(User user) {
        return ticketRepository.findByUser(user);
    }
    
    public Page<Ticket> findByUser(User user, Pageable pageable) {
        return ticketRepository.findByUser(user, pageable);
    }
    
    public List<Ticket> findByEvent(Event event) {
        return ticketRepository.findByEvent(event);
    }
    
    public List<Ticket> findActiveTicketsByEvent(Event event) {
        return ticketRepository.findActiveTicketsByEvent(event);
    }
    
    public List<Ticket> findUpcomingTicketsByUser(User user) {
        return ticketRepository.findUpcomingTicketsByUser(user, LocalDateTime.now());
    }
    
    public long countValidatedTicketsByEvent(Event event) {
        return ticketRepository.countValidatedTicketsByEvent(event);
    }
    
    public long countTicketsPurchasedAfter(LocalDateTime startDate) {
        return ticketRepository.countTicketsPurchasedAfter(startDate);
    }
    
    public List<Ticket> findTicketsValidatedAfter(LocalDateTime startDate) {
        return ticketRepository.findTicketsValidatedAfter(startDate);
    }
    
    public boolean isTicketValid(String qrCode) {
        Optional<Ticket> ticket = ticketRepository.findByQrCode(qrCode);
        if (ticket.isEmpty()) {
            return false;
        }
        
        Ticket t = ticket.get();
        return t.getStatus() == Ticket.TicketStatus.ACTIVE &&
               LocalDateTime.now().isAfter(t.getEvent().getStartDateTime()) &&
               LocalDateTime.now().isBefore(t.getEvent().getEndDateTime());
    }
} 