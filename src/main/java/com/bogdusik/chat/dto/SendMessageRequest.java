package com.bogdusik.chat.dto;

import com.bogdusik.chat.entity.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** STOMP payload sent to {@code /app/sendMessage}: {@code {"content": "...", "user": {"id": 1}}}. */
public record SendMessageRequest(
        @NotBlank(message = "Message content must not be blank")
        @Size(max = Message.MAX_CONTENT_LENGTH,
                message = "Message content must be at most " + Message.MAX_CONTENT_LENGTH + " characters")
        String content,

        @NotNull(message = "Sender is required")
        @Valid
        Sender user) {

    public record Sender(@NotNull(message = "Sender id is required") Long id) {
    }
}
