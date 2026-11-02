package com.arif.smartordermanagement.model;

public class Order {
    private final String id;
    private final String customerName;
    private final String category;
    private final int itemCount;
    private final String totalAmountFormatted;
    private final OrderStatus status;
    private final String timeAgo;

    public Order(String id, String customerName, String category, int itemCount, String totalAmountFormatted, OrderStatus status, String timeAgo) {
        this.id = id;
        this.customerName = customerName;
        this.category = category;
        this.itemCount = itemCount;
        this.totalAmountFormatted = totalAmountFormatted;
        this.status = status;
        this.timeAgo = timeAgo;
    }

    public String getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCategory() {
        return category;
    }

    public int getItemCount() {
        return itemCount;
    }

    public String getTotalAmountFormatted() {
        return totalAmountFormatted;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getTimeAgo() {
        return timeAgo;
    }
}
