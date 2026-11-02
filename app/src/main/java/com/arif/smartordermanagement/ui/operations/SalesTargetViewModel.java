package com.arif.smartordermanagement.ui.operations;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arif.smartordermanagement.data.SalesTargetRepository;
import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.arif.smartordermanagement.model.ProductInfo;
import com.arif.smartordermanagement.model.SalesTarget;
import com.arif.smartordermanagement.model.TargetStatus;
import com.arif.smartordermanagement.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel managing Sales Target drafting, quantity editing, submission,
 * senior adjustments/approval, and status report filtering.
 */
public class SalesTargetViewModel extends AndroidViewModel {

    public static class TargetDraftSummary {
        public final int totalUnits;
        public final double totalAmount;
        public final int activeProductCount;

        public TargetDraftSummary(int totalUnits, double totalAmount, int activeProductCount) {
            this.totalUnits = totalUnits;
            this.totalAmount = totalAmount;
            this.activeProductCount = activeProductCount;
        }
    }

    private final SalesTargetRepository repository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>(null);

    // Draft / Product List State
    private final MutableLiveData<List<ProductInfo>> products = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<TargetDraftSummary> draftSummary = new MutableLiveData<>(new TargetDraftSummary(0, 0.0, 0));
    private String selectedBrandFilter = "All";
    private String productSearchQuery = "";

    // Report State
    private List<SalesTarget> rawReportList = new ArrayList<>();
    private final MutableLiveData<List<SalesTarget>> filteredReportList = new MutableLiveData<>(new ArrayList<>());
    private String selectedStatusFilter = "ALL";
    private String reportSearchQuery = "";

    // Senior review draft
    private final MutableLiveData<SalesTarget> activeReviewTarget = new MutableLiveData<>(null);

    public SalesTargetViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SalesTargetRepository(application);
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<List<ProductInfo>> getProducts() { return products; }
    public LiveData<TargetDraftSummary> getDraftSummary() { return draftSummary; }
    public LiveData<List<SalesTarget>> getFilteredReportList() { return filteredReportList; }
    public LiveData<SalesTarget> getActiveReviewTarget() { return activeReviewTarget; }

    public void clearMessages() {
        errorMessage.setValue(null);
        successMessage.setValue(null);
    }

    /**
     * Loads the product catalog for target setting via @POST(Constants.GET_PRODUCT_LIST).
     */
    public void loadProductCatalog() {
        isLoading.setValue(true);
        clearMessages();
        User user = repository.getCurrentUser();
        repository.getProductData(user, new SalesTargetRepository.RepositoryCallback<>() {
            @Override
            public void onSuccess(List<ProductInfo> result) {
                isLoading.postValue(false);
                List<ProductInfo> fresh = new ArrayList<>();
                if (result != null && !result.isEmpty()) {
                    fresh.addAll(result);
                }
                List<ProductInfo> clonedFresh = new ArrayList<>();
                for (ProductInfo p : fresh) {
                    ProductInfo clone = p.clone();
                    clone.setTargetQty(0);
                    clonedFresh.add(clone);
                }
                products.postValue(clonedFresh);
                recalculateDraftSummary(clonedFresh);
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Updates target quantity for a product in the current draft.
     */
    public void setProductTargetQuantity(String productCode, int qty) {
        List<ProductInfo> current = products.getValue();
        if (current == null) return;
        for (ProductInfo p : current) {
            if (p.getProductCode().equalsIgnoreCase(productCode)) {
                p.setTargetQty(qty);
                break;
            }
        }
        products.setValue(current);
        recalculateDraftSummary(current);
    }

    private void recalculateDraftSummary(List<ProductInfo> list) {
        int units = 0;
        double amount = 0.0;
        int active = 0;
        if (list != null) {
            for (ProductInfo p : list) {
                if (p.getTargetQty() > 0) {
                    units += p.getTargetQty();
                    amount += p.getTargetAmount();
                    active++;
                }
            }
        }
        draftSummary.setValue(new TargetDraftSummary(units, amount, active));
    }

    /**
     * Submits the sales target draft for Senior Approval.
     */
    public void submitSalesTarget(String targetMonth, String remarks) {
        List<ProductInfo> current = products.getValue();
        if (current == null || current.isEmpty()) {
            errorMessage.setValue("Product list is not loaded.");
            return;
        }

        List<ProductInfo> targetItems = new ArrayList<>();
        for (ProductInfo p : current) {
            if (p.getTargetQty() > 0) {
                targetItems.add(p.clone());
            }
        }

        if (targetItems.isEmpty()) {
            errorMessage.setValue("Please enter target quantity for at least one product.");
            return;
        }

        User user = PrefUtils.getInstance(getApplication()).getUserInfo(User.class);
        String employeeName = (user != null && user.getName() != null) ? user.getName() : "Sales Representative";
        String designation = (user != null && user.getRole() != null) ? user.getRole() : "Territory Officer";
        int userId = user != null ? user.getUserId() : 1001;

        String month = (targetMonth != null && !targetMonth.trim().isEmpty())
                ? targetMonth
                : new SimpleDateFormat("MMMM yyyy", Locale.US).format(new Date());

        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date());

        SalesTarget target = new SalesTarget(
                null,
                userId,
                employeeName,
                designation,
                "Dhaka North Territory",
                month,
                dateStr,
                TargetStatus.PENDING,
                targetItems,
                remarks
        );

        isLoading.setValue(true);
        repository.submitSalesTarget(target, new SalesTargetRepository.RepositoryCallback<>() {
            @Override
            public void onSuccess(SalesTarget result) {
                isLoading.postValue(false);
                successMessage.postValue("Sales Target (" + result.getTargetId() + ") submitted successfully for Senior Approval.");
                // Reset draft quantities
                for (ProductInfo p : current) p.setTargetQty(0);
                products.postValue(current);
                recalculateDraftSummary(current);
                loadTargetStatusReport();
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    /**
     * Loads the target status report.
     */
    public void loadTargetStatusReport() {
        isLoading.setValue(true);
        repository.getTargetStatusReport(repository.getCurrentUser(), new SalesTargetRepository.RepositoryCallback<>() {
            @Override
            public void onSuccess(List<SalesTarget> result) {
                isLoading.postValue(false);
                rawReportList = result != null ? result : new ArrayList<>();
                applyReportFilters();
            }

            @Override
            public void onError(String message) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void setReportStatusFilter(String status) {
        this.selectedStatusFilter = status != null ? status : "ALL";
        applyReportFilters();
    }

    public void setReportSearchQuery(String query) {
        this.reportSearchQuery = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        applyReportFilters();
    }

    private void applyReportFilters() {
        List<SalesTarget> filtered = new ArrayList<>();
        for (SalesTarget t : rawReportList) {
            // Status check
            if (!"ALL".equalsIgnoreCase(selectedStatusFilter) && !selectedStatusFilter.equalsIgnoreCase(t.getStatus())) {
                continue;
            }
            // Query check
            if (!reportSearchQuery.isEmpty()) {
                boolean match = t.getTargetId().toLowerCase(Locale.ROOT).contains(reportSearchQuery)
                        || t.getEmployeeName().toLowerCase(Locale.ROOT).contains(reportSearchQuery)
                        || t.getTerritory().toLowerCase(Locale.ROOT).contains(reportSearchQuery)
                        || t.getTargetMonth().toLowerCase(Locale.ROOT).contains(reportSearchQuery);
                if (!match) continue;
            }
            filtered.add(t);
        }
        filteredReportList.setValue(filtered);
    }

    /**
     * Opens a target for senior review and editing.
     */
    public void selectTargetForReview(SalesTarget target) {
        activeReviewTarget.setValue(target != null ? target.clone() : null);
    }

    /**
     * Senior adjusts quantity for a specific product.
     */
    public void seniorAdjustProductQty(String productCode, int adjustedQty) {
        SalesTarget current = activeReviewTarget.getValue();
        if (current == null) return;
        for (ProductInfo p : current.getItems()) {
            if (p.getProductCode().equalsIgnoreCase(productCode)) {
                p.setApprovedQty(adjustedQty);
                break;
            }
        }
        current.recalculateTotals();
        activeReviewTarget.setValue(current);
    }

    /**
     * Senior approves the target with edits and remarks.
     */
    public void seniorApprove(String seniorName, String remarks) {
        SalesTarget target = activeReviewTarget.getValue();
        if (target == null) return;

        isLoading.setValue(true);
        repository.seniorApproveTarget(target.getTargetId(), seniorName, remarks, target.getItems(),
                new SalesTargetRepository.RepositoryCallback<>() {
                    @Override
                    public void onSuccess(SalesTarget result) {
                        isLoading.postValue(false);
                        successMessage.postValue("Target (" + result.getTargetId() + ") has been approved.");
                        activeReviewTarget.postValue(null);
                        loadTargetStatusReport();
                    }

                    @Override
                    public void onError(String message) {
                        isLoading.postValue(false);
                        errorMessage.postValue(message);
                    }
                });
    }

    /**
     * Senior rejects the target with remarks.
     */
    public void seniorReject(String seniorName, String remarks) {
        SalesTarget target = activeReviewTarget.getValue();
        if (target == null) return;

        isLoading.setValue(true);
        repository.seniorRejectTarget(target.getTargetId(), seniorName, remarks,
                new SalesTargetRepository.RepositoryCallback<>() {
                    @Override
                    public void onSuccess(SalesTarget result) {
                        isLoading.postValue(false);
                        successMessage.postValue("Target (" + result.getTargetId() + ") rejected for revision.");
                        activeReviewTarget.postValue(null);
                        loadTargetStatusReport();
                    }

                    @Override
                    public void onError(String message) {
                        isLoading.postValue(false);
                        errorMessage.postValue(message);
                    }
                });
    }
}
