package com.arif.smartordermanagement.model;

public class KpiMetric {
    private final KpiType type;
    private final String title;
    private final String value;
    private final String percentageChange;
    private final boolean isPositive;
    private final String comparisonLabel;

    public KpiMetric(KpiType type, String title, String value, String percentageChange, boolean isPositive, String comparisonLabel) {
        this.type = type;
        this.title = title;
        this.value = value;
        this.percentageChange = percentageChange;
        this.isPositive = isPositive;
        this.comparisonLabel = comparisonLabel;
    }

    public KpiType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getPercentageChange() {
        return percentageChange;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public String getComparisonLabel() {
        return comparisonLabel;
    }
}
