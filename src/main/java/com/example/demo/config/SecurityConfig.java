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
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer; // Often needed if disabling CSRF
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
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the AuthenticationProvider bean (DaoAuthenticationProvider).
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
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin)); // Read from properties
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply CORS to all paths
        return source;
    }

    /**
     * Defines the main SecurityFilterChain bean, configuring HTTP security rules.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Apply CORS using the bean defined above
                .cors(Customizer.withDefaults())

                // Disable CSRF protection - common for stateless APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Disable default request caching
                .requestCache(RequestCacheConfigurer::disable)

                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // --- Public Endpoints ---
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll() // Registration & Login
                        .requestMatchers(HttpMethod.POST, "/api/public/contact").permitAll() // Public Contact Form
                        .requestMatchers("/uploads/**").permitAll() // Static images/files

                        // --- General Property Endpoints ---
                        .requestMatchers(HttpMethod.GET, "/api/properties", "/api/properties/**").authenticated() // View properties
                        .requestMatchers(HttpMethod.POST, "/api/properties").authenticated() // Attempt to create property

                        // --- Owner Property Endpoints ---
                        .requestMatchers("/api/owner/properties/**").authenticated() // Modify/Delete/Upload Images (requires ownership check via @PreAuthorize)

                        // --- Booking Endpoints ---
                        .requestMatchers("/api/bookings/**").authenticated() // General booking actions (requires role/ownership check via @PreAuthorize)

                        // --- Payment Endpoints ---
                        .requestMatchers("/api/payments/**").authenticated() // Payment confirmation (requires role/ownership check via @PreAuthorize)

                        // --- Admin Endpoints ---
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Only ADMIN role

                        // --- Other Authenticated ---
                        .requestMatchers("/api/users/test-auth").authenticated() // Test endpoint

                        // --- Default Rule ---
                        .anyRequest().authenticated() // All other unspecified requests require authentication
                )

                // Set session management to STATELESS (no server-side session)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure the AuthenticationProvider bean
                .authenticationProvider(authenticationProvider())

                // Add the custom JWT filter before the standard username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

        // Optional: Configure Headers, e.g., for H2 console if used during dev
        // .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // Example for H2 console
        ;

        // Build and return the configured security filter chain
        return http.build();
    }
}