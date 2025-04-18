package com.example.demo.controller;

import com.example.demo.model.ContactMessage;
import com.example.demo.service.ContactMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public") // Base path for public endpoints
@CrossOrigin(origins = "*") // Allow all origins for public or use specific from config
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping("/contact")
    public ResponseEntity<?> submitContactForm(@RequestBody ContactMessage contactMessage) {
        // Uses manual getter
        logger.info("Received contact form submission from: {}", contactMessage.getEmail());
        try {
            ContactMessage savedMessage = contactMessageService.saveMessage(contactMessage);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Message received successfully. Thank you!"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid contact form data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving contact message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send message due to a server error."));
        }
    }
}