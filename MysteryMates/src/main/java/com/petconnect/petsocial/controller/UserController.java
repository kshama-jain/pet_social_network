package com.petconnect.petsocial.controller;

import com.petconnect.petsocial.model.*;
import com.petconnect.petsocial.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for user-related operations
 * Follows Single Responsibility Principle by focusing only on user operations
 */
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PetService petService;
    private final PostService postService;
    private final GroupService groupService;
    private final EventService eventService;
    private final HttpServletRequest request;


    /**
     * Show the home page (feed)
     */
    @GetMapping("/")
    public String home(HttpSession session, Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            // Redirect to login if not authenticated
            return "redirect:/login";
        }

        try {
            // Refresh user data
            user = userService.getUserById(user.getId()).orElse(user);
            session.setAttribute("user", user);

            // Try to get posts from followed users
            Page<Post> feedPosts;
            try {
                PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
                feedPosts = postService.getPostsFromFollowing(user.getId(), pageRequest);
            } catch (Exception e) {
                System.err.println("Error loading posts: " + e.getMessage());
                feedPosts = Page.empty();
                model.addAttribute("postError", "Unable to load posts: " + e.getMessage());
            }

            // Try to get upcoming events
            List<Event> upcomingEvents;
            try {
                upcomingEvents = eventService.getUpcomingEvents();
            } catch (Exception e) {
                System.err.println("Error loading events: " + e.getMessage());
                upcomingEvents = new ArrayList<>();
                model.addAttribute("eventError", "Unable to load events: " + e.getMessage());
            }

            // Try to get user groups
            List<Group> userGroups;
            try {
                userGroups = groupService.getGroupsByMember(user.getId());
            } catch (Exception e) {
                System.err.println("Error loading groups: " + e.getMessage());
                userGroups = new ArrayList<>();
                model.addAttribute("groupError", "Unable to load groups: " + e.getMessage());
            }

            model.addAttribute("user", user);
            model.addAttribute("posts", feedPosts);
            model.addAttribute("events", upcomingEvents);
            model.addAttribute("groups", userGroups);

            return "index";
        } catch (Exception e) {
            System.err.println("Error loading home page: " + e.getMessage());
            e.printStackTrace();

            // Add user and error message to model
            model.addAttribute("user", user);
            model.addAttribute("error", "Error loading feed: " + e.getMessage());

            // Provide empty collections to prevent template errors
            model.addAttribute("posts", Page.empty());
            model.addAttribute("events", new ArrayList<>());
            model.addAttribute("groups", new ArrayList<>());

            return "index";
        }
    }

    /**
     * Show the user profile page
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Refresh user data
            user = userService.getUserById(user.getId()).orElse(user);

            // Get user pets
            List<Pet> pets = petService.getPetsByOwner(user.getId());

            // Get user posts
            List<Post> posts = postService.getPostsByUser(user.getId());

            // Manually get user's groups to avoid SQL issues
            List<Group> userGroups = new ArrayList<>();
            try {
                userGroups = groupService.getGroupsByMember(user.getId());
            } catch (Exception e) {
                System.err.println("Error loading user groups: " + e.getMessage());
            }

            model.addAttribute("user", user);
            model.addAttribute("pets", pets);
            model.addAttribute("posts", posts);
            model.addAttribute("userGroups", userGroups); // Pass groups separately

            return "profile";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading profile: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Show another user's profile
     */
    /**
     * Show another user's profile
     */
    /**
     * Show another user's profile
     */
    @GetMapping("/user/{id}")
    public String viewUserProfile(@PathVariable Long id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get the requested user profile
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            return "redirect:/";
        }

        User profileUser = userOpt.get();

        // Instead of using the following collection directly, check manually
        // This works regardless of lazy loading
        boolean isFollowing = userService.checkIfUserFollows(currentUser.getId(), profileUser.getId());

        // Get profile user's pets
        List<Pet> pets = petService.getPetsByOwner(profileUser.getId());

        // Get profile user's posts
        List<Post> posts = postService.getPostsByUser(profileUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", profileUser);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("pets", pets);
        model.addAttribute("posts", posts);

        return "user-profile";
    }

    /**
     * Update user profile
     */
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser,
                                @RequestParam(required = false) MultipartFile profileImage,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Handle profile image upload if provided
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // Create directory if it doesn't exist
                String uploadDir = "uploads/profile-images/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate a unique filename
                String filename = user.getId() + "_" +
                        System.currentTimeMillis() + "_" +
                        profileImage.getOriginalFilename();

                // Save the file
                Path filePath = uploadPath.resolve(filename);
                Files.copy(profileImage.getInputStream(), filePath);

                // Update the user's profile picture
                updatedUser.setProfilePicture("/uploads/profile-images/" + filename);

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload profile image: " + e.getMessage());
                return "redirect:/profile";
            }
        } else {
            // Keep the existing profile picture
            updatedUser.setProfilePicture(user.getProfilePicture());
        }

        // Update user data
        userService.updateUser(user.getId(), updatedUser);

        // Refresh the session with updated user data
        User updatedUserData = userService.getUserById(user.getId()).orElse(user);
        session.setAttribute("user", updatedUserData);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        return "redirect:/profile";
    }

    /**
     * Show the page for adding a new pet
     */

    @GetMapping("/pets/add")
    public String showAddPetForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("pet", new Pet());
        return "add-pet";
    }

    /**
     * Add a new pet
     */
    @PostMapping("/pets/add")
    public String addPet(@ModelAttribute Pet pet,
                         @RequestParam(required = false) MultipartFile petImage,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Handle pet image upload if provided
        if (petImage != null && !petImage.isEmpty()) {
            try {
                // Create directory if it doesn't exist
                String uploadDir = "uploads/pet-images/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate a unique filename
                String filename = user.getId() + "_" +
                        System.currentTimeMillis() + "_" +
                        petImage.getOriginalFilename();

                // Save the file
                Path filePath = uploadPath.resolve(filename);
                Files.copy(petImage.getInputStream(), filePath);

                // Update the pet's picture
                pet.setPicture("/uploads/pet-images/" + filename);

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload pet image: " + e.getMessage());
                return "redirect:/pets/add";
            }
        }

        // Add the pet
        petService.addPet(user.getId(), pet);

        redirectAttributes.addFlashAttribute("success", "Pet added successfully");
        return "redirect:/profile";
    }
    /**
     * Show the page for editing a pet
     */
    @GetMapping("/pets/{id}/edit")
    public String showEditPetForm(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get the pet by ID
        Optional<Pet> petOpt = petService.getPetById(id);

        // Check if pet exists and belongs to the current user
        if (petOpt.isEmpty() || !petOpt.get().getOwner().getId().equals(user.getId())) {
            return "redirect:/profile?error=Pet not found or access denied";
        }

        model.addAttribute("pet", petOpt.get());
        return "edit-pet";
    }

    /**
     * Update a pet
     */
    @PostMapping("/pets/{id}/update")
    public String updatePet(@PathVariable Long id,
                            @ModelAttribute Pet updatedPet,
                            @RequestParam(required = false) MultipartFile petImage,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get the existing pet
        Optional<Pet> existingPetOpt = petService.getPetById(id);

        // Check if pet exists and belongs to the current user
        if (existingPetOpt.isEmpty() || !existingPetOpt.get().getOwner().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Pet not found or access denied");
            return "redirect:/profile";
        }

        Pet existingPet = existingPetOpt.get();

        // Handle pet image upload if provided
        if (petImage != null && !petImage.isEmpty()) {
            try {
                // Create directory if it doesn't exist
                String uploadDir = "uploads/pet-images/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate a unique filename
                String filename = user.getId() + "_" +
                        System.currentTimeMillis() + "_" +
                        petImage.getOriginalFilename();

                // Save the file
                Path filePath = uploadPath.resolve(filename);
                Files.copy(petImage.getInputStream(), filePath);

                // Update the pet's picture
                updatedPet.setPicture("/uploads/pet-images/" + filename);

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload pet image: " + e.getMessage());
                return "redirect:/pets/" + id + "/edit";
            }
        } else {
            // Keep the existing picture if no new one is uploaded
            updatedPet.setPicture(existingPet.getPicture());
        }

        // Ensure the owner and ID remain unchanged
        updatedPet.setOwner(existingPet.getOwner());
        updatedPet.setId(existingPet.getId());

        // Update the pet
        petService.updatePet(updatedPet);

        redirectAttributes.addFlashAttribute("success", "Pet updated successfully");
        return "redirect:/profile";
    }
    /**
     * Delete a pet
     */
    @PostMapping("/pets/{id}/delete")
    public String deletePet(@PathVariable Long id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get the pet by ID
        Optional<Pet> petOpt = petService.getPetById(id);

        // Check if pet exists and belongs to the current user
        if (petOpt.isEmpty() || !petOpt.get().getOwner().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Pet not found or access denied");
            return "redirect:/profile";
        }

        // Delete the pet
        petService.deletePet(id);

        redirectAttributes.addFlashAttribute("success", "Pet deleted successfully");
        return "redirect:/profile";
    }
    /**
     * Show the page for creating a new post
     */
    @GetMapping("/posts/create")
    public String showCreatePostForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get user's pets for tagging in the post
        List<Pet> pets = petService.getPetsByOwner(user.getId());

        // Get user's groups for posting to a group
        List<Group> groups = groupService.getGroupsByMember(user.getId());

        model.addAttribute("postTypes", PostType.values());
        model.addAttribute("pets", pets);
        model.addAttribute("groups", groups);

        return "create-post";
    }

    /**
     * Create a new post
     */
    @PostMapping("/posts/create")
    public String createPost(@RequestParam String title,
                             @RequestParam String content,
                             @RequestParam PostType postType,
                             @RequestParam(required = false) MultipartFile mediaFile,
                             @RequestParam(required = false) List<Long> petIds,
                             @RequestParam(required = false) Long groupId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String mediaUrl = null;

        // Handle media file upload if provided
        if (mediaFile != null && !mediaFile.isEmpty()) {
            try {
                // Create directory if it doesn't exist
                String uploadDir = "uploads/post-media/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate a unique filename
                String filename = user.getId() + "_" +
                        System.currentTimeMillis() + "_" +
                        mediaFile.getOriginalFilename();

                // Save the file
                Path filePath = uploadPath.resolve(filename);
                Files.copy(mediaFile.getInputStream(), filePath);

                // Set the media URL
                mediaUrl = "/uploads/post-media/" + filename;

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload media: " + e.getMessage());
                return "redirect:/posts/create";
            }
        }

        // Convert pet IDs to a set if provided
        Set<Long> petIdsSet = petIds != null ? new HashSet<>(petIds) : null;

        // Create the post
        postService.createPost(user.getId(), title, content, mediaUrl, postType, petIdsSet, groupId);

        redirectAttributes.addFlashAttribute("success", "Post created successfully");
        return "redirect:/";
    }

    /**
     * Show all posts page
     */
    @GetMapping("/posts")
    @Transactional
    public String showAllPosts(HttpSession session, Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get all posts with pagination
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postService.getAllPostsPaged(pageRequest);

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);

        return "posts";
    }

    /**
     * Like a post
     */
    @PostMapping("/posts/{id}/like")
    @Transactional
    @ResponseBody
    public Map<String, Object> likePost(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return response;
        }

        Optional<Post> updatedPost = postService.likePost(id, user.getId());

        if (updatedPost.isPresent()) {
            response.put("success", true);
            response.put("likes", updatedPost.get().getLikes());
        } else {
            response.put("success", false);
            response.put("message", "Failed to like post");
        }

        return response;
    }

    /**
     * Unlike a post
     */
    @PostMapping("/posts/{id}/unlike")
    @Transactional
    @ResponseBody
    public Map<String, Object> unlikePost(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return response;
        }

        Optional<Post> updatedPost = postService.unlikePost(id, user.getId());

        if (updatedPost.isPresent()) {
            response.put("success", true);
            response.put("likes", updatedPost.get().getLikes());
        } else {
            response.put("success", false);
            response.put("message", "Failed to unlike post");
        }

        return response;
    }


    @PostMapping("/posts/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Create and save the comment
        boolean success = postService.addComment(id, user.getId(), content);

        if (!success) {
            redirectAttributes.addFlashAttribute("error", "Failed to add comment");
        }

        // Redirect back to the referring page
        return "redirect:" + (request.getHeader("Referer") != null ? request.getHeader("Referer") : "/");
    }

    /**
     * Follow a user
     */
    @PostMapping("/user/{id}/follow")
    public String followUser(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (userService.followUser(user.getId(), id)) {
            redirectAttributes.addFlashAttribute("success", "You are now following this user");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to follow user");
        }

        return "redirect:/user/" + id;
    }

    /**
     * Unfollow a user
     */
    @PostMapping("/user/{id}/unfollow")
    public String unfollowUser(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (userService.unfollowUser(user.getId(), id)) {
            redirectAttributes.addFlashAttribute("success", "You have unfollowed this user");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to unfollow user");
        }

        return "redirect:/user/" + id;
    }

    /**
     * Show all groups page
     */
    @GetMapping("/groups")
    public String showAllGroups(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get all groups
        List<Group> allGroups = groupService.getAllGroups();

        // Get user's groups
        List<Group> userGroups = groupService.getGroupsByMember(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("allGroups", allGroups);
        model.addAttribute("userGroups", userGroups);

        return "groups";
    }

    /**
     * View a single group by ID
     */
    @GetMapping("/groups/{id}")
    @Transactional
    public String viewGroup(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // Get the group by ID
            Optional<Group> groupOpt = groupService.getGroupById(id);
            if (groupOpt.isEmpty()) {
                // Group not found, redirect to groups page with error
                return "redirect:/groups?error=Group not found";
            }

            Group group = groupOpt.get();

            // Check if user is member of the group
            boolean isMember = group.getMembers().contains(currentUser);

            // Get group posts
            List<Post> groupPosts = postService.getPostsByGroup(id);

            // Add necessary data to the model
            model.addAttribute("group", group);
            model.addAttribute("user", currentUser);
            model.addAttribute("isMember", isMember);
            model.addAttribute("posts", groupPosts);
            model.addAttribute("memberCount", group.getMembers().size());

            return "group-detail"; // This is the template name
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/groups?error=Error viewing group: " + e.getMessage();
        }
    }

    /**
     * Show page for creating a new group
     */
    @GetMapping("/groups/create")
    public String showCreateGroupForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("group", new Group());
        model.addAttribute("groupTypes", Group.GroupType.values());

        return "create-group";
    }

    /**
     * Create a new group
     */
    @PostMapping("/groups/create")
    public String createGroup(@ModelAttribute Group group,
                              @RequestParam(required = false) MultipartFile groupImage,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Handle group image upload if provided
        if (groupImage != null && !groupImage.isEmpty()) {
            try {
                // Create directory if it doesn't exist
                String uploadDir = "uploads/group-images/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate a unique filename
                String filename = user.getId() + "_" +
                        System.currentTimeMillis() + "_" +
                        groupImage.getOriginalFilename();

                // Save the file
                Path filePath = uploadPath.resolve(filename);
                Files.copy(groupImage.getInputStream(), filePath);

                // Update the group's picture URL
                group.setPictureUrl("/uploads/group-images/" + filename);

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload group image: " + e.getMessage());
                return "redirect:/groups/create";
            }
        }

        // Create the group
        groupService.createGroup(user.getId(), group);

        redirectAttributes.addFlashAttribute("success", "Group created successfully");
        return "redirect:/groups";
    }

    /**
     * Join a group
     */
    @PostMapping("/groups/{id}/join")
    public String joinGroup(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (groupService.addMemberToGroup(id, user.getId())) {
            redirectAttributes.addFlashAttribute("success", "You have joined the group");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to join group");
        }

        return "redirect:/groups/" + id;
    }

    /**
     * Leave a group
     */
    @PostMapping("/groups/{id}/leave")
    public String leaveGroup(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (groupService.removeMemberFromGroup(id, user.getId())) {
            redirectAttributes.addFlashAttribute("success", "You have left the group");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to leave group. You may be the creator of this group.");
        }

        return "redirect:/groups";
    }

    /**
     * Show all events page
     */
    @GetMapping("/events")
    public String showAllEvents(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Get upcoming events
        List<Event> upcomingEvents = eventService.getUpcomingEvents();

        // Get user's events
        List<Event> userEvents = eventService.getEventsByAttendee(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("userEvents", userEvents);

        return "events";
    }

    /**
     * Show page for creating a new event
     */
    @GetMapping("/events/create")
    public String showCreateEventForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("event", Event.builder().eventDate(LocalDateTime.now().plusDays(1)).build());

        return "create-event";
    }

    /**
     * Create a new event
     */
    /**
     * Create a new event
     */
    @PostMapping("/events/create")
    public String createEvent(@ModelAttribute Event event,
                              @RequestParam(required = false) MultipartFile eventImage,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Handle event image upload if provided
        if (eventImage != null && !eventImage.isEmpty()) {
            try {
                // Create directory if it doesn't exist
                String uploadDir = "uploads/event-images/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate a unique filename
                String filename = user.getId() + "_" +
                        System.currentTimeMillis() + "_" +
                        eventImage.getOriginalFilename();

                // Save the file
                Path filePath = uploadPath.resolve(filename);
                Files.copy(eventImage.getInputStream(), filePath);

                // Update the event's picture URL
                event.setPictureUrl("/uploads/event-images/" + filename);

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload event image: " + e.getMessage());
                return "redirect:/events/create";
            }
        }

        // Create the event
        Optional<Event> createdEvent = eventService.createEvent(user.getId(), event);

        if (createdEvent.isPresent()) {
            redirectAttributes.addFlashAttribute("success", "Event created successfully");
            return "redirect:/events";
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to create event");
            return "redirect:/events/create";
        }
    }

    /**
     * View a single event
     */
    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }

        Event event = eventOpt.get();
        boolean isAttending = event.getAttendees().contains(user);

        model.addAttribute("event", event);
        model.addAttribute("user", user);
        model.addAttribute("isAttending", isAttending);
        model.addAttribute("attendeeCount", event.getAttendees().size());

        return "event-detail";
    }

    /**
     * Attend an event
     */
    @PostMapping("/events/{id}/attend")
    public String attendEvent(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (eventService.addAttendeeToEvent(id, user.getId())) {
            redirectAttributes.addFlashAttribute("success", "You are now attending this event");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to join event");
        }

        return "redirect:/events/" + id;
    }

    /**
     * Leave an event
     */
    @PostMapping("/events/{id}/leave")
    public String leaveEvent(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (eventService.removeAttendeeFromEvent(id, user.getId())) {
            redirectAttributes.addFlashAttribute("success", "You are no longer attending this event");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to leave event");
        }

        return "redirect:/events/" + id;
    }
}