package com.arif.smartordermanagement.model;

/** Target approval and lifecycle statuses. */
public final class TargetStatus {
    private TargetStatus() {}

    public static final String DRAFT = "Draft";
    public static final String PENDING = "Pending";
    public static final String APPROVED = "Approved";
    public static final String REJECTED = "Rejected";

    public static boolean isApproved(String status) {
        return APPROVED.equalsIgnoreCase(status);
    }

    public static boolean isPending(String status) {
        return PENDING.equalsIgnoreCase(status);
    }

    public static boolean isRejected(String status) {
        return REJECTED.equalsIgnoreCase(status);
    }
}
