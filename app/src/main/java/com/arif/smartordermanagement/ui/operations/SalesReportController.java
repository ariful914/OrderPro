package com.arif.smartordermanagement.ui.operations;

import com.arif.smartordermanagement.data.DemoOperationsStore;

import java.util.LinkedHashMap;
import java.util.Map;

/** Renders the sales summary, customer, product, and period comparison screens. */
final class SalesReportController {
    private final OperationsUiComponents ui;

    SalesReportController(OperationsUiComponents ui) {
        this.ui = ui;
    }

    void render(String destination) {
        ui.addDemoNotice();
        ui.addReportHeaderFilter(null);

        if (OperationsFragment.ACTUAL_SALES.equals(destination)) {
            renderActualSales();
        } else if (OperationsFragment.CUSTOMER_SALES.equals(destination)) {
            renderSalesByCustomer();
        } else if (OperationsFragment.PRODUCT_SALES.equals(destination)) {
            renderSalesByProduct();
        } else {
            renderPeriodComparison();
        }
    }

    private void renderActualSales() {
        double total = 0;
        int count = 0;
        for (DemoOperationsStore.OrderEntry order : DemoOperationsStore.getOrders()) {
            if ("Today".equals(order.date)) {
                total += order.amount;
                count++;
            }
        }
        ui.addMetricCard("Today's sales", ui.money(total), "Across " + count + " demo orders");
        ui.addSectionTitle("Recent sales");
        for (DemoOperationsStore.OrderEntry order : DemoOperationsStore.getOrders()) {
            ui.addRecordCard(order.id + " · " + order.customer,
                    order.product + " · " + order.quantity + " units",
                    ui.money(order.amount), order.status);
        }
    }

    private void renderSalesByCustomer() {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (DemoOperationsStore.OrderEntry order : DemoOperationsStore.getOrders()) {
            totals.put(order.customer, totals.getOrDefault(order.customer, 0.0) + order.amount);
        }
        ui.addSectionTitle("Sales by customer");
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            ui.addRecordCard(entry.getKey(), "Order value", ui.money(entry.getValue()), null);
        }
    }

    private void renderSalesByProduct() {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (DemoOperationsStore.OrderEntry order : DemoOperationsStore.getOrders()) {
            totals.put(order.product, totals.getOrDefault(order.product, 0.0) + order.amount);
        }
        ui.addSectionTitle("Sales by product");
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            ui.addRecordCard(entry.getKey(), "Product revenue", ui.money(entry.getValue()), null);
        }
    }

    private void renderPeriodComparison() {
        ui.addSectionTitle("Period comparison");
        ui.addMetricCard("This week", "৳24,850.00", "184 orders");
        ui.addComparisonRow("This week", 0.74f, "৳24,850");
        ui.addComparisonRow("Last week", 0.61f, "৳21,760");
        ui.addBodyText("Demo comparison: sales are up 14.2% from last week.");
    }
}
