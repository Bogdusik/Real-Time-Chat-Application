package com.example.chat_backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    private Message message;
    private User user;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        message = new Message();
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        testTime = LocalDateTime.now();
    }

    @Test
    void testMessageCreation() {
        // Given & When
        message.setId(1L);
        message.setContent("Test message content");
        message.setTimestamp(testTime);
        message.setUser(user);

        // Then
        assertEquals(1L, message.getId());
        assertEquals("Test message content", message.getContent());
        assertEquals(testTime, message.getTimestamp());
        assertEquals(user, message.getUser());
    }

    @Test
    void testMessageWithNullContent() {
        // Given & When
        message.setContent(null);

        // Then
        assertNull(message.getContent());
    }

    @Test
    void testMessageWithEmptyContent() {
        // Given & When
        message.setContent("");

        // Then
        assertEquals("", message.getContent());
    }

    @Test
    void testMessageTimestamp() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        message.setTimestamp(now);
        
        // Then
        assertEquals(now, message.getTimestamp());
    }

    @Test
    void testMessageUserRelationship() {
        // Given
        User testUser = new User();
        testUser.setId(2L);
        testUser.setUsername("anotherUser");
        
        // When
        message.setUser(testUser);
        
        // Then
        assertEquals(testUser, message.getUser());
        assertEquals(2L, message.getUser().getId());
        assertEquals("anotherUser", message.getUser().getUsername());
    }

    @Test
    void testMessageDefaultValues() {
        // Given - новый объект Message
        Message newMessage = new Message();
        
        // Then - проверяем дефолтные значения
        assertNull(newMessage.getId());
        assertNull(newMessage.getContent());
        assertNull(newMessage.getTimestamp());
        assertNull(newMessage.getUser());
    }
}
