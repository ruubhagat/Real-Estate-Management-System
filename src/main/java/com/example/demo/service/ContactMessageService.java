package com.example.demo.service;

import com.example.demo.model.ContactMessage;
import com.example.demo.repository.ContactMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ContactMessageService.class);

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Transactional
    public ContactMessage saveMessage(ContactMessage message) {
        // Use manual getters
        logger.info("Saving contact message from: {}", message.getEmail());
        if (message.getName() == null || message.getEmail() == null || message.getMessage() == null ||
                message.getName().isBlank() || message.getEmail().isBlank() || message.getMessage().isBlank()) {
            throw new IllegalArgumentException("Name, email, and message cannot be empty.");
        }
        // isRead defaults to false, receivedAt set by @PrePersist
        return contactMessageRepository.save(message);
    }

    // --- Methods for Admin to view/manage messages (Needs Security) ---
    @Transactional(readOnly = true)
    public List<ContactMessage> getAllMessages() {
        // TODO: Add @PreAuthorize("hasRole('ADMIN')") or check role here
        logger.debug("Admin: Fetching all contact messages.");
        return contactMessageRepository.findAll();
    }

    @Transactional
    public boolean markAsRead(Long messageId) {
        // TODO: Add @PreAuthorize("hasRole('ADMIN')") or check role here
        return contactMessageRepository.findById(messageId)
                .map(message -> {
                    message.setRead(true); // Use manual setter
                    contactMessageRepository.save(message);
                    logger.info("Admin: Marked contact message {} as read.", messageId);
                    return true;
                })
                .orElse(false);
    }
}