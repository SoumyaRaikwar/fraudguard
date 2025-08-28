
package com.fraudguard.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudguard.dto.TransactionCreateRequest;
import com.fraudguard.dto.TransactionResponse;
import com.fraudguard.entity.User;
import com.fraudguard.repository.TransactionRepository;
import com.fraudguard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class TransactionIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private User testUser;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/transactions";
        
        testUser = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("transaction@test.com")
                .firstName("Transaction")
                .lastName("Test")
                .isActive(true)
                .riskProfile("LOW")
                .build());
    }

    @Test
    void processTransaction_ShouldPersistToDatabase_WhenValidRequest() {
        // Given
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .userId(testUser.getId())
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .merchantId("merchant-integration-001")
                .transactionType("PURCHASE")
                .ipAddress("192.168.1.100")
                .deviceFingerprint("integration-device-123")
                .build();

        // When
        ResponseEntity<TransactionResponse> response = restTemplate
                .postForEntity(baseUrl, request, TransactionResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(response.getBody().getUserId()).isEqualTo(testUser.getId());
        assertThat(response.getBody().getStatus()).isIn("APPROVED", "PENDING", "DECLINED");
        assertThat(response.getBody().getFraudScore()).isBetween(BigDecimal.ZERO, BigDecimal.ONE);

        // Verify database persistence
        long transactionCount = transactionRepository.count();
        assertThat(transactionCount).isGreaterThan(0);
    }

    @Test
    void processTransaction_ShouldReturnBadRequest_WhenUserNotExists() {
        // Given
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .userId(UUID.randomUUID()) // Non-existent user
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .build();

        // When
        ResponseEntity<String> response = restTemplate
                .postForEntity(baseUrl, request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenExists() {
        // Given - Create transaction first
        TransactionCreateRequest createRequest = TransactionCreateRequest.builder()
                .userId(testUser.getId())
                .amount(new BigDecimal("75.00"))
                .currency("USD")
                .merchantId("merchant-get-001")
                .transactionType("PURCHASE")
                .build();

        ResponseEntity<TransactionResponse> createResponse = restTemplate
                .postForEntity(baseUrl, createRequest, TransactionResponse.class);
        
        UUID transactionId = createResponse.getBody().getId();

        // When
        ResponseEntity<TransactionResponse> getResponse = restTemplate
                .getForEntity(baseUrl + "/" + transactionId, TransactionResponse.class);

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(transactionId);
        assertThat(getResponse.getBody().getAmount()).isEqualTo(new BigDecimal("75.00"));
    }
}
