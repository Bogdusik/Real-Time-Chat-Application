package com.bogdusik.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.bogdusik.chat.dto.MessageResponse;
import com.bogdusik.chat.dto.SendMessageRequest;
import com.bogdusik.chat.entity.Message;
import com.bogdusik.chat.entity.User;
import com.bogdusik.chat.repository.MessageRepository;
import com.bogdusik.chat.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    @Mock
    private MessageRepository messages;

    @Mock
    private UserRepository users;

    private MessageService service;
    private User alice;

    @BeforeEach
    void setUp() {
        service = new MessageService(messages, users, Clock.fixed(NOW, ZoneOffset.UTC));
        alice = withId(new User("alice"), 1L);
    }

    @Test
    void sendSavesTheMessageWithTheSenderAndServerTimeAndReturnsTheSavedVersion() {
        when(users.findById(1L)).thenReturn(Optional.of(alice));
        when(messages.save(any(Message.class))).thenAnswer(call -> withId(call.getArgument(0), 42L));

        MessageResponse response = service.send(request("Hello", 1L));

        ArgumentCaptor<Message> saved = ArgumentCaptor.forClass(Message.class);
        verify(messages).save(saved.capture());
        assertThat(saved.getValue().getContent()).isEqualTo("Hello");
        assertThat(saved.getValue().getUser()).isSameAs(alice);
        assertThat(saved.getValue().getSentAt()).isEqualTo(NOW);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.content()).isEqualTo("Hello");
        assertThat(response.timestamp()).isEqualTo(NOW);
        assertThat(response.user().id()).isEqualTo(1L);
        assertThat(response.user().username()).isEqualTo("alice");
    }

    @Test
    void sendRejectsAnUnknownSenderWithoutSaving() {
        when(users.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.send(request("Hello", 7L)))
                .isInstanceOf(UnknownUserException.class)
                .hasMessageContaining("user 7");
        verify(messages, never()).save(any());
    }

    @Test
    void historyReturnsTheLatestMessagesOldestFirst() {
        Message first = message(1L, "first", NOW.minusSeconds(20));
        Message second = message(2L, "second", NOW.minusSeconds(10));
        Message third = message(3L, "third", NOW);
        when(messages.findLatest(any(Pageable.class))).thenReturn(List.of(third, second, first));

        List<MessageResponse> history = service.history(50);

        assertThat(history).extracting(MessageResponse::content).containsExactly("first", "second", "third");
        verify(messages).findLatest(PageRequest.of(0, 50));
    }

    @Test
    void historyClampsTheLimit() {
        when(messages.findLatest(any(Pageable.class))).thenReturn(List.of());

        service.history(1_000);
        verify(messages).findLatest(PageRequest.of(0, MessageService.MAX_HISTORY_LIMIT));

        service.history(0);
        verify(messages).findLatest(PageRequest.of(0, 1));
    }

    @Test
    void historyKeepsMessagesWithoutASender() {
        Message anonymous = withId(new Message("old message", NOW, null), 5L);
        when(messages.findLatest(any(Pageable.class))).thenReturn(List.of(anonymous));

        assertThat(service.history(10)).singleElement().satisfies(m -> assertThat(m.user()).isNull());
    }

    private Message message(long id, String content, Instant sentAt) {
        return withId(new Message(content, sentAt, alice), id);
    }

    private static SendMessageRequest request(String content, Long senderId) {
        return new SendMessageRequest(content, new SendMessageRequest.Sender(senderId));
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}
