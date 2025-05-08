package com.petconnect.petsocial.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Event entity - represents an event created by users
 */
@Entity
@Getter
@Setter
@ToString(exclude = {"creator", "attendees"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column
    private String location;

    @Column
    private String pictureUrl;

    @Column
    private LocalDateTime createdAt;

    @Column
    private boolean isVirtual;

    @Column
    private String virtualMeetingUrl;

    // The user who created this event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // Users attending this event
    @ManyToMany(mappedBy = "events")
    private Set<User> attendees = new HashSet<>();

    // Before persisting, set the creation timestamp
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Convenience methods to manage bidirectional relationships
    public void addAttendee(User user) {
        attendees.add(user);
        user.getEvents().add(this);
    }

    public void removeAttendee(User user) {
        attendees.remove(user);
        user.getEvents().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}