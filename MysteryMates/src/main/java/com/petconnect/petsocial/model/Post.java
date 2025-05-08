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
 * Post entity - represents a post created by a user
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column
    private String mediaUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @Column
    private int likes;
    
    @Column
    private int shares;
    
    // The user who created this post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // The pets featured in this post
    @ManyToMany
    @JoinTable(
        name = "post_pets",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "pet_id")
    )
    private Set<Pet> featuredPets = new HashSet<>();
    
    // Comments on this post
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
    
    // Group that this post belongs to (if any)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
    
    // Users who liked this post
    @ManyToMany
    @JoinTable(
        name = "post_likes",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedBy = new HashSet<>();
    
    // Before persisting, set the creation timestamp
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Before updating, set the update timestamp
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Convenience methods to manage bidirectional relationships
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }
    
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }
    
    public void addPet(Pet pet) {
        featuredPets.add(pet);
        pet.getPosts().add(this);
    }
    
    public void removePet(Pet pet) {
        featuredPets.remove(pet);
        pet.getPosts().remove(this);
    }
    
    public void addLike(User user) {
        likedBy.add(user);
        likes = likedBy.size();
    }
    
    public void removeLike(User user) {
        likedBy.remove(user);
        likes = likedBy.size();
    }
}
