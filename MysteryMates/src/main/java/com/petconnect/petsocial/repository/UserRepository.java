package com.petconnect.petsocial.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.petconnect.petsocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * Repository for User entity
 * Follows Single Responsibility Principle by focusing only on User data access
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);

    Page<User> findByNameContainingOrUsernameContainingOrEmailContaining(
            String name, String username, String email, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.following f WHERE u.id = :currentUserId AND f.id = :profileUserId")
    boolean isUserFollowing(@Param("currentUserId") Long currentUserId, @Param("profileUserId") Long profileUserId);

   
    @Query(value = "SELECT COUNT(*) FROM user_following WHERE user_id = ?1 AND following_id = ?2", nativeQuery = true)
    long countFollowRelationship(Long userId, Long followingId);

    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
}
