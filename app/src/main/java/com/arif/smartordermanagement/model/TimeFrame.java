package com.arif.smartordermanagement.model;

public enum TimeFrame {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month");

    private final String displayName;

    TimeFrame(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
