package com.arif.smartordermanagement.ui.operations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arif.smartordermanagement.FilterSharedViewModel;
import com.arif.smartordermanagement.FilterSharedViewModelFactory;
import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.data.DemoOperationsStore;
import com.arif.smartordermanagement.databinding.FragmentOperationsBinding;
import com.arif.smartordermanagement.databinding.LayoutOrderEntryBinding;
import com.arif.smartordermanagement.databinding.LayoutSalesTargetSubmitBinding;
import com.arif.smartordermanagement.network.RetrofitClient;
import com.arif.smartordermanagement.ui.common.ReportHeaderFilterView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

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
    public static final String TARGET_CENTER = "target_center";
    public static final String TARGET_SUBMIT = "target_submit";
    public static final String TARGET_APPROVAL = "target_approval";
    public static final String TARGET_REPORT = "target_report";
    public static final String STOCK_INFO = "stock_info";
    public static final String ADMIN_PANEL = "admin_panel";
    public static final String SPECIAL_REQUIREMENTS = "special_requirements";

    private FragmentOperationsBinding binding;
    private DailyInvoiceReportController dailyInvoiceReportController;
    private DailyInvoiceReportViewModel dailyInvoiceReportViewModel;
    private FilterSharedViewModel filterSharedViewModel;
    private SalesTargetViewModel salesTargetViewModel;
    private SalesTargetSubmitController targetSubmitController;
    private SalesTargetReportController targetReportController;
    private SampleOperationsController sampleOperationsController;
    private OperationsTabComponent operationsTabComponent;
    private OperationsUiComponents operationsUiComponents;
    private SalesReportController salesReportController;

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
        binding.fabCreateTarget.setOnClickListener(v -> navigateTo(TARGET_SUBMIT));
        operationsTabComponent = new OperationsTabComponent(requireContext(), binding.operationsContent);
        filterSharedViewModel = new ViewModelProvider(
                requireActivity(), new FilterSharedViewModelFactory(requireActivity().getApplication()))
                .get(FilterSharedViewModel.class);
        operationsUiComponents = new OperationsUiComponents(
                requireContext(), binding.operationsContent, this::navigateTo, this::showMessage,
                filterSharedViewModel);
        salesReportController = new SalesReportController(operationsUiComponents);
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
            case TARGET_CENTER: return getString(R.string.sales_target);
            case TARGET_SUBMIT: return getString(R.string.submit_sales_target);
            case TARGET_APPROVAL: return getString(R.string.senior_approve_target);
            case TARGET_REPORT: return getString(R.string.target_status_report);
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
            case TARGET_CENTER:
                renderTargetCenter();
                break;
            case TARGET_SUBMIT:
                renderTargetSubmit();
                break;
            case TARGET_APPROVAL:
                renderTargetApproval();
                break;
            case TARGET_REPORT:
                renderTargetReport();
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
                sampleOperations().renderRequest();
                break;
            case SAMPLE_REPORT:
                sampleOperations().renderReport();
                break;
            case SAMPLE_APPROVAL:
                sampleOperations().renderApprovals();
                break;
            case SAMPLE_DELIVERY:
                sampleOperations().renderDelivery();
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
        operationsTabComponent.show(labels, destinations, initialDestination, this::showHubPage);
    }

    private void showHubPage(String destination) {
        if (dailyInvoiceReportController != null) {
            dailyInvoiceReportController.clear();
            dailyInvoiceReportController = null;
        }
        targetSubmitController = null;
        targetReportController = null;
        binding.orderCartBar.setVisibility(View.GONE);
        binding.targetSubmitBar.setVisibility(View.GONE);
        binding.fabCreateTarget.setVisibility(TARGET_REPORT.equals(destination) ? View.VISIBLE : View.GONE);
        int contentStart = operationsTabComponent.getTabIndex() + 1;
        if (binding.operationsContent.getChildCount() > contentStart) {
            binding.operationsContent.removeViews(contentStart, binding.operationsContent.getChildCount() - contentStart);
        }
        renderDestination(destination);
    }

    private void refreshHubPage() {
        if (operationsTabComponent.hasTabs()
                && binding.operationsContent.getChildCount() > operationsTabComponent.getTabIndex()) {
            showHubPage(operationsTabComponent.getActiveDestination());
        } else {
            binding.operationsContent.removeAllViews();
            renderDestination(getArguments() != null
                    ? getArguments().getString(ARG_DESTINATION, ORDER_REPORTS) : ORDER_REPORTS);
        }
    }

    private void renderTargetCenter() {
        String[] labels = {
                "Approvals",
                "History"
        };
        String[] destinations = {TARGET_APPROVAL, TARGET_REPORT};
        addDestinationTabs(labels, destinations, TARGET_REPORT);
    }

    private void renderTargetSubmit() {
        binding.fabCreateTarget.setVisibility(View.GONE);
        binding.operationsToolbar.setTitle(R.string.submit_sales_target);
        if (salesTargetViewModel == null) {
            salesTargetViewModel = new ViewModelProvider(this).get(SalesTargetViewModel.class);
        }
        binding.targetSubmitBar.setVisibility(View.VISIBLE);
        LayoutSalesTargetSubmitBinding submitBinding = LayoutSalesTargetSubmitBinding.inflate(
                LayoutInflater.from(requireContext()), binding.operationsContent, true);
        targetSubmitController = new SalesTargetSubmitController(
                requireContext(), submitBinding, binding, getViewLifecycleOwner(), salesTargetViewModel);
        targetSubmitController.render();
    }

    private void renderTargetApproval() {
        binding.fabCreateTarget.setVisibility(View.GONE);
        binding.operationsToolbar.setTitle(R.string.senior_approve_target);
        if (salesTargetViewModel == null) {
            salesTargetViewModel = new ViewModelProvider(this).get(SalesTargetViewModel.class);
        }
        salesTargetViewModel.setReportStatusFilter(com.arif.smartordermanagement.model.TargetStatus.PENDING);
        targetReportController = new SalesTargetReportController(
                requireContext(), binding.operationsContent, getViewLifecycleOwner(), salesTargetViewModel);
        targetReportController.render();
    }

    private void renderTargetReport() {
        binding.fabCreateTarget.setVisibility(View.VISIBLE);
        binding.operationsToolbar.setTitle(R.string.target_status_report);
        if (salesTargetViewModel == null) {
            salesTargetViewModel = new ViewModelProvider(this).get(SalesTargetViewModel.class);
        }
        salesTargetViewModel.setReportStatusFilter("ALL");
        targetReportController = new SalesTargetReportController(
                requireContext(), binding.operationsContent, getViewLifecycleOwner(), salesTargetViewModel);
        targetReportController.render();
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

            if (dailyInvoiceReportViewModel == null) {
                dailyInvoiceReportViewModel = new ViewModelProvider(requireActivity())
                        .get(DailyInvoiceReportViewModel.class);
            }
            dailyInvoiceReportController = new DailyInvoiceReportController(
                    requireContext(), binding.operationsContent, getViewLifecycleOwner(),
                    dailyInvoiceReportViewModel, filterSharedViewModel);
            dailyInvoiceReportController.render();
            return;
        }


        salesReportController.render(destination);
    }



    private SampleOperationsController sampleOperations() {
        if (sampleOperationsController == null) {
            sampleOperationsController = new SampleOperationsController(
                    requireContext(), binding.operationsContent,
                    this::refreshHubPage, this::showMessage);
        }
        return sampleOperationsController;
    }

    private void renderStockInfo() {
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
        addDemoNotice();
        addActionCard(getString(R.string.samples),
                "Manage sample requests, approvals, reports, and delivery.", SAMPLE_CENTER);
        addActionCard(getString(R.string.special_requirement_report),
                "Review customer-specific requests and their status.", SPECIAL_REQUIREMENTS);
        addMetricCard("Open requirements", countOpenRequirements(), "Demo request queue");
        addBodyText("Administrative access controls are not connected in this demo.");
    }

    private void renderSpecialRequirements() {
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



    private void addDemoNotice() {
        operationsUiComponents.addDemoNotice();
    }

    private void addSectionTitle(String title) {
        operationsUiComponents.addSectionTitle(title);
    }

    private void addBodyText(String text) {
        operationsUiComponents.addBodyText(text);
    }

    private void addActionCard(String title, String description, String destination) {
        operationsUiComponents.addActionCard(title, description, destination);
    }

    private void addRecordCard(String title, String detail, @Nullable String amount, @Nullable String status) {
        operationsUiComponents.addRecordCard(title, detail, amount, status);
    }

    private void addMetricCard(String label, String value, String detail) {
        operationsUiComponents.addMetricCard(label, value, detail);
    }

    private void addReportHeaderFilter(
            ReportHeaderFilterView.OnSearchClickListener onSearch) {
        operationsUiComponents.addReportHeaderFilter(onSearch);
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

    @Override
    public void onDestroyView() {
        if (dailyInvoiceReportController != null) dailyInvoiceReportController.clear();
        dailyInvoiceReportController = null;
        sampleOperationsController = null;
        operationsTabComponent = null;
        operationsUiComponents = null;
        salesReportController = null;
        super.onDestroyView();
        binding = null;
    }
}
