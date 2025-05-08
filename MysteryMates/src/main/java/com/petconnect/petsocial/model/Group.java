package com.petconnect.petsocial.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Group entity - represents a group of users with common interest
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`groups`")
public class Group {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    @Column
    private String pictureUrl;
    
    @Column
    private GroupType groupType;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // The creator/owner of the group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
    
    // Members of this group
    @ManyToMany(mappedBy = "groups")
    private Set<User> members = new HashSet<>();
    
    // Posts in this group
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();
    
    // Before persisting, set the creation timestamp
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Convenience methods to manage bidirectional relationships
    public void addMember(User user) {
        members.add(user);
        user.getGroups().add(this);
    }
    
    public void removeMember(User user) {
        members.remove(user);
        user.getGroups().remove(this);
    }
    
    public void addPost(Post post) {
        posts.add(post);
        post.setGroup(this);
    }
    
    public void removePost(Post post) {
        posts.remove(post);
        post.setGroup(null);
    }
    
    // Group type enum
    public enum GroupType {
        PUBLIC, PRIVATE, RESTRICTED
    }
}
