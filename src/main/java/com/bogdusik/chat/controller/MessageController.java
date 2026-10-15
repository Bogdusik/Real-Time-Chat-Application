package com.bogdusik.chat.controller;

import java.util.List;

import com.bogdusik.chat.dto.MessageResponse;
import com.bogdusik.chat.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** Message history for a client that has just loaded or reconnected: the latest messages, oldest first. */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MessageResponse> history(
            @RequestParam(defaultValue = "" + MessageService.DEFAULT_HISTORY_LIMIT) int limit) {
        return messageService.history(limit);
    }
}
