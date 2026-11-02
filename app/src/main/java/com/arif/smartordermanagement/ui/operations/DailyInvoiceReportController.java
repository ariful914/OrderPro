package com.arif.smartordermanagement.ui.operations;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import com.arif.smartordermanagement.ui.common.ReportHeaderFilterView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Renders the daily invoice report and forwards user actions to its ViewModel. */
final class DailyInvoiceReportController {
    private final Context context;
    private final LinearLayout content;
    private final LifecycleOwner lifecycleOwner;
    private final DailyInvoiceReportViewModel viewModel;
    private final TextView[] metricValues = new TextView[3];
    private final MaterialButton[] filterButtons = new MaterialButton[4];
    private final Observer<DailyInvoiceReportViewModel.UiState> stateObserver = this::renderState;

    private SalesInvoiceAdapter adapter;
    private RecyclerView invoiceList;
    private TextView emptyState;
    private MaterialButtonToggleGroup statusFilters;
    private String lastShownError;

    DailyInvoiceReportController(@NonNull Context context, @NonNull LinearLayout content,
                                 @NonNull LifecycleOwner lifecycleOwner,
                                 @NonNull DailyInvoiceReportViewModel viewModel) {
        this.context = context;
        this.content = content;
        this.lifecycleOwner = lifecycleOwner;
        this.viewModel = viewModel;
    }

    void render() {
        addFilter();
        addSummary();
        addStatusFilters();
        addInvoiceList();
        viewModel.getState().observe(lifecycleOwner, stateObserver);
    }

    void clear() {
        viewModel.getState().removeObserver(stateObserver);
        adapter = null;
        invoiceList = null;
        emptyState = null;
        statusFilters = null;
    }

    private void addFilter() {
        ReportHeaderFilterView filterView = new ReportHeaderFilterView(context);
        filterView.setOnSearchClickListener(filter -> new MaterialAlertDialogBuilder(context)
                .setTitle("Confirm report filters")
                .setMessage(filterConfirmation(filter))
                .setNegativeButton("Edit", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Search", (dialog, which) -> {
                    viewModel.load(filter);
                    message("Search Filter applied: " + filter.getDisplayText());
                })
                .show());
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(16);
        content.addView(filterView, params);
    }

    private void addSummary() {
        addSectionTitle("Sales overview");
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels = {"TOTAL SALES", "DISCOUNT", "TOTAL DUE"};
        int[] backgrounds = {
                Color.rgb(224, 233, 255),
                Color.rgb(237, 239, 249),
                Color.rgb(255, 230, 229)
        };
        int[] foregrounds = {
                Color.rgb(39, 61, 112),
                Color.rgb(72, 78, 98),
                Color.rgb(151, 40, 40)
        };

        for (int i = 0; i < labels.length; i++) {
            MaterialCardView card = new MaterialCardView(context);
            card.setRadius(dp(16));
            card.setCardElevation(0);
            card.setCardBackgroundColor(backgrounds[i]);

            LinearLayout details = new LinearLayout(context);
            details.setOrientation(LinearLayout.VERTICAL);
            details.setPadding(dp(12), dp(14), dp(8), dp(14));
            details.addView(text(labels[i], 10, foregrounds[i], true), matchWrap());

            TextView amount = text("৳0", 18, foregrounds[i], true);
            amount.setMaxLines(1);
            amount.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            amount.setAutoSizeTextTypeUniformWithConfiguration(
                    11, 18, 1, TypedValue.COMPLEX_UNIT_SP);
            details.addView(amount, matchWrap());
            card.addView(details);

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            cardParams.setMargins(dp(3), dp(4), dp(3), dp(4));
            row.addView(card, cardParams);
            metricValues[i] = amount;
        }
        content.addView(row, matchWrap());
    }

    private void addStatusFilters() {
        statusFilters = new MaterialButtonToggleGroup(context);
        statusFilters.setSingleSelection(true);
        statusFilters.setSelectionRequired(true);
        statusFilters.setOrientation(LinearLayout.HORIZONTAL);

        String[] labels = {"All", "Accepted", "Pending", "Return"};
        String[] values = {"all", "accepted", "pending", "return"};
        for (int i = 0; i < labels.length; i++) {
            MaterialButton button = new MaterialButton(context, null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);
            button.setId(View.generateViewId());
            button.setText(labels[i]);
            button.setAllCaps(false);
            button.setTag(values[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
            params.setMargins(dp(2), dp(4), dp(2), dp(4));
            statusFilters.addView(button, params);
            filterButtons[i] = button;
        }

        content.addView(statusFilters, matchWrap());
        statusFilters.check(filterButtons[0].getId());
        statusFilters.addOnButtonCheckedListener((group, checkedId, checked) -> {
            if (!checked) return;
            View button = group.findViewById(checkedId);
            viewModel.setStatusFilter(String.valueOf(button.getTag()));
        });
    }

    private void addInvoiceList() {
        addSectionTitle("Invoices by employee");
        emptyState = text("Choose your filters and tap Search to load invoices.",
                15, color(R.color.on_surface_variant_light), false);
        emptyState.setGravity(Gravity.CENTER);
        emptyState.setPadding(dp(20), dp(22), dp(20), dp(22));
        emptyState.setBackgroundColor(color(R.color.surface_light));
        content.addView(emptyState, matchWrap());

        invoiceList = new RecyclerView(context);
        invoiceList.setLayoutManager(new LinearLayoutManager(context));
        invoiceList.setNestedScrollingEnabled(false);
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(220);
        animator.setRemoveDuration(180);
        animator.setMoveDuration(180);
        invoiceList.setItemAnimator(animator);
        invoiceList.setVisibility(View.GONE);
        adapter = new SalesInvoiceAdapter();
        invoiceList.setAdapter(adapter);

        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(8);
        content.addView(invoiceList, params);
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

        adapter.setInvoices(state.invoices);
        adapter.setStatusFilter(state.selectedStatus);
        metricValues[0].setText(money(state.sales));
        metricValues[1].setText(money(state.discount));
        metricValues[2].setText(money(state.due));
        filterButtons[0].setText("All  " + state.invoices.size());
        filterButtons[1].setText("Accepted  " + state.acceptedCount);
        filterButtons[2].setText("Pending  " + state.pendingCount);
        filterButtons[3].setText("Return  " + state.returnCount);

        if (state.isLoading) {
            invoiceList.setVisibility(View.GONE);
            emptyState.setText("Loading invoices…");
            emptyState.setVisibility(View.VISIBLE);
        } else if (state.error != null) {
            invoiceList.setVisibility(View.GONE);
            emptyState.setText(state.error);
            emptyState.setVisibility(View.VISIBLE);
            if (!state.error.equals(lastShownError)) message(state.error);
            lastShownError = state.error;
        } else if (state.hasSearched) {
            updateListVisibility();
        }
    }

    private void updateListVisibility() {
        boolean hasRows = adapter.getItemCount() > 0;
        invoiceList.setVisibility(hasRows ? View.VISIBLE : View.GONE);
        emptyState.setText(hasRows ? "" : "No invoices match this status.");
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

    private void addSectionTitle(String title) {
        TextView heading = text(title, 18, color(R.color.on_surface_light), true);
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(16);
        params.bottomMargin = dp(8);
        content.addView(heading, params);
    }

    private TextView text(String value, int size, int textColor, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(textColor);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private int color(int resource) {
        return context.getColor(resource);
    }

    private int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void message(String text) {
        Snackbar.make(content, text, Snackbar.LENGTH_LONG).show();
    }
}
