package com.bogdusik.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.bogdusik.chat.dto.ErrorResponse;
import com.bogdusik.chat.dto.MessageResponse;
import com.bogdusik.chat.dto.RegisterUserRequest;
import com.bogdusik.chat.dto.SendMessageRequest;
import com.bogdusik.chat.dto.UserResponse;
import com.bogdusik.chat.repository.MessageRepository;
import com.bogdusik.chat.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

/**
 * The whole path a chat message takes: a STOMP client over SockJS sends it, the server validates and saves it,
 * a second client receives the broadcast, and the history endpoint returns it.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ChatIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpleBrokerMessageHandler broker;

    @Autowired
    private MessageRepository messages;

    @Autowired
    private UserRepository users;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        messages.deleteAll();
        users.deleteAll();

        stompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);
    }

    @AfterEach
    void tearDown() {
        stompClient.stop();
    }

    @Test
    void aSentMessageIsBroadcastSavedAndReturnedByTheHistory() throws Exception {
        UserResponse alice = register("alice");

        StompSession subscriber = connect();
        BlockingQueue<MessageResponse> broadcasts = subscribe(subscriber, "/topic/messages", MessageResponse.class);
        awaitSubscribers("/topic/messages", 1);

        StompSession sender = connect();
        sender.send("/app/sendMessage", new SendMessageRequest("Hello over STOMP", new SendMessageRequest.Sender(alice.id())));

        MessageResponse received = broadcasts.poll(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        assertThat(received).as("broadcast on /topic/messages").isNotNull();
        assertThat(received.id()).isNotNull();
        assertThat(received.content()).isEqualTo("Hello over STOMP");
        assertThat(received.timestamp()).isNotNull();
        assertThat(received.user()).isEqualTo(alice);

        assertThat(messages.findById(received.id()))
                .as("saved in PostgreSQL")
                .hasValueSatisfying(saved -> assertThat(saved.getContent()).isEqualTo("Hello over STOMP"));

        ResponseEntity<List<MessageResponse>> history = rest.exchange(
                "/api/messages", HttpMethod.GET, null, new ParameterizedTypeReference<>() { });
        assertThat(history.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(history.getBody()).containsExactly(received);
    }

    @Test
    void anInvalidMessageIsRejectedToTheSenderOnlyAndNotSaved() throws Exception {
        UserResponse alice = register("alice");

        StompSession subscriber = connect();
        BlockingQueue<MessageResponse> broadcasts = subscribe(subscriber, "/topic/messages", MessageResponse.class);
        awaitSubscribers("/topic/messages", 1);

        // same session: the server handles a client's frames in order, so this SUBSCRIBE is in place before the SENDs
        StompSession sender = connect();
        BlockingQueue<ErrorResponse> errors = subscribe(sender, "/user/queue/errors", ErrorResponse.class);

        sender.send("/app/sendMessage", new SendMessageRequest("   ", new SendMessageRequest.Sender(alice.id())));
        sender.send("/app/sendMessage", new SendMessageRequest("Hello", new SendMessageRequest.Sender(9_999L)));
        sender.send("/app/sendMessage", "just a string");

        assertThat(errors.poll(TIMEOUT.toSeconds(), TimeUnit.SECONDS))
                .isEqualTo(new ErrorResponse("Message content must not be blank"));
        assertThat(errors.poll(TIMEOUT.toSeconds(), TimeUnit.SECONDS))
                .isEqualTo(new ErrorResponse("Unknown sender: user 9999 is not registered"));
        assertThat(errors.poll(TIMEOUT.toSeconds(), TimeUnit.SECONDS))
                .isEqualTo(new ErrorResponse("Message body could not be read as a chat message"));
        assertThat(broadcasts.poll(1, TimeUnit.SECONDS)).as("nothing broadcast").isNull();
        assertThat(messages.count()).as("nothing saved").isZero();
    }

    private UserResponse register(String username) {
        ResponseEntity<UserResponse> response =
                rest.postForEntity("/api/users/register", new RegisterUserRequest(username), UserResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private StompSession connect() throws Exception {
        return stompClient
                .connectAsync("http://localhost:" + port + "/ws", new StompSessionHandlerAdapter() { })
                .get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
    }

    private static <T> BlockingQueue<T> subscribe(StompSession session, String destination, Class<T> type) {
        BlockingQueue<T> received = new LinkedBlockingQueue<>();
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return type;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add(type.cast(payload));
            }
        });
        return received;
    }

    /** A SUBSCRIBE on one connection and a SEND on another are not ordered, so wait until the broker has it. */
    private void awaitSubscribers(String destination, int count) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setDestination(destination);
        var probe = MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
        await().atMost(TIMEOUT).until(() -> broker.getSubscriptionRegistry().findSubscriptions(probe).size() >= count);
    }
}
