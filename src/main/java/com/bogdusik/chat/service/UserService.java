package com.bogdusik.chat.service;

import java.util.Optional;

import com.bogdusik.chat.dto.UserResponse;
import com.bogdusik.chat.entity.User;
import com.bogdusik.chat.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    /**
     * Returns the user with this name (surrounding spaces ignored), creating it on first use. There are no
     * passwords: a name identifies a sender, it does not authenticate anyone.
     */
    public UserResponse register(String username) {
        String name = username.strip();
        return users.findByUsername(name)
                .or(() -> create(name))
                .map(UserResponse::from)
                .orElseThrow();
    }

    private Optional<User> create(String name) {
        try {
            return Optional.of(users.saveAndFlush(new User(name)));
        } catch (DataIntegrityViolationException raced) {
            // another request registered the same name between the lookup and the insert
            return users.findByUsername(name);
        }
    }
}
