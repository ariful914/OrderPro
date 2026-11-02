package com.arif.smartordermanagement.ui.operations;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.data.DemoOperationsStore;
import com.arif.smartordermanagement.model.ReportFilter;
import com.arif.smartordermanagement.ui.common.ReportHeaderFilterView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


/** Owns sample request, review, delivery, and report UI for the Operations host. */
final class SampleOperationsController {
    interface MessageHandler {
        void show(String message);
    }

    private final Context context;
    private final LinearLayout content;
    private final Runnable refreshCurrentPage;
    private final MessageHandler messageHandler;

    SampleOperationsController(Context context, LinearLayout content,
                               Runnable refreshCurrentPage, MessageHandler messageHandler) {
        this.context = context;
        this.content = content;
        this.refreshCurrentPage = refreshCurrentPage;
        this.messageHandler = messageHandler;
    }

    void renderRequest() {
        addDemoNotice();
        TextInputEditText customer = addField("Customer or outlet",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        TextInputEditText product = addField("Sample product",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        TextInputEditText quantity = addField("Quantity", InputType.TYPE_CLASS_NUMBER);
        TextInputEditText reason = addField("Reason or notes",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        MaterialButton submit = addButton("Submit requisition");
        submit.setOnClickListener(v -> {
            String customerName = valueOf(customer);
            String productName = valueOf(product);
            String note = valueOf(reason);
            int units;
            try {
                units = Integer.parseInt(valueOf(quantity));
            } catch (NumberFormatException e) {
                messageHandler.show("Enter a valid quantity.");
                return;
            }
            if (customerName.isEmpty() || productName.isEmpty() || note.isEmpty() || units <= 0) {
                messageHandler.show("Complete each field and enter a quantity greater than zero.");
                return;
            }
            DemoOperationsStore.addSample(customerName, productName, units, note);
            messageHandler.show("Requisition saved to the demo report.");
            customer.setText("");
            product.setText("");
            quantity.setText("");
            reason.setText("");
        });
    }

    void renderReport() {
        addDemoNotice();
        addReportFilter();
        for (DemoOperationsStore.SampleEntry sample : DemoOperationsStore.getSamples()) {
            addRecordCard(sample.id + " · " + sample.customer,
                    sample.product + " · " + sample.quantity + " units · " + sample.reason,
                    sample.status);
        }
    }

    void renderApprovals() {
        addDemoNotice();
        int pendingCount = 0;
        for (DemoOperationsStore.SampleEntry sample : DemoOperationsStore.getSamples()) {
            if (!"Pending".equals(sample.status)) continue;
            pendingCount++;

            MaterialCardView card = createCard();
            LinearLayout column = new LinearLayout(context);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setPadding(dp(16), dp(14), dp(16), dp(14));
            column.addView(makeText(sample.id + " · " + sample.customer, 15,
                    color(R.color.on_surface_light), true));
            TextView details = makeText(sample.product + " · " + sample.quantity + " units", 14,
                    color(R.color.on_surface_variant_light), false);
            LinearLayout.LayoutParams detailParams = matchWrap();
            detailParams.topMargin = dp(5);
            column.addView(details, detailParams);
            TextView reason = makeText(sample.reason, 13,
                    color(R.color.on_surface_variant_light), false);
            LinearLayout.LayoutParams reasonParams = matchWrap();
            reasonParams.topMargin = dp(3);
            column.addView(reason, reasonParams);

            LinearLayout actions = new LinearLayout(context);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(Gravity.END);
            LinearLayout.LayoutParams actionsParams = matchWrap();
            actionsParams.topMargin = dp(12);
            MaterialButton reject = new MaterialButton(context);
            reject.setText(R.string.reject);
            reject.setAllCaps(false);
            reject.setCornerRadius(dp(22));
            reject.setTextColor(color(R.color.on_surface_variant_light));
            reject.setStrokeColor(ColorStateList.valueOf(color(R.color.outline_light)));
            reject.setStrokeWidth(dp(1));
            reject.setBackgroundTintList(ColorStateList.valueOf(color(R.color.surface_light)));
            MaterialButton approve = new MaterialButton(context);
            approve.setText(R.string.approve);
            approve.setAllCaps(false);
            approve.setCornerRadius(dp(22));
            LinearLayout.LayoutParams approveParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            approveParams.setMarginStart(dp(8));
            actions.addView(reject);
            actions.addView(approve, approveParams);
            column.addView(actions, actionsParams);
            card.addView(column);
            addCard(card);

            reject.setOnClickListener(v -> updateStatus(sample.id, "Rejected"));
            approve.setOnClickListener(v -> updateStatus(sample.id, "Approved"));
        }
        if (pendingCount == 0) {
            addEmptyState(context.getString(R.string.sample_approval_empty));
        }
    }

    void renderDelivery() {
        addDemoNotice();
        int readyCount = 0;
        for (DemoOperationsStore.SampleEntry sample : DemoOperationsStore.getSamples()) {
            if (!"Approved".equals(sample.status)) continue;
            readyCount++;
            addRecordCard(sample.id + " · " + sample.customer,
                    sample.product + " · " + sample.quantity + " units · " + sample.reason,
                    sample.status);
            MaterialButton delivered = addButton(context.getString(R.string.mark_delivered));
            delivered.setOnClickListener(v -> {
                DemoOperationsStore.updateSampleStatus(sample.id, "Delivered");
                refreshCurrentPage.run();
                messageHandler.show(context.getString(R.string.sample_delivered_message));
            });
        }
        if (readyCount == 0) {
            addEmptyState(context.getString(R.string.no_samples_ready));
        }
    }

    private void updateStatus(String sampleId, String status) {
        DemoOperationsStore.updateSampleStatus(sampleId, status);
        refreshCurrentPage.run();
        messageHandler.show("Approved".equals(status)
                ? context.getString(R.string.sample_approved_message)
                : context.getString(R.string.sample_rejected_message));
    }

    private void addDemoNotice() {
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

    private void addReportFilter() {
        ReportHeaderFilterView filterView = new ReportHeaderFilterView(context);
        filterView.setOnSearchClickListener(filter -> new MaterialAlertDialogBuilder(context)
                .setTitle("Confirm report filters")
                .setMessage(filterSummary(filter))
                .setNegativeButton("Edit", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Search", (dialog, which) ->
                        messageHandler.show("Search Filter applied: " + filter.getDisplayText()))
                .show());
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(16);
        content.addView(filterView, params);
    }

    private String filterSummary(ReportFilter filter) {
        String region = filter.getRegion() != null ? filter.getRegion().getName() : "All Regions";
        String area = filter.getArea() != null ? filter.getArea().getName() : "All Areas";
        String territory = filter.getTerritory() != null
                ? filter.getTerritory().getName() : "All Territories";
        return "Date: " + filter.getFromDate() + " – " + filter.getToDate()
                + "\nRegion: " + region
                + "\nArea: " + area
                + "\nTerritory: " + territory
                + "\n\nSearch with these filters?";
    }

    private TextInputEditText addField(String hint, int inputType) {
        TextInputLayout layout = new TextInputLayout(context);
        layout.setHint(hint);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        float radius = dp(12);
        layout.setBoxCornerRadii(radius, radius, radius, radius);
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setInputType(inputType);
        input.setTextSize(16);
        input.setSingleLine(true);
        layout.addView(input);
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(14);
        content.addView(layout, params);
        return input;
    }

    private MaterialButton addButton(String text) {
        MaterialButton button = new MaterialButton(context);
        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setCornerRadius(dp(26));
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(8);
        params.bottomMargin = dp(8);
        content.addView(button, params);
        return button;
    }

    private void addRecordCard(String title, String detail, String status) {
        MaterialCardView card = createCard();
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout top = new LinearLayout(context);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(makeText(title, 15, color(R.color.on_surface_light), true),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView statusView = makeText(status, 12, statusColor(status), true);
        statusView.setPadding(dp(8), dp(4), dp(8), dp(4));
        top.addView(statusView);
        TextView detailView = makeText(detail, 13, color(R.color.on_surface_variant_light), false);
        LinearLayout.LayoutParams detailParams = matchWrap();
        detailParams.topMargin = dp(5);
        column.addView(top);
        column.addView(detailView, detailParams);
        card.addView(column);
        addCard(card);
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

    private void addEmptyState(String message) {
        TextView empty = makeText(message, 14, color(R.color.on_surface_variant_light), false);
        empty.setPadding(dp(12), dp(16), dp(12), dp(16));
        content.addView(empty, matchWrap());
    }

    private String valueOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
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
        if ("Approved".equals(status) || "Delivered".equals(status)) return color(R.color.trend_positive_green);
        if ("Pending".equals(status)) return color(R.color.status_pending_amber);
        if ("Rejected".equals(status)) return color(R.color.trend_negative_red);
        return color(R.color.on_surface_variant_light);
    }

    private int color(int resourceId) {
        return context.getColor(resourceId);
    }

    private int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
