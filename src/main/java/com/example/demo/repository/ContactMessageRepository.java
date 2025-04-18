package com.example.demo.repository;

import com.example.demo.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    // Optional: Find unread messages, sorted by most recent first
    List<ContactMessage> findByIsReadFalseOrderByReceivedAtDesc();
}