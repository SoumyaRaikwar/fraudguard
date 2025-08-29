package com.fraudguard.repository;

import com.fraudguard.model.User;
import com.fraudguard.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic queries
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByAccountStatus(AccountStatus accountStatus);
    
    // Risk-based queries
    @Query("SELECT u FROM User u WHERE u.riskScore > :threshold")
    List<User> findHighRiskUsers(@Param("threshold") Double threshold);
    
    @Query("SELECT u FROM User u WHERE u.riskScore BETWEEN :minScore AND :maxScore")
    List<User> findUsersByRiskScoreRange(@Param("minScore") Double minScore, 
                                        @Param("maxScore") Double maxScore);
    
    // Location-based queries
    List<User> findByCity(String city);
    
    List<User> findByCountry(String country);
    
    @Query("SELECT u FROM User u WHERE u.city = :city AND u.country = :country")
    List<User> findByLocation(@Param("city") String city, @Param("country") String country);
    
    // Time-based queries
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :dateTime")
    List<User> findInactiveUsersSince(@Param("dateTime") LocalDateTime dateTime);
    
    // Search queries
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
    
    // Statistical queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountStatus = :status")
    Long countByAccountStatus(@Param("status") AccountStatus status);
    
    @Query("SELECT AVG(u.riskScore) FROM User u WHERE u.accountStatus = 'ACTIVE'")
    Double getAverageRiskScore();
    
    @Query("SELECT u.country, COUNT(u) FROM User u GROUP BY u.country ORDER BY COUNT(u) DESC")
    List<Object[]> getUserCountByCountry();
    
    // Complex fraud-related queries
    @Query("SELECT u FROM User u WHERE u.id IN " +
           "(SELECT t.user.id FROM Transaction t WHERE t.status = 'FLAGGED' " +
           "GROUP BY t.user.id HAVING COUNT(t) > :threshold)")
    List<User> findUsersWithMultipleFlaggedTransactions(@Param("threshold") Integer threshold);
    
    @Query("SELECT u FROM User u JOIN u.transactions t " +
           "WHERE t.fraudScore > :fraudScore AND t.createdAt > :since")
    List<User> findUsersWithRecentHighRiskTransactions(@Param("fraudScore") Double fraudScore, 
                                                      @Param("since") LocalDateTime since);
    
    // Update queries
    @Query("UPDATE User u SET u.riskScore = :riskScore, u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId")
    void updateUserRiskScore(@Param("userId") Long userId, @Param("riskScore") Double riskScore);
    
    @Query("UPDATE User u SET u.lastLogin = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId);
    
    @Query("UPDATE User u SET u.accountStatus = :status WHERE u.id = :userId")
    void updateAccountStatus(@Param("userId") Long userId, @Param("status") AccountStatus status);
}
