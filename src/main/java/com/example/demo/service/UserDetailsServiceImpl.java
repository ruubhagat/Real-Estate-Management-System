package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List; // Ensure List is imported
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of Spring Security's UserDetailsService.
 * Loads user-specific data from the database (using UserRepository)
 * to be used for authentication and authorization.
 */
@Service // Marks this class as a Spring Service component
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired // Injects the UserRepository bean
    private UserRepository userRepository;

    /**
     * Locates the user based on the username (which is the email in this application).
     * This method is called by Spring Security during the authentication process (e.g., JWT validation).
     *
     * @param username The username (email) identifying the user whose data is required.
     * @return a UserDetails object containing the user's credentials and authorities (roles).
     * @throws UsernameNotFoundException if the user could not be found or the user has no GrantedAuthority.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Normalize the incoming username (treat as email, trim whitespace, convert to lowercase)
        String email = username.trim().toLowerCase();
        logger.debug("Attempting to load user by username (email): {}", email);

        // Find the user in the database by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    // User not found in the database
                    logger.warn("User not found with email: {}", email);
                    // Throw exception required by the UserDetailsService interface contract
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // --- Convert the user's role string into Spring Security GrantedAuthority objects ---
        // Uses the getRole() method (manual or Lombok-generated) from the User entity
        List<GrantedAuthority> authorities = Stream.of(user.getRole())
                // Ensure the role string exists and is not just whitespace
                .filter(role -> role != null && !role.isBlank())
                // Map the role string (e.g., "CUSTOMER") to a GrantedAuthority object ("ROLE_CUSTOMER")
                // The "ROLE_" prefix is a standard Spring Security convention used by hasRole() checks.
                // .toUpperCase() ensures case-insensitivity from the database storage.
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                // Collect the authorities into a List
                .collect(Collectors.toList());

        // <<<--- ADDED EXTRA LOGGING FOR DEBUGGING ---<<<
        // Print the exact authorities list being created BEFORE returning UserDetails
        System.out.println("[UserDetailsServiceImpl] Authorities created for " + email + ": " + authorities);
        // --- END EXTRA LOGGING ---

        // Check if any authorities were actually created (user might have a null/blank role in DB)
        if (authorities.isEmpty()) {
            logger.warn("User {} has no roles assigned or role is invalid/empty in database.", email);
            // Depending on requirements, you might throw an exception here,
            // assign a default role, or let authentication proceed without roles.
            // For now, we log a warning. Spring Security might deny access later
            // if authorization rules require specific roles.
            // throw new UsernameNotFoundException("User " + email + " has no assigned roles."); // Option
        }

        // Log the loaded user and authorities for debugging purposes
        logger.debug("User found: {}, Authorities loaded: {}", email, authorities);

        // --- Return the Spring Security UserDetails object ---
        // This object encapsulates user information needed by the security framework.
        // It requires username, password (hashed), and authorities.
        // Uses the manual/Lombok-generated getEmail() and getPassword() methods from the User entity.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),    // Principal identifier (username/email)
                user.getPassword(), // The HASHED password from the database
                authorities         // Collection of granted authorities (roles)
        );
        // Other constructor overloads allow setting account status flags (enabled, locked, etc.) if needed.
    }
}