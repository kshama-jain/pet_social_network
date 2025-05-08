package com.petconnect.petsocial.repository;

import com.petconnect.petsocial.model.Group;
import com.petconnect.petsocial.model.Post;
import com.petconnect.petsocial.model.PostType;
import com.petconnect.petsocial.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Post entity
 * Follows Single Responsibility Principle by focusing only on Post data access
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    List<Post> findByUser(User user);

    long countByPostType(PostType postType);

    Page<Post> findByUser(User user, Pageable pageable);
    
    List<Post> findByPostType(PostType postType);
    
    List<Post> findByGroup(Group group);
    
    @Query("SELECT p FROM Post p WHERE p.user IN (SELECT u FROM User u JOIN u.followers f WHERE f = :user)")
    Page<Post> findPostsFromFollowing(User user, Pageable pageable);
    
    Page<Post> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);
    
    @Query("SELECT p FROM Post p JOIN p.featuredPets pet WHERE pet.id = :petId")
    List<Post> findByFeaturedPetId(Long petId);
}
