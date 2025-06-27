package com.eventregistration.controller;

import com.eventregistration.dto.EventRequest;
import com.eventregistration.model.Event;
import com.eventregistration.model.User;
import com.eventregistration.service.EventService;
import com.eventregistration.service.JwtService;
import com.eventregistration.service.UserService;
import jakarta.validation.Valid;
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
import java.util.Optional;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "*")
public class EventController {
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    // Public endpoints
    @GetMapping("/public/available")
    public ResponseEntity<List<Event>> getAvailableEvents() {
        List<Event> events = eventService.findAvailableEvents();
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        List<Event> events = eventService.findUpcomingEvents();
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String keyword) {
        List<Event> events = eventService.searchEvents(keyword);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/category/{category}")
    public ResponseEntity<List<Event>> getEventsByCategory(@PathVariable String category) {
        List<Event> events = eventService.findEventsByCategory(category);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/public/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventService.findById(id);
        return event.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Protected endpoints
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventRequest request,
                                       @RequestHeader("Authorization") String token) {
        try {
            User organizer = getCurrentUser(token);
            Event event = eventService.createEvent(request, organizer);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id,
                                       @Valid @RequestBody EventRequest request,
                                       @RequestHeader("Authorization") String token) {
        try {
            User currentUser = getCurrentUser(token);
            Event existingEvent = eventService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            
            // Check if user is the organizer or admin
            if (!existingEvent.getOrganizer().getId().equals(currentUser.getId()) && 
                currentUser.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized to update this event"));
            }
            
            Event updatedEvent = eventService.updateEvent(id, request);
            return ResponseEntity.ok(updatedEvent);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> publishEvent(@PathVariable Long id,
                                        @RequestHeader("Authorization") String token) {
        try {
            User currentUser = getCurrentUser(token);
            Event existingEvent = eventService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            
            // Check if user is the organizer or admin
            if (!existingEvent.getOrganizer().getId().equals(currentUser.getId()) && 
                currentUser.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized to publish this event"));
            }
            
            Event publishedEvent = eventService.publishEvent(id);
            return ResponseEntity.ok(publishedEvent);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> cancelEvent(@PathVariable Long id,
                                       @RequestHeader("Authorization") String token) {
        try {
            User currentUser = getCurrentUser(token);
            Event existingEvent = eventService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            
            // Check if user is the organizer or admin
            if (!existingEvent.getOrganizer().getId().equals(currentUser.getId()) && 
                currentUser.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized to cancel this event"));
            }
            
            Event cancelledEvent = eventService.cancelEvent(id);
            return ResponseEntity.ok(cancelledEvent);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Page<Event>> getMyEvents(@RequestHeader("Authorization") String token,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        User currentUser = getCurrentUser(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventService.findEventsByOrganizer(currentUser, pageable);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Event>> getEventsByStatus(@PathVariable String status,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Event.EventStatus eventStatus = Event.EventStatus.valueOf(status.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventService.findEventsByStatus(eventStatus, pageable);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/sold-out")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Event>> getSoldOutEvents() {
        List<Event> events = eventService.findSoldOutEvents();
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("available", eventService.isEventAvailable(id));
        response.put("soldOut", eventService.isEventSoldOut(id));
        return ResponseEntity.ok(response);
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