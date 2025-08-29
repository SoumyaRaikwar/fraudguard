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
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date")
    List<User> findUsersWithLastLoginBefore(@Param("date") LocalDateTime date);
}

