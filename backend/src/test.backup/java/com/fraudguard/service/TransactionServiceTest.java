package com.fraudguard.service;

import com.fraudguard.dto.TransactionCreateRequest;
import com.fraudguard.dto.TransactionResponse;
import com.fraudguard.entity.Transaction;
import com.fraudguard.entity.User;
import com.fraudguard.repository.TransactionRepository;
import com.fraudguard.repository.UserRepository;
import com.fraudguard.service.fraud.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Transaction testTransaction;
    private TransactionCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .riskProfile("LOW")
                .build();

        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(testUser.getId())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .status("APPROVED")
                .fraudScore(new BigDecimal("0.15"))
                .riskLevel("LOW")
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = TransactionCreateRequest.builder()
                .userId(testUser.getId())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .ipAddress("192.168.1.1")
                .deviceFingerprint("device123")
                .build();
    }

    @Test
    void processTransaction_ShouldReturnApprovedTransaction_WhenLowRisk() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(fraudDetectionService.calculateFraudScore(any(Transaction.class)))
                .thenReturn(new BigDecimal("0.15"));
        when(fraudDetectionService.determineRiskLevel(any(BigDecimal.class)))
                .thenReturn("LOW");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse result = transactionService.processTransaction(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getFraudScore()).isEqualTo(new BigDecimal("0.15"));
        assertThat(result.getRiskLevel()).isEqualTo("LOW");
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
        
        verify(userRepository).findById(testUser.getId());
        verify(fraudDetectionService).calculateFraudScore(any(Transaction.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTransaction_ShouldReturnDeclinedTransaction_WhenHighRisk() {
        // Given
        testTransaction.setStatus("DECLINED");
        testTransaction.setFraudScore(new BigDecimal("0.95"));
        testTransaction.setRiskLevel("HIGH");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(fraudDetectionService.calculateFraudScore(any(Transaction.class)))
                .thenReturn(new BigDecimal("0.95"));
        when(fraudDetectionService.determineRiskLevel(any(BigDecimal.class)))
                .thenReturn("HIGH");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse result = transactionService.processTransaction(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("DECLINED");
        assertThat(result.getFraudScore()).isEqualTo(new BigDecimal("0.95"));
        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
        
        verify(fraudDetectionService).calculateFraudScore(any(Transaction.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTransaction_ShouldReturnPendingTransaction_WhenMediumRisk() {
        // Given
        testTransaction.setStatus("PENDING");
        testTransaction.setFraudScore(new BigDecimal("0.65"));
        testTransaction.setRiskLevel("MEDIUM");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(fraudDetectionService.calculateFraudScore(any(Transaction.class)))
                .thenReturn(new BigDecimal("0.65"));
        when(fraudDetectionService.determineRiskLevel(any(BigDecimal.class)))
                .thenReturn("MEDIUM");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse result = transactionService.processTransaction(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getFraudScore()).isEqualTo(new BigDecimal("0.65"));
        assertThat(result.getRiskLevel()).isEqualTo("MEDIUM");
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenExists() {
        // Given
        UUID transactionId = testTransaction.getId();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        // When
        TransactionResponse result = transactionService.getTransactionById(transactionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transactionId);
        assertThat(result.getAmount()).isEqualTo(testTransaction.getAmount());
        
        verify(transactionRepository).findById(transactionId);
    }
}

