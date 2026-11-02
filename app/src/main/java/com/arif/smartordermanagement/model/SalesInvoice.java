package com.arif.smartordermanagement.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SalesInvoice {
    @SerializedName("invoice_id") private String invoiceId;
    @SerializedName("invoice_number") private String invoiceNumber;
    private String status;
    @SerializedName("invoice_value") private String invoiceValue;
    @SerializedName("discount_value") private String discountValue;
    @SerializedName("return_amount") private String returnAmount;
    @SerializedName("addi_discount") private String additionalDiscount;
    @SerializedName("payable_amount") private String payableAmount;
    @SerializedName("sale_point_name") private String salePointName;
    private String address;
    @SerializedName("order_date") private String orderDate;
    @SerializedName("invoice_date") private String invoiceDate;
    private String username;
    @SerializedName("employee_name") private String employeeName;
    private String avene;
    private String biumark;
    @SerializedName("bionnex") private String bionnex;
    private String carex;
    private String wahib;
    private String cosmed;

    public String getInvoiceId() { return text(invoiceId); }
    public String getInvoiceNumber() { return text(invoiceNumber); }
    public String getStatus() { return text(status); }
    public String getInvoiceValue() { return amountValue(invoiceValue); }
    public String getDiscountValue() { return amountValue(discountValue); }
    public String getReturnAmount() { return amountValue(returnAmount); }
    public String getAdditionalDiscount() { return amountValue(additionalDiscount); }
    public String getPayableAmount() { return amountValue(payableAmount); }
    public String getSalePointName() { return text(salePointName); }
    public String getAddress() { return text(address); }
    public String getOrderDate() { return text(orderDate); }
    public String getInvoiceDate() { return text(invoiceDate); }
    public String getUsername() { return text(username); }
    public String getEmployeeName() { return text(employeeName); }
    public String getAveneAmount() { return amountValue(avene); }
    public String getBiumarkAmount() { return amountValue(biumark); }
    public String getBionnexAmount() { return amountValue(bionnex); }
    public String getCosmedAmount() { return amountValue(cosmed); }
    public String getWahibAmount() { return amountValue(wahib); }
    public String getBrandTotals() {
        List<String> brands = new ArrayList<>();
        addBrand(brands, "Avene", avene);
        addBrand(brands, "Biumark", biumark);
        addBrand(brands, "Bionnex", bionnex);
        addBrand(brands, "Carex", carex);
        addBrand(brands, "Wahib", wahib);
        addBrand(brands, "Cosmed", cosmed);
        if (brands.isEmpty()) return "";

        StringBuilder result = new StringBuilder("Brands  •  ");
        for (int i = 0; i < brands.size(); i++) {
            if (i > 0) result.append("  •  ");
            result.append(brands.get(i));
        }
        return result.toString();
    }

    private static void addBrand(List<String> brands, String name, String rawAmount) {
        String raw = amountValue(rawAmount);
        try {
            BigDecimal amount = new BigDecimal(raw.replace(",", "").trim());
            if (amount.signum() == 0) return;
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(0);
            brands.add(name + " ৳" + format.format(amount));
        } catch (NumberFormatException ignored) {
            brands.add(name + " ৳" + raw);
        }
    }

    private static String text(String value) { return value == null ? "" : value.trim(); }
    private static String amountValue(String value) { return value == null || value.trim().isEmpty() ? "0" : value.trim(); }
}
