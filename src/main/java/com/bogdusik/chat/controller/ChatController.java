package com.bogdusik.chat.controller;

import com.bogdusik.chat.dto.ErrorResponse;
import com.bogdusik.chat.dto.MessageResponse;
import com.bogdusik.chat.dto.SendMessageRequest;
import com.bogdusik.chat.service.MessageService;
import com.bogdusik.chat.service.UnknownUserException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/** STOMP side of the chat: clients send to {@code /app/sendMessage} and subscribe to {@code /topic/messages}. */
@Controller
public class ChatController {

    public static final String ERRORS_QUEUE = "/queue/errors";

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final MessageService messageService;

    public ChatController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** Validates and saves the message, then broadcasts the saved version (with its id and timestamp). */
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public MessageResponse sendMessage(@Valid @Payload SendMessageRequest request) {
        return messageService.send(request);
    }

    /** A rejected message is answered on the sender's own {@code /user/queue/errors}; nothing is broadcast. */
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser(destinations = ERRORS_QUEUE, broadcast = false)
    public ErrorResponse invalidMessage(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        String reason = result == null ? null
                : result.getFieldErrors().stream().map(FieldError::getDefaultMessage).findFirst().orElse(null);
        return new ErrorResponse(reason == null ? "Invalid message" : reason);
    }

    @MessageExceptionHandler(UnknownUserException.class)
    @SendToUser(destinations = ERRORS_QUEUE, broadcast = false)
    public ErrorResponse unknownUser(UnknownUserException e) {
        return new ErrorResponse(e.getMessage());
    }

    @MessageExceptionHandler(MessageConversionException.class)
    @SendToUser(destinations = ERRORS_QUEUE, broadcast = false)
    public ErrorResponse unreadableMessage(MessageConversionException e) {
        return new ErrorResponse("Message body is not valid JSON");
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = ERRORS_QUEUE, broadcast = false)
    public ErrorResponse unexpected(Exception e) {
        log.error("Failed to handle a chat message", e);
        return new ErrorResponse("The message could not be sent");
    }
}
