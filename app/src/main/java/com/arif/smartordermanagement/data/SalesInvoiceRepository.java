package com.arif.smartordermanagement.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.arif.smartordermanagement.model.ApiResponse;
import com.arif.smartordermanagement.model.ReportFilter;
import com.arif.smartordermanagement.model.SalesInvoice;
import com.arif.smartordermanagement.model.User;
import com.arif.smartordermanagement.network.ApiInterface;
import com.arif.smartordermanagement.network.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

/** Loads daily invoice report data from the authenticated sales-report endpoint. */
public final class SalesInvoiceRepository {
    public interface Callback {
        void onLoaded(List<SalesInvoice> invoices);
        void onError(String message);
    }

    private final Context appContext;
    private final ApiInterface api;

    public SalesInvoiceRepository(@NonNull Context context) {
        appContext = context.getApplicationContext();
        api = RetrofitClient.createAuthenticatedService(appContext, ApiInterface.class);
    }

    public Call<ApiResponse<List<SalesInvoice>>> load(
            ReportFilter filter, Callback callback) {
        User user = PrefUtils.getInstance(appContext).getUserInfo(User.class);
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            callback.onError("User information is unavailable. Please sign in again.");
            return null;
        }

        String fromDate = apiDate(filter.getFromDate());
        String toDate = apiDate(filter.getToDate());
        if (fromDate == null || toDate == null) {
            callback.onError("Select valid from and to dates.");
            return null;
        }

        ApiInterface.SalesReportRequest request = new ApiInterface.SalesReportRequest(
                user.getUserId(), user.getUsername(),
                selectedId(filter.getArea() != null ? filter.getArea().getId() : null),
                fromDate,
                selectedId(filter.getRegion() != null ? filter.getRegion().getId() : null),
                selectedId(filter.getSubArea() != null ? filter.getSubArea().getId() : null),
                selectedId(filter.getTerritory() != null ? filter.getTerritory().getId() : null),
                toDate,
                filter.getZone() != null ? selectedId(filter.getZone().getId())
                        : user.getZoneId() != null ? user.getZoneId() : 0,
                user.getDepotId() != null ? user.getDepotId() : 0,
                0);

        Call<ApiResponse<List<SalesInvoice>>> call = api.getSalesReport(request);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<SalesInvoice>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SalesInvoice>>> call,
                                   @NonNull Response<ApiResponse<List<SalesInvoice>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    callback.onLoaded(response.body().getData());
                } else {
                    callback.onError("Could not load invoices. Check your connection and try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SalesInvoice>>> call,
                                  @NonNull Throwable error) {
                if (!call.isCanceled()) {
                    callback.onError("Could not load invoices. Check your connection and try again.");
                }
            }
        });
        return call;
    }

    private int selectedId(String id) {
        if (id == null || id.trim().isEmpty() || "ALL".equalsIgnoreCase(id)) return 0;
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String apiDate(String displayDate) {
        try {
            java.util.Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(displayDate);
            return date == null ? null : new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
        } catch (ParseException | NullPointerException ignored) {
            return null;
        }
    }
}
