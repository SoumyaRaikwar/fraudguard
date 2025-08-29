package com.fraudguard.repository;

import com.fraudguard.model.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    
    // Basic queries
    List<UserBehavior> findByUserId(Long userId);
    
    Optional<UserBehavior> findByUserIdAndBehaviorType(Long userId, String behaviorType);
    
    List<UserBehavior> findByBehaviorType(String behaviorType);
    
    // Latest behavior queries
    @Query("SELECT ub FROM UserBehavior ub WHERE ub.user.id = :userId " +
           "ORDER BY ub.updatedAt DESC")
    List<UserBehavior> findLatestUserBehavior(@Param("userId") Long userId);
    
    @Query("SELECT ub FROM UserBehavior ub WHERE ub.user.id = :userId " +
           "AND ub.behaviorType = :behaviorType ORDER BY ub.updatedAt DESC")
    Optional<UserBehavior> findLatestUserBehaviorByType(@Param("userId") Long userId, 
                                                       @Param("behaviorType") String behaviorType);
    
    // Risk-based queries
    @Query("SELECT ub FROM UserBehavior ub WHERE ub.riskScore > :threshold")
    List<UserBehavior> findHighRiskBehaviors(@Param("threshold") BigDecimal threshold);
    
    @Query("SELECT ub FROM UserBehavior ub WHERE ub.riskScore BETWEEN :minScore AND :maxScore")
    List<UserBehavior> findBehaviorsByRiskRange(@Param("minScore") BigDecimal minScore, 
                                              @Param("maxScore") BigDecimal maxScore);
    
    // Time-based queries
    @Query("SELECT ub FROM UserBehavior ub WHERE ub.createdAt > :since")
    List<UserBehavior> findRecentBehaviorUpdates(@Param("since") LocalDateTime since);
    
    @Query("SELECT ub FROM UserBehavior ub WHERE ub.analysisPeriodStart >= :startDate " +
           "AND ub.analysisPeriodEnd <= :endDate")
    List<UserBehavior> findBehaviorsByAnalysisPeriod(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
    
    // Statistical queries
    @Query("SELECT AVG(ub.avgTransactionAmount) FROM UserBehavior ub " +
           "WHERE ub.behaviorType = :behaviorType")
    BigDecimal getAverageTransactionAmountByType(@Param("behaviorType") String behaviorType);
    
    @Query("SELECT ub.behaviorType, COUNT(ub), AVG(ub.riskScore) FROM UserBehavior ub " +
           "GROUP BY ub.behaviorType")
    List<Object[]> getBehaviorStatistics();
    
    // Update queries
    @Query("UPDATE UserBehavior ub SET ub.riskScore = :riskScore, " +
           "ub.updatedAt = CURRENT_TIMESTAMP WHERE ub.id = :behaviorId")
    void updateRiskScore(@Param("behaviorId") Long behaviorId, 
                        @Param("riskScore") BigDecimal riskScore);
}
