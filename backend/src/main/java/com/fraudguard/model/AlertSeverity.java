package com.fraudguard.model;

public enum AlertSeverity {
    LOW("Low risk alert"),
    MEDIUM("Medium risk alert"),
    HIGH("High risk alert"),
    CRITICAL("Critical risk alert");
    
    private final String description;
    
    AlertSeverity(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getPriorityLevel() {
        switch (this) {
            case LOW: return 1;
            case MEDIUM: return 2;
            case HIGH: return 3;
            case CRITICAL: return 4;
            default: return 0;
        }
    }
    
    public boolean requiresImmediateAttention() {
        return this == HIGH || this == CRITICAL;
    }
}
