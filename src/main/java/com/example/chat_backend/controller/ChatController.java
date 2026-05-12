package com.example.chat_backend.controller;

import com.example.chat_backend.entity.Message;
import com.example.chat_backend.entity.User;
import com.example.chat_backend.repository.MessageRepository;
import com.example.chat_backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ChatController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/messages")
    @ResponseBody
    public List<Message> getMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        int clampedSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, clampedSize, Sort.by(Sort.Direction.ASC, "timestamp"));
        return messageRepository.findAll(pageable).getContent();
    }

    @PostMapping("/api/users/register")
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username cannot be empty"));
        }
        if (username.length() > 50) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username too long (max 50 chars)"));
        }
        User existing = userRepository.findByUsername(username.trim());
        if (existing != null) {
            return ResponseEntity.ok(existing);
        }
        User newUser = new User();
        newUser.setUsername(username.trim());
        return ResponseEntity.ok(userRepository.save(newUser));
    }

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        if (message.getContent() == null || message.getContent().isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (message.getContent().length() > 5000) {
            throw new IllegalArgumentException("Message content exceeds maximum length");
        }

        message.setTimestamp(LocalDateTime.now());

        if (message.getUser() != null && message.getUser().getId() != null) {
            Optional<User> userOptional = userRepository.findById(message.getUser().getId());
            userOptional.ifPresent(message::setUser);
        }

        messageRepository.save(message);
        return message;
    }
}