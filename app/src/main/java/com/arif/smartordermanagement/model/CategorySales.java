package com.arif.smartordermanagement.model;

public class CategorySales {
    private final String categoryName;
    private final String salesAmountFormatted;
    private final float sharePercentage;

    public CategorySales(String categoryName, String salesAmountFormatted, float sharePercentage) {
        this.categoryName = categoryName;
        this.salesAmountFormatted = salesAmountFormatted;
        this.sharePercentage = sharePercentage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getSalesAmountFormatted() {
        return salesAmountFormatted;
    }

    public float getSharePercentage() {
        return sharePercentage;
    }
}
