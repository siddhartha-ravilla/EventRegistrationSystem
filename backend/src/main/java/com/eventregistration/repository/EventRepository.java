package com.eventregistration.repository;

import com.eventregistration.model.Event;
import com.eventregistration.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    Page<Event> findByStatus(Event.EventStatus status, Pageable pageable);
    
    Page<Event> findByOrganizer(User organizer, Pageable pageable);
    
    List<Event> findByStartDateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<Event> findByCategory(String category);
    
    @Query("SELECT e FROM Event e WHERE e.availableTickets > 0 AND e.status = 'PUBLISHED'")
    List<Event> findAvailableEvents();
    
    @Query("SELECT e FROM Event e WHERE e.title LIKE %:keyword% OR e.description LIKE %:keyword%")
    List<Event> searchEvents(@Param("keyword") String keyword);
    
    @Query("SELECT e FROM Event e WHERE e.startDateTime >= :now AND e.status = 'PUBLISHED' ORDER BY e.startDateTime")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer = :organizer")
    long countEventsByOrganizer(@Param("organizer") User organizer);
    
    @Query("SELECT e FROM Event e WHERE e.availableTickets = 0 AND e.status = 'PUBLISHED'")
    List<Event> findSoldOutEvents();
} 