package com.arif.smartordermanagement.ui.operations;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.arif.smartordermanagement.databinding.DialogSalesTargetDetailBinding;
import com.arif.smartordermanagement.databinding.DialogSalesTargetSeniorEditBinding;
import com.arif.smartordermanagement.databinding.ItemSalesTargetCardBinding;
import com.arif.smartordermanagement.databinding.LayoutSalesTargetReportBinding;
import com.arif.smartordermanagement.model.ProductInfo;
import com.arif.smartordermanagement.model.SalesTarget;
import com.arif.smartordermanagement.model.TargetStatus;
import com.arif.smartordermanagement.model.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller managing the Target Status Report view:
 * summary KPIs, status and search filtering, target details, and Senior Edit & Approval.
 */
public final class SalesTargetReportController {

    private final Context context;
    private final LinearLayout container;
    private final LifecycleOwner lifecycleOwner;
    private final SalesTargetViewModel viewModel;
    private final NumberFormat currencyFormat = initCurrencyFormat();

    private static NumberFormat initCurrencyFormat() {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format;
    }

    private LayoutSalesTargetReportBinding binding;
    private List<SalesTarget> currentList = new ArrayList<>();

    public SalesTargetReportController(@NonNull Context context,
                                       @NonNull LinearLayout container,
                                       @NonNull LifecycleOwner lifecycleOwner,
                                       @NonNull SalesTargetViewModel viewModel) {
        this.context = context;
        this.container = container;
        this.lifecycleOwner = lifecycleOwner;
        this.viewModel = viewModel;
    }

    public void render() {
        binding = LayoutSalesTargetReportBinding.inflate(LayoutInflater.from(context), container, false);
        container.addView(binding.getRoot());

        setupSearchAndFilter();
        observeViewModel();

        viewModel.loadTargetStatusReport();
    }

    private void setupSearchAndFilter() {
        binding.chipGroupReportStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                String text = chip.getText().toString();
                if (text.contains("Pending")) {
                    viewModel.setReportStatusFilter(TargetStatus.PENDING);
                } else if (text.contains("Approved")) {
                    viewModel.setReportStatusFilter(TargetStatus.APPROVED);
                } else if (text.contains("Rejected")) {
                    viewModel.setReportStatusFilter(TargetStatus.REJECTED);
                } else {
                    viewModel.setReportStatusFilter("ALL");
                }
            }
        });

        binding.etReportSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setReportSearchQuery(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        viewModel.getFilteredReportList().observe(lifecycleOwner, targets -> {
            if (targets != null) {
                this.currentList = targets;
                updateKpis(targets);
                renderTargetCards(targets);
            }
        });

        viewModel.getSuccessMessage().observe(lifecycleOwner, msg -> {
            if (msg != null && !msg.isEmpty() && binding != null) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getErrorMessage().observe(lifecycleOwner, err -> {
            if (err != null && !err.isEmpty() && binding != null) {
                Snackbar.make(binding.getRoot(), err, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateKpis(List<SalesTarget> targets) {
        double totalVal = 0.0;
        int totalUnits = 0;
        int pending = 0;
        int approved = 0;

        for (SalesTarget t : targets) {
            totalVal += t.getApprovedAmount();
            totalUnits += t.getApprovedQty();
            if (TargetStatus.isPending(t.getStatus())) pending++;
            else if (TargetStatus.isApproved(t.getStatus())) approved++;
        }

        binding.tvKpiTotalValue.setText(formatMoney(totalVal));
        binding.tvKpiTotalUnits.setText(totalUnits + " units");
        binding.tvKpiPendingCount.setText(pending + " Pending");
        binding.tvKpiApprovedCount.setText(approved + " Approved");
    }

    private void renderTargetCards(List<SalesTarget> targets) {
        LinearLayout list = binding.reportTargetsContainer;
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (targets.isEmpty()) {
            binding.tvReportEmptyState.setVisibility(View.VISIBLE);
            return;
        }
        binding.tvReportEmptyState.setVisibility(View.GONE);

        for (SalesTarget target : targets) {
            ItemSalesTargetCardBinding card = ItemSalesTargetCardBinding.inflate(inflater, list, false);

            card.tvTargetId.setText(target.getTargetId());
            card.tvTargetMonth.setText(target.getTargetMonth());
            card.tvEmployeeDetails.setText(target.getEmployeeName() + " · " + target.getTerritory());
            card.tvSubmissionDate.setText(target.getSubmissionDate());

            // Status Styling
            String status = target.getStatus();
            card.tvTargetStatusBadge.setText(status);
            if (TargetStatus.isApproved(status)) {
                card.tvTargetStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.trend_positive_green_bg)));
                card.tvTargetStatusBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.trend_positive_green));
                card.btnReviewEdit.setVisibility(View.GONE);
            } else if (TargetStatus.isPending(status)) {
                card.tvTargetStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.status_pending_amber_bg)));
                card.tvTargetStatusBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.status_pending_amber));
                card.btnReviewEdit.setVisibility(View.VISIBLE);
                card.btnReviewEdit.setText("Senior Review & Edit");
            } else {
                card.tvTargetStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.trend_negative_red_bg)));
                card.tvTargetStatusBadge.setTextColor(
                        ContextCompat.getColor(context, R.color.trend_negative_red));
                card.btnReviewEdit.setVisibility(View.VISIBLE);
                card.btnReviewEdit.setText("Revise & Edit");
            }

            // Senior edited badge
            card.tvSeniorEditedBadge.setVisibility(target.isSeniorEdited() ? View.VISIBLE : View.GONE);

            // Metrics
            card.tvTotalUnits.setText(target.getApprovedQty() + " units");
            card.tvTotalValue.setText(formatMoney(target.getApprovedAmount()));

            card.btnViewDetails.setOnClickListener(v -> showTargetDetailDialog(target));
            card.btnReviewEdit.setOnClickListener(v -> showSeniorEditDialog(target));

            list.addView(card.getRoot());
        }
    }

    /**
     * Shows detail breakdown of a sales target.
     */
    private void showTargetDetailDialog(SalesTarget target) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        DialogSalesTargetDetailBinding d = DialogSalesTargetDetailBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(d.getRoot());

        d.tvDetailTargetId.setText(target.getTargetId());
        d.tvDetailTargetMonth.setText("Target Period: " + target.getTargetMonth());
        d.tvDetailStatusBadge.setText(target.getStatus());

        if (TargetStatus.isApproved(target.getStatus())) {
            d.tvDetailStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.trend_positive_green_bg)));
            d.tvDetailStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.trend_positive_green));
        } else if (TargetStatus.isPending(target.getStatus())) {
            d.tvDetailStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.status_pending_amber_bg)));
            d.tvDetailStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.status_pending_amber));
        } else {
            d.tvDetailStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.trend_negative_red_bg)));
            d.tvDetailStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.trend_negative_red));
        }

        d.tvDetailSubmitterInfo.setText("Submitted by: " + target.getEmployeeName() + " (" + target.getDesignation() + ") · " + target.getSubmissionDate());
        if (target.getApprovedBy() != null && !target.getApprovedBy().isEmpty()) {
            d.tvDetailApprovalInfo.setText("Approved by: " + target.getApprovedBy() + " · " + target.getApprovalDate());
        } else {
            d.tvDetailApprovalInfo.setText("Awaiting senior review");
        }

        // Add Product Breakdown Rows
        LinearLayout list = d.detailProductsContainer;
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        for (ProductInfo p : target.getItems()) {
            View row = inflater.inflate(android.R.layout.simple_list_item_2, list, false);
            TextView text1 = row.findViewById(android.R.id.text1);
            TextView text2 = row.findViewById(android.R.id.text2);

            int finalQty = p.getApprovedQty() > 0 ? p.getApprovedQty() : p.getTargetQty();
            text1.setText(p.getProductName() + " (" + p.getBrandName() + ")");
            text1.setTextSize(14);
            text1.setTextColor(ContextCompat.getColor(context, R.color.on_surface_light));

            String qtyDetail = "Qty: " + finalQty + " units @ TP " + formatMoney(p.getTp())
                    + " = " + formatMoney(p.getApprovedAmount());
            if (p.getApprovedQty() > 0 && p.getApprovedQty() != p.getTargetQty()) {
                qtyDetail += " (Edited from: " + p.getTargetQty() + ")";
            }
            text2.setText(qtyDetail);
            text2.setTextSize(12);
            text2.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant_light));

            list.addView(row);
        }

        d.tvDetailTotalUnits.setText("Total: " + target.getApprovedQty() + " units");
        d.tvDetailTotalAmount.setText("Value: " + formatMoney(target.getApprovedAmount()));

        if (target.getSeniorRemarks() != null && !target.getSeniorRemarks().isEmpty()) {
            d.tvDetailRemarks.setText("Senior Feedback: " + target.getSeniorRemarks());
        } else if (target.getSubmitterRemarks() != null && !target.getSubmitterRemarks().isEmpty()) {
            d.tvDetailRemarks.setText("Submitter Note: " + target.getSubmitterRemarks());
        } else {
            d.tvDetailRemarks.setVisibility(View.GONE);
        }

        d.btnDetailClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Shows dialog for Senior to Review, Edit Target Quantities, and Approve or Reject.
     */
    private void showSeniorEditDialog(SalesTarget target) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        DialogSalesTargetSeniorEditBinding d = DialogSalesTargetSeniorEditBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(d.getRoot());

        viewModel.selectTargetForReview(target);

        d.tvSeniorDialogTargetId.setText(target.getTargetId() + " (" + target.getTargetMonth() + ")");
        d.tvSeniorDialogSubmitter.setText(target.getEmployeeName() + " · " + target.getTerritory());
        if (target.getSubmitterRemarks() != null && !target.getSubmitterRemarks().isEmpty()) {
            d.tvSubmitterNoteDisplay.setText("Submitter Note: " + target.getSubmitterRemarks());
            d.tvSubmitterNoteDisplay.setVisibility(View.VISIBLE);
        } else {
            d.tvSubmitterNoteDisplay.setVisibility(View.GONE);
        }

        // Working copy of products for senior adjustments
        List<ProductInfo> workingItems = new ArrayList<>();
        for (ProductInfo item : target.getItems()) {
            ProductInfo clone = item.clone();
            if (clone.getApprovedQty() <= 0) {
                clone.setApprovedQty(clone.getTargetQty());
            }
            workingItems.add(clone);
        }

        Runnable updateSummary = () -> {
            int units = 0;
            double amount = 0.0;
            for (ProductInfo p : workingItems) {
                int q = p.getApprovedQty();
                units += q;
                amount += (q * p.getEffectiveUnitPrice());
            }
            d.tvSeniorLiveUnits.setText(units + " units");
            d.tvSeniorLiveAmount.setText(formatMoney(amount));
        };
        updateSummary.run();

        // Render editable product items for Senior
        LinearLayout productContainer = d.seniorEditProductListContainer;
        productContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        for (ProductInfo p : workingItems) {
            View itemView = inflater.inflate(R.layout.item_sales_target_product, productContainer, false);

            TextView tvBrand = itemView.findViewById(R.id.tvBrandBadge);
            TextView tvSku = itemView.findViewById(R.id.tvProductSku);
            TextView tvName = itemView.findViewById(R.id.tvProductName);
            TextView tvPrice = itemView.findViewById(R.id.tvProductPrice);
            TextView tvPack = itemView.findViewById(R.id.tvPackSize);
            TextView tvPrev = itemView.findViewById(R.id.tvPreviousQty);
            EditText etQty = itemView.findViewById(R.id.etTargetQty);
            TextView tvSubtotal = itemView.findViewById(R.id.tvItemSubtotal);
            View btnDec = itemView.findViewById(R.id.btnQtyDecrease);
            View btnInc = itemView.findViewById(R.id.btnQtyIncrease);

            tvBrand.setText(p.getBrandName());
            tvSku.setText("SKU: " + p.getProductCode());
            tvPack.setText(p.getPackSize());
            tvName.setText(p.getProductName());
            tvPrice.setText("TP: " + formatMoney(p.getTp()) + " (Submitted: " + p.getTargetQty() + ")");
            tvPrev.setText("Org Qty: " + p.getTargetQty());

            etQty.setText(String.valueOf(p.getApprovedQty()));
            tvSubtotal.setText(formatMoney(p.getApprovedAmount()));

            btnDec.setOnClickListener(v -> {
                int cur = p.getApprovedQty();
                if (cur > 0) {
                    p.setApprovedQty(cur - 1);
                    etQty.setText(String.valueOf(cur - 1));
                    tvSubtotal.setText(formatMoney(p.getApprovedAmount()));
                    viewModel.seniorAdjustProductQty(p.getProductCode(), cur - 1);
                    updateSummary.run();
                }
            });

            btnInc.setOnClickListener(v -> {
                int cur = p.getApprovedQty();
                p.setApprovedQty(cur + 1);
                etQty.setText(String.valueOf(cur + 1));
                tvSubtotal.setText(formatMoney(p.getApprovedAmount()));
                viewModel.seniorAdjustProductQty(p.getProductCode(), cur + 1);
                updateSummary.run();
            });

            etQty.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s == null) return;
                    int val = 0;
                    try {
                        val = Integer.parseInt(s.toString().trim());
                    } catch (NumberFormatException ignored) {}
                    if (val != p.getApprovedQty()) {
                        p.setApprovedQty(val);
                        tvSubtotal.setText(formatMoney(p.getApprovedAmount()));
                        viewModel.seniorAdjustProductQty(p.getProductCode(), val);
                        updateSummary.run();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            productContainer.addView(itemView);
        }

        d.btnSeniorDialogClose.setOnClickListener(v -> dialog.dismiss());

        d.btnSeniorApprove.setOnClickListener(v -> {
            String remarks = d.etSeniorRemarks.getText() != null
                    ? d.etSeniorRemarks.getText().toString().trim()
                    : "";
            User seniorUser = PrefUtils.getInstance(context).getUserInfo(User.class);
            String seniorName = (seniorUser != null && seniorUser.getName() != null)
                    ? seniorUser.getName() + " (Senior Authority)"
                    : "Senior Sales Manager";
            viewModel.seniorApprove(seniorName, remarks.isEmpty() ? "Approved with senior adjustments." : remarks);
            dialog.dismiss();
        });

        d.btnSeniorReject.setOnClickListener(v -> {
            String remarks = d.etSeniorRemarks.getText() != null
                    ? d.etSeniorRemarks.getText().toString().trim()
                    : "";
            if (remarks.isEmpty()) {
                d.etSeniorRemarks.setError("Please provide feedback reason for rejection.");
                return;
            }
            User seniorUser = PrefUtils.getInstance(context).getUserInfo(User.class);
            String seniorName = (seniorUser != null && seniorUser.getName() != null)
                    ? seniorUser.getName()
                    : "Senior Sales Manager";
            viewModel.seniorReject(seniorName, remarks);
            dialog.dismiss();
        });

        dialog.show();
    }

    private String formatMoney(double amount) {
        return "৳" + currencyFormat.format(amount);
    }
}
