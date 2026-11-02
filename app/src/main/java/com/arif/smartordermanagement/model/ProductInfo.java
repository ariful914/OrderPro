package com.arif.smartordermanagement.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a product used for sales target submission, editing, and approval.
 */
public class ProductInfo implements Serializable, Cloneable {

    @SerializedName(value = "product_id", alternate = {"id", "productId"})
    private int productId;

    @SerializedName(value = "product_code", alternate = {"sku", "productCode", "code"})
    private String productCode;

    @SerializedName(value = "product_name", alternate = {"name", "productName"})
    private String productName;

    @SerializedName(value = "manufacturer", alternate = {"brand", "brandName"})
    private String brandName;

    @SerializedName(value = "category_name", alternate = {"category", "categoryName"})
    private String category;

    @SerializedName(value = "pack_size", alternate = {"packSize", "unit", "pack"})
    private String packSize;

    @SerializedName(value = "tp", alternate = {"trade_price", "tradePrice"})
    private double tp;

    @SerializedName(value = "mrp", alternate = {"unit_price", "unitPrice"})
    private double mrp;

    @SerializedName(value = "target_qty", alternate = {"quantity", "targetQty", "qty"})
    private int targetQty;

    @SerializedName(value = "approved_qty", alternate = {"approvedQty"})
    private int approvedQty;

    @SerializedName(value = "previous_qty", alternate = {"previousQty", "prevQty"})
    private int previousQty;

    @SerializedName("remarks")
    private String remarks;

    public ProductInfo() {
        this.packSize = "Standard";
        this.remarks = "";
    }

    public ProductInfo(int productId, String productCode, String productName, String brandName,
                       String category, String packSize, double tp, double mrp, int previousQty) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.brandName = brandName;
        this.category = category;
        this.packSize = packSize;
        this.tp = tp;
        this.mrp = mrp;
        this.previousQty = previousQty;
        this.targetQty = 0;
        this.approvedQty = 0;
        this.remarks = "";
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductCode() { return productCode != null ? productCode : ""; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName != null ? productName : ""; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrandName() { return brandName != null ? brandName : "General"; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getCategory() { return category != null ? category : "All"; }
    public void setCategory(String category) { this.category = category; }

    public String getPackSize() { return packSize != null ? packSize : "Standard"; }
    public void setPackSize(String packSize) { this.packSize = packSize; }

    public double getTp() { return tp; }
    public void setTp(double tp) { this.tp = tp; }

    public double getMrp() { return mrp; }
    public void setMrp(double mrp) { this.mrp = mrp; }

    public int getTargetQty() { return targetQty; }
    public void setTargetQty(int targetQty) {
        this.targetQty = Math.max(0, targetQty);
    }

    public int getApprovedQty() { return approvedQty; }
    public void setApprovedQty(int approvedQty) {
        this.approvedQty = Math.max(0, approvedQty);
    }

    public int getPreviousQty() { return previousQty; }
    public void setPreviousQty(int previousQty) { this.previousQty = previousQty; }

    public String getRemarks() { return remarks != null ? remarks : ""; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    /** Returns effective unit price for calculations (TP if available, otherwise MRP). */
    public double getEffectiveUnitPrice() {
        return tp > 0 ? tp : mrp;
    }

    /** Returns calculated total value for the user-submitted target quantity. */
    public double getTargetAmount() {
        return targetQty * getEffectiveUnitPrice();
    }

    /** Returns calculated total value for the senior-approved quantity. */
    public double getApprovedAmount() {
        int qty = approvedQty > 0 ? approvedQty : targetQty;
        return qty * getEffectiveUnitPrice();
    }

    @Override
    public ProductInfo clone() {
        try {
            return (ProductInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            ProductInfo clone = new ProductInfo(this.productId, this.productCode, this.productName,
                    this.brandName, this.category, this.packSize, this.tp, this.mrp, this.previousQty);
            clone.setTargetQty(this.targetQty);
            clone.setApprovedQty(this.approvedQty);
            clone.setRemarks(this.remarks);
            return clone;
        }
    }

    /**
     * Default catalog fallback items for demo / offline resilience.
     */
    public static List<ProductInfo> getDefaultProducts() {
        List<ProductInfo> list = new ArrayList<>();
        list.add(new ProductInfo(101, "371077", "Avene Xeracalm Cleansing Gel · 100ml", "Avene", "Cosmed", "100 ml", 1900.0, 2185.0, 30));
        list.add(new ProductInfo(102, "396005", "Avene Cleanance Gel · 200ml", "Avene", "Cosmed", "200 ml", 1960.0, 2250.0, 45));
        list.add(new ProductInfo(103, "396007", "Thermal Spring Water Spray · 150ml", "Avene", "Cosmed", "150 ml", 1420.0, 1600.0, 60));
        list.add(new ProductInfo(104, "410221", "Carex Daily Moisture Hand Wash · 250ml", "Carex", "Carex", "250 ml", 520.0, 650.0, 100));
        list.add(new ProductInfo(105, "410228", "Carex Antibacterial Hand Gel · 200ml", "Carex", "Carex", "200 ml", 430.0, 540.0, 120));
        list.add(new ProductInfo(106, "500014", "Daily Care Essentials Bundle", "Carex", "Others", "Bundle", 1250.0, 1490.0, 25));
        list.add(new ProductInfo(107, "620011", "Bionnex Prevent Anti-Hair Loss Serum", "Bionnex", "Cosmed", "50 ml", 2850.0, 3200.0, 20));
        list.add(new ProductInfo(108, "620018", "Bionnex Whitening Cream SPF 50", "Bionnex", "Cosmed", "50 ml", 2150.0, 2450.0, 35));
        list.add(new ProductInfo(109, "710042", "Biumark Advanced Acne Spot Treatment", "Biumark", "Cosmed", "30 ml", 1680.0, 1950.0, 40));
        list.add(new ProductInfo(110, "830005", "Wahib Herbal Conditioning Shampoo", "Wahib", "Others", "200 ml", 480.0, 580.0, 80));
        return list;
    }
}
