package com.arif.smartordermanagement.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a Sales Target submission, including product lines,
 * user details, senior edits, and approval metadata.
 */
public class SalesTarget implements Serializable, Cloneable {

    @SerializedName(value = "target_id", alternate = {"targetId", "id"})
    private String targetId;

    @SerializedName(value = "user_id", alternate = {"userId"})
    private int userId;

    @SerializedName(value = "employee_name", alternate = {"employeeName", "userName"})
    private String employeeName;

    @SerializedName(value = "designation", alternate = {"role", "designationName"})
    private String designation;

    @SerializedName(value = "territory", alternate = {"territoryName"})
    private String territory;

    @SerializedName(value = "target_month", alternate = {"targetMonth", "month"})
    private String targetMonth;

    @SerializedName(value = "submission_date", alternate = {"submissionDate", "date"})
    private String submissionDate;

    @SerializedName("status")
    private String status;

    @SerializedName("items")
    private List<ProductInfo> items;

    @SerializedName(value = "total_qty", alternate = {"totalQty", "totalUnits"})
    private int totalQty;

    @SerializedName(value = "total_amount", alternate = {"totalAmount"})
    private double totalAmount;

    @SerializedName(value = "approved_qty", alternate = {"approvedQty", "approvedUnits"})
    private int approvedQty;

    @SerializedName(value = "approved_amount", alternate = {"approvedAmount"})
    private double approvedAmount;

    @SerializedName(value = "submitter_remarks", alternate = {"submitterRemarks", "notes"})
    private String submitterRemarks;

    @SerializedName(value = "senior_remarks", alternate = {"seniorRemarks", "approvalRemarks"})
    private String seniorRemarks;

    @SerializedName(value = "approved_by", alternate = {"approvedBy", "seniorName"})
    private String approvedBy;

    @SerializedName(value = "approval_date", alternate = {"approvalDate"})
    private String approvalDate;

    @SerializedName("is_senior_edited")
    private boolean seniorEdited;

    public SalesTarget() {
        this.items = new ArrayList<>();
        this.status = TargetStatus.PENDING;
        this.submitterRemarks = "";
        this.seniorRemarks = "";
    }

    public SalesTarget(String targetId, int userId, String employeeName, String designation,
                       String territory, String targetMonth, String submissionDate,
                       String status, List<ProductInfo> items, String submitterRemarks) {
        this.targetId = targetId;
        this.userId = userId;
        this.employeeName = employeeName;
        this.designation = designation;
        this.territory = territory;
        this.targetMonth = targetMonth;
        this.submissionDate = submissionDate;
        this.status = status != null ? status : TargetStatus.PENDING;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.submitterRemarks = submitterRemarks != null ? submitterRemarks : "";
        this.seniorRemarks = "";
        recalculateTotals();
    }

    public void recalculateTotals() {
        int sumUnits = 0;
        double sumValue = 0.0;
        int sumApprovedUnits = 0;
        double sumApprovedValue = 0.0;
        boolean hasEdits = false;

        if (items != null) {
            for (ProductInfo product : items) {
                sumUnits += product.getTargetQty();
                sumValue += product.getTargetAmount();

                int appQty = product.getApprovedQty() > 0 ? product.getApprovedQty() : product.getTargetQty();
                sumApprovedUnits += appQty;
                sumApprovedValue += product.getApprovedAmount();

                if (product.getApprovedQty() > 0 && product.getApprovedQty() != product.getTargetQty()) {
                    hasEdits = true;
                }
            }
        }
        this.totalQty = sumUnits;
        this.totalAmount = sumValue;
        this.approvedQty = sumApprovedUnits;
        this.approvedAmount = sumApprovedValue;
        if (hasEdits) {
            this.seniorEdited = true;
        }
    }

    public String getTargetId() { return targetId != null ? targetId : ""; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmployeeName() { return employeeName != null ? employeeName : "Field Representative"; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDesignation() { return designation != null ? designation : "Territory Officer"; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getTerritory() { return territory != null ? territory : "General Territory"; }
    public void setTerritory(String territory) { this.territory = territory; }

    public String getTargetMonth() { return targetMonth != null ? targetMonth : ""; }
    public void setTargetMonth(String targetMonth) { this.targetMonth = targetMonth; }

    public String getSubmissionDate() { return submissionDate != null ? submissionDate : ""; }
    public void setSubmissionDate(String submissionDate) { this.submissionDate = submissionDate; }

    public String getStatus() { return status != null ? status : TargetStatus.PENDING; }
    public void setStatus(String status) { this.status = status; }

    public List<ProductInfo> getItems() {
        if (items == null) items = new ArrayList<>();
        return items;
    }
    public void setItems(List<ProductInfo> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        recalculateTotals();
    }

    public int getTotalQty() { return totalQty; }
    public double getTotalAmount() { return totalAmount; }

    public int getApprovedQty() { return approvedQty > 0 ? approvedQty : totalQty; }
    public double getApprovedAmount() { return approvedAmount > 0 ? approvedAmount : totalAmount; }

    public String getSubmitterRemarks() { return submitterRemarks != null ? submitterRemarks : ""; }
    public void setSubmitterRemarks(String submitterRemarks) { this.submitterRemarks = submitterRemarks; }

    public String getSeniorRemarks() { return seniorRemarks != null ? seniorRemarks : ""; }
    public void setSeniorRemarks(String seniorRemarks) { this.seniorRemarks = seniorRemarks; }

    public String getApprovedBy() { return approvedBy != null ? approvedBy : ""; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getApprovalDate() { return approvalDate != null ? approvalDate : ""; }
    public void setApprovalDate(String approvalDate) { this.approvalDate = approvalDate; }

    public boolean isSeniorEdited() { return seniorEdited; }
    public void setSeniorEdited(boolean seniorEdited) { this.seniorEdited = seniorEdited; }

    @Override
    public SalesTarget clone() {
        try {
            SalesTarget clone = (SalesTarget) super.clone();
            if (this.items != null) {
                List<ProductInfo> clonedItems = new ArrayList<>();
                for (ProductInfo p : this.items) clonedItems.add(p.clone());
                clone.items = clonedItems;
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            List<ProductInfo> clonedItems = new ArrayList<>();
            if (this.items != null) {
                for (ProductInfo p : this.items) clonedItems.add(p.clone());
            }
            SalesTarget clone = new SalesTarget(this.targetId, this.userId, this.employeeName,
                    this.designation, this.territory, this.targetMonth, this.submissionDate,
                    this.status, clonedItems, this.submitterRemarks);
            clone.setSeniorRemarks(this.seniorRemarks);
            clone.setApprovedBy(this.approvedBy);
            clone.setApprovalDate(this.approvalDate);
            clone.setSeniorEdited(this.seniorEdited);
            return clone;
        }
    }
}
