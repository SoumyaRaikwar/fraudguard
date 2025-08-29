package com.fraudguard.service;

import com.fraudguard.dto.AlertCreateRequest;
import com.fraudguard.dto.AlertResponse;
import com.fraudguard.entity.Alert;
import com.fraudguard.entity.Transaction;
import com.fraudguard.repository.AlertRepository;
import com.fraudguard.service.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AlertService alertService;

    private Transaction testTransaction;
    private Alert testAlert;
    private AlertCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("10000.00"))
                .fraudScore(new BigDecimal("0.95"))
                .riskLevel("HIGH")
                .build();

        testAlert = Alert.builder()
                .id(UUID.randomUUID())
                .transactionId(testTransaction.getId())
                .userId(testTransaction.getUserId())
                .alertType("FRAUD_DETECTION")
                .severity("HIGH")
                .message("High fraud risk detected")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = AlertCreateRequest.builder()
                .transactionId(testTransaction.getId())
                .userId(testTransaction.getUserId())
                .alertType("FRAUD_DETECTION")
                .severity("HIGH")
                .message("High fraud risk detected")
                .build();
    }

    @Test
    void createAlert_ShouldReturnAlert_WhenValidRequest() {
        // Given
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        // When
        AlertResponse result = alertService.createAlert(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo(testTransaction.getId());
        assertThat(result.getAlertType()).isEqualTo("FRAUD_DETECTION");
        assertThat(result.getSeverity()).isEqualTo("HIGH");
        assertThat(result.getStatus()).isEqualTo("OPEN");
        
        verify(alertRepository).save(any(Alert.class));
        verify(emailService).sendFraudAlert(any(Alert.class));
    }

    @Test
    void createFraudAlert_ShouldCreateAlert_ForHighRiskTransaction() {
        // Given
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        // When
        AlertResponse result = alertService.createFraudAlert(testTransaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo(testTransaction.getId());
        assertThat(result.getAlertType()).isEqualTo("FRAUD_DETECTION");
        assertThat(result.getSeverity()).isEqualTo("HIGH");
        
        verify(alertRepository).save(any(Alert.class));
        verify(emailService).sendFraudAlert(any(Alert.class));
    }

    @Test
    void resolveAlert_ShouldUpdateAlertStatus() {
        // Given
        UUID alertId = testAlert.getId();
        UUID resolvedBy = UUID.randomUUID();
        testAlert.setStatus("RESOLVED");
        testAlert.setResolvedAt(LocalDateTime.now());
        testAlert.setResolvedBy(resolvedBy);

        when(alertRepository.findById(alertId)).thenReturn(java.util.Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        // When
        AlertResponse result = alertService.resolveAlert(alertId, resolvedBy, "False positive");

        // Then
        assertThat(result.getStatus()).isEqualTo("RESOLVED");
        assertThat(result.getResolvedBy()).isEqualTo(resolvedBy);
        
        verify(alertRepository).findById(alertId);
        verify(alertRepository).save(any(Alert.class));
    }
}
