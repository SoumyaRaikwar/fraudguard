package com.fraudguard.model;

public enum AccountStatus {
    ACTIVE("Account is active"),
    SUSPENDED("Account is suspended"),
    BLOCKED("Account is blocked"),
    PENDING_VERIFICATION("Account pending verification"),
    CLOSED("Account is closed");
    
    private final String description;
    
    AccountStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this == ACTIVE;
    }
    
    public boolean isBlocked() {
        return this == BLOCKED || this == SUSPENDED;
    }
}
