package com.petconnect.petsocial.controller;

import com.petconnect.petsocial.auth.AuthenticationStrategy;
import com.petconnect.petsocial.auth.EmailAuth;
import com.petconnect.petsocial.auth.PhoneAuth;
import com.petconnect.petsocial.model.User;
import com.petconnect.petsocial.service.AuthenticationService;
import com.petconnect.petsocial.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;

/**
 * Controller for authentication-related operations
 * Follows Single Responsibility Principle by focusing only on authentication
 */
@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationService authService;
    private final UserService userService;
    private final EmailAuth emailAuth;
    private final PhoneAuth phoneAuth;
    
    /**
     * Initialize authentication strategies when the controller is created
     */
    @PostConstruct
    public void init() {
        authService.registerStrategy(emailAuth);
        authService.registerStrategy(phoneAuth);
    }
    
    /**
     * Show the login page
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
    
    /**
     * Process a login attempt
     */
    @PostMapping("/login")
    public String login(@RequestParam String identifier,
                        @RequestParam String password,
                        @RequestParam(defaultValue = "EMAIL") String authMethod,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        System.out.println("Login attempt: identifier=" + identifier + ", authMethod=" + authMethod);

        boolean authenticated = false;
        User user = null;

        try {
            if (authMethod.equals("EMAIL")) {
                System.out.println("Authenticating with EMAIL strategy");
                authenticated = authService.authenticate("EMAIL", identifier, password);
                System.out.println("Authentication result: " + authenticated);

                if (authenticated) {
                    user = userService.getUserByEmail(identifier).orElse(null);
                    System.out.println("User found by email: " + (user != null ? "yes (id: " + user.getId() + ")" : "no"));
                }
            } else if (authMethod.equals("PHONE")) {
                System.out.println("Authenticating with PHONE strategy");
                authenticated = authService.authenticate("PHONE", identifier, password);
                System.out.println("Authentication result: " + authenticated);

                if (authenticated) {
                    user = userService.getUserByPhone(identifier).orElse(null);
                    System.out.println("User found by phone: " + (user != null ? "yes (id: " + user.getId() + ")" : "no"));
                }
            }

            if (authenticated && user != null) {
                // Store user in session
                session.setAttribute("user", user);
                System.out.println("User set in session. Redirecting to home page.");
                return "redirect:/";
            }

            System.out.println("Authentication failed or user not found");
            redirectAttributes.addFlashAttribute("error", "Invalid credentials");
            return "redirect:/login";
        } catch (Exception e) {
            System.out.println("Exception during login: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Login error: " + e.getMessage());
            return "redirect:/login";
        }
    }


    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    /**
     * Process a registration attempt
     */
    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                          RedirectAttributes redirectAttributes) {
        
        // Check if username, email, or phone is already taken
        if (authService.isUsernameTaken(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username is already taken");
            return "redirect:/register";
        }
        
        if (authService.isEmailRegistered(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email is already registered");
            return "redirect:/register";
        }
        
        if (user.getPhone() != null && !user.getPhone().isEmpty() && 
            authService.isPhoneRegistered(user.getPhone())) {
            redirectAttributes.addFlashAttribute("error", "Phone number is already registered");
            return "redirect:/register";
        }
        
        // Set current time as last login
        user.setLastLogin(LocalDateTime.now().toString());
        
        // Register the user
        authService.registerUser(user);
        
        redirectAttributes.addFlashAttribute("success", "Registration successful. Please login.");
        return "redirect:/login";
    }


    /**
     * Process a logout attempt
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
