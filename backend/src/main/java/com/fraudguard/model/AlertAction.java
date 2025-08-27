package com.fraudguard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_actions", indexes = {
    @Index(name = "idx_action_alert", columnList = "fraud_alert_id"),
    @Index(name = "idx_action_created", columnList = "created_at")
})
public class AlertAction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraud_alert_id", nullable = false)
    private FraudAlert fraudAlert;
    
    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType; // BLOCK_CARD, REQUIRE_2FA, NOTIFY_USER, etc.
    
    @Column(name = "action_status", nullable = false, length = 50)
    private String actionStatus; // PENDING, COMPLETED, FAILED
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "performed_by", length = 100)
    private String performedBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Constructors
    public AlertAction() {
        this.createdAt = LocalDateTime.now();
    }
    
    public AlertAction(FraudAlert fraudAlert, String actionType, String description) {
        this();
        this.fraudAlert = fraudAlert;
        this.actionType = actionType;
        this.description = description;
        this.actionStatus = "PENDING";
    }
    
    // Utility methods
    public void complete() {
        this.actionStatus = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }
    
    public void fail() {
        this.actionStatus = "FAILED";
        this.completedAt = LocalDateTime.now();
    }
    
    public boolean isPending() {
        return "PENDING".equals(actionStatus);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(actionStatus);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public FraudAlert getFraudAlert() { return fraudAlert; }
    public void setFraudAlert(FraudAlert fraudAlert) { this.fraudAlert = fraudAlert; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public String getActionStatus() { return actionStatus; }
    public void setActionStatus(String actionStatus) { this.actionStatus = actionStatus; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
