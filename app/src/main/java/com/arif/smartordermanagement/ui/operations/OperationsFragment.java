package com.arif.smartordermanagement.ui.operations;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.data.DemoOperationsStore;
import com.arif.smartordermanagement.databinding.FragmentOperationsBinding;
import com.arif.smartordermanagement.databinding.LayoutOrderEntryBinding;
import com.arif.smartordermanagement.model.ReportFilter;
import com.arif.smartordermanagement.network.RetrofitClient;
import com.arif.smartordermanagement.ui.common.ReportHeaderFilterView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperationsFragment extends Fragment {

    private static final String ARG_DESTINATION = "destination";

    public static final String ORDER_SUBMIT = "order_submit";
    public static final String ORDER_REPORTS = "order_reports";
    public static final String DAILY_INVOICE = "daily_invoice";
    public static final String ACTUAL_SALES = "actual_sales";
    public static final String CUSTOMER_SALES = "customer_sales";
    public static final String PRODUCT_SALES = "product_sales";
    public static final String SALES_COMPARISON = "sales_comparison";
    public static final String SAMPLE_REQUEST = "sample_request";
    public static final String SAMPLE_REPORT = "sample_report";
    public static final String SAMPLE_APPROVAL = "sample_approval";
    public static final String SAMPLE_DELIVERY = "sample_delivery";
    public static final String SAMPLE_CENTER = "sample_center";
    private int hubTabIndex = -1;
    private String activeTabDestination;
    public static final String STOCK_INFO = "stock_info";
    public static final String ADMIN_PANEL = "admin_panel";
    public static final String SPECIAL_REQUIREMENTS = "special_requirements";

    private FragmentOperationsBinding binding;
    private DailyInvoiceReportController dailyInvoiceReportController;
    private DailyInvoiceReportViewModel dailyInvoiceReportViewModel;

    public static OperationsFragment newInstance(String destination) {
        OperationsFragment fragment = new OperationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DESTINATION, destination);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOperationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String destination = getArguments() != null ? getArguments().getString(ARG_DESTINATION, ORDER_REPORTS) : ORDER_REPORTS;
        binding.operationsToolbar.setTitle(getPageTitle(destination));
        binding.operationsToolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        renderDestination(destination);
    }

    private String getPageTitle(String destination) {
        switch (destination) {
            case ORDER_SUBMIT: return getString(R.string.order_submit);
            case ORDER_REPORTS: return getString(R.string.order_reports);
            case DAILY_INVOICE: return getString(R.string.daily_invoice);
            case ACTUAL_SALES: return getString(R.string.actual_sales);
            case CUSTOMER_SALES: return getString(R.string.customer_sales);
            case PRODUCT_SALES: return getString(R.string.product_sales);
            case SALES_COMPARISON: return getString(R.string.sales_comparison);
            case SAMPLE_REQUEST: return getString(R.string.sample_requisition);
            case SAMPLE_REPORT: return getString(R.string.sample_report);
            case SAMPLE_APPROVAL: return getString(R.string.sample_approvals);
            case SAMPLE_DELIVERY: return getString(R.string.sample_delivery);
            case SAMPLE_CENTER: return getString(R.string.samples);
            case STOCK_INFO: return getString(R.string.stock_info);
            case ADMIN_PANEL: return getString(R.string.admin_panel);
            case SPECIAL_REQUIREMENTS: return getString(R.string.special_requirement_report);
            default: return getString(R.string.order_reports);
        }
    }

    private void renderDestination(String destination) {
        switch (destination) {
            case ORDER_SUBMIT:
                renderOrderForm();
                break;
            case ORDER_REPORTS:
                renderOrderReportMenu();
                break;
            case SAMPLE_CENTER:
                renderSampleCenter();
                break;
            case DAILY_INVOICE:
            case ACTUAL_SALES:
            case CUSTOMER_SALES:
            case PRODUCT_SALES:
            case SALES_COMPARISON:
                renderSalesReport(destination);
                break;
            case SAMPLE_REQUEST:
                renderSampleForm();
                break;
            case SAMPLE_REPORT:
                renderSampleReport();
                break;
            case SAMPLE_APPROVAL:
                renderSampleApprovals();
                break;
            case SAMPLE_DELIVERY:
                renderSampleDelivery();
                break;
            case STOCK_INFO:
                renderStockInfo();
                break;
            case ADMIN_PANEL:
                renderAdminPanel();
                break;
            case SPECIAL_REQUIREMENTS:
                renderSpecialRequirements();
                break;
            default:
                renderOrderReportMenu();
        }
    }

    private void renderOrderReportMenu() {
        String[] labels = {
                getString(R.string.report_tab_daily_invoice),
                getString(R.string.report_tab_actual_sales),
                getString(R.string.report_tab_customer_sales),
                getString(R.string.report_tab_product_sales),
                getString(R.string.report_tab_comparison)
        };
        String[] destinations = {DAILY_INVOICE, ACTUAL_SALES, CUSTOMER_SALES, PRODUCT_SALES, SALES_COMPARISON};
        addDestinationTabs(labels, destinations, DAILY_INVOICE);
    }

    private void renderSampleCenter() {
        String[] labels = {
                getString(R.string.sample_tab_request),
                getString(R.string.sample_tab_approvals),
                getString(R.string.sample_tab_report),
                getString(R.string.sample_tab_delivery)
        };
        String[] destinations = {SAMPLE_REQUEST, SAMPLE_APPROVAL, SAMPLE_REPORT, SAMPLE_DELIVERY};
        addDestinationTabs(labels, destinations, SAMPLE_REQUEST);
    }

    private void addDestinationTabs(String[] labels, String[] destinations, String initialDestination) {
        TabLayout tabs = new TabLayout(requireContext());
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        for (String label : labels) tabs.addTab(tabs.newTab().setText(label), false);

        hubTabIndex = binding.operationsContent.getChildCount();
        activeTabDestination = initialDestination;
        binding.operationsContent.addView(tabs, paramsMatchWrap());
        tabs.selectTab(tabs.getTabAt(indexOf(destinations, initialDestination)));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                showHubPage(destinations[tab.getPosition()]);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        showHubPage(initialDestination);
    }

    private int indexOf(String[] values, String value) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(value)) return i;
        return 0;
    }

    private void showHubPage(String destination) {
        if (dailyInvoiceReportController != null) {
            dailyInvoiceReportController.clear();
            dailyInvoiceReportController = null;
        }
        activeTabDestination = destination;
        int contentStart = hubTabIndex + 1;
        if (binding.operationsContent.getChildCount() > contentStart) {
            binding.operationsContent.removeViews(contentStart, binding.operationsContent.getChildCount() - contentStart);
        }
        renderDestination(destination);
    }

    private void refreshHubPage() {
        if (hubTabIndex >= 0 && binding.operationsContent.getChildCount() > hubTabIndex) {
            showHubPage(activeTabDestination);
        } else {
            binding.operationsContent.removeAllViews();
            renderDestination(getArguments() != null
                    ? getArguments().getString(ARG_DESTINATION, ORDER_REPORTS) : ORDER_REPORTS);
        }
    }

    private void renderOrderForm() {
        binding.operationsToolbar.setTitle("Create Order");
        binding.orderCartBar.setVisibility(View.VISIBLE);
        LayoutOrderEntryBinding order = LayoutOrderEntryBinding.inflate(
                LayoutInflater.from(requireContext()), binding.operationsContent, true);
        new OrderEntryController(requireContext(), order, binding).bind();
    }





    private void renderSalesReport(String destination) {
        if (DAILY_INVOICE.equals(destination)) {
            addIntro(reportDescription(destination));
            if (dailyInvoiceReportViewModel == null) {
                dailyInvoiceReportViewModel = new ViewModelProvider(this)
                        .get(DailyInvoiceReportViewModel.class);
            }
            dailyInvoiceReportController = new DailyInvoiceReportController(
                    requireContext(), binding.operationsContent, getViewLifecycleOwner(),
                    dailyInvoiceReportViewModel);
            dailyInvoiceReportController.render();
            return;
        }

        addIntro(reportDescription(destination));
        addDemoNotice();
        addReportHeaderFilter(null);

        if (ACTUAL_SALES.equals(destination)) {
            double total = 0;
            int count = 0;
            for (DemoOperationsStore.OrderEntry order : DemoOperationsStore.getOrders()) {
                if ("Today".equals(order.date)) {
                    total += order.amount;
                    count++;
                }
            }
            addMetricCard("Today's sales", money(total), "Across " + count + " demo orders");
            addSectionTitle("Recent sales");
            for (DemoOperationsStore.OrderEntry order : DemoOperationsStore.getOrders()) {
                addRecordCard(order.id + " · " + order.customer,
                        order.product + " · " + order.quantity + " units",
                        money(order.amount), order.status);
            }
            return;
        }

        if (CUSTOMER_SALES.equals(destination)) {
            Map<String, Double> totals = new LinkedHashMap<>();
            for (DemoOperationsStore.OrderEntry order : DemoOperationsStore.getOrders()) {
                totals.put(order.customer, totals.getOrDefault(order.customer, 0.0) + order.amount);
            }
            addSectionTitle("Sales by customer");
            for (Map.Entry<String, Double> entry : totals.entrySet()) {
                addRecordCard(entry.getKey(), "Order value", money(entry.getValue()), null);
            }
            return;
        }

        if (PRODUCT_SALES.equals(destination)) {
            Map<String, Double> totals = new LinkedHashMap<>();
            for (DemoOperationsStore.OrderEntry order : DemoOperationsStore.getOrders()) {
                totals.put(order.product, totals.getOrDefault(order.product, 0.0) + order.amount);
            }
            addSectionTitle("Sales by product");
            for (Map.Entry<String, Double> entry : totals.entrySet()) {
                addRecordCard(entry.getKey(), "Product revenue", money(entry.getValue()), null);
            }
            return;
        }

        addSectionTitle("Period comparison");
        addMetricCard("This week", "৳24,850.00", "184 orders");
        addComparisonRow("This week", 0.74f, "৳24,850");
        addComparisonRow("Last week", 0.61f, "৳21,760");
        addBodyText("Demo comparison: sales are up 14.2% from last week.");
    }

    private String reportDescription(String destination) {
        if (DAILY_INVOICE.equals(destination)) return "Browse invoice activity for a selected date.";
        if (ACTUAL_SALES.equals(destination)) return "Track recorded order totals and sales volume.";
        if (CUSTOMER_SALES.equals(destination)) return "See which customers contribute to sales.";
        if (PRODUCT_SALES.equals(destination)) return "Compare revenue generated by each product.";
        return "Compare sales with the previous reporting period.";
    }

    private void renderSampleForm() {
        addIntro("Request product samples for a customer or outlet.");
        addDemoNotice();
        TextInputEditText customer = addField("Customer or outlet", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        TextInputEditText product = addField("Sample product", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        TextInputEditText quantity = addField("Quantity", InputType.TYPE_CLASS_NUMBER);
        TextInputEditText reason = addField("Reason or notes", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        MaterialButton submit = addButton("Submit requisition");
        submit.setOnClickListener(v -> {
            String customerName = valueOf(customer);
            String productName = valueOf(product);
            String note = valueOf(reason);
            int units;
            try {
                units = Integer.parseInt(valueOf(quantity));
            } catch (NumberFormatException e) {
                showMessage("Enter a valid quantity.");
                return;
            }

            if (customerName.isEmpty() || productName.isEmpty() || note.isEmpty() || units <= 0) {
                showMessage("Complete each field and enter a quantity greater than zero.");
                return;
            }

            DemoOperationsStore.addSample(customerName, productName, units, note);
            showMessage("Requisition saved to the demo report.");
            customer.setText("");
            product.setText("");
            quantity.setText("");
            reason.setText("");
        });

    }

    private void renderSampleReport() {
        addIntro("Track sample requests and their current status.");
        addDemoNotice();
        addReportHeaderFilter(filter -> {
            // Header search filter callback
        });
        for (DemoOperationsStore.SampleEntry sample : DemoOperationsStore.getSamples()) {
            addRecordCard(sample.id + " · " + sample.customer,
                    sample.product + " · " + sample.quantity + " units · " + sample.reason,
                    null, sample.status);
        }
    }

    private void renderSampleApprovals() {
        addIntro(getString(R.string.sample_approval_intro));
        addDemoNotice();
        int pendingCount = 0;
        for (DemoOperationsStore.SampleEntry sample : DemoOperationsStore.getSamples()) {
            if (!"Pending".equals(sample.status)) continue;
            pendingCount++;

            MaterialCardView card = createCard();
            LinearLayout column = new LinearLayout(requireContext());
            column.setOrientation(LinearLayout.VERTICAL);
            column.setPadding(dp(16), dp(14), dp(16), dp(14));
            column.addView(makeText(sample.id + " · " + sample.customer, 15,
                    color(R.color.on_surface_light), true));
            TextView details = makeText(sample.product + " · " + sample.quantity + " units", 14,
                    color(R.color.on_surface_variant_light), false);
            LinearLayout.LayoutParams detailParams = paramsMatchWrap();
            detailParams.topMargin = dp(5);
            column.addView(details, detailParams);
            TextView reason = makeText(sample.reason, 13, color(R.color.on_surface_variant_light), false);
            LinearLayout.LayoutParams reasonParams = paramsMatchWrap();
            reasonParams.topMargin = dp(3);
            column.addView(reason, reasonParams);

            LinearLayout actions = new LinearLayout(requireContext());
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(android.view.Gravity.END);
            LinearLayout.LayoutParams actionsParams = paramsMatchWrap();
            actionsParams.topMargin = dp(12);
            MaterialButton reject = new MaterialButton(requireContext());
            reject.setText(R.string.reject);
            reject.setAllCaps(false);
            reject.setCornerRadius(dp(22));
            reject.setTextColor(color(R.color.on_surface_variant_light));
            reject.setStrokeColor(android.content.res.ColorStateList.valueOf(color(R.color.outline_light)));
            reject.setStrokeWidth(dp(1));
            reject.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color(R.color.surface_light)));
            MaterialButton approve = new MaterialButton(requireContext());
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

            reject.setOnClickListener(v -> decideSample(sample.id, "Rejected"));
            approve.setOnClickListener(v -> decideSample(sample.id, "Approved"));
        }
        if (pendingCount == 0) {
            TextView empty = makeText(getString(R.string.sample_approval_empty), 14,
                    color(R.color.on_surface_variant_light), false);
            empty.setPadding(dp(12), dp(16), dp(12), dp(16));
            binding.operationsContent.addView(empty, paramsMatchWrap());
        }

    }

    private void decideSample(String sampleId, String status) {
        DemoOperationsStore.updateSampleStatus(sampleId, status);
        refreshHubPage();
        showMessage("Approved".equals(status)
                ? getString(R.string.sample_approved_message)
                : getString(R.string.sample_rejected_message));
    }

    private void renderSampleDelivery() {
        addIntro(getString(R.string.sample_delivery_intro));
        addDemoNotice();
        int readyCount = 0;
        for (DemoOperationsStore.SampleEntry sample : DemoOperationsStore.getSamples()) {
            if (!"Approved".equals(sample.status)) continue;
            readyCount++;
            addRecordCard(sample.id + " · " + sample.customer,
                    sample.product + " · " + sample.quantity + " units · " + sample.reason,
                    null, sample.status);
            MaterialButton delivered = addButton(getString(R.string.mark_delivered));
            delivered.setOnClickListener(v -> {
                DemoOperationsStore.updateSampleStatus(sample.id, "Delivered");
                refreshHubPage();
                showMessage(getString(R.string.sample_delivered_message));
            });
        }
        if (readyCount == 0) {
            TextView empty = makeText(getString(R.string.no_samples_ready), 14,
                    color(R.color.on_surface_variant_light), false);
            empty.setPadding(dp(12), dp(16), dp(12), dp(16));
            binding.operationsContent.addView(empty, paramsMatchWrap());
        }

    }

    private void renderStockInfo() {
        addIntro("Review current product availability.");
        addDemoNotice();
        addSectionTitle("Inventory");
        String[][] stock = {
                {"Wireless keyboard", "42", "In stock"},
                {"A4 paper", "8", "Low stock"},
                {"Label printer", "0", "Out of stock"},
                {"Design software", "16", "In stock"},
                {"USB-C hub", "5", "Low stock"}
        };
        for (String[] item : stock) {
            addRecordCard(item[0], "Available quantity", item[1] + " units", item[2]);
        }
    }

    private void renderAdminPanel() {
        addIntro("Administration and operational reports.");
        addDemoNotice();
        addActionCard(getString(R.string.samples),
                "Manage sample requests, approvals, reports, and delivery.", SAMPLE_CENTER);
        addActionCard(getString(R.string.special_requirement_report),
                "Review customer-specific requests and their status.", SPECIAL_REQUIREMENTS);
        addMetricCard("Open requirements", countOpenRequirements(), "Demo request queue");
        addBodyText("Administrative access controls are not connected in this demo.");
    }

    private void renderSpecialRequirements() {
        addIntro("Review special requests submitted by customers and the team.");
        addDemoNotice();
        addReportHeaderFilter(filter -> {
            // Header search filter callback
        });
        for (DemoOperationsStore.RequirementEntry requirement : DemoOperationsStore.getRequirements()) {
            addRecordCard(requirement.id + " · " + requirement.requester,
                    requirement.summary + " · " + requirement.date,
                    null, requirement.status);
        }
    }

    private String countOpenRequirements() {
        int count = 0;
        for (DemoOperationsStore.RequirementEntry requirement : DemoOperationsStore.getRequirements()) {
            if (!"Approved".equals(requirement.status)) count++;
        }
        return String.valueOf(count);
    }

    private void addIntro(String text) {
        TextView intro = makeText(text, 15, color(R.color.on_surface_variant_light), false);
        LinearLayout.LayoutParams params = paramsMatchWrap();
        params.bottomMargin = dp(18);
        binding.operationsContent.addView(intro, params);
    }

    private void addDemoNotice() {
        TextView notice = makeText(getString(R.string.demo_data_notice), 12, color(R.color.on_google_blue_container), true);
        notice.setPadding(dp(12), dp(10), dp(12), dp(10));
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(color(R.color.google_blue_container));
        background.setCornerRadius(dp(12));
        notice.setBackground(background);
        LinearLayout.LayoutParams params = paramsMatchWrap();
        params.bottomMargin = dp(18);
        binding.operationsContent.addView(notice, params);
    }

    private void addSectionTitle(String title) {
        TextView heading = makeText(title, 18, color(R.color.on_surface_light), true);
        LinearLayout.LayoutParams params = paramsMatchWrap();
        params.topMargin = dp(8);
        params.bottomMargin = dp(10);
        binding.operationsContent.addView(heading, params);
    }

    private void addBodyText(String text) {
        TextView body = makeText(text, 13, color(R.color.on_surface_variant_light), false);
        LinearLayout.LayoutParams params = paramsMatchWrap();
        params.topMargin = dp(12);
        binding.operationsContent.addView(body, params);
    }

    private void addActionCard(String title, String description, String destination) {
        MaterialCardView card = createCard();
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout textColumn = new LinearLayout(requireContext());
        textColumn.setOrientation(LinearLayout.VERTICAL);
        TextView titleView = makeText(title, 16, color(R.color.on_surface_light), true);
        TextView descriptionView = makeText(description, 13, color(R.color.on_surface_variant_light), false);
        LinearLayout.LayoutParams descParams = paramsMatchWrap();
        descParams.topMargin = dp(4);
        textColumn.addView(titleView);
        textColumn.addView(descriptionView, descParams);
        row.addView(textColumn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView arrow = makeText("›", 28, color(R.color.google_blue), false);
        row.addView(arrow);
        card.addView(row);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> navigateTo(destination));
        addCard(card);
    }

    private void addRecordCard(String title, String detail, @Nullable String amount, @Nullable String status) {
        MaterialCardView card = createCard();
        LinearLayout column = new LinearLayout(requireContext());
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout top = new LinearLayout(requireContext());
        top.setGravity(android.view.Gravity.CENTER_VERTICAL);
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
        LinearLayout.LayoutParams detailParams = paramsMatchWrap();
        detailParams.topMargin = dp(5);
        column.addView(top);
        column.addView(detailView, detailParams);
        card.addView(column);
        addCard(card);
    }

    private void addMetricCard(String label, String value, String detail) {
        MaterialCardView card = createCard();
        LinearLayout column = new LinearLayout(requireContext());
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(18), dp(18), dp(18), dp(18));
        column.addView(makeText(label, 13, color(R.color.on_surface_variant_light), false));
        TextView total = makeText(value, 26, color(R.color.google_blue), true);
        LinearLayout.LayoutParams totalParams = paramsMatchWrap();
        totalParams.topMargin = dp(4);
        column.addView(total, totalParams);
        TextView caption = makeText(detail, 13, color(R.color.on_surface_variant_light), false);
        LinearLayout.LayoutParams captionParams = paramsMatchWrap();
        captionParams.topMargin = dp(3);
        column.addView(caption, captionParams);
        card.addView(column);
        addCard(card);
    }

    private void addComparisonRow(String label, float progress, String value) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView period = makeText(label, 14, color(R.color.on_surface_light), true);
        row.addView(period, new LinearLayout.LayoutParams(dp(90), ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearProgressIndicator bar = new LinearProgressIndicator(requireContext());
        bar.setMax(100);
        bar.setProgressCompat((int) (progress * 100), false);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, dp(8), 1f);
        barParams.setMargins(dp(8), 0, dp(8), 0);
        row.addView(bar, barParams);
        row.addView(makeText(value, 13, color(R.color.on_surface_variant_light), false));
        LinearLayout.LayoutParams rowParams = paramsMatchWrap();
        rowParams.bottomMargin = dp(12);
        binding.operationsContent.addView(row, rowParams);
    }

    private ReportHeaderFilterView addReportHeaderFilter(ReportHeaderFilterView.OnSearchClickListener onSearch) {
        ReportHeaderFilterView filterView = new ReportHeaderFilterView(requireContext());
        filterView.setOnSearchClickListener(filter -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirm report filters")
                    .setMessage(getFilterConfirmationText(filter))
                    .setNegativeButton("Edit", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Search", (dialog, which) -> {
                        if (onSearch != null) {
                            onSearch.onSearch(filter);
                        }
                        showMessage("Search Filter applied: " + filter.getDisplayText());
                    })
                    .show();
        });
        LinearLayout.LayoutParams params = paramsMatchWrap();
        params.bottomMargin = dp(16);
        binding.operationsContent.addView(filterView, params);
        return filterView;
    }

    private String getFilterConfirmationText(ReportFilter filter) {
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
        TextInputLayout layout = new TextInputLayout(requireContext());
        layout.setHint(hint);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        float radius = dp(12);
        layout.setBoxCornerRadii(radius, radius, radius, radius);

        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setInputType(inputType);
        input.setTextSize(16);
        input.setSingleLine(true);
        if ((inputType & InputType.TYPE_CLASS_NUMBER) != 0) {
            input.setInputType(inputType);
        }
        // Let TextInputLayout create compatible child layout parameters for its EditText.
        layout.addView(input);

        LinearLayout.LayoutParams params = paramsMatchWrap();
        params.bottomMargin = dp(14);
        binding.operationsContent.addView(layout, params);
        return input;
    }

    private MaterialButton addButton(String text) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setCornerRadius(dp(26));
        LinearLayout.LayoutParams params = paramsMatchWrap();
        params.topMargin = dp(8);
        params.bottomMargin = dp(8);
        binding.operationsContent.addView(button, params);
        return button;
    }

    private void addEmptyState() {
        TextView empty = makeText("No demo invoices for this date.", 14, color(R.color.on_surface_variant_light), false);
        empty.setPadding(dp(12), dp(16), dp(12), dp(16));
        binding.operationsContent.addView(empty, paramsMatchWrap());
    }

    private MaterialCardView createCard() {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(color(R.color.surface_light));
        card.setStrokeColor(color(R.color.outline_light));
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(16));
        card.setCardElevation(dp(1));
        return card;
    }

    private void addCard(MaterialCardView card) {
        LinearLayout.LayoutParams params = paramsMatchWrap();
        params.bottomMargin = dp(12);
        binding.operationsContent.addView(card, params);
    }

    private TextView makeText(String text, int sizeSp, int textColor, boolean bold) {
        TextView view = new TextView(requireContext());
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(textColor);
        if (bold) view.setTypeface(view.getTypeface(), Typeface.BOLD);
        return view;
    }

    private void navigateTo(String destination) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.screen_enter, R.anim.screen_exit, R.anim.screen_pop_enter, R.anim.screen_pop_exit)
                .replace(R.id.fragment_container, newInstance(destination))
                .addToBackStack(destination)
                .commit();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.operationsContent, message, Snackbar.LENGTH_LONG).show();
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

    private int statusColor(String status) {
        if ("Approved".equals(status) || "Completed".equals(status)) return color(R.color.trend_positive_green);
        if ("Pending".equals(status) || "Under review".equals(status)) return color(R.color.status_pending_amber);
        if ("Rejected".equals(status)) return color(R.color.trend_negative_red);
        return color(R.color.on_surface_variant_light);
    }

    private int color(int resourceId) {
        return requireContext().getColor(resourceId);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout.LayoutParams paramsMatchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onDestroyView() {
        if (dailyInvoiceReportController != null) dailyInvoiceReportController.clear();
        dailyInvoiceReportController = null;
        super.onDestroyView();
        binding = null;
    }
}
