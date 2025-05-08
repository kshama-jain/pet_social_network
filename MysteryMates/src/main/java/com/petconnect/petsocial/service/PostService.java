package com.petconnect.petsocial.service;

import com.petconnect.petsocial.factory.PostFactory;
import com.petconnect.petsocial.model.*;
import com.petconnect.petsocial.repository.GroupRepository;
import com.petconnect.petsocial.repository.PetRepository;
import com.petconnect.petsocial.repository.PostRepository;
import com.petconnect.petsocial.repository.UserRepository;
import com.petconnect.petsocial.repository.CommentRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;

/**
 * Service for post-related operations
 * Follows Single Responsibility Principle by focusing only on post management
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final GroupRepository groupRepository;
    private final CommentRepository commentRepository;
    private final PostFactory postFactory;

    /**
     * Get all posts
     *
     * @return List of all posts
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * Get all posts with pagination
     *
     * @param pageable Pagination information
     * @return Page of posts
     */
    public Page<Post> getAllPostsPaged(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    /**
     * Find a post by ID
     *
     * @param id The post ID
     * @return The post, if found
     */
    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    /**
     * Get posts by user
     *
     * @param userId The user ID
     * @return List of posts by the user
     */
    public List<Post> getPostsByUser(Long userId) {
        return userRepository.findById(userId)
                .map(postRepository::findByUser)
                .orElse(List.of());
    }

    /**
     * Get posts by user with pagination
     *
     * @param userId The user ID
     * @param pageable Pagination information
     * @return Page of posts by the user
     */
    public Page<Post> getPostsByUserPaged(Long userId, Pageable pageable) {
        return userRepository.findById(userId)
                .map(user -> postRepository.findByUser(user, pageable))
                .orElse(Page.empty());
    }

    /**
     * Get posts by post type
     *
     * @param postType The post type
     * @return List of posts of the specified type
     */
    public List<Post> getPostsByType(PostType postType) {
        return postRepository.findByPostType(postType);
    }

    /**
     * Get posts by group
     *
     * @param groupId The group ID
     * @return List of posts in the group
     */
    public List<Post> getPostsByGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .map(postRepository::findByGroup)
                .orElse(List.of());
    }

    /**
     * Get posts from users that a user is following
     *
     * @param userId The user ID
     * @param pageable Pagination information
     * @return Page of posts from followed users
     */
    public Page<Post> getPostsFromFollowing(Long userId, Pageable pageable) {
        return userRepository.findById(userId)
                .map(user -> postRepository.findPostsFromFollowing(user, pageable))
                .orElse(Page.empty());
    }

    /**
     * Create a new post using the Post Factory
     *
     * @param userId The user ID
     * @param title The post title
     * @param content The post content
     * @param mediaUrl The media URL (if any)
     * @param postType The post type
     * @param petIds The IDs of pets featured in the post
     * @param groupId The group ID (if the post is in a group)
     * @return The created post
     */
    public Optional<Post> createPost(Long userId, String title, String content,
                                     String mediaUrl, PostType postType, Set<Long> petIds, Long groupId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        Post post = postFactory.createPost(user, title, content, mediaUrl, postType);

        // Add featured pets if specified
        if (petIds != null && !petIds.isEmpty()) {
            List<Pet> pets = petRepository.findAllById(petIds);
            pets.forEach(post::addPet);
        }

        // Add to group if specified
        if (groupId != null) {
            groupRepository.findById(groupId).ifPresent(post::setGroup);
        }

        user.addPost(post);
        Post savedPost = postRepository.save(post);
        userRepository.save(user);

        return Optional.of(savedPost);
    }

    /**
     * Update a post
     *
     * @param id The post ID
     * @param updatedPost The updated post data
     * @return The updated post
     */
    public Optional<Post> updatePost(Long id, Post updatedPost) {
        return postRepository.findById(id)
                .map(post -> {
                    post.setTitle(updatedPost.getTitle());
                    post.setContent(updatedPost.getContent());
                    post.setMediaUrl(updatedPost.getMediaUrl());
                    return postRepository.save(post);
                });
    }

    /**
     * Delete a post
     *
     * @param id The post ID
     */
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    /**
     * Like a post
     *
     * @param postId The post ID
     * @param userId The user ID
     * @return The updated post
     */
    public Optional<Post> likePost(Long postId, Long userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (postOpt.isPresent() && userOpt.isPresent()) {
            Post post = postOpt.get();
            User user = userOpt.get();

            post.addLike(user);
            return Optional.of(postRepository.save(post));
        }

        return Optional.empty();
    }

    /**
     * Unlike a post
     *
     * @param postId The post ID
     * @param userId The user ID
     * @return The updated post
     */
    public Optional<Post> unlikePost(Long postId, Long userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (postOpt.isPresent() && userOpt.isPresent()) {
            Post post = postOpt.get();
            User user = userOpt.get();

            post.removeLike(user);
            return Optional.of(postRepository.save(post));
        }

        return Optional.empty();
    }
    @Transactional
    public boolean addComment(Long postId, Long userId, String content) {
        try {
            // Get the post and user
            Optional<Post> postOpt = postRepository.findById(postId);
            Optional<User> userOpt = userRepository.findById(userId);

            if (postOpt.isPresent() && userOpt.isPresent()) {
                Post post = postOpt.get();
                User user = userOpt.get();

                // Create and set up the comment
                Comment comment = new Comment();
                comment.setContent(content);
                comment.setUser(user);
                comment.setPost(post);
                comment.setCreatedAt(LocalDateTime.now());

                // Save the comment
                commentRepository.save(comment);

                // Add the comment to the post
                post.addComment(comment);
                postRepository.save(post);

                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error adding comment: " + e.getMessage());
            return false;
        }
    }
    /**
     * Search for posts by keyword in title or content
     *
     * @param keyword The search keyword
     * @param pageable Pagination information
     * @return Page of posts matching the search criteria
     */
    public Page<Post> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
    }

    /**
     * Get posts featuring a specific pet
     *
     * @param petId The pet ID
     * @return List of posts featuring the pet
     */
    public List<Post> getPostsByFeaturedPet(Long petId) {
        return postRepository.findByFeaturedPetId(petId);
    }

    /**
     * Get the total count of posts
     *
     * @return The number of posts
     */
    public long getPostCount() {
        return postRepository.count();
    }

    /**
     * Get post type statistics for analytics
     *
     * @return Map of post type to count
     */
    public Map<String, Long> getPostTypeStats() {
        // Implementation depends on your repository capabilities
        // This is a simple implementation for now
        return Map.of(
                "TEXT", postRepository.countByPostType(PostType.TEXT),
                "PHOTO", postRepository.countByPostType(PostType.PHOTO),
                "VIDEO", postRepository.countByPostType(PostType.VIDEO)
        );
    }

    /**
     * Get engagement statistics for analytics
     *
     * @return Map of engagement metric to value
     */
    public Map<String, Long> getEngagementStats() {
        // Implementation depends on your repository capabilities
        // This is a simple implementation for now
        long totalLikes = postRepository.findAll().stream()
                .mapToLong(post -> post.getLikedBy().size())
                .sum();

        long totalComments = postRepository.findAll().stream()
                .mapToLong(post -> post.getComments().size())
                .sum();

        return Map.of("likes", totalLikes, "comments", totalComments);
    }
}