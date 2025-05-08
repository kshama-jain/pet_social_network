package com.petconnect.petsocial.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pet entity - represents a pet owned by a user
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pets")
public class Pet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String species;
    
    @Column
    private String breed;
    
    @Column
    private Integer age;
    
    @Column
    private String gender;
    
    @Column
    private String picture;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Pet's birthday
    @Column
    private String birthday;
    
    // Pet's adoption date
    @Column
    private String adoptionDate;
    
    // Many pets can belong to one owner (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
    
    // Posts featuring this pet
    @ManyToMany(mappedBy = "featuredPets")
    private java.util.Set<Post> posts = new java.util.HashSet<>();
}
