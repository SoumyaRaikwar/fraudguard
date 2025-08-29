package com.fraudguard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudguard.dto.FraudAnalysisRequest;
import com.fraudguard.dto.FraudAnalysisResponse;
import com.fraudguard.service.FraudDetectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FraudDetectionController.class)
class FraudDetectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ANALYST")
    void analyzeFraud_ShouldReturn200_WhenValidRequest() throws Exception {
        // Given
        FraudAnalysisRequest request = FraudAnalysisRequest.builder()
                .transactionId(UUID.randomUUID())
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .userId(UUID.randomUUID())
                .build();

        FraudAnalysisResponse response = FraudAnalysisResponse.builder()
                .transactionId(request.getTransactionId())
                .fraudScore(new BigDecimal("0.75"))
                .riskLevel("MEDIUM")
                .approved(false)
                .explanation("Transaction amount exceeds user's typical spending pattern")
                .build();

        when(fraudDetectionService.analyzeFraud(any(FraudAnalysisRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/fraud/analyze")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.fraudScore").value(0.75))
                .andExpected(jsonPath("$.riskLevel").value("MEDIUM"))
                .andExpected(jsonPath("$.approved").value(false))
                .andExpected(jsonPath("$.explanation").exists());
    }

    @Test
    void analyzeFraud_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        // Given
        FraudAnalysisRequest request = FraudAnalysisRequest.builder()
                .transactionId(UUID.randomUUID())
                .amount(new BigDecimal("500.00"))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/fraud/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isUnauthorized());
    }
}
