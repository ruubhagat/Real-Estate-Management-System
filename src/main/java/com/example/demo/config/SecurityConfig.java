package com.example.demo.config;

import com.example.demo.config.filter.JwtAuthenticationFilter;
import com.example.demo.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Ensure HttpMethod is imported
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Enables @PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration class.
 * Enables web security, method-level security (@PreAuthorize), and configures JWT, CORS,
 * authentication providers, and HTTP authorization rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // *** Crucial for @PreAuthorize annotations in controllers to work ***
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Your custom UserDetailsService

    @Value("${cors.allowed.origin}") // Allowed origin from application.properties
    private String allowedOrigin;

    /**
     * Defines the PasswordEncoder bean using BCrypt.
     * Used for hashing passwords during registration and verifying them during login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the AuthenticationProvider bean (DaoAuthenticationProvider).
     * Uses the custom UserDetailsService and PasswordEncoder to authenticate users
     * based on credentials stored in the database.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // How to find the user
        authProvider.setPasswordEncoder(passwordEncoder());     // How to check the password
        return authProvider;
    }

    /**
     * Defines the AuthenticationManager bean, needed for the login process.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings.
     * Allows requests from the specified frontend origin (e.g., http://localhost:3001).
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin)); // Read from properties
        // Specify allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        // Specify allowed headers in requests
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true); // Allow cookies/auth headers
        configuration.setMaxAge(3600L); // How long pre-flight response can be cached
        // Apply this configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Defines the main SecurityFilterChain bean, configuring HTTP security rules.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Apply the CORS configuration bean defined above
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection (common for stateless APIs using tokens)
                .requestCache(RequestCacheConfigurer::disable) // Disable request caching

                // Configure authorization rules for different request paths
                .authorizeHttpRequests(authz -> authz
                        // --- Public Endpoints (No authentication required) ---
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll() // User registration and login
                        .requestMatchers(HttpMethod.POST, "/api/public/contact").permitAll() // Public contact form submission
                        .requestMatchers("/uploads/**").permitAll() // Allow access to uploaded static images/files

                        // --- General Property Endpoints ---
                        // VVV--- RULE FOR VIEWING PROPERTIES ---VVV
                        // Allows ANY authenticated user (Customer, Owner, Admin) to view property lists and details
                        .requestMatchers(HttpMethod.GET, "/api/properties", "/api/properties/**").authenticated()
                        // VVV--- END RULE FOR VIEWING ---VVV
                        // Allows ANY authenticated user to *attempt* to create a property (service layer handles owner assignment)
                        .requestMatchers(HttpMethod.POST, "/api/properties").authenticated()

                        // --- Owner Property Endpoints (Modify/Delete/Upload Images) ---
                        // Requires authentication. Specific ownership check is done using @PreAuthorize in OwnerPropertyController.
                        .requestMatchers("/api/owner/properties/**").authenticated()

                        // --- Booking Endpoints ---
                        // Requires authentication. Specific role/ownership checks are handled in BookingService or with @PreAuthorize in BookingController.
                        .requestMatchers("/api/bookings/**").authenticated()

                        // --- Admin Endpoints ---
                        // Requires the user to have the 'ADMIN' role (prefixed with ROLE_ internally by Spring Security)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // --- Other Authenticated Endpoints ---
                        .requestMatchers("/api/users/test-auth").authenticated() // Example test endpoint

                        // --- Default Rule ---
                        // Any other request not explicitly matched above requires authentication
                        .anyRequest().authenticated()
                )

                // Configure session management to be stateless (no HTTP sessions created)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Register the custom AuthenticationProvider (uses UserDetailsService + PasswordEncoder)
                .authenticationProvider(authenticationProvider())

                // Add the custom JWT Filter to process the Authorization header before the standard authentication filters
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Build and return the configured HttpSecurity object
        return http.build();
    }
}