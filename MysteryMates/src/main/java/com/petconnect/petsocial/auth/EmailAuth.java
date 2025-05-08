package com.petconnect.petsocial.auth;

import com.petconnect.petsocial.model.User;
import com.petconnect.petsocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Email authentication strategy implementation
 * Implements the Strategy pattern for email-based authentication
 * Follows Single Responsibility Principle by focusing only on email authentication
 */
@Component
@RequiredArgsConstructor
public class EmailAuth implements AuthenticationStrategy {

    private final UserRepository userRepository;

    @Override
    public boolean authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Direct string comparison instead of BCrypt
            return password.equals(user.getPassword());
        }

        return false;
    }

    @Override
    public String generateAuthFactor(String email) {
        // For email authentication, we don't generate an auth factor
        // This could be extended to implement password reset tokens
        return null;
    }

    @Override
    public String getStrategyType() {
        return "EMAIL";
    }
}