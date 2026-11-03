package com.arif.smartordermanagement.ui.operations;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.arif.smartordermanagement.FilterSharedViewModel;
import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.model.ReportFilter;
import com.arif.smartordermanagement.ui.common.ReportHeaderFilterView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.NumberFormat;
import java.util.Locale;

/** Shared view builders for the programmatic Operations screens. */
final class OperationsUiComponents {
    interface NavigationHandler {
        void navigate(String destination);
    }

    interface MessageHandler {
        void show(String message);
    }

    private final Context context;
    private final LinearLayout content;
    private final NavigationHandler navigationHandler;
    private final MessageHandler messageHandler;
    private final FilterSharedViewModel filterSharedViewModel;

    OperationsUiComponents(Context context, LinearLayout content,
                           NavigationHandler navigationHandler, MessageHandler messageHandler,
                           FilterSharedViewModel filterSharedViewModel) {
        this.context = context;
        this.content = content;
        this.navigationHandler = navigationHandler;
        this.messageHandler = messageHandler;
        this.filterSharedViewModel = filterSharedViewModel;
    }

    void addDemoNotice() {
        TextView notice = makeText(context.getString(R.string.demo_data_notice), 12,
                color(R.color.on_google_blue_container), true);
        notice.setPadding(dp(12), dp(10), dp(12), dp(10));
        GradientDrawable background = new GradientDrawable();
        background.setColor(color(R.color.google_blue_container));
        background.setCornerRadius(dp(12));
        notice.setBackground(background);
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(18);
        content.addView(notice, params);
    }

    void addSectionTitle(String title) {
        TextView heading = makeText(title, 18, color(R.color.on_surface_light), true);
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(8);
        params.bottomMargin = dp(10);
        content.addView(heading, params);
    }

    void addBodyText(String text) {
        TextView body = makeText(text, 13, color(R.color.on_surface_variant_light), false);
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(12);
        content.addView(body, params);
    }

    void addActionCard(String title, String description, String destination) {
        MaterialCardView card = createCard();
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        TextView titleView = makeText(title, 16, color(R.color.on_surface_light), true);
        TextView descriptionView = makeText(description, 13, color(R.color.on_surface_variant_light), false);
        LinearLayout.LayoutParams descParams = matchWrap();
        descParams.topMargin = dp(4);
        textColumn.addView(titleView);
        textColumn.addView(descriptionView, descParams);
        row.addView(textColumn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        row.addView(makeText("›", 28, color(R.color.google_blue), false));
        card.addView(row);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> navigationHandler.navigate(destination));
        addCard(card);
    }

    void addRecordCard(String title, String detail, @Nullable String amount, @Nullable String status) {
        MaterialCardView card = createCard();
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout top = new LinearLayout(context);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView titleView = makeText(title, 15, color(R.color.on_surface_light), true);
        top.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (status != null) {
            TextView statusView = makeText(status, 12, statusColor(status), true);
            statusView.setPadding(dp(8), dp(4), dp(8), dp(4));
            top.addView(statusView);
        } else if (amount != null) {
            top.addView(makeText(amount, 15, color(R.color.google_blue), true));
        }

        TextView detailView = makeText(detail, 13, color(R.color.on_surface_variant_light), false);
        LinearLayout.LayoutParams detailParams = matchWrap();
        detailParams.topMargin = dp(5);
        column.addView(top);
        column.addView(detailView, detailParams);
        card.addView(column);
        addCard(card);
    }

    void addMetricCard(String label, String value, String detail) {
        MaterialCardView card = createCard();
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(18), dp(18), dp(18), dp(18));
        column.addView(makeText(label, 13, color(R.color.on_surface_variant_light), false));
        TextView total = makeText(value, 26, color(R.color.google_blue), true);
        LinearLayout.LayoutParams totalParams = matchWrap();
        totalParams.topMargin = dp(4);
        column.addView(total, totalParams);
        TextView caption = makeText(detail, 13, color(R.color.on_surface_variant_light), false);
        LinearLayout.LayoutParams captionParams = matchWrap();
        captionParams.topMargin = dp(3);
        column.addView(caption, captionParams);
        card.addView(column);
        addCard(card);
    }

    void addComparisonRow(String label, float progress, String value) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(makeText(label, 14, color(R.color.on_surface_light), true),
                new LinearLayout.LayoutParams(dp(90), ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearProgressIndicator bar = new LinearProgressIndicator(context);
        bar.setMax(100);
        bar.setProgressCompat((int) (progress * 100), false);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, dp(8), 1f);
        barParams.setMargins(dp(8), 0, dp(8), 0);
        row.addView(bar, barParams);
        row.addView(makeText(value, 13, color(R.color.on_surface_variant_light), false));
        LinearLayout.LayoutParams rowParams = matchWrap();
        rowParams.bottomMargin = dp(12);
        content.addView(row, rowParams);
    }

    void addReportHeaderFilter(
            ReportHeaderFilterView.OnSearchClickListener onSearch) {
        ReportHeaderFilterView filterView = new ReportHeaderFilterView(context);
        filterView.setSharedViewModel(filterSharedViewModel);
        filterView.setOnSearchClickListener(filter ->
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Confirm report filters")
                        .setMessage(filterConfirmationText(filter))
                        .setNegativeButton("Edit", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Search", (dialog, which) -> {
                            if (onSearch != null) onSearch.onSearch(filter);
                            messageHandler.show("Search Filter applied: " + filter.getDisplayText());
                        })
                        .show());
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(16);
        content.addView(filterView, params);
    }

    String money(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "৳" + format.format(value);
    }

    int color(int resourceId) {
        return context.getColor(resourceId);
    }

    int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private String filterConfirmationText(ReportFilter filter) {
        String zone = filter.getZone() != null ? filter.getZone().getName() : "All Zones";
        String region = filter.getRegion() != null ? filter.getRegion().getName() : "All Regions";
        String area = filter.getArea() != null ? filter.getArea().getName() : "All Areas";
        String territory = filter.getTerritory() != null
                ? filter.getTerritory().getName() : "All Territories";
        return "Date: " + filter.getFromDate() + " – " + filter.getToDate()
                + "\nZone: " + zone
                + "\nRegion: " + region
                + "\nArea: " + area
                + "\nTerritory: " + territory
                + "\n\nSearch with these filters?";
    }

    private MaterialCardView createCard() {
        MaterialCardView card = new MaterialCardView(context);
        card.setCardBackgroundColor(color(R.color.surface_light));
        card.setStrokeColor(color(R.color.outline_light));
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(16));
        card.setCardElevation(dp(1));
        return card;
    }

    private void addCard(MaterialCardView card) {
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(12);
        content.addView(card, params);
    }

    private TextView makeText(String text, int sizeSp, int textColor, boolean bold) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(textColor);
        if (bold) view.setTypeface(view.getTypeface(), Typeface.BOLD);
        return view;
    }

    private int statusColor(String status) {
        if ("Approved".equals(status) || "Completed".equals(status)) return color(R.color.trend_positive_green);
        if ("Pending".equals(status) || "Under review".equals(status)) return color(R.color.status_pending_amber);
        if ("Rejected".equals(status)) return color(R.color.trend_negative_red);
        return color(R.color.on_surface_variant_light);
    }
}
