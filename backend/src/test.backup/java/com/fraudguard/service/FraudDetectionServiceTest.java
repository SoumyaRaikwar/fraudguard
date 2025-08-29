package com.fraudguard.service;

import com.fraudguard.dto.FraudAnalysisRequest;
import com.fraudguard.dto.FraudAnalysisResponse;
import com.fraudguard.entity.Transaction;
import com.fraudguard.external.MLServiceClient;
import com.fraudguard.service.fraud.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock
    private MLServiceClient mlServiceClient;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private Transaction testTransaction;
    private FraudAnalysisRequest analysisRequest;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .ipAddress("192.168.1.1")
                .deviceFingerprint("device123")
                .build();

        analysisRequest = FraudAnalysisRequest.builder()
                .transactionId(testTransaction.getId())
                .amount(testTransaction.getAmount())
                .currency(testTransaction.getCurrency())
                .merchantId(testTransaction.getMerchantId())
                .userId(testTransaction.getUserId())
                .build();
    }

    @Test
    void calculateFraudScore_ShouldReturnLowScore_ForNormalTransaction() {
        // Given
        when(mlServiceClient.getFraudScore(any())).thenReturn(new BigDecimal("0.15"));

        // When
        BigDecimal fraudScore = fraudDetectionService.calculateFraudScore(testTransaction);

        // Then
        assertThat(fraudScore).isEqualTo(new BigDecimal("0.15"));
    }

    @Test
    void calculateFraudScore_ShouldReturnHighScore_ForSuspiciousTransaction() {
        // Given
        testTransaction.setAmount(new BigDecimal("50000.00")); // High amount
        when(mlServiceClient.getFraudScore(any())).thenReturn(new BigDecimal("0.85"));

        // When
        BigDecimal fraudScore = fraudDetectionService.calculateFraudScore(testTransaction);

        // Then
        assertThat(fraudScore).isEqualTo(new BigDecimal("0.85"));
    }

    @Test
    void determineRiskLevel_ShouldReturnLow_ForLowScore() {
        // When
        String riskLevel = fraudDetectionService.determineRiskLevel(new BigDecimal("0.2"));

        // Then
        assertThat(riskLevel).isEqualTo("LOW");
    }

    @Test
    void determineRiskLevel_ShouldReturnMedium_ForMediumScore() {
        // When
        String riskLevel = fraudDetectionService.determineRiskLevel(new BigDecimal("0.5"));

        // Then
        assertThat(riskLevel).isEqualTo("MEDIUM");
    }

    @Test
    void determineRiskLevel_ShouldReturnHigh_ForHighScore() {
        // When
        String riskLevel = fraudDetectionService.determineRiskLevel(new BigDecimal("0.9"));

        // Then
        assertThat(riskLevel).isEqualTo("HIGH");
    }

    @Test
    void analyzeFraud_ShouldReturnAnalysis_WhenValidRequest() {
        // Given
        when(mlServiceClient.getFraudScore(any())).thenReturn(new BigDecimal("0.75"));

        // When
        FraudAnalysisResponse result = fraudDetectionService.analyzeFraud(analysisRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo(analysisRequest.getTransactionId());
        assertThat(result.getFraudScore()).isEqualTo(new BigDecimal("0.75"));
        assertThat(result.getRiskLevel()).isEqualTo("MEDIUM");
        assertThat(result.isApproved()).isFalse(); // Medium/High risk should not be approved
        assertThat(result.getExplanation()).isNotNull();
    }

    @Test
    void shouldApproveTransaction_ShouldReturnTrue_ForLowRisk() {
        // When
        boolean shouldApprove = fraudDetectionService.shouldApproveTransaction(new BigDecimal("0.25"));

        // Then
        assertThat(shouldApprove).isTrue();
    }

    @Test
    void shouldApproveTransaction_ShouldReturnFalse_ForHighRisk() {
        // When
        boolean shouldApprove = fraudDetectionService.shouldApproveTransaction(new BigDecimal("0.85"));

        // Then
        assertThat(shouldApprove).isFalse();
    }
}
