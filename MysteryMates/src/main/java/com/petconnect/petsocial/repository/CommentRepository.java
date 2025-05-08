package com.petconnect.petsocial.repository;

import com.petconnect.petsocial.model.Comment;
import com.petconnect.petsocial.model.Post;
import com.petconnect.petsocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity
 * Follows Single Responsibility Principle by focusing only on Comment data access
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByPost(Post post);
    
    List<Comment> findByUser(User user);
    
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    
    long countByPost(Post post);
}
