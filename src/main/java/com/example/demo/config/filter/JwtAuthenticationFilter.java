package com.example.demo.config.filter;

import com.example.demo.service.JwtService;
import com.example.demo.service.UserDetailsServiceImpl; // Use the specific implementation
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// Using SLF4J logger is generally better than System.out, but System.out is fine for quick debugging
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Mark this as a Spring component to be automatically detected
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Optional: Use SLF4J logger for better logging control
    // private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Inject specific UserDetailsService implementation

    /**
     * Filters incoming requests to check for a valid JWT in the Authorization header.
     * If valid, it sets the authentication context for Spring Security.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain // Passes the request to the next filter in the chain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // --- Skip filter for login/register --- Optional optimization
        // if (request.getServletPath().contains("/api/users/login") || request.getServletPath().contains("/api/users/register")) {
        //    filterChain.doFilter(request, response);
        //    return;
        // }

        // 1. Check if Authorization header is missing or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continue chain, security context remains unauthenticated
            return;
        }

        // 2. Extract the JWT token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);

        try {
            // 3. Extract user email (subject) from JWT using JwtService
            userEmail = jwtService.extractUsername(jwt);

            // 4. Check: Is email present in token AND is user NOT already authenticated in this request?
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Load UserDetails from the database via UserDetailsService
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 6. Validate the token: Checks signature, expiration, and compares username.
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // 7. If token is valid, create an Authentication object for Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,           // Principal
                            null,                  // Credentials (null for token auth)
                            userDetails.getAuthorities() // Authorities (roles) loaded by UserDetailsService
                    );

                    // 8. Set additional details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 9. Update the SecurityContextHolder - THIS AUTHENTICATES THE USER FOR THIS REQUEST
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // <<<--- ADDED LOGGING LINE FOR DEBUGGING ROLES --->>>
                    System.out.println("[JwtAuthFilter] User authenticated: " + userEmail + ", Roles from UserDetails: " + userDetails.getAuthorities());

                } else {
                    System.out.println("[JwtAuthFilter] Token validation failed for user: " + userEmail); // Log validation failure
                }
            } else {
                // Optional log if email null or user already authenticated
                // System.out.println("[JwtAuthFilter] Skipping auth context update. Email: " + userEmail + ", Existing Auth: " + (SecurityContextHolder.getContext().getAuthentication() != null));
            }
        } catch (ExpiredJwtException e) {
            System.err.println("[JwtAuthFilter] JWT token is expired: " + e.getMessage());
        } catch (SignatureException | MalformedJwtException e) {
            System.err.println("[JwtAuthFilter] JWT token is invalid ("+ e.getClass().getSimpleName() +"): " + e.getMessage());
        } catch (UsernameNotFoundException e) {
            System.err.println("[JwtAuthFilter] User not found during JWT validation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[JwtAuthFilter] Cannot set user authentication: " + e.getMessage());
        }

        // Clear context if an error occurred that wasn't handled above, just in case
        // Although specific catches should ideally handle context clearing if needed
        // if (SecurityContextHolder.getContext().getAuthentication() != null && !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
        //     SecurityContextHolder.clearContext();
        // }


        // 10. Continue the filter chain for the request to proceed to the controller
        filterChain.doFilter(request, response);
    }
}