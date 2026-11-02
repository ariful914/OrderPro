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
import com.arif.smartordermanagement.data.SalesTargetRepository;
import com.arif.smartordermanagement.databinding.DialogOrderReviewBinding;
import com.arif.smartordermanagement.databinding.FragmentOperationsBinding;
import com.arif.smartordermanagement.databinding.ItemOrderCatalogProductBinding;
import com.arif.smartordermanagement.databinding.ItemOrderReviewProductBinding;
import com.arif.smartordermanagement.databinding.LayoutOrderEntryBinding;
import com.arif.smartordermanagement.model.ProductInfo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class OrderEntryController {

    private final Context context;
    private final LayoutOrderEntryBinding order;
    private final FragmentOperationsBinding host;
    private final SalesTargetRepository repository;
    private final List<ProductInfo> products = new ArrayList<>(ProductInfo.getDefaultProducts());
    private final Map<String, Integer> quantities = new LinkedHashMap<>();
    private String selectedCategory = "All Products";

    OrderEntryController(Context context, LayoutOrderEntryBinding order, FragmentOperationsBinding host) {
        this.context = context;
        this.order = order;
        this.host = host;
        this.repository = new SalesTargetRepository(context);
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
        loadProductsFromApi();
    }

    private void loadProductsFromApi() {
        repository.getProductData(null, new SalesTargetRepository.RepositoryCallback<>() {
            @Override
            public void onSuccess(List<ProductInfo> result) {
                if (result != null && !result.isEmpty()) {
                    products.clear();
                    products.addAll(result);
                    renderProducts();
                    updateCart();
                }
            }

            @Override
            public void onError(String message) {
                // Repository falls back to default products on failure
            }
        });
    }

    private void renderProducts() {
        LinearLayout list = order.orderProductList;
        list.removeAllViews();
        String query = valueOf(order.orderSearch).toLowerCase(Locale.ROOT);
        int visibleCount = 0;

        for (ProductInfo product : products) {
            String category = product.getCategory();
            String brand = product.getBrandName();
            if (!"All Products".equals(selectedCategory)
                    && !category.equalsIgnoreCase(selectedCategory)
                    && !brand.equalsIgnoreCase(selectedCategory)) {
                continue;
            }
            if (!query.isEmpty()
                    && !product.getProductName().toLowerCase(Locale.ROOT).contains(query)
                    && !product.getProductCode().toLowerCase(Locale.ROOT).contains(query)
                    && !brand.toLowerCase(Locale.ROOT).contains(query)) continue;

            visibleCount++;
            ItemOrderCatalogProductBinding item = ItemOrderCatalogProductBinding.inflate(
                    LayoutInflater.from(context), list, false);
            item.tvProductBrand.setText(brand);
            item.tvProductSku.setText("SKU: " + product.getProductCode());
            item.tvProductType.setText(product.getPackSize());
            item.tvProductName.setText(product.getProductName());
            item.tvProductPrice.setText("TP: " + money(product.getTp()) + "  |  MRP: " + money(product.getMrp()));
            Integer q = quantities.get(product.getProductCode());
            int qty = q != null ? q : 0;
            item.tvProductQuantity.setText(String.valueOf(qty));
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

    private void changeQuantity(ProductInfo product, int delta) {
        String sku = product.getProductCode();
        Integer existing = quantities.get(sku);
        int current = existing != null ? existing : 0;
        int next = current + delta;
        if (next <= 0) quantities.remove(sku);
        else quantities.put(sku, next);
        updateCart();
        renderProducts();
    }

    private void updateCart() {
        int productCount = 0;
        int unitCount = 0;
        double total = 0;
        for (ProductInfo product : products) {
            Integer q = quantities.get(product.getProductCode());
            int quantity = q != null ? q : 0;
            if (quantity > 0) productCount++;
            unitCount += quantity;
            total += quantity * product.getTp();
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
        for (ProductInfo product : products) {
            String sku = product.getProductCode();
            Integer q = quantities.get(sku);
            int quantity = q != null ? q : 0;
            if (quantity <= 0) continue;

            productsInCart++;
            unitsInCart += quantity;
            ItemOrderReviewProductBinding item = ItemOrderReviewProductBinding.inflate(
                    LayoutInflater.from(context), review.reviewProductList, false);
            item.tvReviewProductName.setText(product.getProductName());
            item.tvReviewProductQuantity.setText(String.valueOf(quantity));
            item.tvReviewUnitPrice.setText(money(product.getTp()) + " each");
            item.tvReviewLineTotal.setText(money(quantity * product.getTp()));
            item.btnReviewDecrease.setOnClickListener(v -> {
                changeQuantity(product, -1);
                populateReview(review);
            });
            item.btnReviewIncrease.setOnClickListener(v -> {
                changeQuantity(product, 1);
                populateReview(review);
            });
            item.btnReviewRemove.setOnClickListener(v -> {
                quantities.remove(sku);
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
        for (ProductInfo product : products) {
            Integer q = quantities.get(product.getProductCode());
            int quantity = q != null ? q : 0;
            if (quantity > 0) {
                DemoOperationsStore.addOrder(customer, product.getProductName(), quantity, quantity * product.getTp());
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
}
