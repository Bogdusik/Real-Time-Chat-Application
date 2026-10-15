package com.bogdusik.chat.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings under the {@code chat.} prefix.
 *
 * @param allowedOrigins browser origins allowed to call the REST API and open the STOMP endpoint
 *                       (comma-separated in {@code CORS_ALLOWED_ORIGINS}; patterns such as
 *                       {@code http://localhost:*} are accepted)
 */
@ConfigurationProperties(prefix = "chat")
public record ChatProperties(List<String> allowedOrigins) {

    public ChatProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }

    public String[] allowedOriginsArray() {
        return allowedOrigins.toArray(String[]::new);
    }
}
