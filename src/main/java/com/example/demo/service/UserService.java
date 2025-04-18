package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Import if used here
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Optional: Inject PasswordEncoder if registration/login logic moves here
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    // Example method if you move logic here
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    // Example if login logic was here (demonstrating getter usage)
    /*
    public Optional<User> checkLoginCredentials(String email, String rawPassword) {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Assumes passwordEncoder is injected and User has getPassword() via Lombok
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return userOpt;
            }
        }
        return Optional.empty();
    }
    */

    // Example if registration logic was here
    /*
    public User registerNewUser(User user) {
         // Assumes User has getPassword() and setPassword() via Lombok
         user.setPassword(passwordEncoder.encode(user.getPassword()));
         user.setEmail(user.getEmail().trim().toLowerCase());
         // set default role etc.
         return userRepository.save(user);
    }
    */

}