package com.arif.smartordermanagement.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.databinding.FragmentDashboardBinding;
import com.arif.smartordermanagement.databinding.ItemKpiCardBinding;
import com.arif.smartordermanagement.model.CategorySales;
import com.arif.smartordermanagement.model.KpiMetric;
import com.arif.smartordermanagement.model.KpiType;
import com.arif.smartordermanagement.model.TimeFrame;
import com.arif.smartordermanagement.ui.login.LoginFragment;
import com.arif.smartordermanagement.ui.operations.OperationsFragment;
import com.arif.smartordermanagement.ui.operations.OrderReportsFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupToolbar();
        setupQuickActions();
        setupFilters();
        observeViewModel();
    }

    private void setupQuickActions() {
        binding.btnOrderSubmit.setOnClickListener(v -> navigateToOperations(OperationsFragment.ORDER_SUBMIT));
        binding.btnOrderReports.setOnClickListener(v -> navigateToOrderReports());
        binding.btnSampleCenter.setOnClickListener(v -> navigateToOperations(OperationsFragment.SAMPLE_CENTER));
        binding.btnStockInfo.setOnClickListener(v -> navigateToOperations(OperationsFragment.STOCK_INFO));
        binding.btnSalesTarget.setOnClickListener(v -> navigateToOperations(OperationsFragment.TARGET_CENTER));
    }

    private void navigateToOrderReports() {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.screen_enter, R.anim.screen_exit, R.anim.screen_pop_enter, R.anim.screen_pop_exit)
                .replace(R.id.fragment_container, new OrderReportsFragment())
                .addToBackStack("order_reports")
                .commit();
    }

    private void navigateToOperations(String destination) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.screen_enter, R.anim.screen_exit, R.anim.screen_pop_enter, R.anim.screen_pop_exit)
                .replace(R.id.fragment_container, OperationsFragment.newInstance(destination))
                .addToBackStack(destination)
                .commit();
    }

    private void setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_dashboard);

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_logout) {
                viewModel.logout();
                return true;
            }

            String destination = null;
            if (itemId == R.id.action_order_submit) {
                destination = OperationsFragment.ORDER_SUBMIT;
            } else if (itemId == R.id.action_order_reports) {
                navigateToOrderReports();
                return true;
            } else if (itemId == R.id.action_sales_target) {
                destination = OperationsFragment.TARGET_CENTER;
            } else if (itemId == R.id.action_samples) {
                destination = OperationsFragment.SAMPLE_CENTER;
            } else if (itemId == R.id.action_stock_info) {
                destination = OperationsFragment.STOCK_INFO;
            } else if (itemId == R.id.action_admin_panel) {
                destination = OperationsFragment.ADMIN_PANEL;
            }

            if (destination != null) {
                navigateToOperations(destination);
                return true;
            }
            return false;
        });

        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.toolbar.setSubtitle(user.getRole());
                String timeGreeting = getTimeBasedGreeting();
                String fullName = user.getName() == null ? "" : user.getName().trim();
                String firstName = fullName.contains(" ")
                        ? fullName.substring(0, fullName.indexOf(' '))
                        : fullName;
                binding.tvGreetingTitle.setText(timeGreeting + ", " + firstName);
                binding.tvGreetingTitle.setContentDescription(timeGreeting + ", " + fullName);
                binding.tvUserDesignation.setText(user.getRole() + " • " + user.getCompanyName());
            }
        });
    }

    private String getTimeBasedGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good evening";
        } else {
            return "Good night";
        }
    }

    private void setupFilters() {
        binding.chipGroupTimeframe.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipToday) {
                viewModel.selectTimeFrame(TimeFrame.TODAY);
            } else if (checkedId == R.id.chipThisMonth) {
                viewModel.selectTimeFrame(TimeFrame.THIS_MONTH);
            } else {
                viewModel.selectTimeFrame(TimeFrame.THIS_WEEK);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressTopLoading.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
        });

        viewModel.getKpiMetrics().observe(getViewLifecycleOwner(), this::updateKpiCards);

        viewModel.getSalesTrend().observe(getViewLifecycleOwner(), trend -> {
            if (trend != null) {
                binding.tvSalesTrendTitle.setText("Sales Performance (" + trend.getTimeFrame().getDisplayName() + ")");
                binding.tvSalesTrendAvg.setText("Avg: " + trend.getAverageSalesFormatted());
                binding.tvSalesTrendTotal.setText(trend.getTotalSalesFormatted());
                binding.chartSalesBar.setSalesTrend(trend);
            }
        });

        viewModel.getCategorySales().observe(getViewLifecycleOwner(), this::updateCategorySales);

        viewModel.getIsLoggedOut().observe(getViewLifecycleOwner(), loggedOut -> {
            if (Boolean.TRUE.equals(loggedOut) && getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.screen_enter, R.anim.screen_exit, R.anim.screen_pop_enter, R.anim.screen_pop_exit)
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit();
            }
        });
    }

    private void updateKpiCards(List<KpiMetric> kpis) {
        if (kpis == null || kpis.size() < 4) return;

        ItemKpiCardBinding[] cardBindings = new ItemKpiCardBinding[]{
                ItemKpiCardBinding.bind(binding.cardKpi1.getRoot()),
                ItemKpiCardBinding.bind(binding.cardKpi2.getRoot()),
                ItemKpiCardBinding.bind(binding.cardKpi3.getRoot()),
                ItemKpiCardBinding.bind(binding.cardKpi4.getRoot())
        };

        for (int i = 0; i < 4; i++) {
            KpiMetric kpi = kpis.get(i);
            ItemKpiCardBinding b = cardBindings[i];

            b.tvKpiTitle.setText(kpi.getTitle());
            b.tvKpiValue.setText(kpi.getValue());
            b.tvPercentageChange.setText(kpi.getPercentageChange());
            b.tvComparisonLabel.setText(kpi.getComparisonLabel());

            // Icon & Colors
            int iconRes = R.drawable.ic_money;
            int iconTint = requireContext().getColor(R.color.google_blue);
            int iconBg = requireContext().getColor(R.color.google_blue_container);

            if (kpi.getType() == KpiType.ORDERS) {
                iconRes = R.drawable.ic_cart;
                iconTint = requireContext().getColor(R.color.on_surface_variant_light);
                iconBg = requireContext().getColor(R.color.tonal_surface_light);
            } else if (kpi.getType() == KpiType.AVERAGE_ORDER_VALUE) {
                iconRes = R.drawable.ic_receipt;
                iconTint = requireContext().getColor(R.color.google_blue);
                iconBg = requireContext().getColor(R.color.google_blue_container);
            } else if (kpi.getType() == KpiType.PENDING_DELIVERIES) {
                iconRes = R.drawable.ic_shipping;
                iconTint = requireContext().getColor(R.color.status_pending_amber);
                iconBg = requireContext().getColor(R.color.status_pending_amber_bg);
            }

            b.imgKpiIcon.setImageResource(iconRes);
            b.imgKpiIcon.setColorFilter(iconTint);
            b.layoutKpiIconBg.getBackground().setTint(iconBg);

            // Trend Badge
            int trendColor = requireContext().getColor(
                    kpi.isPositive() ? R.color.trend_positive_green : R.color.trend_negative_red);
            int trendBg = requireContext().getColor(
                    kpi.isPositive() ? R.color.trend_positive_green_bg : R.color.trend_negative_red_bg);
            int trendIcon = kpi.isPositive() ? R.drawable.ic_trending_up : R.drawable.ic_trending_down;

            b.tvPercentageChange.setTextColor(trendColor);
            b.imgTrendIcon.setImageResource(trendIcon);
            b.imgTrendIcon.setColorFilter(trendColor);
            b.layoutTrendBadge.getBackground().setTint(trendBg);
        }
    }

    private void updateCategorySales(List<CategorySales> categories) {
        if (categories == null) return;

        binding.layoutCategoriesContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (CategorySales cat : categories) {
            View catView = inflater.inflate(R.layout.item_category_sales, binding.layoutCategoriesContainer, false);

            TextView tvTitle = catView.findViewById(R.id.tvCategoryTitle);
            TextView tvValue = catView.findViewById(R.id.tvCategoryValue);
            LinearProgressIndicator progress = catView.findViewById(R.id.progressCategory);

            tvTitle.setText(cat.getCategoryName());
            tvValue.setText(cat.getSalesAmountFormatted() + " (" + (int) (cat.getSharePercentage() * 100) + "%)");
            progress.setProgress((int) (cat.getSharePercentage() * 100));

            binding.layoutCategoriesContainer.addView(catView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
