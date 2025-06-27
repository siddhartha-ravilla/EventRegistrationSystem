package com.eventregistration.controller;

import com.eventregistration.model.Ticket;
import com.eventregistration.model.User;
import com.eventregistration.service.JwtService;
import com.eventregistration.service.TicketService;
import com.eventregistration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tickets")
@CrossOrigin(origins = "*")
public class TicketController {
    
    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/purchase/{eventId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> purchaseTicket(@PathVariable Long eventId,
                                          @RequestHeader("Authorization") String token) {
        try {
            User currentUser = getCurrentUser(token);
            Ticket ticket = ticketService.purchaseTicket(eventId, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket purchased successfully");
            response.put("ticketNumber", ticket.getTicketNumber());
            response.put("qrCode", ticket.getQrCode());
            response.put("status", ticket.getStatus());
            response.put("purchasedAt", ticket.getPurchasedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> validateTicket(@RequestBody Map<String, String> request,
                                          @RequestHeader("Authorization") String token) {
        try {
            String qrCode = request.get("qrCode");
            User validator = getCurrentUser(token);
            String validatorName = validator.getFirstName() + " " + validator.getLastName();
            
            Ticket validatedTicket = ticketService.validateTicket(qrCode, validatorName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket validated successfully");
            response.put("ticketNumber", validatedTicket.getTicketNumber());
            response.put("validatedAt", validatedTicket.getValidatedAt());
            response.put("validatedBy", validatedTicket.getValidatedBy());
            response.put("eventTitle", validatedTicket.getEvent().getTitle());
            response.put("userName", validatedTicket.getUser().getFirstName() + " " + validatedTicket.getUser().getLastName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{ticketId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelTicket(@PathVariable Long ticketId,
                                        @RequestHeader("Authorization") String token) {
        try {
            User currentUser = getCurrentUser(token);
            Ticket cancelledTicket = ticketService.cancelTicket(ticketId, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket cancelled successfully");
            response.put("ticketNumber", cancelledTicket.getTicketNumber());
            response.put("status", cancelledTicket.getStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/my-tickets")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Ticket>> getMyTickets(@RequestHeader("Authorization") String token,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        User currentUser = getCurrentUser(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> tickets = ticketService.findByUser(currentUser, pageable);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/my-upcoming-tickets")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Ticket>> getMyUpcomingTickets(@RequestHeader("Authorization") String token) {
        User currentUser = getCurrentUser(token);
        List<Ticket> tickets = ticketService.findUpcomingTicketsByUser(currentUser);
        return ResponseEntity.ok(tickets);
    }
    
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Ticket>> getTicketsByEvent(@PathVariable Long eventId) {
        // This would need to be implemented with EventService to get the Event object
        // For now, returning empty list as placeholder
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/validate/{qrCode}")
    public ResponseEntity<?> checkTicketValidity(@PathVariable String qrCode) {
        try {
            boolean isValid = ticketService.isTicketValid(qrCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                Ticket ticket = ticketService.findByQrCode(qrCode).orElse(null);
                if (ticket != null) {
                    response.put("ticketNumber", ticket.getTicketNumber());
                    response.put("eventTitle", ticket.getEvent().getTitle());
                    response.put("userName", ticket.getUser().getFirstName() + " " + ticket.getUser().getLastName());
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/{ticketNumber}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTicketByNumber(@PathVariable String ticketNumber,
                                             @RequestHeader("Authorization") String token) {
        try {
            User currentUser = getCurrentUser(token);
            Ticket ticket = ticketService.findByTicketNumber(ticketNumber)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));
            
            // Check if user owns the ticket or is admin
            if (!ticket.getUser().getId().equals(currentUser.getId()) && 
                currentUser.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized to view this ticket"));
            }
            
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    private User getCurrentUser(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            if (jwtService.validateJwtToken(jwt)) {
                String username = jwtService.getUserNameFromJwtToken(jwt);
                return userService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }
        }
        throw new RuntimeException("Invalid token");
    }
} 