package com.petconnect.petsocial.repository;

import com.petconnect.petsocial.model.Group;
import com.petconnect.petsocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Repository for Group entity
 * Follows Single Responsibility Principle by focusing only on Group data access
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Page<Group> findByNameContaining(String name, Pageable pageable);
    long countByGroupType(Group.GroupType type);

    List<Group> findByCreator(User creator);

    @Query("SELECT g FROM Group g JOIN g.members m WHERE m = :member")
    List<Group> findByMember(User member);

    // Add this method to find groups by member ID
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.id = :userId")
    List<Group> findByMemberId(@Param("userId") Long userId);

    List<Group> findByNameContaining(String name);

    List<Group> findByGroupType(Group.GroupType groupType);
}