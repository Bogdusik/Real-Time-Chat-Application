package com.bogdusik.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** STOMP endpoint; SockJS clients connect here and fall back to HTTP streaming or polling. */
    public static final String ENDPOINT = "/ws";

    private final ChatProperties properties;

    public WebSocketConfig(ChatProperties properties) {
        this.properties = properties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic for broadcasts, /queue for replies to one session (/user/queue/errors)
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        // deliver to each client in the order the broker published
        registry.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // handle each client's frames in the order they arrive: a SUBSCRIBE is in place before the client's
        // next SEND, and one client's messages are saved and broadcast in the order they were sent
        registry.setPreserveReceiveOrder(true);
        registry.addEndpoint(ENDPOINT)
                .setAllowedOriginPatterns(properties.allowedOriginsArray())
                .withSockJS();
    }
}
