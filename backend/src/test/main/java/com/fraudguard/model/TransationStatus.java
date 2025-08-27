package com.fraudguard.model;

public enum TransactionStatus {
    PENDING("Transaction is being processed"),
    APPROVED("Transaction approved"),
    FLAGGED("Transaction flagged for review"),
    REJECTED("Transaction rejected"),
    UNDER_REVIEW("Transaction under manual review");
    
    private final String description;
    
    TransactionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
