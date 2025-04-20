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
@CrossOrigin(origins = "http://localhost:3001") // Adjust CORS as needed
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    // --- User Registration Endpoint ---
    @PostMapping("/register")
    // Consider adding @Valid if you add validation annotations to RegisterRequest DTO
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        logger.info("Received registration request for email: {}", request.getEmail());

        // 1. Basic Input Validation (Check for null or blank required fields)
        if (request.getName() == null || request.getName().isBlank() ||
                request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank())
        {
            logger.warn("Registration failed: Missing required fields (Name, Email, Password).");
            // Return 400 Bad Request for invalid input
            return ResponseEntity.badRequest().body(Map.of("message", "Name, email, and password are required."));
        }

        // 2. Role Validation (Prevent self-registration as ADMIN)
        String requestedRole = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().trim().toUpperCase()
                : "CUSTOMER"; // Default to CUSTOMER if role is missing/blank

        // --- VVV ADDED ROLE VALIDATION VVV ---
        if ("ADMIN".equals(requestedRole)) {
            logger.warn("Registration attempt with ADMIN role rejected for email: {}", request.getEmail());
            // Return 403 Forbidden as this action is not allowed via this endpoint
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Cannot register as ADMIN via this form."));
        }
        // --- ^^^ END ROLE VALIDATION ^^^ ---

        // 3. Check if Email Already Exists
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Registration failed: Email already registered: {}", email);
            // Return 409 Conflict for existing resource
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email already registered."));
        }

        // 4. Create and Save New User
        User newUser = new User();
        newUser.setName(request.getName().trim());
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password
        newUser.setRole(requestedRole); // Set validated role (CUSTOMER or PROPERTY_OWNER)

        try {
            User savedUser = userRepository.save(newUser);
            logger.info("User registered successfully with ID: {} and Email: {}", savedUser.getId(), savedUser.getEmail());

            // --- IMPORTANT: Do NOT return the password hash in the response ---
            // Create a response map or a UserDTO that excludes sensitive info
            Map<String, Object> userResponse = Map.of(
                    "id", savedUser.getId(),
                    "name", savedUser.getName(),
                    "email", savedUser.getEmail(),
                    "role", savedUser.getRole()
            );

            // Return 201 Created status with success message and user info (excluding password)
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "User registered successfully!",
                    "user", userResponse
            ));
        } catch (Exception e) { // Catch potential database or other exceptions during save
            logger.error("Error during user registration persistence for email {}: {}", email, e.getMessage(), e);
            // Return 500 Internal Server Error for unexpected issues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error during registration. Please try again later."));
        }
    }

    // --- User Login Endpoint ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        logger.debug("Received login request for email: {}", request.getEmail());

        // Basic Input Validation
        if (request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank())
        {
            logger.warn("Login failed: Missing email or password.");
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required."));
        }

        String email = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Verify the provided password against the stored hash
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.info("Login successful for user: {}", email);

                // Create UserDetails for JWT generation (needed by JwtService)
                // Ensure roles are prefixed with ROLE_ for Spring Security's hasRole checks
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
                org.springframework.security.core.userdetails.User springSecurityUser =
                        new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPassword(), // Password hash is needed here by User constructor, but not exposed
                                authorities
                        );

                // Generate JWT Token
                String jwtToken = jwtService.generateToken(springSecurityUser);

                // Return successful response with token and user info (excluding password)
                return ResponseEntity.ok(Map.of(
                        "message", "Login successful",
                        "token", jwtToken,
                        "userId", user.getId(),
                        "userEmail", user.getEmail(),
                        "userRole", user.getRole()
                ));
            } else {
                // Password mismatch
                logger.warn("Login failed: Invalid credentials (password mismatch) for user: {}", email);
                // Return 401 Unauthorized for bad credentials
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials."));
            }
        } else {
            // User email not found
            logger.warn("Login failed: User not found: {}", email);
            // Return 401 Unauthorized (don't reveal if user exists or password was wrong)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials."));
        }
    }

    // --- Authentication Test Endpoint ---
    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuthentication() {
        logger.debug("Received request for /test-auth");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            logger.warn("/test-auth called without proper authentication.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated or authentication is anonymous.");
        }

        String currentPrincipalName = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", "));
        logger.info("/test-auth successful for User: {}, Roles: [{}]", currentPrincipalName, roles);
        return ResponseEntity.ok("Authenticated! User: " + currentPrincipalName + ", Roles: [" + roles + "]");
    }

}