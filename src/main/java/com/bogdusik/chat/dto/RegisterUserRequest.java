package com.bogdusik.chat.dto;

import com.bogdusik.chat.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank(message = "Username must not be blank")
        @Size(max = User.MAX_USERNAME_LENGTH,
                message = "Username must be at most " + User.MAX_USERNAME_LENGTH + " characters")
        String username) {
}
