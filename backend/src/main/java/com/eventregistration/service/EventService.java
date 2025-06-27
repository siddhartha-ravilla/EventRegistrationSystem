package com.eventregistration.service;

import com.eventregistration.dto.EventRequest;
import com.eventregistration.model.Event;
import com.eventregistration.model.User;
import com.eventregistration.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public Event createEvent(EventRequest request, User organizer) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setCapacity(request.getCapacity());
        event.setAvailableTickets(request.getCapacity());
        event.setPrice(request.getPrice());
        event.setImageUrl(request.getImageUrl());
        event.setCategory(request.getCategory());
        event.setOrganizer(organizer);
        event.setStatus(Event.EventStatus.DRAFT);
        
        return eventRepository.save(event);
    }
    
    @Transactional
    public Event updateEvent(Long eventId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setCapacity(request.getCapacity());
        event.setPrice(request.getPrice());
        event.setImageUrl(request.getImageUrl());
        event.setCategory(request.getCategory());
        
        return eventRepository.save(event);
    }
    
    @Transactional
    public Event publishEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        event.setStatus(Event.EventStatus.PUBLISHED);
        Event savedEvent = eventRepository.save(event);
        
        // Send notification
        notificationService.sendEventUpdateNotification(
            event.getTitle(), 
            "Event has been published and is now available for registration"
        );
        
        return savedEvent;
    }
    
    @Transactional
    public Event cancelEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        event.setStatus(Event.EventStatus.CANCELLED);
        Event savedEvent = eventRepository.save(event);
        
        // Send notification
        notificationService.sendEventUpdateNotification(
            event.getTitle(), 
            "Event has been cancelled"
        );
        
        return savedEvent;
    }
    
    @Transactional
    public boolean reserveTicket(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        if (event.getAvailableTickets() > 0) {
            event.setAvailableTickets(event.getAvailableTickets() - 1);
            eventRepository.save(event);
            return true;
        }
        return false;
    }
    
    @Transactional
    public void releaseTicket(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        if (event.getAvailableTickets() < event.getCapacity()) {
            event.setAvailableTickets(event.getAvailableTickets() + 1);
            eventRepository.save(event);
        }
    }
    
    public Optional<Event> findById(Long eventId) {
        return eventRepository.findById(eventId);
    }
    
    public List<Event> findAvailableEvents() {
        return eventRepository.findAvailableEvents();
    }
    
    public List<Event> findUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now());
    }
    
    public List<Event> searchEvents(String keyword) {
        return eventRepository.searchEvents(keyword);
    }
    
    public List<Event> findEventsByCategory(String category) {
        return eventRepository.findByCategory(category);
    }
    
    public Page<Event> findEventsByStatus(Event.EventStatus status, Pageable pageable) {
        return eventRepository.findByStatus(status, pageable);
    }
    
    public Page<Event> findEventsByOrganizer(User organizer, Pageable pageable) {
        return eventRepository.findByOrganizer(organizer, pageable);
    }
    
    public List<Event> findEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findByStartDateTimeBetween(start, end);
    }
    
    public List<Event> findSoldOutEvents() {
        return eventRepository.findSoldOutEvents();
    }
    
    public long countEventsByOrganizer(User organizer) {
        return eventRepository.countEventsByOrganizer(organizer);
    }
    
    public boolean isEventAvailable(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        return event.isPresent() && 
               event.get().getStatus() == Event.EventStatus.PUBLISHED && 
               event.get().getAvailableTickets() > 0;
    }
    
    public boolean isEventSoldOut(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        return event.isPresent() && event.get().getAvailableTickets() == 0;
    }
} 