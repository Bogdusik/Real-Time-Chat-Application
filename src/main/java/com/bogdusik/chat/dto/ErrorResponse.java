package com.bogdusik.chat.dto;

/** Error body for rejected REST requests and for rejected STOMP messages on {@code /user/queue/errors}. */
public record ErrorResponse(String error) {
}
