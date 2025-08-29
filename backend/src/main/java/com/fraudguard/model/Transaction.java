package com.fraudguard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_user", columnList = "user_id"),
    @Index(name = "idx_transaction_time", columnList = "transaction_time"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_fraud_score", columnList = "fraud_score"),
    @Index(name = "idx_transaction_amount", columnList = "amount")
})
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @NotNull(message = "Merchant is required")
    @Size(max = 255, message = "Merchant name cannot exceed 255 characters")
    @Column(nullable = false)
    private String merchant;
    
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;
    
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;
    
    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "fraud_score", precision = 3, scale = 2)
    private BigDecimal fraudScore = BigDecimal.ZERO;
    
    @Column(name = "transaction_type", length = 50)
    private String transactionType;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;
    
    @Column(name = "merchant_category_code", length = 10)
    private String merchantCategoryCode;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode = "USD";
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "device_fingerprint")
    private String deviceFingerprint;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "is_weekend")
    private Boolean isWeekend;
    
    @Column(name = "is_night_time")
    private Boolean isNightTime;
    
    @Column(name = "days_since_last_transaction")
    private Integer daysSinceLastTransaction;
    
    @Column(name = "velocity_1h")
    private Integer velocity1h = 0;
    
    @Column(name = "velocity_24h")
    private Integer velocity24h = 0;
    
    @Column(name = "velocity_7d")
    private Integer velocity7d = 0;
    
    @Column(name = "amount_1h")
    private BigDecimal amount1h = BigDecimal.ZERO;
    
    @Column(name = "amount_24h")
    private BigDecimal amount24h = BigDecimal.ZERO;
    
    @Column(name = "amount_7d")
    private BigDecimal amount7d = BigDecimal.ZERO;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @JsonIgnore
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FraudAlert> fraudAlerts = new ArrayList<>();
    
    @JsonIgnore
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionFeature> features = new ArrayList<>();
    
    // Constructors
    public Transaction() {
        LocalDateTime now = LocalDateTime.now();
        this.transactionTime = now;
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    public Transaction(User user, BigDecimal amount, String merchant) {
        this();
        this.user = user;
        this.amount = amount;
        this.merchant = merchant;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.transactionTime == null) {
            this.transactionTime = now;
        }
        this.createdAt = now;
        this.updatedAt = now;
        
        // Set time-based features
        this.isWeekend = isWeekendTime(this.transactionTime);
        this.isNightTime = isNightTime(this.transactionTime);
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Utility methods
    private boolean isWeekendTime(LocalDateTime dateTime) {
        int dayOfWeek = dateTime.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
    }
    
    private boolean isNightTime(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        return hour < 6 || hour > 22; // Between 10 PM and 6 AM
    }
    
    public boolean isHighRisk() {
        return fraudScore != null && fraudScore.compareTo(new BigDecimal("0.7")) > 0;
    }
    
    public boolean isLargeAmount() {
        return amount.compareTo(new BigDecimal("1000.00")) > 0;
    }
    
    public boolean isFlagged() {
        return status == TransactionStatus.FLAGGED;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDateTime getTransactionTime() { return transactionTime; }
    public void setTransactionTime(LocalDateTime transactionTime) { this.transactionTime = transactionTime; }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    public BigDecimal getFraudScore() { return fraudScore; }
    public void setFraudScore(BigDecimal fraudScore) { this.fraudScore = fraudScore; }
    
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }
    
    public String getMerchantCategoryCode() { return merchantCategoryCode; }
    public void setMerchantCategoryCode(String merchantCategoryCode) { this.merchantCategoryCode = merchantCategoryCode; }
    
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Boolean getIsWeekend() { return isWeekend; }
    public void setIsWeekend(Boolean isWeekend) { this.isWeekend = isWeekend; }
    
    public Boolean getIsNightTime() { return isNightTime; }
    public void setIsNightTime(Boolean isNightTime) { this.isNightTime = isNightTime; }
    
    public Integer getDaysSinceLastTransaction() { return daysSinceLastTransaction; }
    public void setDaysSinceLastTransaction(Integer daysSinceLastTransaction) { this.daysSinceLastTransaction = daysSinceLastTransaction; }
    
    public Integer getVelocity1h() { return velocity1h; }
    public void setVelocity1h(Integer velocity1h) { this.velocity1h = velocity1h; }
    
    public Integer getVelocity24h() { return velocity24h; }
    public void setVelocity24h(Integer velocity24h) { this.velocity24h = velocity24h; }
    
    public Integer getVelocity7d() { return velocity7d; }
    public void setVelocity7d(Integer velocity7d) { this.velocity7d = velocity7d; }
    
    public BigDecimal getAmount1h() { return amount1h; }
    public void setAmount1h(BigDecimal amount1h) { this.amount1h = amount1h; }
    
    public BigDecimal getAmount24h() { return amount24h; }
    public void setAmount24h(BigDecimal amount24h) { this.amount24h = amount24h; }
    
    public BigDecimal getAmount7d() { return amount7d; }
    public void setAmount7d(BigDecimal amount7d) { this.amount7d = amount7d; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public List<FraudAlert> getFraudAlerts() { return fraudAlerts; }
    public void setFraudAlerts(List<FraudAlert> fraudAlerts) { this.fraudAlerts = fraudAlerts; }
    
    public List<TransactionFeature> getFeatures() { return features; }
    public void setFeatures(List<TransactionFeature> features) { this.features = features; }
}
