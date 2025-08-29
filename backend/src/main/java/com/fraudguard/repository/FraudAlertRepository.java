package com.fraudguard.repository;

import com.fraudguard.model.FraudAlert;
import com.fraudguard.model.AlertSeverity;
import com.fraudguard.model.AlertStatus;
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
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    
    // Basic queries
    Optional<FraudAlert> findByAlertId(String alertId);
    
    List<FraudAlert> findByStatus(AlertStatus status);
    
    List<FraudAlert> findBySeverity(AlertSeverity severity);
    
    List<FraudAlert> findByAlertType(String alertType);
    
    // Transaction-based queries
    List<FraudAlert> findByTransactionId(Long transactionId);
    
    List<FraudAlert> findByTransactionTransactionId(String transactionId);
    
    // User-based queries
    List<FraudAlert> findByUserId(Long userId);
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.user.id = :userId " +
           "AND fa.createdAt > :since ORDER BY fa.createdAt DESC")
    List<FraudAlert> findRecentAlertsByUser(@Param("userId") Long userId, 
                                          @Param("since") LocalDateTime since);
    
    // Status-based queries
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.status = 'OPEN' ORDER BY fa.severity DESC, fa.createdAt")
    List<FraudAlert> findOpenAlertsOrderBySeverity();
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.status IN ('OPEN', 'IN_PROGRESS') " +
           "ORDER BY fa.severity DESC, fa.createdAt")
    List<FraudAlert> findActiveAlerts();
    
    Page<FraudAlert> findByStatusOrderByCreatedAtDesc(AlertStatus status, Pageable pageable);
    
    // Severity-based queries
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.severity IN ('HIGH', 'CRITICAL') " +
           "AND fa.status = 'OPEN'")
    List<FraudAlert> findCriticalOpenAlerts();
    
    List<FraudAlert> findBySeverityAndStatusOrderByCreatedAtDesc(AlertSeverity severity, AlertStatus status);
    
    // Time-based queries
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.createdAt BETWEEN :start AND :end")
    List<FraudAlert> findAlertsBetween(@Param("start") LocalDateTime start, 
                                     @Param("end") LocalDateTime end);
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.createdAt > :since ORDER BY fa.createdAt DESC")
    List<FraudAlert> findRecentAlerts(@Param("since") LocalDateTime since);
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.status = 'OPEN' " +
           "AND fa.createdAt < :threshold ORDER BY fa.createdAt")
    List<FraudAlert> findStaleAlerts(@Param("threshold") LocalDateTime threshold);
    
    // Confidence score queries
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.confidenceScore > :threshold")
    List<FraudAlert> findHighConfidenceAlerts(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.confidenceScore BETWEEN :min AND :max")
    List<FraudAlert> findAlertsByConfidenceRange(@Param("min") BigDecimal min, 
                                               @Param("max") BigDecimal max);
    
    // Model-based queries
    List<FraudAlert> findByModelName(String modelName);
    
    List<FraudAlert> findByRuleTriggered(String ruleTriggered);
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.modelName = :modelName " +
           "AND fa.modelVersion = :version")
    List<FraudAlert> findByModelAndVersion(@Param("modelName") String modelName, 
                                         @Param("version") String version);
    
    // False positive queries
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.falsePositive = true")
    List<FraudAlert> findFalsePositives();
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.falsePositive = false " +
           "AND fa.status = 'RESOLVED'")
    List<FraudAlert> findValidResolvedAlerts();
    
    // Analyst queries
    List<FraudAlert> findByResolvedBy(String resolvedBy);
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.resolvedBy = :analyst " +
           "AND fa.resolvedAt BETWEEN :start AND :end")
    List<FraudAlert> findAlertsResolvedByAnalyst(@Param("analyst") String analyst,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
    
    // Statistical queries
    @Query("SELECT fa.status, COUNT(fa) FROM FraudAlert fa GROUP BY fa.status")
    List<Object[]> getAlertCountsByStatus();
    
    @Query("SELECT fa.severity, COUNT(fa) FROM FraudAlert fa WHERE fa.createdAt > :since " +
           "GROUP BY fa.severity ORDER BY fa.severity")
    List<Object[]> getAlertCountsBySeverity(@Param("since") LocalDateTime since);
    
    @Query("SELECT fa.alertType, COUNT(fa) FROM FraudAlert fa WHERE fa.createdAt > :since " +
           "GROUP BY fa.alertType ORDER BY COUNT(fa) DESC")
    List<Object[]> getAlertCountsByType(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(fa) FROM FraudAlert fa WHERE fa.createdAt > :since " +
           "AND fa.falsePositive = false")
    Long countValidAlertsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(fa) FROM FraudAlert fa WHERE fa.createdAt > :since " +
           "AND fa.falsePositive = true")
    Long countFalsePositivesSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(fa.confidenceScore) FROM FraudAlert fa WHERE fa.falsePositive = false")
    BigDecimal getAverageConfidenceScoreForValidAlerts();
    
    // Performance queries
    @Query("SELECT fa.resolvedBy, COUNT(fa), AVG(TIMESTAMPDIFF(HOUR, fa.createdAt, fa.resolvedAt)) " +
           "FROM FraudAlert fa WHERE fa.resolvedAt IS NOT NULL " +
           "AND fa.resolvedAt > :since GROUP BY fa.resolvedBy")
    List<Object[]> getAnalystPerformanceMetrics(@Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, fa.createdAt, fa.resolvedAt)) " +
           "FROM FraudAlert fa WHERE fa.resolvedAt IS NOT NULL " +
           "AND fa.severity = :severity")
    Double getAverageResolutionTimeForSeverity(@Param("severity") AlertSeverity severity);
    
    // Search queries
    @Query("SELECT fa FROM FraudAlert fa WHERE " +
           "LOWER(fa.reason) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(fa.alertType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(fa.analystNotes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<FraudAlert> searchAlerts(@Param("searchTerm") String searchTerm);
    
    // Complex queries
    @Query("SELECT fa FROM FraudAlert fa JOIN fa.transaction t " +
           "WHERE t.amount > :minAmount AND fa.severity = :severity " +
           "AND fa.status = 'OPEN'")
    List<FraudAlert> findHighValueAlertsWithSeverity(@Param("minAmount") BigDecimal minAmount,
                                                    @Param("severity") AlertSeverity severity);
    
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.transaction.user.id = :userId " +
           "AND fa.createdAt > :since AND fa.status != 'FALSE_POSITIVE' " +
           "ORDER BY fa.createdAt DESC")
    List<FraudAlert> findValidAlertsForUser(@Param("userId") Long userId, 
                                          @Param("since") LocalDateTime since);
    
    // Update queries
    @Query("UPDATE FraudAlert fa SET fa.status = :status, fa.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE fa.id = :alertId")
    void updateAlertStatus(@Param("alertId") Long alertId, @Param("status") AlertStatus status);
    
    @Query("UPDATE FraudAlert fa SET fa.status = :status, fa.resolvedBy = :resolvedBy, " +
           "fa.resolvedAt = CURRENT_TIMESTAMP, fa.analystNotes = :notes, " +
           "fa.updatedAt = CURRENT_TIMESTAMP WHERE fa.id = :alertId")
    void resolveAlert(@Param("alertId") Long alertId, @Param("status") AlertStatus status,
                     @Param("resolvedBy") String resolvedBy, @Param("notes") String notes);
    
    @Query("UPDATE FraudAlert fa SET fa.falsePositive = true, fa.status = 'FALSE_POSITIVE', " +
           "fa.resolvedBy = :resolvedBy, fa.resolvedAt = CURRENT_TIMESTAMP, " +
           "fa.updatedAt = CURRENT_TIMESTAMP WHERE fa.id = :alertId")
    void markAsFalsePositive(@Param("alertId") Long alertId, @Param("resolvedBy") String resolvedBy);
}
