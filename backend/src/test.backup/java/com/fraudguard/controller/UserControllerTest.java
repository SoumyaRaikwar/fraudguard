package com.fraudguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudguard.dto.UserCreateRequest;
import com.fraudguard.dto.UserResponse;
import com.fraudguard.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturn201_WhenValidRequest() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .build();

        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturn200_WhenUserExists() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UserResponse response = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userService.getUserById(eq(userId))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.id").value(userId.toString()))
                .andExpected(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void createUser_ShouldReturn400_WhenInvalidEmail() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .email("invalid-email")
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isBadRequest());
    }

    @Test
    void createUser_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isUnauthorized());
    }
}
