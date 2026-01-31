package com.example.chat_backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.example.chat_backend.TestConstants;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserCreation() {
        // Given & When
        user.setId(1L);
        user.setUsername(TestConstants.TEST_USERNAME);

        // Then
        assertEquals(1L, user.getId());
        assertEquals(TestConstants.TEST_USERNAME, user.getUsername());
    }

    @Test
    void testUserWithNullUsername() {
        // Given & When
        user.setUsername(null);

        // Then
        assertNull(user.getUsername());
    }

    @Test
    void testUserWithEmptyUsername() {
        // Given & When
        user.setUsername("");

        // Then
        assertEquals("", user.getUsername());
    }

    @Test
    void testUserWithLongUsername() {
        // Given
        String longUsername = "a".repeat(100);
        
        // When
        user.setUsername(longUsername);
        
        // Then
        assertEquals(longUsername, user.getUsername());
        assertEquals(100, user.getUsername().length());
    }

    @Test
    void testUserWithSpecialCharacters() {
        // Given
        String specialUsername = TestConstants.SPECIAL_CHARS_USERNAME;
        
        // When
        user.setUsername(specialUsername);
        
        // Then
        assertEquals(specialUsername, user.getUsername());
    }

    @Test
    void testUserDefaultValues() {
        // Given - новый объект User
        User newUser = new User();
        
        // Then - проверяем дефолтные значения
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
    }

    @Test
    void testUserIdUpdate() {
        // Given
        user.setId(1L);
        
        // When
        user.setId(2L);
        
        // Then
        assertEquals(2L, user.getId());
    }

    @Test
    void testUserUsernameUpdate() {
        // Given
        user.setUsername(TestConstants.OLD_USERNAME);
        
        // When
        user.setUsername(TestConstants.NEW_USERNAME);
        
        // Then
        assertEquals(TestConstants.NEW_USERNAME, user.getUsername());
    }
}
