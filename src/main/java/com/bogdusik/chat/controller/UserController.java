package com.bogdusik.chat.controller;

import com.bogdusik.chat.dto.RegisterUserRequest;
import com.bogdusik.chat.dto.UserResponse;
import com.bogdusik.chat.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Joins the chat under a name: returns the existing user with that name or creates it. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return userService.register(request.username());
    }
}
