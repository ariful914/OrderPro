package com.arif.smartordermanagement.data;

import com.arif.smartordermanagement.model.CategorySales;
import com.arif.smartordermanagement.model.KpiMetric;
import com.arif.smartordermanagement.model.KpiType;
import com.arif.smartordermanagement.model.Order;
import com.arif.smartordermanagement.model.OrderStatus;
import com.arif.smartordermanagement.model.SalesDataPoint;
import com.arif.smartordermanagement.model.SalesTrend;
import com.arif.smartordermanagement.model.TimeFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SalesRepository {

    public interface DataCallback<T> {
        void onLoaded(T data);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void getKpiMetrics(TimeFrame timeFrame, DataCallback<List<KpiMetric>> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {}

            List<KpiMetric> metrics;
            if (timeFrame == TimeFrame.TODAY) {
                metrics = Arrays.asList(
                        new KpiMetric(KpiType.REVENUE, "Total Revenue", "৳3,420.50", "+8.4%", true, "vs yesterday"),
                        new KpiMetric(KpiType.ORDERS, "Orders Placed", "28", "+12.0%", true, "vs yesterday"),
                        new KpiMetric(KpiType.AVERAGE_ORDER_VALUE, "Avg Order Value", "৳122.16", "-1.2%", false, "vs yesterday"),
                        new KpiMetric(KpiType.PENDING_DELIVERIES, "Pending Delivery", "5", "-20.0%", true, "shipped today")
                );
            } else if (timeFrame == TimeFrame.THIS_MONTH) {
                metrics = Arrays.asList(
                        new KpiMetric(KpiType.REVENUE, "Total Revenue", "৳98,400.00", "+18.7%", true, "vs last month"),
                        new KpiMetric(KpiType.ORDERS, "Orders Placed", "712", "+11.3%", true, "vs last month"),
                        new KpiMetric(KpiType.AVERAGE_ORDER_VALUE, "Avg Order Value", "৳138.20", "+4.5%", true, "vs last month"),
                        new KpiMetric(KpiType.PENDING_DELIVERIES, "Pending Delivery", "18", "+2.0%", false, "in queue")
                );
            } else { // THIS_WEEK
                metrics = Arrays.asList(
                        new KpiMetric(KpiType.REVENUE, "Total Revenue", "৳24,850.00", "+14.2%", true, "vs last week"),
                        new KpiMetric(KpiType.ORDERS, "Orders Placed", "184", "+8.5%", true, "vs last week"),
                        new KpiMetric(KpiType.AVERAGE_ORDER_VALUE, "Avg Order Value", "৳135.05", "+3.1%", true, "vs last week"),
                        new KpiMetric(KpiType.PENDING_DELIVERIES, "Pending Delivery", "12", "-15.0%", true, "processing")
                );
            }

            callback.onLoaded(metrics);
        });
    }

    public void getSalesTrend(TimeFrame timeFrame, DataCallback<SalesTrend> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {}

            SalesTrend trend;
            if (timeFrame == TimeFrame.TODAY) {
                trend = new SalesTrend(
                        timeFrame,
                        "৳3,420.50",
                        "৳427.50 / hr",
                        Arrays.asList(
                                new SalesDataPoint("8 AM", 210.0),
                                new SalesDataPoint("10 AM", 450.0),
                                new SalesDataPoint("12 PM", 820.0),
                                new SalesDataPoint("2 PM", 640.0),
                                new SalesDataPoint("4 PM", 590.0),
                                new SalesDataPoint("6 PM", 710.0)
                        )
                );
            } else if (timeFrame == TimeFrame.THIS_MONTH) {
                trend = new SalesTrend(
                        timeFrame,
                        "৳98,400.00",
                        "৳24,600.00 / wk",
                        Arrays.asList(
                                new SalesDataPoint("W1", 21500.0),
                                new SalesDataPoint("W2", 23800.0),
                                new SalesDataPoint("W3", 26900.0),
                                new SalesDataPoint("W4", 26200.0)
                        )
                );
            } else { // THIS_WEEK
                trend = new SalesTrend(
                        timeFrame,
                        "৳24,850.00",
                        "৳3,550.00 / day",
                        Arrays.asList(
                                new SalesDataPoint("Mon", 2800.0),
                                new SalesDataPoint("Tue", 3200.0),
                                new SalesDataPoint("Wed", 4100.0),
                                new SalesDataPoint("Thu", 3600.0),
                                new SalesDataPoint("Fri", 5200.0),
                                new SalesDataPoint("Sat", 3900.0),
                                new SalesDataPoint("Sun", 2050.0)
                        )
                );
            }

            callback.onLoaded(trend);
        });
    }

    public void getCategoryBreakdown(TimeFrame timeFrame, DataCallback<List<CategorySales>> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            List<CategorySales> categories = Arrays.asList(
                    new CategorySales("Electronics", "৳10,437.00", 0.42f),
                    new CategorySales("Office Supplies", "৳6,212.50", 0.25f),
                    new CategorySales("Hardware & Tools", "৳4,970.00", 0.20f),
                    new CategorySales("Software Licenses", "৳3,230.50", 0.13f)
            );

            callback.onLoaded(categories);
        });
    }

    public void getRecentOrders(String query, OrderStatus filterStatus, DataCallback<List<Order>> callback) {
        executor.execute(() -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {}

            List<Order> allOrders = Arrays.asList(
                    new Order("#ORD-9841", "TechCorp Systems", "Electronics", 12, "৳2,450.00", OrderStatus.COMPLETED, "10 mins ago"),
                    new Order("#ORD-9840", "Green Valley Ltd", "Office Supplies", 4, "৳380.50", OrderStatus.PROCESSING, "25 mins ago"),
                    new Order("#ORD-9839", "Metro Logistics", "Hardware & Tools", 8, "৳1,120.00", OrderStatus.PENDING, "1 hour ago"),
                    new Order("#ORD-9838", "Horizon Media", "Software Licenses", 2, "৳599.00", OrderStatus.COMPLETED, "2 hours ago"),
                    new Order("#ORD-9837", "Nexus Retail", "Electronics", 15, "৳3,890.00", OrderStatus.COMPLETED, "3 hours ago"),
                    new Order("#ORD-9836", "Cascade Traders", "Office Supplies", 6, "৳445.00", OrderStatus.CANCELLED, "5 hours ago"),
                    new Order("#ORD-9835", "Pinnacle Group", "Hardware & Tools", 10, "৳1,850.00", OrderStatus.PROCESSING, "Yesterday")
            );

            String q = query != null ? query.trim().toLowerCase() : "";
            List<Order> filtered = new ArrayList<>();

            for (Order order : allOrders) {
                boolean matchesQuery = q.isEmpty() ||
                        order.getId().toLowerCase().contains(q) ||
                        order.getCustomerName().toLowerCase().contains(q) ||
                        order.getCategory().toLowerCase().contains(q);

                boolean matchesStatus = filterStatus == null || order.getStatus() == filterStatus;

                if (matchesQuery && matchesStatus) {
                    filtered.add(order);
                }
            }

            callback.onLoaded(filtered);
        });
    }
}
