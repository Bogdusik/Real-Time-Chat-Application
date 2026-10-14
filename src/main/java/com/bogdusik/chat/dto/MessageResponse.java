package com.bogdusik.chat.dto;

import java.time.Instant;

import com.bogdusik.chat.entity.Message;

/** A message as the clients see it, both in the broadcast and in the history. */
public record MessageResponse(Long id, String content, Instant timestamp, UserResponse user) {

    public static MessageResponse from(Message message) {
        UserResponse user = message.getUser() == null ? null : UserResponse.from(message.getUser());
        return new MessageResponse(message.getId(), message.getContent(), message.getSentAt(), user);
    }
}
