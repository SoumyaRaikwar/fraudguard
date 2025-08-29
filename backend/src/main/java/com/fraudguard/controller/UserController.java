package com.fraudguard.controller;

import com.fraudguard.dto.UserCreateRequest;
import com.fraudguard.model.User;
import com.fraudguard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Received request to create user: {}", request.getEmail());
        return userService.createUser(request);
    }
    
    @GetMapping("/{email}")
    public User getUser(@PathVariable String email) {
        log.info("Fetching user by email: {}", email);
        return userService.findByEmail(email);
    }
}
