package com.petconnect.petsocial.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Regular user entity that extends Person
 * Has additional fields and relationships specific to regular users
 */
@Entity
@Getter
@Setter
@ToString(exclude = {"pets", "posts", "groups", "events", "followers", "following"})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends Person {

    @Column
    private String bio;

    @Column
    private String location;

    @Column
    private String profilePicture;

    // One user can have multiple pets
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets = new ArrayList<>();

    // One user can create multiple posts
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    // One user can be a member of multiple groups
    @ManyToMany
    @JoinTable(name = "user_groups",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<Group> groups = new HashSet<>();

    // One user can participate in multiple events
    @ManyToMany
    @JoinTable(
            name = "user_events",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events = new HashSet<>();

    // One user can have many followers (other users)
    @ManyToMany
    @JoinTable(
            name = "user_followers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private Set<User> followers = new HashSet<>();

    // One user can follow many other users
    @ManyToMany(mappedBy = "followers")
    private Set<User> following = new HashSet<>();

    // Convenience methods to manage bidirectional relationships
    public void addPet(Pet pet) {
        pets.add(pet);
        pet.setOwner(this);
    }

    public void removePet(Pet pet) {
        pets.remove(pet);
        pet.setOwner(null);
    }

    public void addPost(Post post) {
        posts.add(post);
        post.setUser(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setUser(null);
    }

    public void joinGroup(Group group) {
        groups.add(group);
        group.getMembers().add(this);
    }

    public void leaveGroup(Group group) {
        groups.remove(group);
        group.getMembers().remove(this);
    }

    public void attendEvent(Event event) {
        events.add(event);
        event.getAttendees().add(this);
    }

    public void leaveEvent(Event event) {
        events.remove(event);
        event.getAttendees().remove(this);
    }

    public void follow(User user) {
        following.add(user);
        user.getFollowers().add(this);
    }

    public void unfollow(User user) {
        following.remove(user);
        user.getFollowers().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}