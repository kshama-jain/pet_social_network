package com.petconnect.petsocial.service;

import com.petconnect.petsocial.model.Pet;
import com.petconnect.petsocial.model.User;
import com.petconnect.petsocial.repository.PetRepository;
import com.petconnect.petsocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for pet-related operations
 * Follows Single Responsibility Principle by focusing only on pet management
 */
@Service
@RequiredArgsConstructor
public class PetService {
//
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all pets
     * 
     * @return List of all pets
     */
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }
    
    /**
     * Find a pet by ID
     * 
     * @param id The pet ID
     * @return The pet, if found
     */
    public Optional<Pet> getPetById(Long id) {
        return petRepository.findById(id);
    }
    
    /**
     * Find pets by owner
     * 
     * @param ownerId The owner's user ID
     * @return List of the owner's pets
     */
    public List<Pet> getPetsByOwner(Long ownerId) {
        Optional<User> ownerOpt = userRepository.findById(ownerId);
        return ownerOpt.map(petRepository::findByOwner).orElse(List.of());
    }
    
    /**
     * Find pets by species
     * 
     * @param species The species to search for
     * @return List of pets of the specified species
     */
    public List<Pet> getPetsBySpecies(String species) {
        return petRepository.findBySpecies(species);
    }
    
    /**
     * Find pets by breed
     * 
     * @param breed The breed to search for
     * @return List of pets of the specified breed
     */
    public List<Pet> getPetsByBreed(String breed) {
        return petRepository.findByBreed(breed);
    }
    
    /**
     * Add a new pet for a user
     * 
     * @param ownerId The owner's user ID
     * @param pet The pet to add
     * @return The added pet
     */
    public Optional<Pet> addPet(Long ownerId, Pet pet) {
        return userRepository.findById(ownerId)
                .map(owner -> {
                    pet.setOwner(owner);
                    Pet savedPet = petRepository.save(pet);
                    owner.addPet(savedPet);
                    userRepository.save(owner);
                    return savedPet;
                });
    }
    
    /**
     * Update a pet's information
     * 
     * @param id The pet ID
     * @param updatedPet The updated pet data
     * @return The updated pet
     */
    public Optional<Pet> updatePet(Long id, Pet updatedPet) {
        return petRepository.findById(id)
                .map(pet -> {
                    pet.setName(updatedPet.getName());
                    pet.setSpecies(updatedPet.getSpecies());
                    pet.setBreed(updatedPet.getBreed());
                    pet.setAge(updatedPet.getAge());
                    pet.setGender(updatedPet.getGender());
                    pet.setPicture(updatedPet.getPicture());
                    pet.setDescription(updatedPet.getDescription());
                    pet.setBirthday(updatedPet.getBirthday());
                    pet.setAdoptionDate(updatedPet.getAdoptionDate());
                    return petRepository.save(pet);
                });
    }
    
    /**
     * Delete a pet
     * 
     * @param id The pet ID
     */
    public void deletePet(Long id) {
        petRepository.findById(id).ifPresent(pet -> {
            User owner = pet.getOwner();
            if (owner != null) {
                owner.removePet(pet);
                userRepository.save(owner);
            }
            petRepository.deleteById(id);
        });
    }

    public Pet updatePet(Pet pet) {
        return petRepository.save(pet);
    }
    /**
     * Search for pets by name
     * 
     * @param nameKeyword The name keyword to search for
     * @return List of pets matching the search criteria
     */
    public List<Pet> searchPetsByName(String nameKeyword) {
        return petRepository.findByNameContaining(nameKeyword);
    }
}
