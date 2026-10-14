package com.bogdusik.chat.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ChatProperties properties;

    public WebConfig(ChatProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(properties.allowedOriginsArray())
                .allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type");
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
