package com.petconnect.petsocial.auth;

import com.petconnect.petsocial.model.User;
import com.petconnect.petsocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;

/**
 * Phone authentication strategy implementation
 * Implements the Strategy pattern for phone-based authentication
 * Follows Single Responsibility Principle by focusing only on phone authentication
 */
@Component
@RequiredArgsConstructor
public class PhoneAuth implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final Random random = new Random();

    @Override
    public boolean authenticate(String phone, String password) {
        Optional<User> userOpt = userRepository.findByPhone(phone);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Direct string comparison instead of BCrypt
            return password.equals(user.getPassword());
        }

        return false;
    }

    @Override
    public String generateAuthFactor(String phone) {
        // Generate a 6-digit OTP
        // In a real application, this would send an SMS
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public String getStrategyType() {
        return "PHONE";
    }
}