package com.bogdusik.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import com.bogdusik.chat.TestcontainersConfiguration;
import com.bogdusik.chat.entity.Message;
import com.bogdusik.chat.entity.User;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

/** Runs against PostgreSQL in a container, so the queries and constraints are the ones production uses. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class MessageRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-10-14T12:00:00Z");

    @Autowired
    private MessageRepository messages;

    @Autowired
    private UserRepository users;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findLatestReturnsTheNewestMessagesFirstWithTheirSendersLoaded() {
        User alice = users.save(new User("alice"));
        for (int i = 1; i <= 5; i++) {
            messages.save(new Message("message " + i, T0.plusSeconds(i), alice));
        }
        entityManager.flush();
        entityManager.clear();

        List<Message> latest = messages.findLatest(PageRequest.of(0, 3));

        assertThat(latest).extracting(Message::getContent).containsExactly("message 5", "message 4", "message 3");
        assertThat(latest).allSatisfy(m -> {
            assertThat(Hibernate.isInitialized(m.getUser())).isTrue();
            assertThat(m.getUser().getUsername()).isEqualTo("alice");
        });
    }

    @Test
    void findLatestIncludesMessagesWithoutASender() {
        User alice = users.save(new User("alice"));
        messages.save(new Message("from alice", T0, alice));
        messages.save(new Message("written before senders were required", T0.minusSeconds(60), null));
        entityManager.flush();
        entityManager.clear();

        assertThat(messages.findLatest(PageRequest.of(0, 10)))
                .extracting(Message::getContent)
                .containsExactly("from alice", "written before senders were required");
    }

    @Test
    void usernamesAreUniqueAndFoundByName() {
        users.saveAndFlush(new User("bob"));

        assertThat(users.findByUsername("bob")).isPresent();
        assertThat(users.findByUsername("nobody")).isEmpty();
        assertThatThrownBy(() -> users.saveAndFlush(new User("bob")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
