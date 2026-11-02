package com.arif.smartordermanagement.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arif.smartordermanagement.data.AuthRepository;
import com.arif.smartordermanagement.data.SalesRepository;
import com.arif.smartordermanagement.model.CategorySales;
import com.arif.smartordermanagement.model.KpiMetric;
import com.arif.smartordermanagement.model.SalesTrend;
import com.arif.smartordermanagement.model.TimeFrame;
import com.arif.smartordermanagement.model.User;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DashboardViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final SalesRepository salesRepository;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>(null);
    private final MutableLiveData<TimeFrame> selectedTimeFrame = new MutableLiveData<>(TimeFrame.THIS_WEEK);
    private final MutableLiveData<List<KpiMetric>> kpiMetrics = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<SalesTrend> salesTrend = new MutableLiveData<>(null);
    private final MutableLiveData<List<CategorySales>> categorySales = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoggedOut = new MutableLiveData<>(false);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        salesRepository = new SalesRepository();
        loadInitialData();
    }

    public void loadInitialData() {
        currentUser.setValue(authRepository.getCurrentUser());
        loadDashboardMetrics(TimeFrame.THIS_WEEK);
    }

    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<TimeFrame> getSelectedTimeFrame() { return selectedTimeFrame; }
    public LiveData<List<KpiMetric>> getKpiMetrics() { return kpiMetrics; }
    public LiveData<SalesTrend> getSalesTrend() { return salesTrend; }
    public LiveData<List<CategorySales>> getCategorySales() { return categorySales; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsLoggedOut() { return isLoggedOut; }

    public void selectTimeFrame(TimeFrame timeFrame) {
        if (selectedTimeFrame.getValue() == timeFrame && Boolean.FALSE.equals(isLoading.getValue())) return;
        selectedTimeFrame.setValue(timeFrame);
        loadDashboardMetrics(timeFrame);
    }

    private void loadDashboardMetrics(TimeFrame timeFrame) {
        isLoading.setValue(true);
        AtomicInteger pendingSections = new AtomicInteger(3);

        salesRepository.getKpiMetrics(timeFrame, metrics -> {
            kpiMetrics.postValue(metrics);
            finishSectionLoad(pendingSections);
        });
        salesRepository.getSalesTrend(timeFrame, trend -> {
            salesTrend.postValue(trend);
            finishSectionLoad(pendingSections);
        });
        salesRepository.getCategoryBreakdown(timeFrame, categories -> {
            categorySales.postValue(categories);
            finishSectionLoad(pendingSections);
        });
    }

    private void finishSectionLoad(AtomicInteger pendingSections) {
        if (pendingSections.decrementAndGet() == 0) {
            isLoading.postValue(false);
        }
    }

    public void logout() {
        authRepository.logout();
        isLoggedOut.setValue(true);
    }
}
