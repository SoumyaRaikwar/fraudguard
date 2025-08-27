package com.fraudguard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
    
    @NotBlank(message = "Alert type is required")
    @Column(name = "alert_type", nullable = false)
    private String alertType;
    
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    private Boolean resolved;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (resolved == null) {
            resolved = false;
        }
        if (severity == null) {
            severity = AlertSeverity.MEDIUM;
        }
    }
}

enum AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}
