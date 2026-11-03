package com.arif.smartordermanagement.ui.operations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arif.smartordermanagement.R;

public class OrderReportsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.reportsToolbar);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        bindReportCard(view.findViewById(R.id.reportDailyInvoice), R.string.daily_invoice,
                "Browse invoices and payment activity by date.", OperationsFragment.DAILY_INVOICE);
        bindReportCard(view.findViewById(R.id.reportActualSales), R.string.actual_sales,
                "Review recorded sales totals and order volume.", OperationsFragment.ACTUAL_SALES);
        bindReportCard(view.findViewById(R.id.reportCustomerSales), R.string.customer_sales,
                "Find your highest contributing customers.", OperationsFragment.CUSTOMER_SALES);
        bindReportCard(view.findViewById(R.id.reportProductSales), R.string.product_sales,
                "See sales and revenue across your products.", OperationsFragment.PRODUCT_SALES);
        bindReportCard(view.findViewById(R.id.reportComparison), R.string.sales_comparison,
                "Compare this period with the previous one.", OperationsFragment.SALES_COMPARISON);
        bindReportCard(view.findViewById(R.id.reportSalesTarget), R.string.target_status_report,
                "Check submissions, reviews, and approval progress.", OperationsFragment.TARGET_REPORT);
    }

    private void bindReportCard(View card, int titleRes, String description, String destination) {
        TextView title = card.findViewById(R.id.reportTitle);
        TextView subtitle = card.findViewById(R.id.reportDescription);
        title.setText(titleRes);
        subtitle.setText(description);
        card.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.screen_enter, R.anim.screen_exit, R.anim.screen_pop_enter, R.anim.screen_pop_exit)
                .replace(R.id.fragment_container, OperationsFragment.newInstance(destination))
                .addToBackStack(destination)
                .commit());
    }

}
