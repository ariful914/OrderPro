package com.arif.smartordermanagement;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arif.smartordermanagement.model.ApiResponse;
import com.arif.smartordermanagement.model.Area;
import com.arif.smartordermanagement.model.Region;
import com.arif.smartordermanagement.model.ReportFilter;
import com.arif.smartordermanagement.model.SubArea;
import com.arif.smartordermanagement.model.Territory;
import com.arif.smartordermanagement.network.ApiInterface;
import com.arif.smartordermanagement.network.RetrofitClient;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/** Shares report filters and asynchronously loads dependent location lists. */
public final class FilterSharedViewModel extends AndroidViewModel {
    private final ApiInterface api;
    private final MutableLiveData<ReportFilter> selectedFilter = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<Region>>> regions = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<Area>>> areas = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<SubArea>>> subAreas = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<Territory>>> territories = new MutableLiveData<>();

    private Call<ApiResponse<List<Region>>> regionsCall;
    private Call<ApiResponse<List<Area>>> areasCall;
    private Call<ApiResponse<List<SubArea>>> subAreasCall;
    private Call<ApiResponse<List<Territory>>> territoriesCall;
    private String loadedRegionZoneId;
    private String loadedAreaRegionId;
    private String loadedSubAreaAreaId;
    private String loadedTerritorySubAreaId;

    public FilterSharedViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.createAuthenticatedService(application, ApiInterface.class);
    }

    public ReportFilter getCurrentFilter() { return selectedFilter.getValue(); }

    public void setCurrentFilter(ReportFilter filter) {
        if (filter != null) selectedFilter.setValue(filter);
    }

    public LiveData<ApiResponse<List<Region>>> getRegions() { return regions; }
    public LiveData<ApiResponse<List<Area>>> getAreas() { return areas; }
    public LiveData<ApiResponse<List<SubArea>>> getSubAreas() { return subAreas; }
    public LiveData<ApiResponse<List<Territory>>> getTerritories() { return territories; }

    public void loadRegions(String zoneId, boolean force) {
        if (isBlank(zoneId)) return;
        if (!force && zoneId.equals(loadedRegionZoneId) && hasSuccessfulValue(regions)) return;
        cancel(regionsCall);
        loadedRegionZoneId = zoneId;
        regions.setValue(ApiResponse.loading(null));
        regionsCall = api.getRegionsByZone(zoneId);
        regionsCall.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Region>>> call,
                                   @NonNull Response<ApiResponse<List<Region>>> response) {
                if (call.isCanceled() || call != regionsCall) return;
                ApiResponse<List<Region>> body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null) {
                    regions.postValue(ApiResponse.success(body.getData()));
                } else {
                    loadedRegionZoneId = null;
                    regions.postValue(ApiResponse.error("Could not load regions. Try again.", null));
                }
                regionsCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Region>>> call, @NonNull Throwable error) {
                if (call.isCanceled() || call != regionsCall) return;
                regionsCall = null;
                loadedRegionZoneId = null;
                regions.postValue(ApiResponse.error("Could not load regions. Check your connection.", null));
            }
        });
    }

    public void loadAreas(String regionId, boolean force) {
        if (isBlank(regionId) || "ALL".equalsIgnoreCase(regionId)) {
            clearAreasAndDescendants();
            return;
        }
        if (!force && regionId.equals(loadedAreaRegionId) && hasSuccessfulValue(areas)) return;
        cancel(areasCall);
        cancel(subAreasCall);
        cancel(territoriesCall);
        loadedAreaRegionId = regionId;
        loadedSubAreaAreaId = null;
        loadedTerritorySubAreaId = null;
        areas.setValue(ApiResponse.loading(null));
        subAreas.setValue(ApiResponse.success(Collections.emptyList()));
        territories.setValue(ApiResponse.success(Collections.emptyList()));
        areasCall = api.getAreasByRegion(regionId);
        areasCall.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Area>>> call,
                                   @NonNull Response<ApiResponse<List<Area>>> response) {
                if (call.isCanceled() || call != areasCall) return;
                ApiResponse<List<Area>> body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null) {
                    areas.postValue(ApiResponse.success(body.getData()));
                } else {
                    loadedAreaRegionId = null;
                    areas.postValue(ApiResponse.error("Could not load areas. Try again.", null));
                }
                areasCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Area>>> call, @NonNull Throwable error) {
                if (call.isCanceled() || call != areasCall) return;
                areasCall = null;
                loadedAreaRegionId = null;
                areas.postValue(ApiResponse.error("Could not load areas. Check your connection.", null));
            }
        });
    }

    public void loadSubAreas(String areaId, boolean force) {
        if (isBlank(areaId) || "ALL".equalsIgnoreCase(areaId)) {
            clearSubAreasAndTerritories();
            return;
        }
        if (!force && areaId.equals(loadedSubAreaAreaId) && hasSuccessfulValue(subAreas)) return;
        cancel(subAreasCall);
        cancel(territoriesCall);
        loadedSubAreaAreaId = areaId;
        loadedTerritorySubAreaId = null;
        subAreas.setValue(ApiResponse.loading(null));
        territories.setValue(ApiResponse.success(Collections.emptyList()));
        subAreasCall = api.getSubAreasByArea(areaId);
        subAreasCall.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SubArea>>> call,
                                   @NonNull Response<ApiResponse<List<SubArea>>> response) {
                if (call.isCanceled() || call != subAreasCall) return;
                ApiResponse<List<SubArea>> body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null) {
                    subAreas.postValue(ApiResponse.success(body.getData()));
                } else {
                    loadedSubAreaAreaId = null;
                    subAreas.postValue(ApiResponse.error("Could not load sub-areas. Try again.", null));
                }
                subAreasCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SubArea>>> call,
                                  @NonNull Throwable error) {
                if (call.isCanceled() || call != subAreasCall) return;
                subAreasCall = null;
                loadedSubAreaAreaId = null;
                subAreas.postValue(ApiResponse.error(
                        "Could not load sub-areas. Check your connection.", null));
            }
        });
    }

    public void loadTerritories(String subAreaId, boolean force) {
        if (isBlank(subAreaId) || "ALL".equalsIgnoreCase(subAreaId)) {
            clearTerritories();
            return;
        }
        if (!force && subAreaId.equals(loadedTerritorySubAreaId)
                && hasSuccessfulValue(territories)) return;
        cancel(territoriesCall);
        loadedTerritorySubAreaId = subAreaId;
        territories.setValue(ApiResponse.loading(null));
        territoriesCall = api.getTerritoriesBySubArea(subAreaId);
        territoriesCall.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Territory>>> call,
                                   @NonNull Response<ApiResponse<List<Territory>>> response) {
                if (call.isCanceled() || call != territoriesCall) return;
                ApiResponse<List<Territory>> body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null) {
                    territories.postValue(ApiResponse.success(body.getData()));
                } else {
                    loadedTerritorySubAreaId = null;
                    territories.postValue(ApiResponse.error("Could not load territories. Try again.", null));
                }
                territoriesCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Territory>>> call,
                                  @NonNull Throwable error) {
                if (call.isCanceled() || call != territoriesCall) return;
                territoriesCall = null;
                loadedTerritorySubAreaId = null;
                territories.postValue(ApiResponse.error(
                        "Could not load territories. Check your connection.", null));
            }
        });
    }

    private void clearAreasAndDescendants() {
        cancel(areasCall);
        loadedAreaRegionId = null;
        areas.setValue(ApiResponse.success(Collections.emptyList()));
        clearSubAreasAndTerritories();
    }

    private void clearSubAreasAndTerritories() {
        cancel(subAreasCall);
        loadedSubAreaAreaId = null;
        subAreas.setValue(ApiResponse.success(Collections.emptyList()));
        clearTerritories();
    }

    private void clearTerritories() {
        cancel(territoriesCall);
        loadedTerritorySubAreaId = null;
        territories.setValue(ApiResponse.success(Collections.emptyList()));
    }

    private <T> boolean hasSuccessfulValue(MutableLiveData<ApiResponse<List<T>>> data) {
        ApiResponse<List<T>> value = data.getValue();
        return value != null && value.getStatus() == ApiResponse.Status.SUCCESS;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void cancel(Call<?> call) {
        if (call != null) call.cancel();
    }

    @Override
    protected void onCleared() {
        cancel(regionsCall);
        cancel(areasCall);
        cancel(subAreasCall);
        cancel(territoriesCall);
        super.onCleared();
    }
}
