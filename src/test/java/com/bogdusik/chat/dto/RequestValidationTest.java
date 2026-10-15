package com.bogdusik.chat.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import com.bogdusik.chat.entity.Message;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** The bean-validation rules the controllers apply with {@code @Valid}. */
class RequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void aMessageWithContentAndSenderIsValid() {
        assertThat(validator.validate(message("Hello", 1L))).isEmpty();
    }

    @Test
    void blankContentIsRejected() {
        assertThat(messages(validator.validate(message("   ", 1L))))
                .containsExactly("Message content must not be blank");
    }

    @Test
    void contentOverTheLimitIsRejected() {
        String tooLong = "x".repeat(Message.MAX_CONTENT_LENGTH + 1);

        assertThat(messages(validator.validate(message(tooLong, 1L))))
                .containsExactly("Message content must be at most 5000 characters");
        assertThat(validator.validate(message("x".repeat(Message.MAX_CONTENT_LENGTH), 1L))).isEmpty();
    }

    @Test
    void aMissingSenderIsRejected() {
        assertThat(messages(validator.validate(new SendMessageRequest("Hello", null))))
                .containsExactly("Sender is required");
        assertThat(messages(validator.validate(message("Hello", null))))
                .containsExactly("Sender id is required");
    }

    @Test
    void usernamesMustBeNonBlankAndAtMostFiftyCharacters() {
        assertThat(validator.validate(new RegisterUserRequest("alice"))).isEmpty();
        assertThat(messages(validator.validate(new RegisterUserRequest(" "))))
                .containsExactly("Username must not be blank");
        assertThat(messages(validator.validate(new RegisterUserRequest("a".repeat(51)))))
                .containsExactly("Username must be at most 50 characters");
    }

    private static SendMessageRequest message(String content, Long senderId) {
        return new SendMessageRequest(content, new SendMessageRequest.Sender(senderId));
    }

    private static <T> Set<String> messages(Set<ConstraintViolation<T>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }
}
