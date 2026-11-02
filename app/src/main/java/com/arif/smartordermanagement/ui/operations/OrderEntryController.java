package com.arif.smartordermanagement.ui.operations;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.data.DemoOperationsStore;
import com.arif.smartordermanagement.databinding.DialogOrderReviewBinding;
import com.arif.smartordermanagement.databinding.FragmentOperationsBinding;
import com.arif.smartordermanagement.databinding.ItemOrderCatalogProductBinding;
import com.arif.smartordermanagement.databinding.ItemOrderReviewProductBinding;
import com.arif.smartordermanagement.databinding.LayoutOrderEntryBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class OrderEntryController {

    private final Context context;
    private final LayoutOrderEntryBinding order;
    private final FragmentOperationsBinding host;
    private final List<CatalogProduct> products = Arrays.asList(
            new CatalogProduct("371077", "AVENE", "Standard", "Avene Xeracalm Cleansing Gel · 100ml", 1900, 2185, "Cosmed"),
            new CatalogProduct("396005", "AVENE", "Standard", "Avene Cleanance Gel · 200ml", 1960, 2250, "Cosmed"),
            new CatalogProduct("396007", "AVENE", "Standard", "Thermal Spring Water Spray · 150ml", 1420, 1600, "Cosmed"),
            new CatalogProduct("410221", "CAREX", "Standard", "Carex Daily Moisture Hand Wash · 250ml", 520, 650, "Carex"),
            new CatalogProduct("410228", "CAREX", "Standard", "Carex Antibacterial Hand Gel · 200ml", 430, 540, "Carex"),
            new CatalogProduct("500014", "OTHER", "Standard", "Daily Care Essentials Bundle", 1250, 1490, "Others")
    );
    private final Map<String, Integer> quantities = new LinkedHashMap<>();
    private String selectedCategory = "All Products";

    OrderEntryController(Context context, LayoutOrderEntryBinding order, FragmentOperationsBinding host) {
        this.context = context;
        this.order = order;
        this.host = host;
    }

    void bind() {
        Runnable updateProducts = this::renderProducts;
        order.orderCategoryGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == order.chipCosmed.getId()) selectedCategory = "Cosmed";
            else if (checkedId == order.chipCarex.getId()) selectedCategory = "Carex";
            else if (checkedId == order.chipOthers.getId()) selectedCategory = "Others";
            else selectedCategory = "All Products";
            updateProducts.run();
        });
        order.orderSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateProducts.run();
            }

            @Override public void afterTextChanged(Editable s) {}
        });
        host.btnReviewOrder.setOnClickListener(v -> showReview());
        renderProducts();
        updateCart();
    }

    private void renderProducts() {
        LinearLayout list = order.orderProductList;
        list.removeAllViews();
        String query = valueOf(order.orderSearch).toLowerCase(Locale.ROOT);
        int visibleCount = 0;

        for (CatalogProduct product : products) {
            if (!"All Products".equals(selectedCategory) && !selectedCategory.equals(product.category)) continue;
            if (!query.isEmpty()
                    && !product.name.toLowerCase(Locale.ROOT).contains(query)
                    && !product.sku.toLowerCase(Locale.ROOT).contains(query)
                    && !product.brand.toLowerCase(Locale.ROOT).contains(query)) continue;

            visibleCount++;
            ItemOrderCatalogProductBinding item = ItemOrderCatalogProductBinding.inflate(
                    LayoutInflater.from(context), list, false);
            item.tvProductBrand.setText(product.brand);
            item.tvProductSku.setText("SKU: " + product.sku);
            item.tvProductType.setText(product.type);
            item.tvProductName.setText(product.name);
            item.tvProductPrice.setText("TP: " + money(product.tp) + "  |  MRP: " + money(product.mrp));
            item.tvProductQuantity.setText(String.valueOf(quantities.getOrDefault(product.sku, 0)));
            item.btnDecreaseQuantity.setOnClickListener(v -> changeQuantity(product, -1));
            item.btnIncreaseQuantity.setOnClickListener(v -> changeQuantity(product, 1));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dp(12);
            list.addView(item.getRoot(), params);
        }

        if (visibleCount == 0) {
            TextView empty = new TextView(context);
            empty.setText("No products match your search.");
            empty.setTextSize(14);
            empty.setTextColor(context.getColor(R.color.on_surface_variant_light));
            empty.setPadding(dp(8), dp(16), dp(8), dp(16));
            list.addView(empty);
        }
    }

    private void changeQuantity(CatalogProduct product, int delta) {
        int current = quantities.getOrDefault(product.sku, 0);
        int next = current + delta;
        if (next <= 0) quantities.remove(product.sku);
        else quantities.put(product.sku, next);
        updateCart();
        renderProducts();
    }

    private void updateCart() {
        int productCount = 0;
        int unitCount = 0;
        double total = 0;
        for (CatalogProduct product : products) {
            int quantity = quantities.getOrDefault(product.sku, 0);
            if (quantity > 0) productCount++;
            unitCount += quantity;
            total += quantity * product.tp;
        }
        host.tvCartItems.setText(productCount + " products · " + unitCount + " units");
        host.tvCartTotal.setText(money(total));
        host.btnReviewOrder.setEnabled(unitCount > 0);
    }

    private void showReview() {
        DialogOrderReviewBinding review = DialogOrderReviewBinding.inflate(
                LayoutInflater.from(context));
        populateReview(review);

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(review.getRoot());
        review.btnReviewClose.setOnClickListener(v -> dialog.dismiss());
        review.btnReviewSubmit.setOnClickListener(v -> {
            if (quantities.isEmpty()) return;
            if (valueOf(order.orderCustomer).isEmpty()) {
                dialog.dismiss();
                order.orderCustomer.setError("Enter a customer or outlet");
                order.orderCustomer.requestFocus();
                return;
            }
            dialog.dismiss();
            submitOrder();
        });
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

    private void populateReview(DialogOrderReviewBinding review) {
        review.tvReviewCustomer.setText(valueOf(order.orderCustomer).isEmpty()
                ? "Choose a customer or outlet before submitting"
                : valueOf(order.orderCustomer));
        review.reviewProductList.removeAllViews();

        int productsInCart = 0;
        int unitsInCart = 0;
        for (CatalogProduct product : products) {
            int quantity = quantities.getOrDefault(product.sku, 0);
            if (quantity <= 0) continue;

            productsInCart++;
            unitsInCart += quantity;
            ItemOrderReviewProductBinding item = ItemOrderReviewProductBinding.inflate(
                    LayoutInflater.from(context), review.reviewProductList, false);
            item.tvReviewProductName.setText(product.name);
            item.tvReviewProductQuantity.setText(String.valueOf(quantity));
            item.tvReviewUnitPrice.setText(money(product.tp) + " each");
            item.tvReviewLineTotal.setText(money(quantity * product.tp));
            item.btnReviewDecrease.setOnClickListener(v -> {
                changeQuantity(product, -1);
                populateReview(review);
            });
            item.btnReviewIncrease.setOnClickListener(v -> {
                changeQuantity(product, 1);
                populateReview(review);
            });
            item.btnReviewRemove.setOnClickListener(v -> {
                quantities.remove(product.sku);
                updateCart();
                renderProducts();
                populateReview(review);
            });
            review.reviewProductList.addView(item.getRoot());
        }

        if (productsInCart == 0) {
            TextView empty = new TextView(context);
            empty.setText("Your cart is empty. Close this sheet to add products.");
            empty.setTextSize(14);
            empty.setTextColor(context.getColor(R.color.on_surface_variant_light));
            empty.setPadding(dp(8), dp(16), dp(8), dp(16));
            review.reviewProductList.addView(empty);
        }
        review.tvReviewItems.setText(productsInCart + " products · " + unitsInCart + " units");
        review.tvReviewTotal.setText(host.tvCartTotal.getText());
        review.btnReviewSubmit.setEnabled(unitsInCart > 0);
    }

    private void submitOrder() {
        String customer = valueOf(order.orderCustomer);
        if (customer.isEmpty()) {
            order.orderCustomer.setError("Enter a customer or outlet");
            order.orderCustomer.requestFocus();
            return;
        }

        int units = 0;
        for (CatalogProduct product : products) {
            int quantity = quantities.getOrDefault(product.sku, 0);
            if (quantity > 0) {
                DemoOperationsStore.addOrder(customer, product.name, quantity, quantity * product.tp);
                units += quantity;
            }
        }
        if (units == 0) {
            showMessage("Add at least one product to the cart.");
            return;
        }

        quantities.clear();
        updateCart();
        renderProducts();
        showMessage("Order saved to demo reports.");
    }

    private void showMessage(String message) {
        Snackbar.make(order.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private String valueOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String money(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return "৳" + format.format(value);
    }

    private int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static final class CatalogProduct {
        final String sku;
        final String brand;
        final String type;
        final String name;
        final double tp;
        final double mrp;
        final String category;

        CatalogProduct(String sku, String brand, String type, String name, double tp, double mrp, String category) {
            this.sku = sku;
            this.brand = brand;
            this.type = type;
            this.name = name;
            this.tp = tp;
            this.mrp = mrp;
            this.category = category;
        }
    }
}
