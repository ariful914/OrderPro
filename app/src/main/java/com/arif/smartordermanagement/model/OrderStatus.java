package com.arif.smartordermanagement.model;

public enum OrderStatus {
    COMPLETED("Completed"),
    PROCESSING("Processing"),
    PENDING("Pending"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
