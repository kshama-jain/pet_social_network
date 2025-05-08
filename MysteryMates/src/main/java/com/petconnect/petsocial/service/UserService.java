package com.petconnect.petsocial.service;

import com.petconnect.petsocial.model.Pet;
import com.petconnect.petsocial.model.User;
import com.petconnect.petsocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for user-related operations
 * Follows Single Responsibility Principle by focusing only on user management
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Get all users
     *
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Find a user by ID
     *
     * @param id The user ID
     * @return The user, if found
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find a user by username
     *
     * @param username The username
     * @return The user, if found
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find a user by email
     *
     * @param email The email
     * @return The user, if found
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find a user by phone number
     *
     * @param phone The phone number
     * @return The user, if found
     */
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    /**
     * Save a user
     *
     * @param user The user to save
     * @return The saved user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Update a user's profile
     *
     * @param id The user ID
     * @param updatedUser The updated user data
     * @return The updated user
     */
    public Optional<User> updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setBio(updatedUser.getBio());
                    user.setLocation(updatedUser.getLocation());
                    user.setProfilePicture(updatedUser.getProfilePicture());
                    // Do not update sensitive fields like password here
                    return userRepository.save(user);
                });
    }

    /**
     * Delete a user
     *
     * @param id The user ID
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Add a follower relationship between users
     *
     * @param followerId The follower user ID
     * @param followedId The followed user ID
     * @return true if successful, false otherwise
     */
    public boolean followUser(Long followerId, Long followedId) {
        Optional<User> followerOpt = userRepository.findById(followerId);
        Optional<User> followedOpt = userRepository.findById(followedId);

        if (followerOpt.isPresent() && followedOpt.isPresent()) {
            User follower = followerOpt.get();
            User followed = followedOpt.get();

            // Add the following relationship
            follower.follow(followed);

            userRepository.save(follower);
            userRepository.save(followed);
            return true;
        }

        return false;
    }

    /**
     * Remove a follower relationship between users
     *
     * @param followerId The follower user ID
     * @param followedId The followed user ID
     * @return true if successful, false otherwise
     */
    public boolean unfollowUser(Long followerId, Long followedId) {
        Optional<User> followerOpt = userRepository.findById(followerId);
        Optional<User> followedOpt = userRepository.findById(followedId);

        if (followerOpt.isPresent() && followedOpt.isPresent()) {
            User follower = followerOpt.get();
            User followed = followedOpt.get();

            // Remove the following relationship
            follower.unfollow(followed);

            userRepository.save(follower);
            userRepository.save(followed);
            return true;
        }

        return false;
    }

    /**
     * Get a user's followers
     *
     * @param userId The user ID
     * @return List of followers, or empty list if user not found
     */
    public List<User> getUserFollowers(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getFollowers().stream().toList())
                .orElse(List.of());
    }

    /**
     * Get users that a user is following
     *
     * @param userId The user ID
     * @return List of followed users, or empty list if user not found
     */
    public List<User> getUserFollowing(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getFollowing().stream().toList())
                .orElse(List.of());
    }

    /**
     * Get the total count of users
     *
     * @return The number of users
     */
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * Get users with pagination
     *
     * @param pageable Pagination information
     * @return Page of users
     */
    public Page<User> getUsersPaged(PageRequest pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Search for users by keyword in name, username, or email
     *
     * @param keyword The search keyword
     * @param pageable Pagination information
     * @return Page of users matching the search criteria
     */
    public Page<User> searchUsers(String keyword, PageRequest pageable) {
        return userRepository.findByNameContainingOrUsernameContainingOrEmailContaining(
                keyword, keyword, keyword, pageable);
    }

    /**
     * Get pets belonging to a user
     *
     * @param userId The user ID
     * @return List of pets owned by the user
     */
    public List<Pet> getUserPets(Long userId) {
        return userRepository.findById(userId)
                .map(User::getPets)
                .map(pets -> pets.stream().toList())
                .orElse(List.of());
    }

    /**
     * Get user growth data for analytics
     *
     * @return Map of date to number of users registered
     */
    public Map<String, Long> getUserGrowthData() {
        // Implementation depends on your repository capabilities
        // This is a simple implementation for now
        return Map.of("2023-01", 10L, "2023-02", 15L, "2023-03", 25L);
    }

    /**
     * Get user location statistics for analytics
     *
     * @return Map of location to number of users
     */
    public Map<String, Long> getUserLocationStats() {
        // Implementation depends on your repository capabilities
        // This is a simple implementation for now
        return Map.of("New York", 20L, "San Francisco", 15L, "Chicago", 10L);
    }

    /**
     * Get most active users based on post and comment count
     *
     * @param limit Number of users to return
     * @return List of most active users
     */
    public List<User> getMostActiveUsers(int limit) {
        // Implementation depends on your repository capabilities
        // For now, just return a few random users from the repository
        return userRepository.findAll().stream().limit(limit).toList();
    }
    /**
     * Check if one user follows another
     *
     * @param followerId ID of the follower user
     * @param followedId ID of the user being followed
     * @return true if follower follows followed, false otherwise
     */
    public boolean checkIfUserFollows(Long followerId, Long followedId) {
        // This uses a direct query to check the relationship without relying on lazy loading
        return userRepository.countFollowRelationship(followerId, followedId) > 0;
    }
}