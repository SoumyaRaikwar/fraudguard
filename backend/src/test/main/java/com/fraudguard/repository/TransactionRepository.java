package com.fraudguard.repository;

import com.fraudguard.model.Transaction;
import com.fraudguard.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserIdOrderByTransactionTimeDesc(Long userId);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE t.fraudScore > :threshold")
    List<Transaction> findHighRiskTransactions(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT t FROM Transaction t WHERE t.transactionTime BETWEEN :start AND :end")
    List<Transaction> findTransactionsBetween(@Param("start") LocalDateTime start, 
                                            @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM Transaction t WHERE t.amount > :amount")
    List<Transaction> findLargeTransactions(@Param("amount") BigDecimal amount);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.transactionTime > :since")
    Long countUserTransactionsSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
