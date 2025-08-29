package com.fraudguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudguard.dto.TransactionCreateRequest;
import com.fraudguard.dto.TransactionResponse;
import com.fraudguard.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void processTransaction_ShouldReturn201_WhenValidRequest() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .status("APPROVED")
                .fraudScore(new BigDecimal("0.15"))
                .riskLevel("LOW")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionService.processTransaction(any(TransactionCreateRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isCreated())
                .andExpected(jsonPath("$.amount").value(100.00))
                .andExpected(jsonPath("$.currency").value("USD"))
                .andExpected(jsonPath("$.status").value("APPROVED"))
                .andExpected(jsonPath("$.fraudScore").value(0.15))
                .andExpected(jsonPath("$.riskLevel").value("LOW"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void processTransaction_ShouldReturn400_WhenNegativeAmount() throws Exception {
        // Given
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("-100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void processTransaction_ShouldReturn400_WhenMissingRequiredFields() throws Exception {
        // Given
        TransactionCreateRequest request = TransactionCreateRequest.builder()
                .amount(new BigDecimal("100.00"))
                // Missing userId, currency, etc.
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isBadRequest());
    }
}
