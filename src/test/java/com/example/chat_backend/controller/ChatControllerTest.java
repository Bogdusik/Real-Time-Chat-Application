package com.example.chat_backend.controller;

import com.example.chat_backend.entity.Message;
import com.example.chat_backend.entity.User;
import com.example.chat_backend.repository.MessageRepository;
import com.example.chat_backend.TestConstants;
import com.example.chat_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatController chatController;

    private User testUser;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(TestConstants.TEST_USERNAME);

        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setContent(TestConstants.TEST_MESSAGE);
        testMessage.setUser(testUser);
        testMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testGetMessages() {
        // Given
        List<Message> expectedMessages = Arrays.asList(testMessage);
        when(messageRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp")))
                .thenReturn(expectedMessages);

        // When
        List<Message> actualMessages = chatController.getMessages();

        // Then
        assertEquals(expectedMessages, actualMessages);
        assertEquals(1, actualMessages.size());
        assertEquals(TestConstants.TEST_MESSAGE, actualMessages.get(0).getContent());
        verify(messageRepository, times(1))
                .findAll(Sort.by(Sort.Direction.ASC, "timestamp"));
    }

    @Test
    void testGetMessagesEmptyList() {
        // Given
        List<Message> emptyList = Arrays.asList();
        when(messageRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp")))
                .thenReturn(emptyList);

        // When
        List<Message> actualMessages = chatController.getMessages();

        // Then
        assertTrue(actualMessages.isEmpty());
        verify(messageRepository, times(1))
                .findAll(Sort.by(Sort.Direction.ASC, "timestamp"));
    }

    @Test
    void testSendMessageWithExistingUser() {
        // Given
        Message inputMessage = new Message();
        inputMessage.setContent(TestConstants.NEW_MESSAGE);
        inputMessage.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(messageRepository.save(any(Message.class))).thenReturn(inputMessage);

        // When
        Message result = chatController.sendMessage(inputMessage);

        // Then
        assertNotNull(result);
        assertEquals(TestConstants.NEW_MESSAGE, result.getContent());
        assertEquals(testUser, result.getUser());
        assertNotNull(result.getTimestamp());
        
        verify(userRepository, times(1)).findById(1L);
        verify(messageRepository, times(1)).save(inputMessage);
    }

    @Test
    void testSendMessageWithNonExistentUser() {
        // Given
        Message inputMessage = new Message();
        inputMessage.setContent("Message from unknown user");
        inputMessage.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(messageRepository.save(any(Message.class))).thenReturn(inputMessage);

        // When
        Message result = chatController.sendMessage(inputMessage);

        // Then
        assertNotNull(result);
        assertEquals("Message from unknown user", result.getContent());
        assertNotNull(result.getTimestamp());
        
        verify(userRepository, times(1)).findById(1L);
        verify(messageRepository, times(1)).save(inputMessage);
    }

    @Test
    void testSendMessageWithNullUser() {
        // Given
        Message inputMessage = new Message();
        inputMessage.setContent("Message without user");
        inputMessage.setUser(null);

        when(messageRepository.save(any(Message.class))).thenReturn(inputMessage);

        // When
        Message result = chatController.sendMessage(inputMessage);

        // Then
        assertNotNull(result);
        assertEquals("Message without user", result.getContent());
        assertNotNull(result.getTimestamp());
        
        verify(userRepository, never()).findById(anyLong());
        verify(messageRepository, times(1)).save(inputMessage);
    }

    @Test
    void testSendMessageWithUserWithoutId() {
        // Given
        User userWithoutId = new User();
        userWithoutId.setUsername(TestConstants.USER_WITHOUT_ID);
        
        Message inputMessage = new Message();
        inputMessage.setContent("Message from user without ID");
        inputMessage.setUser(userWithoutId);

        when(messageRepository.save(any(Message.class))).thenReturn(inputMessage);

        // When
        Message result = chatController.sendMessage(inputMessage);

        // Then
        assertNotNull(result);
        assertEquals("Message from user without ID", result.getContent());
        assertNotNull(result.getTimestamp());
        
        verify(userRepository, never()).findById(anyLong());
        verify(messageRepository, times(1)).save(inputMessage);
    }

    @Test
    void testSendMessageTimestampIsSet() {
        // Given
        Message inputMessage = new Message();
        inputMessage.setContent("Time test message");
        inputMessage.setUser(testUser);
        
        LocalDateTime beforeCall = LocalDateTime.now();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(messageRepository.save(any(Message.class))).thenReturn(inputMessage);

        // When
        Message result = chatController.sendMessage(inputMessage);

        // Then
        LocalDateTime afterCall = LocalDateTime.now();
        
        assertNotNull(result.getTimestamp());
        assertTrue(result.getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(result.getTimestamp().isBefore(afterCall.plusSeconds(1)));
    }
}
