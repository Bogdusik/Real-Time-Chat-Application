package com.bogdusik.chat.repository;

import java.util.Optional;

import com.bogdusik.chat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
