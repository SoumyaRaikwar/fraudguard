package com.fraudguard.repository;

import com.fraudguard.model.TransactionFeature;
import com.fraudguard.model.FeatureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionFeatureRepository extends JpaRepository<TransactionFeature, Long> {
    
    // Basic queries
    List<TransactionFeature> findByTransactionId(Long transactionId);
    
    List<TransactionFeature> findByFeatureName(String featureName);
    
    List<TransactionFeature> findByFeatureType(FeatureType featureType);
    
    // Feature value queries
    @Query("SELECT tf FROM TransactionFeature tf WHERE tf.featureName = :featureName " +
           "AND tf.featureValue > :threshold")
    List<TransactionFeature> findFeaturesAboveThreshold(@Param("featureName") String featureName,
                                                       @Param("threshold") BigDecimal threshold);
    
    // Importance queries
    @Query("SELECT tf FROM TransactionFeature tf WHERE tf.importanceScore > :threshold " +
           "ORDER BY tf.importanceScore DESC")
    List<TransactionFeature> findHighImportanceFeatures(@Param("threshold") BigDecimal threshold);
    
    // Statistical queries
    @Query("SELECT tf.featureName, AVG(tf.featureValue), MAX(tf.featureValue), MIN(tf.featureValue) " +
           "FROM TransactionFeature tf WHERE tf.featureType = 'NUMERIC' " +
           "GROUP BY tf.featureName")
    List<Object[]> getNumericFeatureStatistics();
    
    @Query("SELECT tf.featureName, COUNT(tf) FROM TransactionFeature tf " +
           "GROUP BY tf.featureName ORDER BY COUNT(tf) DESC")
    List<Object[]> getFeatureUsageStatistics();
}
