package com.eventregistration.repository;

import com.eventregistration.model.Event;
import com.eventregistration.model.Ticket;
import com.eventregistration.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    List<Ticket> findByUser(User user);
    
    List<Ticket> findByEvent(Event event);
    
    Page<Ticket> findByUser(User user, Pageable pageable);
    
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    
    Optional<Ticket> findByQrCode(String qrCode);
    
    List<Ticket> findByStatus(Ticket.TicketStatus status);
    
    @Query("SELECT t FROM Ticket t WHERE t.event = :event AND t.status = 'ACTIVE'")
    List<Ticket> findActiveTicketsByEvent(@Param("event") Event event);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event = :event AND t.status = 'VALIDATED'")
    long countValidatedTicketsByEvent(@Param("event") Event event);
    
    @Query("SELECT t FROM Ticket t WHERE t.user = :user AND t.event.startDateTime >= :now")
    List<Ticket> findUpcomingTicketsByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Ticket t WHERE t.event = :event AND t.user = :user")
    List<Ticket> findTicketsByEventAndUser(@Param("event") Event event, @Param("user") User user);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.purchasedAt >= :startDate")
    long countTicketsPurchasedAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT t FROM Ticket t WHERE t.validatedAt IS NOT NULL AND t.validatedAt >= :startDate")
    List<Ticket> findTicketsValidatedAfter(@Param("startDate") LocalDateTime startDate);
} 