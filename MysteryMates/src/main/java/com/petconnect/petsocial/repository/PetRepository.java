package com.petconnect.petsocial.repository;

import com.petconnect.petsocial.model.Pet;
import com.petconnect.petsocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Pet entity
 * Follows Single Responsibility Principle by focusing only on Pet data access
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    List<Pet> findByOwner(User owner);
    
    List<Pet> findBySpecies(String species);
    
    List<Pet> findByBreed(String breed);
    
    List<Pet> findByNameContaining(String name);
}
