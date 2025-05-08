package com.petconnect.petsocial.factory;

import com.petconnect.petsocial.model.Post;
import com.petconnect.petsocial.model.PostType;
import com.petconnect.petsocial.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Factory class for creating different types of posts
 * Implements the Factory design pattern
 * Follows Single Responsibility Principle by focusing only on post creation
 */
@Component
public class PostFactory {
    
    /**
     * Create a text post
     * 
     * @param user The user creating the post
     * @param title The post title
     * @param content The post text content
     * @return The created Post object
     */
    public Post createTextPost(User user, String title, String content) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .postType(PostType.TEXT)
                .createdAt(LocalDateTime.now())
                .likes(0)
                .shares(0)
                .build();
    }
    
    /**
     * Create a photo post
     * 
     * @param user The user creating the post
     * @param title The post title
     * @param content The post text content
     * @param photoUrl The URL to the uploaded photo
     * @return The created Post object
     */
    public Post createPhotoPost(User user, String title, String content, String photoUrl) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .mediaUrl(photoUrl)
                .postType(PostType.PHOTO)
                .createdAt(LocalDateTime.now())
                .likes(0)
                .shares(0)
                .build();
    }
    
    /**
     * Create a video post
     * 
     * @param user The user creating the post
     * @param title The post title
     * @param content The post text content
     * @param videoUrl The URL to the uploaded video
     * @return The created Post object
     */
    public Post createVideoPost(User user, String title, String content, String videoUrl) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .mediaUrl(videoUrl)
                .postType(PostType.VIDEO)
                .createdAt(LocalDateTime.now())
                .likes(0)
                .shares(0)
                .build();
    }
    
    /**
     * Create a poll post
     * 
     * @param user The user creating the post
     * @param title The post title
     * @param content The post question and poll options in JSON format
     * @return The created Post object
     */
    public Post createPollPost(User user, String title, String content) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .postType(PostType.POLL)
                .createdAt(LocalDateTime.now())
                .likes(0)
                .shares(0)
                .build();
    }
    
    /**
     * Generic method to create any type of post
     * 
     * @param user The user creating the post
     * @param title The post title
     * @param content The post content
     * @param mediaUrl The media URL (optional)
     * @param postType The type of post
     * @return The created Post object
     */
    public Post createPost(User user, String title, String content, String mediaUrl, PostType postType) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .mediaUrl(mediaUrl)
                .postType(postType)
                .createdAt(LocalDateTime.now())
                .likes(0)
                .shares(0)
                .build();
    }
}
