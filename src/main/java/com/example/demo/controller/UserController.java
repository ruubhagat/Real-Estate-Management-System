package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
// No PropertyService needed here
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users") // Base path ONLY for users
@CrossOrigin(origins = "http://localhost:3001")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    // No PropertyService needed here

    // --- User Endpoints ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        logger.info("Received registration request for email: {}", request.getEmail());

        // <<<--- CORRECTED IF STATEMENT (Line ~44) ---vvv
        // Check for null or blank values for required fields
        if (request.getName() == null || request.getName().isBlank() ||
                request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank())
        // <<<--- END CORRECTED IF ---vvv
        {
            logger.warn("Registration failed: Missing required fields.");
            return ResponseEntity.badRequest().body(Map.of("message", "Name, email, and password are required."));
        }
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Registration failed: Email already registered: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already registered."));
        }
        User newUser = new User();
        // Uses manual setters
        newUser.setName(request.getName().trim());
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        String role = (request.getRole() != null && !request.getRole().isBlank()) ? request.getRole().trim().toUpperCase() : "CUSTOMER";
        newUser.setRole(role);
        try {
            User savedUser = userRepository.save(newUser);
            savedUser.setPassword(null); // Uses manual setter
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully!", "user", savedUser));
        } catch (Exception e) {
            logger.error("Error during user registration for email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error during registration."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        logger.debug("Received login request for email: {}", request.getEmail());

        // <<<--- CORRECTED IF STATEMENT (Line ~71) ---vvv
        // Check for null or blank values
        if (request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank())
        // <<<--- END CORRECTED IF ---vvv
        {
            logger.warn("Login failed: Missing email or password.");
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required."));
        }
        String email = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Uses manual getter
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.info("Login successful for user: {}", email);
                // Uses manual getters
                org.springframework.security.core.userdetails.User springSecurityUser =
                        new org.springframework.security.core.userdetails.User(
                                user.getEmail(), user.getPassword(),
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                        );
                String jwtToken = jwtService.generateToken(springSecurityUser);
                // Uses manual getters
                return ResponseEntity.ok(Map.of(
                        "message", "Login successful", "token", jwtToken,
                        "userId", user.getId(), "userEmail", user.getEmail(), "userRole", user.getRole()
                ));
            } else {
                logger.warn("Login failed: Invalid credentials for user: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials."));
            }
        } else {
            logger.warn("Login failed: User not found: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials."));
        }
    }

    @GetMapping("/test-auth") // Endpoint is now /api/users/test-auth
    public ResponseEntity<String> testAuthentication() {
        logger.debug("Received request for /test-auth");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated or authentication is anonymous.");
        }
        String currentPrincipalName = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", "));
        return ResponseEntity.ok("Authenticated! User: " + currentPrincipalName + ", Roles: [" + roles + "]");
    }

    // NO PROPERTY ENDPOINTS IN THIS CONTROLLER
}