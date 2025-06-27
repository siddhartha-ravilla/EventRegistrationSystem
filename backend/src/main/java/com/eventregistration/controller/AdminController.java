package com.eventregistration.controller;

import com.eventregistration.model.User;
import com.eventregistration.service.EventService;
import com.eventregistration.service.TicketService;
import com.eventregistration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private TicketService ticketService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // User statistics
        long totalUsers = userService.findAllUsers().size();
        long totalAdmins = userService.findAllAdmins().size();
        long newUsersThisMonth = userService.countUsersRegisteredAfter(
            LocalDateTime.now().minusMonths(1)
        );
        
        // Event statistics
        long totalEvents = eventService.findAvailableEvents().size();
        long soldOutEvents = eventService.findSoldOutEvents().size();
        long upcomingEvents = eventService.findUpcomingEvents().size();
        
        // Ticket statistics
        long totalTickets = ticketService.countTicketsPurchasedAfter(
            LocalDateTime.now().minusYears(10)
        );
        long ticketsThisMonth = ticketService.countTicketsPurchasedAfter(
            LocalDateTime.now().minusMonths(1)
        );
        long validatedTicketsToday = ticketService.findTicketsValidatedAfter(
            LocalDateTime.now().minusDays(1)
        ).size();
        
        stats.put("users", Map.of(
            "total", totalUsers,
            "admins", totalAdmins,
            "newThisMonth", newUsersThisMonth
        ));
        
        stats.put("events", Map.of(
            "total", totalEvents,
            "soldOut", soldOutEvents,
            "upcoming", upcomingEvents
        ));
        
        stats.put("tickets", Map.of(
            "total", totalTickets,
            "thisMonth", ticketsThisMonth,
            "validatedToday", validatedTicketsToday
        ));
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        List<User> users = userService.findUsersByRole(userRole);
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        try {
            userService.toggleUserStatus(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User status toggled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/users/{userId}/change-password")
    public ResponseEntity<?> changeUserPassword(@PathVariable Long userId,
                                              @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            if (newPassword == null || newPassword.length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters long");
            }
            
            userService.changePassword(userId, newPassword);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/analytics/events")
    public ResponseEntity<Map<String, Object>> getEventAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Event statistics by status
        long publishedEvents = eventService.findEventsByStatus(
            com.eventregistration.model.Event.EventStatus.PUBLISHED, 
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getTotalElements();
        
        long draftEvents = eventService.findEventsByStatus(
            com.eventregistration.model.Event.EventStatus.DRAFT, 
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getTotalElements();
        
        long cancelledEvents = eventService.findEventsByStatus(
            com.eventregistration.model.Event.EventStatus.CANCELLED, 
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getTotalElements();
        
        analytics.put("byStatus", Map.of(
            "published", publishedEvents,
            "draft", draftEvents,
            "cancelled", cancelledEvents
        ));
        
        // Recent activity
        List<com.eventregistration.model.Event> upcomingEvents = eventService.findUpcomingEvents();
        List<com.eventregistration.model.Event> soldOutEvents = eventService.findSoldOutEvents();
        
        analytics.put("recentActivity", Map.of(
            "upcomingEvents", upcomingEvents.size(),
            "soldOutEvents", soldOutEvents.size()
        ));
        
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/analytics/tickets")
    public ResponseEntity<Map<String, Object>> getTicketAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Ticket statistics
        long totalTickets = ticketService.countTicketsPurchasedAfter(
            LocalDateTime.now().minusYears(10)
        );
        
        long ticketsThisWeek = ticketService.countTicketsPurchasedAfter(
            LocalDateTime.now().minusWeeks(1)
        );
        
        long ticketsThisMonth = ticketService.countTicketsPurchasedAfter(
            LocalDateTime.now().minusMonths(1)
        );
        
        List<com.eventregistration.model.Ticket> validatedToday = ticketService.findTicketsValidatedAfter(
            LocalDateTime.now().minusDays(1)
        );
        
        analytics.put("purchases", Map.of(
            "total", totalTickets,
            "thisWeek", ticketsThisWeek,
            "thisMonth", ticketsThisMonth
        ));
        
        analytics.put("validations", Map.of(
            "today", validatedToday.size()
        ));
        
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/system/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Basic health checks
            userService.findAllUsers(); // Test database connection
            eventService.findAvailableEvents();
            ticketService.countTicketsPurchasedAfter(LocalDateTime.now().minusYears(10));
            
            health.put("status", "healthy");
            health.put("database", "connected");
            health.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());
        }
        
        return ResponseEntity.ok(health);
    }
} 