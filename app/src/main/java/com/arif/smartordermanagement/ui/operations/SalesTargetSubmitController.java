package com.arif.smartordermanagement.ui.operations;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.databinding.DialogSalesTargetReviewBinding;
import com.arif.smartordermanagement.databinding.FragmentOperationsBinding;
import com.arif.smartordermanagement.databinding.ItemOrderReviewProductBinding;
import com.arif.smartordermanagement.databinding.ItemSalesTargetProductBinding;
import com.arif.smartordermanagement.databinding.LayoutSalesTargetSubmitBinding;
import com.arif.smartordermanagement.model.ProductInfo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Controller managing the UI interactions for Sales Target creation,
 * product list display via @POST(Constants.GET_PRODUCT_LIST), quantity editing, review, and submission.
 */
public final class SalesTargetSubmitController {

    private final Context context;
    private final LayoutSalesTargetSubmitBinding binding;
    private final FragmentOperationsBinding host;
    private final LifecycleOwner lifecycleOwner;
    private final SalesTargetViewModel viewModel;
    private final NumberFormat currencyFormat = initCurrencyFormat();

    private static NumberFormat initCurrencyFormat() {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format;
    }

    private String selectedMonth;
    private String nextMonth;
    private String selectedBrand = "All";
    private String searchQuery = "";
    private List<ProductInfo> productList = new ArrayList<>();

    public SalesTargetSubmitController(@NonNull Context context,
                                       @NonNull LayoutSalesTargetSubmitBinding binding,
                                       @NonNull FragmentOperationsBinding host,
                                       @NonNull LifecycleOwner lifecycleOwner,
                                       @NonNull SalesTargetViewModel viewModel) {
        this.context = context;
        this.binding = binding;
        this.host = host;
        this.lifecycleOwner = lifecycleOwner;
        this.viewModel = viewModel;
    }

    public void render() {
        setupPeriodSelection();
        setupSearchAndBrandFilter();
        setupActions();
        observeViewModel();

        // Trigger load via API: @POST(Constants.GET_PRODUCT_LIST) Call<ApiResponse<List<ProductInfo>>> getProductData(@Body User user);
        viewModel.loadProductCatalog();
    }

    private void setupPeriodSelection() {
        Calendar current = Calendar.getInstance();
        Calendar following = (Calendar) current.clone();
        following.add(Calendar.MONTH, 1);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        selectedMonth = monthFormat.format(current.getTime());
        nextMonth = monthFormat.format(following.getTime());

        binding.chipMonthCurrent.setText(selectedMonth);
        binding.chipMonthNext.setText(nextMonth);
        binding.chipGroupTargetMonth.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            selectedMonth = checkedIds.get(0) == binding.chipMonthNext.getId()
                    ? nextMonth : monthFormat.format(current.getTime());
        });
    }

    private void setupSearchAndBrandFilter() {
        binding.brandChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == binding.chipBrandAvene.getId()) {
                selectedBrand = "Avene";
            } else if (checkedId == binding.chipBrandCarex.getId()) {
                selectedBrand = "Carex";
            } else if (checkedId == binding.chipBrandCosmed.getId()) {
                selectedBrand = "Cosmed";
            } else if (checkedId == binding.chipBrandBiumark.getId()) {
                selectedBrand = "Biumark";
            } else if (checkedId == binding.chipBrandWahib.getId()) {
                selectedBrand = "Wahib";
            } else {
                selectedBrand = "All";
            }
            renderProductItems();
        });

        binding.etTargetSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString().trim().toLowerCase(Locale.ROOT) : "";
                renderProductItems();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupActions() {
        host.btnReviewAndSubmitTarget.setOnClickListener(v -> {
            int totalUnits = 0;
            for (ProductInfo p : productList) {
                if (p.getTargetQty() > 0) {
                    totalUnits += p.getTargetQty();
                }
            }
            if (totalUnits == 0) {
                Snackbar.make(binding.getRoot(), "Set target quantity for at least one product before reviewing.", Snackbar.LENGTH_LONG).show();
                return;
            }
            showReviewDialog();
        });
    }

    private void showReviewDialog() {
        DialogSalesTargetReviewBinding review = DialogSalesTargetReviewBinding.inflate(
                LayoutInflater.from(context));

        review.tvReviewPeriodSubtitle.setText(selectedMonth + " Target Summary");

        int totalUnits = 0;
        double totalAmount = 0.0;
        review.reviewProductList.removeAllViews();

        for (ProductInfo product : productList) {
            int qty = product.getTargetQty();
            if (qty <= 0) continue;

            totalUnits += qty;
            double lineTotal = qty * product.getEffectiveUnitPrice();
            totalAmount += lineTotal;

            ItemOrderReviewProductBinding item = ItemOrderReviewProductBinding.inflate(
                    LayoutInflater.from(context), review.reviewProductList, false);
            item.tvReviewProductName.setText(product.getProductName());
            item.tvReviewProductQuantity.setText(qty + " units");
            item.tvReviewUnitPrice.setText(formatMoney(product.getEffectiveUnitPrice()) + " each");
            item.tvReviewLineTotal.setText(formatMoney(lineTotal));

            item.btnReviewDecrease.setVisibility(View.GONE);
            item.btnReviewIncrease.setVisibility(View.GONE);
            item.btnReviewRemove.setVisibility(View.GONE);

            review.reviewProductList.addView(item.getRoot());
        }

        review.tvReviewTotalUnits.setText(totalUnits + " units");
        review.tvReviewTotalValue.setText(formatMoney(totalAmount));

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(review.getRoot());
        review.btnReviewClose.setOnClickListener(v -> dialog.dismiss());


        dialog.setOnShowListener(ignored -> {
            View sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                ViewGroup.LayoutParams params = sheet.getLayoutParams();
                params.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.82f);
                sheet.setLayoutParams(params);
            }
            dialog.getBehavior().setSkipCollapsed(true);
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        dialog.show();
    }

    private void observeViewModel() {
        viewModel.getProducts().observe(lifecycleOwner, products -> {
            if (products != null) {
                this.productList = products;
                renderProductItems();
            }
        });

        viewModel.getDraftSummary().observe(lifecycleOwner, summary -> {
            if (summary != null) {
                host.tvTargetSubmitSummary.setText(summary.totalUnits + " units · " + formatMoney(summary.totalAmount));
            }
        });

        viewModel.getErrorMessage().observe(lifecycleOwner, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void renderProductItems() {
        LinearLayout list = binding.targetProductsContainer;
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        int count = 0;
        for (ProductInfo product : productList) {
            // Brand filter
            if (!"All".equalsIgnoreCase(selectedBrand)) {
                String brand = product.getBrandName();
                String cat = product.getCategory();
                boolean matchesBrand = brand != null && brand.toLowerCase(Locale.ROOT).contains(selectedBrand.toLowerCase(Locale.ROOT));
                boolean matchesCat = cat != null && cat.toLowerCase(Locale.ROOT).contains(selectedBrand.toLowerCase(Locale.ROOT));
                if (!matchesBrand && !matchesCat) {
                    continue;
                }
            }

            // Search product name, SKU, brand, or pack size; API fields may be absent.
            if (!searchQuery.isEmpty()) {
                boolean matches = containsQuery(product.getProductName())
                        || containsQuery(product.getProductCode())
                        || containsQuery(product.getBrandName())
                        || containsQuery(product.getPackSize());
                if (!matches) continue;
            }

            count++;
            ItemSalesTargetProductBinding item = ItemSalesTargetProductBinding.inflate(inflater, list, false);

            item.tvBrandBadge.setText(product.getBrandName());
            item.tvProductSku.setText("SKU: " + product.getProductCode());
            item.tvPackSize.setText(product.getPackSize());
            item.tvProductName.setText(product.getProductName());
            item.tvProductPrice.setText("TP: " + formatMoney(product.getTp()) + "  |  MRP: " + formatMoney(product.getMrp()));
            item.tvPreviousQty.setText("Last month: " + product.getPreviousQty());

            // Quantity & Live Amount
            EditText etQty = item.etTargetQty;
            etQty.setText(String.valueOf(product.getTargetQty()));
            item.tvItemSubtotal.setText(formatMoney(product.getTargetAmount()));

            item.btnQtyDecrease.setOnClickListener(v -> {
                int current = product.getTargetQty();
                if (current > 0) {
                    int updated = current - 1;
                    product.setTargetQty(updated);
                    etQty.setText(String.valueOf(updated));
                    item.tvItemSubtotal.setText(formatMoney(product.getTargetAmount()));
                    viewModel.setProductTargetQuantity(product.getProductCode(), updated);
                }
            });

            item.btnQtyIncrease.setOnClickListener(v -> {
                int current = product.getTargetQty();
                int updated = current + 1;
                product.setTargetQty(updated);
                etQty.setText(String.valueOf(updated));
                item.tvItemSubtotal.setText(formatMoney(product.getTargetAmount()));
                viewModel.setProductTargetQuantity(product.getProductCode(), updated);
            });

            etQty.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s == null) return;
                    int val = 0;
                    try {
                        val = Integer.parseInt(s.toString().trim());
                    } catch (NumberFormatException ignored) {}
                    if (val != product.getTargetQty()) {
                        product.setTargetQty(val);
                        item.tvItemSubtotal.setText(formatMoney(product.getTargetAmount()));
                        viewModel.setProductTargetQuantity(product.getProductCode(), val);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            list.addView(item.getRoot());
        }

        if (count == 0) {
            TextView empty = new TextView(context);
            empty.setText("No products match the selected brand or search.");
            empty.setTextColor(context.getResources().getColor(R.color.on_surface_variant_light));
            empty.setPadding(16, 32, 16, 32);
            empty.setGravity(android.view.Gravity.CENTER);
            list.addView(empty);
        }
    }

    private boolean containsQuery(String value) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(searchQuery);
    }

    private String formatMoney(double amount) {
        return "৳" + currencyFormat.format(amount);
    }
}
