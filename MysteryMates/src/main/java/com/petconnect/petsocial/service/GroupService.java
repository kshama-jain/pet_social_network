package com.petconnect.petsocial.service;

import com.petconnect.petsocial.model.Group;
import com.petconnect.petsocial.model.User;
import com.petconnect.petsocial.repository.GroupRepository;
import com.petconnect.petsocial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
// Add this import at the top of your GroupService.java file
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for group-related operations
 * Follows Single Responsibility Principle by focusing only on group management
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    /**
     * Get all groups
     *
     * @return List of all groups
     */
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    /**
     * Find a group by ID
     *
     * @param id The group ID
     * @return The group, if found
     */
    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    /**
     * Get groups created by a user
     *
     * @param userId The user ID
     * @return List of groups created by the user
     */
    public List<Group> getGroupsByCreator(Long userId) {
        return userRepository.findById(userId)
                .map(groupRepository::findByCreator)
                .orElse(List.of());
    }

    /**
     * Get groups that a user is a member of
     *
     * @param userId The user ID
     * @return List of groups the user is a member of
     */
    // In GroupService.java
    // In GroupService.java
    public List<Group> getGroupsByMember(Long userId) {
        try {
            // Change this line from findByMembers_Id to findByMemberId
            return groupRepository.findByMemberId(userId);
        } catch (Exception e) {
            System.err.println("Error retrieving groups for user " + userId + ": " + e.getMessage());
            // Return an empty list rather than failing
            return new ArrayList<>();
        }
    }

    /**
     * Create a new group
     *
     * @param creatorId The creator's user ID
     * @param group The group to create
     * @return The created group
     */
    public Optional<Group> createGroup(Long creatorId, Group group) {
        return userRepository.findById(creatorId)
                .map(creator -> {
                    group.setCreator(creator);
                    group.setCreatedAt(LocalDateTime.now());
                    Group savedGroup = groupRepository.save(group);

                    // Add creator as a member
                    savedGroup.addMember(creator);
                    groupRepository.save(savedGroup);

                    return savedGroup;
                });
    }

    /**
     * Update a group
     *
     * @param id The group ID
     * @param updatedGroup The updated group data
     * @return The updated group
     */
    public Optional<Group> updateGroup(Long id, Group updatedGroup) {
        return groupRepository.findById(id)
                .map(group -> {
                    group.setName(updatedGroup.getName());
                    group.setDescription(updatedGroup.getDescription());
                    group.setPictureUrl(updatedGroup.getPictureUrl());
                    group.setGroupType(updatedGroup.getGroupType());
                    return groupRepository.save(group);
                });
    }

    /**
     * Delete a group
     *
     * @param id The group ID
     */
    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    /**
     * Add a user to a group
     *
     * @param groupId The group ID
     * @param userId The user ID
     * @return true if successful, false otherwise
     */
    public boolean addMemberToGroup(Long groupId, Long userId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (groupOpt.isPresent() && userOpt.isPresent()) {
            Group group = groupOpt.get();
            User user = userOpt.get();

            group.addMember(user);
            groupRepository.save(group);
            return true;
        }

        return false;
    }

    /**
     * Remove a user from a group
     *
     * @param groupId The group ID
     * @param userId The user ID
     * @return true if successful, false otherwise
     */
    public boolean removeMemberFromGroup(Long groupId, Long userId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (groupOpt.isPresent() && userOpt.isPresent()) {
            Group group = groupOpt.get();
            User user = userOpt.get();

            // Don't remove the creator from the group
            if (group.getCreator().equals(user)) {
                return false;
            }

            group.removeMember(user);
            groupRepository.save(group);
            return true;
        }

        return false;
    }

    /**
     * Search for groups by name
     *
     * @param nameKeyword The name keyword to search for
     * @return List of groups matching the search criteria
     */
    public List<Group> searchGroupsByName(String nameKeyword) {
        return groupRepository.findByNameContaining(nameKeyword);
    }

    /**
     * Get groups by type
     *
     * @param groupType The group type
     * @return List of groups of the specified type
     */
    public List<Group> getGroupsByType(Group.GroupType groupType) {
        return groupRepository.findByGroupType(groupType);
    }

    /**
     * Get the total count of groups
     *
     * @return The number of groups
     */
    public long getGroupCount() {
        return groupRepository.count();
    }

    /**
     * Get groups with pagination
     *
     * @param pageable Pagination information
     * @return Page of groups
     */
    public Page<Group> getAllGroupsPaged(PageRequest pageable) {
        return groupRepository.findAll(pageable);
    }

    /**
     * Search for groups by name with pagination
     *
     * @param keyword The search keyword
     * @param pageable Pagination information
     * @return Page of groups matching the search criteria
     */
    public Page<Group> searchGroupsByNamePaged(String keyword, PageRequest pageable) {
        return groupRepository.findByNameContaining(keyword, pageable);
    }

    /**
     * Get group type statistics for analytics
     *
     * @return Map of group type to count
     */
    public Map<String, Long> getGroupTypeStats() {
        // Implementation depends on your repository capabilities
        // This is a simple implementation for now
        return Map.of(
                "PUBLIC", groupRepository.countByGroupType(Group.GroupType.PUBLIC),
                "PRIVATE", groupRepository.countByGroupType(Group.GroupType.PRIVATE),
                "RESTRICTED", groupRepository.countByGroupType(Group.GroupType.RESTRICTED)
        );
    }
}