package com.fraudguard.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudguard.dto.UserCreateRequest;
import com.fraudguard.dto.UserResponse;
import com.fraudguard.entity.User;
import com.fraudguard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class UserIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/users";
    }

    @Test
    void createUser_ShouldPersistToDatabase_WhenValidRequest() {
        // Given
        UserCreateRequest request = UserCreateRequest.builder()
                .email("integration@test.com")
                .firstName("Integration")
                .lastName("Test")
                .password("password123")
                .build();

        // When
        ResponseEntity<UserResponse> response = restTemplate
                .postForEntity(baseUrl, request, UserResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("integration@test.com");

        // Verify database persistence
        boolean userExists = userRepository.existsByEmail("integration@test.com");
        assertThat(userExists).isTrue();
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExistsInDatabase() {
        // Given - Create user in database
        User savedUser = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("existing@test.com")
                .firstName("Existing")
                .lastName("User")
                .isActive(true)
                .riskProfile("LOW")
                .build());

        // When
        ResponseEntity<UserResponse> response = restTemplate
                .getForEntity(baseUrl + "/" + savedUser.getId(), UserResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedUser.getId());
        assertThat(response.getBody().getEmail()).isEqualTo("existing@test.com");
    }

    @Test
    void createUser_ShouldReturnConflict_WhenEmailAlreadyExists() {
        // Given - Create user with existing email
        userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("duplicate@test.com")
                .firstName("Existing")
                .lastName("User")
                .isActive(true)
                .build());

        UserCreateRequest request = UserCreateRequest.builder()
                .email("duplicate@test.com")
                .firstName("Duplicate")
                .lastName("User")
                .password("password123")
                .build();

        // When
        ResponseEntity<String> response = restTemplate
                .postForEntity(baseUrl, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
