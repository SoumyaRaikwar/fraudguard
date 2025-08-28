package com.fraudguard.repository;

import com.fraudguard.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private UUID testUserId;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .status("APPROVED")
                .fraudScore(new BigDecimal("0.15"))
                .riskLevel("LOW")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByUserId_ShouldReturnTransactions_ForSpecificUser() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        Transaction otherTransaction = createTransactionForUser(otherUserId);
        
        entityManager.persistAndFlush(testTransaction);
        entityManager.persistAndFlush(otherTransaction);

        // When
        List<Transaction> userTransactions = transactionRepository.findByUserId(testUserId);

        // Then
        assertThat(userTransactions).hasSize(1);
        assertThat(userTransactions.get(0).getUserId()).isEqualTo(testUserId);
    }

    @Test
    void findByRiskLevel_ShouldReturnHighRiskTransactions() {
        // Given
        Transaction highRiskTxn = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .amount(new BigDecimal("5000.00"))
                .fraudScore(new BigDecimal("0.95"))
                .riskLevel("HIGH")
                .status("DECLINED")
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(testTransaction); // LOW risk
        entityManager.persistAndFlush(highRiskTxn); // HIGH risk

        // When
        List<Transaction> highRiskTransactions = transactionRepository.findByRiskLevel("HIGH");

        // Then
        assertThat(highRiskTransactions).hasSize(1);
        assertThat(highRiskTransactions.get(0).getRiskLevel()).isEqualTo("HIGH");
        assertThat(highRiskTransactions.get(0).getFraudScore()).isEqualTo(new BigDecimal("0.95"));
    }

    @Test
    void findByUserIdAndCreatedAtBetween_ShouldReturnTransactionsInDateRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime oldDate = LocalDateTime.now().minusDays(2);

        Transaction recentTxn = createTransactionWithDate(testUserId, start.plusHours(1));
        Transaction oldTxn = createTransactionWithDate(testUserId, oldDate);

        entityManager.persistAndFlush(recentTxn);
        entityManager.persistAndFlush(oldTxn);

        // When
        List<Transaction> recentTransactions = transactionRepository
                .findByUserIdAndCreatedAtBetween(testUserId, start, end);

        // Then
        assertThat(recentTransactions).hasSize(1);
        assertThat(recentTransactions.get(0).getCreatedAt()).isAfter(start);
        assertThat(recentTransactions.get(0).getCreatedAt()).isBefore(end);
    }

    @Test
    void findByFraudScoreGreaterThan_ShouldReturnSuspiciousTransactions() {
        // Given
        Transaction suspiciousTxn = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(testUserId)
                .amount(new BigDecimal("1000.00"))
                .fraudScore(new BigDecimal("0.85"))
                .riskLevel("HIGH")
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(testTransaction); // Score 0.15
        entityManager.persistAndFlush(suspiciousTxn); // Score 0.85

        // When
        List<Transaction> suspiciousTransactions = 
                transactionRepository.findByFraudScoreGreaterThan(new BigDecimal("0.7"));

        // Then
        assertThat(suspiciousTransactions).hasSize(1);
        assertThat(suspiciousTransactions.get(0).getFraudScore())
                .isGreaterThan(new BigDecimal("0.7"));
    }

    @Test
    void findByStatus_WithPagination_ShouldReturnPagedResults() {
        // Given
        for (int i = 0; i < 15; i++) {
            Transaction txn = createTransactionWithStatus("APPROVED");
            entityManager.persistAndFlush(txn);
        }

        PageRequest pageRequest = PageRequest.of(0, 10);

        // When
        Page<Transaction> approvedTransactions = 
                transactionRepository.findByStatus("APPROVED", pageRequest);

        // Then
        assertThat(approvedTransactions.getContent()).hasSize(10);
        assertThat(approvedTransactions.getTotalElements()).isEqualTo(15);
        assertThat(approvedTransactions.getTotalPages()).isEqualTo(2);
        assertThat(approvedTransactions.getNumber()).isEqualTo(0);
    }

    private Transaction createTransactionForUser(UUID userId) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .merchantId("merchant-002")
                .transactionType("PURCHASE")
                .status("APPROVED")
                .fraudScore(new BigDecimal("0.25"))
                .riskLevel("LOW")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Transaction createTransactionWithDate(UUID userId, LocalDateTime createdAt) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .status("APPROVED")
                .fraudScore(new BigDecimal("0.20"))
                .riskLevel("LOW")
                .createdAt(createdAt)
                .build();
    }

    private Transaction createTransactionWithStatus(String status) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .status(status)
                .fraudScore(new BigDecimal("0.15"))
                .riskLevel("LOW")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
