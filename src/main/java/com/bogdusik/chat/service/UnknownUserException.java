package com.bogdusik.chat.service;

/** A message names a sender that was never registered. */
public class UnknownUserException extends RuntimeException {

    public UnknownUserException(Long userId) {
        super("Unknown sender: user " + userId + " is not registered");
    }
}
