package com.fraudguard.repository;

import com.fraudguard.model.Transaction;
import com.fraudguard.model.TransactionStatus;
import com.fraudguard.model.TransactionType;
import com.fraudguard.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Basic queries
    Optional<Transaction> findByTransactionId(String transactionId);
    
    List<Transaction> findByUserIdOrderByTransactionTimeDesc(Long userId);
    
    Page<Transaction> findByUserIdOrderByTransactionTimeDesc(Long userId, Pageable pageable);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    List<Transaction> findByTransactionType(TransactionType transactionType);
    
    List<Transaction> findByPaymentMethod(PaymentMethod paymentMethod);
    
    // Amount-based queries
    @Query("SELECT t FROM Transaction t WHERE t.amount > :amount")
    List<Transaction> findLargeTransactions(@Param("amount") BigDecimal amount);
    
    @Query("SELECT t FROM Transaction t WHERE t.amount BETWEEN :minAmount AND :maxAmount")
    List<Transaction> findTransactionsByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                                   @Param("maxAmount") BigDecimal maxAmount);
    
    // Time-based queries
    @Query("SELECT t FROM Transaction t WHERE t.transactionTime BETWEEN :start AND :end")
    List<Transaction> findTransactionsBetween(@Param("start") LocalDateTime start, 
                                            @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM Transaction t WHERE t.transactionTime > :since ORDER BY t.transactionTime DESC")
    List<Transaction> findRecentTransactions(@Param("since") LocalDateTime since);
    
    // Fraud-related queries
    @Query("SELECT t FROM Transaction t WHERE t.fraudScore > :threshold")
    List<Transaction> findHighRiskTransactions(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT t FROM Transaction t WHERE t.isFraud = true")
    List<Transaction> findFraudulentTransactions();
    
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FLAGGED' ORDER BY t.fraudScore DESC")
    List<Transaction> findFlaggedTransactionsOrderByRisk();
    
    // Merchant-based queries
    List<Transaction> findByMerchant(String merchant);
    
    List<Transaction> findByMerchantCategory(String merchantCategory);
    
    @Query("SELECT t FROM Transaction t WHERE t.merchant LIKE %:merchantName%")
    List<Transaction> findTransactionsByMerchantPattern(@Param("merchantName") String merchantName);
    
    // Location-based queries
    List<Transaction> findByLocation(String location);
    
    @Query("SELECT t FROM Transaction t WHERE t.latitude BETWEEN :minLat AND :maxLat " +
           "AND t.longitude BETWEEN :minLon AND :maxLon")
    List<Transaction> findTransactionsInArea(@Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
                                           @Param("minLon") Double minLon, @Param("maxLon") Double maxLon);
    
    // User behavior queries
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionTime > :since")
    Long countUserTransactionsSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionTime > :since AND t.status = 'APPROVED'")
    BigDecimal sumUserTransactionAmountSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionTime BETWEEN :start AND :end " +
           "ORDER BY t.transactionTime DESC")
    List<Transaction> findUserTransactionsInPeriod(@Param("userId") Long userId,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);
    
    // Velocity checks
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionTime > :since AND t.amount > :minAmount")
    Long countUserLargeTransactionsSince(@Param("userId") Long userId, 
                                        @Param("since") LocalDateTime since,
                                        @Param("minAmount") BigDecimal minAmount);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.ipAddress = :ipAddress " +
           "AND t.transactionTime > :since")
    Long countTransactionsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.deviceId = :deviceId " +
           "AND t.transactionTime > :since")
    Long countTransactionsByDeviceSince(@Param("deviceId") String deviceId, @Param("since") LocalDateTime since);
    
    // Statistical queries
    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.status = 'APPROVED' AND t.transactionTime > :since")
    BigDecimal getAverageTransactionAmountForUser(@Param("userId") Long userId, 
                                                 @Param("since") LocalDateTime since);
    
    @Query("SELECT MAX(t.amount) FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.status = 'APPROVED'")
    BigDecimal getMaxTransactionAmountForUser(@Param("userId") Long userId);
    
    @Query("SELECT t.merchant, COUNT(t), SUM(t.amount) FROM Transaction t " +
           "WHERE t.transactionTime > :since GROUP BY t.merchant ORDER BY COUNT(t) DESC")
    List<Object[]> getMerchantStatistics(@Param("since") LocalDateTime since);
    
    @Query("SELECT t.status, COUNT(t) FROM Transaction t " +
           "WHERE t.transactionTime > :since GROUP BY t.status")
    List<Object[]> getTransactionStatusCounts(@Param("since") LocalDateTime since);
    
    // Pattern detection queries
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.merchant = :merchant AND t.transactionTime > :since " +
           "ORDER BY t.transactionTime DESC")
    List<Transaction> findRepeatedMerchantTransactions(@Param("userId") Long userId,
                                                     @Param("merchant") String merchant,
                                                     @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Transaction t WHERE t.amount = :amount " +
           "AND t.transactionTime BETWEEN :start AND :end")
    List<Transaction> findTransactionsByExactAmount(@Param("amount") BigDecimal amount,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
    
    // Real-time monitoring queries
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' " +
           "AND t.createdAt < :timeThreshold ORDER BY t.createdAt")
    List<Transaction> findStuckTransactions(@Param("timeThreshold") LocalDateTime timeThreshold);
    
    @Query("SELECT t FROM Transaction t WHERE t.fraudScore > :threshold " +
           "AND t.status = 'PENDING' ORDER BY t.fraudScore DESC, t.createdAt")
    List<Transaction> findPendingHighRiskTransactions(@Param("threshold") BigDecimal threshold);
    
    // Update queries
    @Query("UPDATE Transaction t SET t.status = :status, t.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE t.id = :transactionId")
    void updateTransactionStatus(@Param("transactionId") Long transactionId, 
                                @Param("status") TransactionStatus status);
    
    @Query("UPDATE Transaction t SET t.fraudScore = :fraudScore, t.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE t.id = :transactionId")
    void updateFraudScore(@Param("transactionId") Long transactionId, 
                         @Param("fraudScore") BigDecimal fraudScore);
    
    @Query("UPDATE Transaction t SET t.isFraud = :isFraud, t.fraudReason = :reason, " +
           "t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :transactionId")
    void markAsFraud(@Param("transactionId") Long transactionId, 
                     @Param("isFraud") Boolean isFraud, 
                     @Param("reason") String reason);
}
