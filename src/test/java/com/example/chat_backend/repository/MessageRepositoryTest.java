package com.example.chat_backend.repository;

import com.example.chat_backend.entity.Message;
import com.example.chat_backend.entity.User;
import com.example.chat_backend.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(TestConstants.TEST_USERNAME);
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void testSaveAndFindMessage() {
        // Given
        Message message = new Message();
        message.setContent(TestConstants.TEST_MESSAGE);
        message.setTimestamp(LocalDateTime.now());
        message.setUser(testUser);

        // When
        Message savedMessage = messageRepository.save(message);
        entityManager.flush();

        // Then
        assertNotNull(savedMessage.getId());
        assertEquals(TestConstants.TEST_MESSAGE, savedMessage.getContent());
        assertEquals(testUser, savedMessage.getUser());
    }

    @Test
    void testFindAllOrderedByTimestamp() {
        // Given
        LocalDateTime time1 = LocalDateTime.now().minusMinutes(2);
        LocalDateTime time2 = LocalDateTime.now().minusMinutes(1);
        LocalDateTime time3 = LocalDateTime.now();

        Message message1 = createMessage("First message", time1);
        Message message2 = createMessage("Second message", time2);
        Message message3 = createMessage("Third message", time3);

        messageRepository.save(message3); // Сохраняем в неправильном порядке
        messageRepository.save(message1);
        messageRepository.save(message2);
        entityManager.flush();

        // When
        List<Message> messages = messageRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp"));

        // Then
        assertEquals(3, messages.size());
        assertEquals("First message", messages.get(0).getContent());
        assertEquals("Second message", messages.get(1).getContent());
        assertEquals("Third message", messages.get(2).getContent());
    }

    @Test
    void testFindAllDescendingOrder() {
        // Given
        LocalDateTime time1 = LocalDateTime.now().minusMinutes(2);
        LocalDateTime time2 = LocalDateTime.now().minusMinutes(1);

        Message message1 = createMessage("Older message", time1);
        Message message2 = createMessage("Newer message", time2);

        messageRepository.save(message1);
        messageRepository.save(message2);
        entityManager.flush();

        // When
        List<Message> messages = messageRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));

        // Then
        assertEquals(2, messages.size());
        assertEquals("Newer message", messages.get(0).getContent());
        assertEquals("Older message", messages.get(1).getContent());
    }

    @Test
    void testMessageUserRelationship() {
        // Given
        Message message = createMessage("Test message", LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);
        entityManager.flush();
        entityManager.clear(); // Очищаем контекст для проверки lazy loading

        // When
        Message foundMessage = messageRepository.findById(savedMessage.getId()).orElse(null);

        // Then
        assertNotNull(foundMessage);
        assertNotNull(foundMessage.getUser());
        assertEquals(testUser.getUsername(), foundMessage.getUser().getUsername());
    }

    @Test
    void testDeleteMessage() {
        // Given
        Message message = createMessage("Message to delete", LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);
        entityManager.flush();

        // When
        messageRepository.deleteById(savedMessage.getId());
        entityManager.flush();

        // Then
        assertFalse(messageRepository.findById(savedMessage.getId()).isPresent());
    }

    @Test
    void testCountMessages() {
        // Given
        messageRepository.save(createMessage("Message 1", LocalDateTime.now()));
        messageRepository.save(createMessage("Message 2", LocalDateTime.now()));
        messageRepository.save(createMessage("Message 3", LocalDateTime.now()));
        entityManager.flush();

        // When
        long count = messageRepository.count();

        // Then
        assertEquals(3, count);
    }

    private Message createMessage(String content, LocalDateTime timestamp) {
        Message message = new Message();
        message.setContent(content);
        message.setTimestamp(timestamp);
        message.setUser(testUser);
        return message;
    }
}
