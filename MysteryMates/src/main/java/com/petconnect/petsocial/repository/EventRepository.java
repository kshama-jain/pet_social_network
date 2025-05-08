package com.petconnect.petsocial.repository;

import com.petconnect.petsocial.model.Event;
import com.petconnect.petsocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;



import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Event entity
 * Follows Single Responsibility Principle by focusing only on Event data access
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByCreator(User creator);
    
    @Query("SELECT e FROM Event e JOIN e.attendees a WHERE a = :attendee")
    List<Event> findByAttendee(User attendee);

    Page<Event> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);

    List<Event> findByEventDateAfter(LocalDateTime date);
    
    List<Event> findByEventDateBefore(LocalDateTime date);
    
    List<Event> findByTitleContainingOrDescriptionContaining(String titleKeyword, String descriptionKeyword);
    
    List<Event> findByIsVirtual(boolean isVirtual);
    
    List<Event> findByLocationContaining(String location);
}
