package com.example.chat_backend.repository;

import com.example.chat_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {
        // Given
        User user = new User();
        user.setUsername("testUser");

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("testUser", savedUser.getUsername());
    }

    @Test
    void testFindById() {
        // Given
        User user = new User();
        user.setUsername("findByIdTest");
        User savedUser = entityManager.persistAndFlush(user);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("findByIdTest", foundUser.get().getUsername());
        assertEquals(savedUser.getId(), foundUser.get().getId());
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<User> foundUser = userRepository.findById(nonExistentId);

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testUpdateUser() {
        // Given
        User user = new User();
        user.setUsername("originalUsername");
        User savedUser = entityManager.persistAndFlush(user);

        // When
        savedUser.setUsername("updatedUsername");
        User updatedUser = userRepository.save(savedUser);
        entityManager.flush();

        // Then
        assertEquals("updatedUsername", updatedUser.getUsername());
        assertEquals(savedUser.getId(), updatedUser.getId());
    }

    @Test
    void testDeleteUser() {
        // Given
        User user = new User();
        user.setUsername("userToDelete");
        User savedUser = entityManager.persistAndFlush(user);

        // When
        userRepository.deleteById(savedUser.getId());
        entityManager.flush();

        // Then
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testFindAllUsers() {
        // Given
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");
        User user3 = new User();
        user3.setUsername("user3");

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);

        // When
        Iterable<User> users = userRepository.findAll();

        // Then
        assertNotNull(users);
        long count = 0;
        for (User user : users) {
            count++;
            assertNotNull(user.getId());
            assertNotNull(user.getUsername());
        }
        assertEquals(3, count);
    }

    @Test
    void testCountUsers() {
        // Given
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // When
        long count = userRepository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    void testExistsById() {
        // Given
        User user = new User();
        user.setUsername("existsTest");
        User savedUser = entityManager.persistAndFlush(user);

        // When & Then
        assertTrue(userRepository.existsById(savedUser.getId()));
        assertFalse(userRepository.existsById(999L));
    }

    @Test
    void testSaveUserWithNullUsername() {
        // Given
        User user = new User();
        user.setUsername(null);

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();

        // Then
        assertNotNull(savedUser.getId());
        assertNull(savedUser.getUsername());
    }

    @Test
    void testSaveUserWithEmptyUsername() {
        // Given
        User user = new User();
        user.setUsername("");

        // When
        User savedUser = userRepository.save(user);
        entityManager.flush();

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("", savedUser.getUsername());
    }
}
