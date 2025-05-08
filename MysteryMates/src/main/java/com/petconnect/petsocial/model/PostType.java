package com.petconnect.petsocial.model;

/**
 * Enumeration of post types in the pet social network
 * This follows Open/Closed Principle as new post types can be added
 * without modifying existing code
 */
public enum PostType {
    TEXT("text"),
    PHOTO("photo"),
    VIDEO("video"),
    POLL("poll");
    
    private final String type;
    
    PostType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
}
