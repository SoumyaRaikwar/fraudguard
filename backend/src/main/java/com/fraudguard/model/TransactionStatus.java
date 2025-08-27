package com.fraudguard.model;

public enum TransactionStatus {
    PENDING("Transaction is being processed"),
    APPROVED("Transaction approved"),
    FLAGGED("Transaction flagged for review"),
    REJECTED("Transaction rejected"),
    UNDER_REVIEW("Transaction under manual review"),
    CANCELLED("Transaction cancelled"),
    TIMEOUT("Transaction timed out"),
    FAILED("Transaction failed");
    
    private final String description;
    
    TransactionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCompleted() {
        return this == APPROVED || this == REJECTED || this == CANCELLED;
    }
    
    public boolean requiresReview() {
        return this == FLAGGED || this == UNDER_REVIEW;
    }
    
    public boolean isPending() {
        return this == PENDING;
    }
}
