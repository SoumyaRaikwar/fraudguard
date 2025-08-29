package com.fraudguard.model;

public enum AlertStatus {
    OPEN("Alert is open and needs investigation"),
    IN_PROGRESS("Alert is being investigated"),
    RESOLVED("Alert has been resolved"),
    FALSE_POSITIVE("Alert was a false positive"),
    ESCALATED("Alert has been escalated");
    
    private final String description;
    
    AlertStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this == OPEN || this == IN_PROGRESS || this == ESCALATED;
    }
    
    public boolean isClosed() {
        return this == RESOLVED || this == FALSE_POSITIVE;
    }
}
