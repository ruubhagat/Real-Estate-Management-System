package com.example.demo.config;

import com.example.demo.config.filter.JwtAuthenticationFilter;
import com.example.demo.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Import HttpMethod
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
 * Enables web security, method-level security, and configures JWT, CORS,
 * authentication providers, and authorization rules.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Value("${cors.allowed.origin}")
    private String allowedOrigin;

    /**
     * Defines the PasswordEncoder bean (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the AuthenticationProvider bean.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Defines the AuthenticationManager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the CORS configuration source bean.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin)); // Use value from properties
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Defines the main SecurityFilterChain bean.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Apply CORS config bean
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF
                .requestCache(RequestCacheConfigurer::disable) // Disable request cache

                // Configure authorization rules using lambda DSL
                .authorizeHttpRequests(authz -> authz
                        // --- Public Endpoints ---
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/public/contact").permitAll()
                        .requestMatchers("/uploads/**").permitAll() // Allow public access to uploaded files

                        // --- Property Endpoints (Require Authentication) ---
                        // Any authenticated user can view properties
                        .requestMatchers(HttpMethod.GET, "/api/properties", "/api/properties/**").authenticated()
                        // Any authenticated user can create/update/delete (service layer handles ownership/admin check)
                        .requestMatchers(HttpMethod.POST, "/api/properties").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/properties/{id}/images").authenticated() // Image upload
                        .requestMatchers(HttpMethod.PUT, "/api/properties/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/properties/**").authenticated()

                        // --- Booking Endpoints ---
                        // Any authenticated user can POST a booking request
                        .requestMatchers(HttpMethod.POST, "/api/bookings").authenticated()
                        // Viewing lists requires specific roles (or use @PreAuthorize in controller)
                        .requestMatchers("/api/bookings/my/customer").authenticated() // Rely on @PreAuthorize("hasRole('CUSTOMER')") in controller
                        .requestMatchers("/api/bookings/my/owner").hasRole("PROPERTY_OWNER") // Or use HttpSecurity rule
                        // Viewing/updating specific booking requires auth (service layer checks details)
                        .requestMatchers(HttpMethod.GET, "/api/bookings/{id}").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/{id}/status").authenticated()

                        // --- Admin Endpoints ---
                        // All requests under /api/admin/ require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // --- Other Authenticated Endpoints ---
                        .requestMatchers("/api/users/test-auth").authenticated()


                        // --- Default Rule ---
                        // Any other request not explicitly matched above requires authentication
                        .anyRequest().authenticated()
                )

                // Configure session management to be stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Register the custom AuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // Add the custom JWT Filter before the standard UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Build and return the configured HttpSecurity object
        return http.build();
    }
}