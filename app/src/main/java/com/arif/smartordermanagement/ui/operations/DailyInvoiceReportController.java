package com.arif.smartordermanagement.ui.operations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.model.ReportFilter;
import com.arif.smartordermanagement.model.SalesInvoice;
import com.arif.smartordermanagement.ui.common.ReportHeaderFilterView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Binds the daily invoice report layout and forwards user actions to its ViewModel. */
final class DailyInvoiceReportController {
    private final Context context;
    private final LinearLayout content;
    private final LifecycleOwner lifecycleOwner;
    private final DailyInvoiceReportViewModel viewModel;
    private final TextView[] metricValues = new TextView[3];
    private final TextView[] brandValues = new TextView[5];
    private final MaterialButton[] filterButtons = new MaterialButton[4];
    private final Observer<DailyInvoiceReportViewModel.UiState> stateObserver = this::renderState;

    private SalesInvoiceAdapter adapter;
    private RecyclerView invoiceList;
    private TextView emptyState;
    private MaterialButtonToggleGroup statusFilters;
    private View filterOptionsContent;
    private View filterOptionsHeader;
    private TextView filterOptionsIcon;
    private String lastShownError;
    private List<SalesInvoice> renderedInvoices;
    private String renderedStatus;

    DailyInvoiceReportController(@NonNull Context context, @NonNull LinearLayout content,
                                 @NonNull LifecycleOwner lifecycleOwner,
                                 @NonNull DailyInvoiceReportViewModel viewModel) {
        this.context = context;
        this.content = content;
        this.lifecycleOwner = lifecycleOwner;
        this.viewModel = viewModel;
    }

    void render() {
        View report = LayoutInflater.from(context)
                .inflate(R.layout.fragment_daily_invoice_report, content, false);
        content.addView(report);

        ReportHeaderFilterView filterView = report.findViewById(R.id.reportHeaderFilter);
        filterView.setOnSearchClickListener(this::confirmSearch);
        filterOptionsContent = filterView;
        filterOptionsHeader = report.findViewById(R.id.filterOptionsHeader);
        filterOptionsIcon = report.findViewById(R.id.filterOptionsIcon);
        filterOptionsHeader.setOnClickListener(view -> setFilterExpanded(
                filterOptionsContent.getVisibility() != View.VISIBLE));

        metricValues[0] = report.findViewById(R.id.totalSalesValue);
        metricValues[1] = report.findViewById(R.id.discountValue);
        metricValues[2] = report.findViewById(R.id.additionalDiscountValue);
        brandValues[0] = report.findViewById(R.id.brandAveneValue);
        brandValues[1] = report.findViewById(R.id.brandBiumarkValue);
        brandValues[2] = report.findViewById(R.id.brandBionnexValue);
        brandValues[3] = report.findViewById(R.id.brandCosmedValue);
        brandValues[4] = report.findViewById(R.id.brandWahibValue);

        statusFilters = report.findViewById(R.id.statusFilters);
        filterButtons[0] = report.findViewById(R.id.filterAll);
        filterButtons[1] = report.findViewById(R.id.filterAccepted);
        filterButtons[2] = report.findViewById(R.id.filterPending);
        filterButtons[3] = report.findViewById(R.id.filterReturned);
        for (MaterialButton button : filterButtons) {
            button.setContentDescription(String.valueOf(button.getText()));
        }
        statusFilters.check(filterButtons[0].getId());
        statusFilters.addOnButtonCheckedListener((group, checkedId, checked) -> {
            if (!checked) return;
            View button = group.findViewById(checkedId);
            viewModel.setStatusFilter(String.valueOf(button.getTag()));
        });

        emptyState = report.findViewById(R.id.emptyState);
        invoiceList = report.findViewById(R.id.invoiceList);
        invoiceList.setLayoutManager(new LinearLayoutManager(context));
        invoiceList.setNestedScrollingEnabled(false);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(220);
        animator.setRemoveDuration(180);
        animator.setMoveDuration(180);
        animator.setChangeDuration(0);
        invoiceList.setItemAnimator(animator);
        adapter = new SalesInvoiceAdapter();
        invoiceList.setAdapter(adapter);

        viewModel.getState().observe(lifecycleOwner, stateObserver);
    }

    void clear() {
        viewModel.getState().removeObserver(stateObserver);
        adapter = null;
        invoiceList = null;
        emptyState = null;
        statusFilters = null;
        filterOptionsContent = null;
        filterOptionsHeader = null;
        filterOptionsIcon = null;
    }

    private void confirmSearch(ReportFilter filter) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Confirm report filters")
                .setMessage(filterConfirmation(filter))
                .setNegativeButton("Edit", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Search", (dialog, which) -> {
                    viewModel.load(filter);
                    message("Search Filter applied: " + filter.getDisplayText());
                })
                .show();
    }

    private void renderState(DailyInvoiceReportViewModel.UiState state) {
        if (statusFilters != null) {
            for (MaterialButton button : filterButtons) {
                if (button.getTag().equals(state.selectedStatus)
                        && statusFilters.getCheckedButtonId() != button.getId()) {
                    statusFilters.check(button.getId());
                    break;
                }
            }
        }

        boolean invoicesUpdated = renderedInvoices != state.invoices;
        if (invoicesUpdated) {
            adapter.setInvoices(state.invoices);
            renderedInvoices = state.invoices;
        }
        if (!state.selectedStatus.equals(renderedStatus)) {
            adapter.setStatusFilter(state.selectedStatus);
            renderedStatus = state.selectedStatus;
        }

        metricValues[0].setText(money(state.sales));
        metricValues[1].setText(money(state.discount));
        metricValues[2].setText(money(state.additionalDiscount));
        String[] brandNames = {"Avene", "Biumark", "Bionnex", "Cosmed", "Wahib"};
        for (int i = 0; i < brandValues.length; i++) {
            brandValues[i].setText(money(state.brandTotals.get(brandNames[i])));
        }
        String[] labels = {
                context.getString(R.string.daily_invoice_label_all),
                context.getString(R.string.daily_invoice_label_accepted),
                context.getString(R.string.daily_invoice_label_pending),
                context.getString(R.string.daily_invoice_label_returned)
        };
        int[] counts = {state.invoices.size(), state.acceptedCount,
                state.pendingCount, state.returnCount};
        for (int i = 0; i < filterButtons.length; i++) {
            filterButtons[i].setText(context.getString(
                    R.string.daily_invoice_status_count, labels[i], counts[i]));
            filterButtons[i].setContentDescription(labels[i] + ", " + counts[i] + " invoices");
        }

        if (state.isLoading) {
            invoiceList.setVisibility(View.GONE);
            emptyState.setText(R.string.daily_invoice_loading);
            emptyState.setVisibility(View.VISIBLE);
        } else if (state.error != null) {
            invoiceList.setVisibility(View.GONE);
            emptyState.setText(state.error);
            emptyState.setVisibility(View.VISIBLE);
            if (!state.error.equals(lastShownError)) message(state.error);
            lastShownError = state.error;
        } else if (state.hasSearched) {
            updateListVisibility();
            if (invoicesUpdated) setFilterExpanded(false);
        }
    }

    private void setFilterExpanded(boolean expanded) {
        if (filterOptionsContent == null) return;
        filterOptionsContent.setVisibility(expanded ? View.VISIBLE : View.GONE);
        if (filterOptionsIcon != null) {
            filterOptionsIcon.animate().cancel();
            filterOptionsIcon.animate().rotation(expanded ? 180f : 0f).setDuration(160).start();
        }
        if (filterOptionsHeader != null) {
            filterOptionsHeader.setContentDescription(context.getString(
                    expanded ? R.string.daily_invoice_filters_expanded
                            : R.string.daily_invoice_filters_collapsed));
        }
    }

    private void updateListVisibility() {
        boolean hasRows = adapter.getItemCount() > 0;
        invoiceList.setVisibility(hasRows ? View.VISIBLE : View.GONE);
        emptyState.setText(hasRows ? "" : context.getString(R.string.daily_invoice_no_matches));
        emptyState.setVisibility(hasRows ? View.GONE : View.VISIBLE);
    }

    private String filterConfirmation(ReportFilter filter) {
        String region = filter.getRegion() != null ? filter.getRegion().getName() : "All Regions";
        String area = filter.getArea() != null ? filter.getArea().getName() : "All Areas";
        String territory = filter.getTerritory() != null
                ? filter.getTerritory().getName() : "All Territories";
        return "Date: " + filter.getFromDate() + " – " + filter.getToDate()
                + "\nRegion: " + region + "\nArea: " + area + "\nTerritory: " + territory
                + "\n\nSearch with these filters?";
    }

    private String money(BigDecimal amount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return "৳" + format.format(amount);
    }

    private void message(String text) {
        Snackbar.make(content, text, Snackbar.LENGTH_LONG).show();
    }
}
