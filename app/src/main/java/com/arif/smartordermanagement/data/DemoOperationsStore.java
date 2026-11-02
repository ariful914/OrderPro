package com.arif.smartordermanagement.data;

import java.util.ArrayList;
import java.util.List;

/** In-memory sample records for the operations screens until a business API is connected. */
public final class DemoOperationsStore {

    private static final List<OrderEntry> ORDERS = new ArrayList<>();
    private static final List<SampleEntry> SAMPLES = new ArrayList<>();
    private static final List<RequirementEntry> REQUIREMENTS = new ArrayList<>();
    private static int nextOrderNumber = 9842;
    private static int nextSampleNumber = 1022;

    static {
        ORDERS.add(new OrderEntry("#ORD-9841", "TechCorp Systems", "Wireless keyboard", 12, 2450.00, "Completed", "Today"));
        ORDERS.add(new OrderEntry("#ORD-9840", "Green Valley Ltd", "A4 paper", 4, 380.50, "Processing", "Today"));
        ORDERS.add(new OrderEntry("#ORD-9839", "Metro Logistics", "Label printer", 8, 1120.00, "Pending", "Yesterday"));
        ORDERS.add(new OrderEntry("#ORD-9838", "Horizon Media", "Design software", 2, 599.00, "Completed", "Yesterday"));

        SAMPLES.add(new SampleEntry("SR-1021", "Nexus Retail", "Wireless keyboard", 2, "Product evaluation", "Pending"));
        SAMPLES.add(new SampleEntry("SR-1020", "Cascade Traders", "Label printer", 1, "New outlet opening", "Approved"));

        REQUIREMENTS.add(new RequirementEntry("REQ-301", "Nexus Retail", "Custom product bundle pricing", "Today", "Under review"));
        REQUIREMENTS.add(new RequirementEntry("REQ-300", "Green Valley Ltd", "Branded packaging samples", "Yesterday", "Approved"));
    }

    private DemoOperationsStore() {}

    public static synchronized List<OrderEntry> getOrders() {
        return new ArrayList<>(ORDERS);
    }

    public static synchronized void addOrder(String customer, String product, int quantity, double amount) {
        ORDERS.add(0, new OrderEntry("#ORD-" + nextOrderNumber++, customer, product, quantity, amount, "Pending", "Today"));
    }

    public static synchronized List<SampleEntry> getSamples() {
        return new ArrayList<>(SAMPLES);
    }

    public static synchronized void addSample(String customer, String product, int quantity, String reason) {
        SAMPLES.add(0, new SampleEntry("SR-" + nextSampleNumber++, customer, product, quantity, reason, "Pending"));
    }

    public static synchronized void updateSampleStatus(String sampleId, String status) {
        for (SampleEntry sample : SAMPLES) {
            if (sample.id.equals(sampleId)) {
                sample.status = status;
                return;
            }
        }
    }

    public static synchronized List<RequirementEntry> getRequirements() {
        return new ArrayList<>(REQUIREMENTS);
    }

    public static final class OrderEntry {
        public final String id;
        public final String customer;
        public final String product;
        public final int quantity;
        public final double amount;
        public final String status;
        public final String date;

        private OrderEntry(String id, String customer, String product, int quantity, double amount, String status, String date) {
            this.id = id;
            this.customer = customer;
            this.product = product;
            this.quantity = quantity;
            this.amount = amount;
            this.status = status;
            this.date = date;
        }
    }

    public static final class SampleEntry {
        public final String id;
        public final String customer;
        public final String product;
        public final int quantity;
        public final String reason;
        public String status;

        private SampleEntry(String id, String customer, String product, int quantity, String reason, String status) {
            this.id = id;
            this.customer = customer;
            this.product = product;
            this.quantity = quantity;
            this.reason = reason;
            this.status = status;
        }
    }

    public static final class RequirementEntry {
        public final String id;
        public final String requester;
        public final String summary;
        public final String date;
        public final String status;

        private RequirementEntry(String id, String requester, String summary, String date, String status) {
            this.id = id;
            this.requester = requester;
            this.summary = summary;
            this.date = date;
            this.status = status;
        }
    }
}
