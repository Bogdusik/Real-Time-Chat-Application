package com.bogdusik.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.bogdusik.chat.dto.UserResponse;
import com.bogdusik.chat.entity.User;
import com.bogdusik.chat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository users;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(users);
    }

    @Test
    void registerReturnsTheExistingUserWithoutCreatingAnother() {
        when(users.findByUsername("alice")).thenReturn(Optional.of(withId(new User("alice"), 1L)));

        UserResponse user = service.register("alice");

        assertThat(user).isEqualTo(new UserResponse(1L, "alice"));
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void registerCreatesANewUserWithTheTrimmedName() {
        when(users.findByUsername("bob")).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(User.class))).thenAnswer(call -> withId(call.getArgument(0), 2L));

        UserResponse user = service.register("  bob ");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("bob");
        assertThat(user).isEqualTo(new UserResponse(2L, "bob"));
    }

    @Test
    void registerReturnsTheWinnerWhenAnotherRequestCreatedTheSameNameFirst() {
        when(users.findByUsername("carol"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(withId(new User("carol"), 3L)));
        when(users.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThat(service.register("carol")).isEqualTo(new UserResponse(3L, "carol"));
    }

    private static User withId(User user, Long id) {
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
