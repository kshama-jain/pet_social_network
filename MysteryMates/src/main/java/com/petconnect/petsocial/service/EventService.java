package com.petconnect.petsocial.service;

import com.petconnect.petsocial.model.Event;
import com.petconnect.petsocial.model.User;
import com.petconnect.petsocial.repository.EventRepository;
import com.petconnect.petsocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for event-related operations
 * Follows Single Responsibility Principle by focusing only on event management
 */
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Get all events
     *
     * @return List of all events
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Find an event by ID
     *
     * @param id The event ID
     * @return The event, if found
     */
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    /**
     * Get events created by a user
     *
     * @param userId The user ID
     * @return List of events created by the user
     */
    public List<Event> getEventsByCreator(Long userId) {
        return userRepository.findById(userId)
                .map(eventRepository::findByCreator)
                .orElse(List.of());
    }

    /**
     * Get events that a user is attending
     *
     * @param userId The user ID
     * @return List of events the user is attending
     */
    public List<Event> getEventsByAttendee(Long userId) {
        return userRepository.findById(userId)
                .map(eventRepository::findByAttendee)
                .orElse(List.of());
    }

    /**
     * Get upcoming events (events after the current date)
     *
     * @return List of upcoming events
     */
    public List<Event> getUpcomingEvents() {
        return eventRepository.findByEventDateAfter(LocalDateTime.now());
    }

    /**
     * Get past events (events before the current date)
     *
     * @return List of past events
     */
    public List<Event> getPastEvents() {
        return eventRepository.findByEventDateBefore(LocalDateTime.now());
    }

    /**
     * Create a new event
     *
     * @param creatorId The creator's user ID
     * @param event The event to create
     * @return The created event
     */
    public Optional<Event> createEvent(Long creatorId, Event event) {
        return userRepository.findById(creatorId)
                .map(creator -> {
                    event.setCreator(creator);
                    event.setCreatedAt(LocalDateTime.now());
                    Event savedEvent = eventRepository.save(event);

                    // Add creator as an attendee
                    savedEvent.addAttendee(creator);
                    eventRepository.save(savedEvent);

                    return savedEvent;
                });
    }

    /**
     * Update an event
     *
     * @param id The event ID
     * @param updatedEvent The updated event data
     * @return The updated event
     */
    public Optional<Event> updateEvent(Long id, Event updatedEvent) {
        return eventRepository.findById(id)
                .map(event -> {
                    event.setTitle(updatedEvent.getTitle());
                    event.setDescription(updatedEvent.getDescription());
                    event.setEventDate(updatedEvent.getEventDate());
                    event.setLocation(updatedEvent.getLocation());
                    event.setPictureUrl(updatedEvent.getPictureUrl());
                    event.setVirtual(updatedEvent.isVirtual());
                    event.setVirtualMeetingUrl(updatedEvent.getVirtualMeetingUrl());
                    return eventRepository.save(event);
                });
    }

    /**
     * Delete an event
     *
     * @param id The event ID
     */
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    /**
     * Add a user as an attendee to an event
     *
     * @param eventId The event ID
     * @param userId The user ID
     * @return true if successful, false otherwise
     */
    public boolean addAttendeeToEvent(Long eventId, Long userId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (eventOpt.isPresent() && userOpt.isPresent()) {
            Event event = eventOpt.get();
            User user = userOpt.get();

            event.addAttendee(user);
            eventRepository.save(event);
            return true;
        }

        return false;
    }

    /**
     * Remove a user as an attendee from an event
     *
     * @param eventId The event ID
     * @param userId The user ID
     * @return true if successful, false otherwise
     */
    public boolean removeAttendeeFromEvent(Long eventId, Long userId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (eventOpt.isPresent() && userOpt.isPresent()) {
            Event event = eventOpt.get();
            User user = userOpt.get();

            // Don't remove the creator from the event
            if (event.getCreator().equals(user)) {
                return false;
            }

            event.removeAttendee(user);
            eventRepository.save(event);
            return true;
        }

        return false;
    }

    /**
     * Search for events by keyword in title or description
     *
     * @param keyword The search keyword
     * @return List of events matching the search criteria
     */
    public List<Event> searchEvents(String keyword) {
        return eventRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }

    /**
     * Get virtual or in-person events
     *
     * @param isVirtual true for virtual events, false for in-person events
     * @return List of events of the specified type
     */
    public List<Event> getEventsByVirtuality(boolean isVirtual) {
        return eventRepository.findByIsVirtual(isVirtual);
    }

    /**
     * Get events by location
     *
     * @param location The location to search for
     * @return List of events at the specified location
     */
    public List<Event> getEventsByLocation(String location) {
        return eventRepository.findByLocationContaining(location);
    }

    /**
     * Get the total count of events
     *
     * @return The number of events
     */
    public long getEventCount() {
        return eventRepository.count();
    }

    /**
     * Get events with pagination
     *
     * @param pageable Pagination information
     * @return Page of events
     */
    public Page<Event> getAllEventsPaged(PageRequest pageable) {
        return eventRepository.findAll(pageable);
    }

    /**
     * Search for events by keyword in title or description with pagination
     *
     * @param keyword The search keyword
     * @param pageable Pagination information
     * @return Page of events matching the search criteria
     */
    public Page<Event> searchEventsPaged(String keyword, PageRequest pageable) {
        return eventRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword, pageable);
    }
}