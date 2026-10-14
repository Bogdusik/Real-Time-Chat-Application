package com.bogdusik.chat.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bogdusik.chat.dto.MessageResponse;
import com.bogdusik.chat.dto.SendMessageRequest;
import com.bogdusik.chat.entity.Message;
import com.bogdusik.chat.entity.User;
import com.bogdusik.chat.repository.MessageRepository;
import com.bogdusik.chat.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    public static final int DEFAULT_HISTORY_LIMIT = 50;
    public static final int MAX_HISTORY_LIMIT = 100;

    private final MessageRepository messages;
    private final UserRepository users;
    private final Clock clock;

    public MessageService(MessageRepository messages, UserRepository users, Clock clock) {
        this.messages = messages;
        this.users = users;
        this.clock = clock;
    }

    /**
     * Saves a message from a registered sender. The transaction commits before this method returns, so the
     * caller broadcasts only messages that are already in the database.
     */
    @Transactional
    public MessageResponse send(SendMessageRequest request) {
        Long senderId = request.user().id();
        User sender = users.findById(senderId).orElseThrow(() -> new UnknownUserException(senderId));
        // milliseconds: PostgreSQL keeps microseconds, so a finer value would differ between broadcast and history
        Instant sentAt = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        Message saved = messages.save(new Message(request.content(), sentAt, sender));
        return MessageResponse.from(saved);
    }

    /** The latest {@code limit} messages (clamped to 1..{@value #MAX_HISTORY_LIMIT}), oldest first. */
    @Transactional(readOnly = true)
    public List<MessageResponse> history(int limit) {
        int size = Math.max(1, Math.min(limit, MAX_HISTORY_LIMIT));
        List<MessageResponse> latest = new ArrayList<>(
                messages.findLatest(PageRequest.of(0, size)).stream().map(MessageResponse::from).toList());
        Collections.reverse(latest);
        return latest;
    }
}
