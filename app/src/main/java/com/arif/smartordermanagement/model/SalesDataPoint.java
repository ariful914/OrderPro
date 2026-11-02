package com.arif.smartordermanagement.model;

public class SalesDataPoint {
    private final String label;
    private final double amount;

    public SalesDataPoint(String label, double amount) {
        this.label = label;
        this.amount = amount;
    }

    public String getLabel() {
        return label;
    }

    public double getAmount() {
        return amount;
    }
}
