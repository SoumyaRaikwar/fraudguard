package com.fraudguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_features", indexes = {
    @Index(name = "idx_feature_transaction", columnList = "transaction_id"),
    @Index(name = "idx_feature_name", columnList = "feature_name")
})
public class TransactionFeature {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;
    
    @Column(name = "feature_name", nullable = false, length = 100)
    private String featureName;
    
    @Column(name = "feature_value", columnDefinition = "TEXT")
    private String featureValue;
    
    @Column(name = "feature_type", length = 50)
    private String featureType; // NUMERIC, CATEGORICAL, BOOLEAN
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public TransactionFeature() {
        this.createdAt = LocalDateTime.now();
    }
    
    public TransactionFeature(Transaction transaction, String featureName, String featureValue, String featureType) {
        this();
        this.transaction = transaction;
        this.featureName = featureName;
        this.featureValue = featureValue;
        this.featureType = featureType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }
    
    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
    
    public String getFeatureValue() { return featureValue; }
    public void setFeatureValue(String featureValue) { this.featureValue = featureValue; }
    
    public String getFeatureType() { return featureType; }
    public void setFeatureType(String featureType) { this.featureType = featureType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
