package com.fraudguard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @NotNull(message = "Merchant is required")
    @Column(nullable = false)
    private String merchant;
    
    private String category;
    
    private String location;
    
    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    @Column(name = "fraud_score", precision = 3, scale = 2)
    private BigDecimal fraudScore;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FraudAlert> fraudAlerts;
    
    @PrePersist
    protected void onCreate() {
        if (transactionTime == null) {
            transactionTime = LocalDateTime.now();
        }
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
        if (fraudScore == null) {
            fraudScore = BigDecimal.ZERO;
        }
    }
}

