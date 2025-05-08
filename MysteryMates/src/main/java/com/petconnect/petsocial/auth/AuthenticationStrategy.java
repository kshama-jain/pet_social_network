package com.petconnect.petsocial.auth;

/**
 * Authentication Strategy interface
 * Implements the Strategy pattern for different authentication methods
 * Follows Open/Closed Principle by allowing new authentication strategies
 * without modifying existing code
 */
public interface AuthenticationStrategy {
    
    /**
     * Authenticate a user based on the provided credentials
     * 
     * @param identifier The user identifier (email, phone, etc.)
     * @param credential The credential (password, OTP, etc.)
     * @return true if authentication is successful, false otherwise
     */
    boolean authenticate(String identifier, String credential);
    
    /**
     * Generate a new authentication factor (OTP, token, etc.) if applicable
     * 
     * @param identifier The user identifier
     * @return The generated authentication factor, or null if not applicable
     */
    String generateAuthFactor(String identifier);
    
    /**
     * Get the type of authentication strategy
     * 
     * @return The authentication strategy type
     */
    String getStrategyType();
}
