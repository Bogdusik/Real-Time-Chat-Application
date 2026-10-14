package com.bogdusik.chat.dto;

import com.bogdusik.chat.entity.User;

public record UserResponse(Long id, String username) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername());
    }
}
