package com.petconnect.petsocial.service;

import com.petconnect.petsocial.auth.AuthenticationStrategy;
import com.petconnect.petsocial.model.User;
import com.petconnect.petsocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for authentication-related operations
 * Follows Single Responsibility Principle by focusing only on authentication
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final Map<String, AuthenticationStrategy> strategies = new HashMap<>();

    /**
     * Register a new authentication strategy
     *
     * @param strategy The authentication strategy implementation
     */
    public void registerStrategy(AuthenticationStrategy strategy) {
        strategies.put(strategy.getStrategyType(), strategy);
    }

    /**
     * Authenticate using the specified strategy
     *
     * @param strategyType The type of authentication strategy to use
     * @param identifier The user identifier (email, phone, etc.)
     * @param credential The credential (password, OTP, etc.)
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticate(String strategyType, String identifier, String credential) {
        AuthenticationStrategy strategy = strategies.get(strategyType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown authentication strategy: " + strategyType);
        }

        boolean result = strategy.authenticate(identifier, credential);

        // Update last login time if authentication was successful
        if (result) {
            if (strategyType.equals("EMAIL")) {
                Optional<User> userOpt = userRepository.findByEmail(identifier);
                userOpt.ifPresent(user -> {
                    user.setLastLogin(LocalDateTime.now().toString());
                    userRepository.save(user);
                });
            } else if (strategyType.equals("PHONE")) {
                Optional<User> userOpt = userRepository.findByPhone(identifier);
                userOpt.ifPresent(user -> {
                    user.setLastLogin(LocalDateTime.now().toString());
                    userRepository.save(user);
                });
            }
        }

        return result;
    }

    /**
     * Register a new user
     *
     * @param user The user to register
     * @return The registered user
     */
    public User registerUser(User user) {
        // Store password as plaintext instead of hashing
        return userRepository.save(user);
    }



    /**
     * Check if a username is already taken
     *
     * @param username The username to check
     * @return true if the username is already taken, false otherwise
     */
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username) ;
    }

    /**
     * Check if an email is already registered
     *
     * @param email The email to check
     * @return true if the email is already registered, false otherwise
     */
    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email) ;
    }

    /**
     * Check if a phone number is already registered
     *
     * @param phone The phone number to check
     * @return true if the phone number is already registered, false otherwise
     */
    public boolean isPhoneRegistered(String phone) {
        return userRepository.existsByPhone(phone);
    }

    /**
     * Find a user by username
     *
     * @param username The username to search for
     * @return The user, if found
     */
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find an admin by username
     *
     * @param username The username to search for
     * @return The admin, if found
     */

}