package com.arif.smartordermanagement.ui.operations;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arif.smartordermanagement.data.SalesInvoiceRepository;
import com.arif.smartordermanagement.model.ReportFilter;
import com.arif.smartordermanagement.model.SalesInvoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import retrofit2.Call;

/** Holds daily invoice report data and derives the selected status summary. */
public final class DailyInvoiceReportViewModel extends AndroidViewModel {
    private final SalesInvoiceRepository repository;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.initial());
    private List<SalesInvoice> invoices = Collections.emptyList();
    private String selectedStatus = "all";
    private long requestGeneration;
    private Call<?> activeCall;

    public DailyInvoiceReportViewModel(@NonNull Application application) {
        super(application);
        repository = new SalesInvoiceRepository(application);
    }

    public LiveData<UiState> getState() {
        return state;
    }

    public void setStatusFilter(String status) {
        selectedStatus = status == null ? "all" : status.toLowerCase(Locale.ROOT);
        UiState current = state.getValue();
        state.setValue(UiState.create(invoices, selectedStatus, current != null && current.hasSearched,
                current != null && current.isLoading, current == null ? null : current.error));
    }

    public void load(ReportFilter filter) {
        if (activeCall != null) activeCall.cancel();
        long generation = ++requestGeneration;
        state.setValue(UiState.create(invoices, selectedStatus, true, true, null));
        activeCall = repository.load(filter, new SalesInvoiceRepository.Callback() {
            @Override
            public void onLoaded(List<SalesInvoice> result) {
                if (generation != requestGeneration) return;
                invoices = Collections.unmodifiableList(new ArrayList<>(result));
                activeCall = null;
                state.postValue(UiState.create(invoices, selectedStatus, true, false, null));
            }

            @Override
            public void onError(String message) {
                if (generation != requestGeneration) return;
                activeCall = null;
                state.postValue(UiState.create(invoices, selectedStatus, true, false, message));
            }
        });
    }

    @Override
    protected void onCleared() {
        requestGeneration++;
        if (activeCall != null) activeCall.cancel();
        activeCall = null;
        super.onCleared();
    }

    public static final class UiState {
        public final List<SalesInvoice> invoices;
        public final String selectedStatus;
        public final boolean isLoading;
        public final boolean hasSearched;
        public final String error;
        public final BigDecimal sales;
        public final BigDecimal discount;
        public final BigDecimal additionalDiscount;
        public final Map<String, BigDecimal> brandTotals;
        public final int acceptedCount;
        public final int pendingCount;
        public final int returnCount;

        private UiState(List<SalesInvoice> invoices, String selectedStatus, boolean hasSearched,
                        boolean isLoading, String error, BigDecimal sales, BigDecimal discount,
                        BigDecimal additionalDiscount, Map<String, BigDecimal> brandTotals,
                        int acceptedCount,
                        int pendingCount, int returnCount) {
            this.invoices = invoices;
            this.selectedStatus = selectedStatus;
            this.isLoading = isLoading;
            this.hasSearched = hasSearched;
            this.error = error;
            this.sales = sales;
            this.discount = discount;
            this.additionalDiscount = additionalDiscount;
            this.brandTotals = brandTotals;
            this.acceptedCount = acceptedCount;
            this.pendingCount = pendingCount;
            this.returnCount = returnCount;
        }

        private static UiState initial() {
            return create(Collections.emptyList(), "all", false, false, null);
        }

        private static UiState create(List<SalesInvoice> invoices, String selectedStatus,
                                      boolean hasSearched, boolean isLoading, String error) {
            BigDecimal sales = BigDecimal.ZERO;
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal additionalDiscount = BigDecimal.ZERO;
            Map<String, BigDecimal> brandTotals = new LinkedHashMap<>();
            brandTotals.put("Avene", BigDecimal.ZERO);
            brandTotals.put("Biumark", BigDecimal.ZERO);
            brandTotals.put("Bionnex", BigDecimal.ZERO);
            brandTotals.put("Cosmed", BigDecimal.ZERO);
            brandTotals.put("Wahib", BigDecimal.ZERO);
            int accepted = 0, pending = 0, returned = 0;

            for (SalesInvoice invoice : invoices) {
                String status = invoice.getStatus().toLowerCase(Locale.ROOT);
                if ("accepted".equals(status)) accepted++;
                else if (status.contains("pending")) pending++;
                else if (status.contains("return")) returned++;

                if (!matches(selectedStatus, status)) continue;
                sales = sales.add(amount(invoice.getInvoiceValue()));
                discount = discount.add(amount(invoice.getDiscountValue()));
                additionalDiscount = additionalDiscount.add(amount(invoice.getAdditionalDiscount()));
                addBrand(brandTotals, "Avene", invoice.getAveneAmount());
                addBrand(brandTotals, "Biumark", invoice.getBiumarkAmount());
                addBrand(brandTotals, "Bionnex", invoice.getBionnexAmount());
                addBrand(brandTotals, "Cosmed", invoice.getCosmedAmount());
                addBrand(brandTotals, "Wahib", invoice.getWahibAmount());
            }

            return new UiState(invoices, selectedStatus, hasSearched, isLoading, error,
                    sales, discount, additionalDiscount,
                    Collections.unmodifiableMap(brandTotals), accepted, pending, returned);
        }

        private static void addBrand(Map<String, BigDecimal> totals, String brand, String value) {
            BigDecimal total = totals.get(brand);
            if (total == null) total = BigDecimal.ZERO;
            totals.put(brand, total.add(amount(value)));
        }

        private static boolean matches(String filter, String status) {
            return "all".equals(filter)
                    || ("accepted".equals(filter) && "accepted".equals(status))
                    || ("pending".equals(filter) && status.contains("pending"))
                    || ("return".equals(filter) && status.contains("return"));
        }

        private static BigDecimal amount(String value) {
            try {
                return new BigDecimal(value.replace("৳", "").replace(",", "").trim());
            } catch (NumberFormatException | NullPointerException ignored) {
                return BigDecimal.ZERO;
            }
        }
    }
}
