package com.arif.smartordermanagement.network;

import com.arif.smartordermanagement.model.ApiResponse;
import com.arif.smartordermanagement.model.CategorySales;
import com.arif.smartordermanagement.model.KpiMetric;
import com.arif.smartordermanagement.model.Order;
import com.arif.smartordermanagement.model.SalesTrend;
import com.arif.smartordermanagement.model.SalesInvoice;
import com.arif.smartordermanagement.model.User;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @POST("sign-in")
    Call<ApiResponse<User>> login(@Body LoginRequest request);

    @GET("dashboard/kpis")
    Call<ApiResponse<List<KpiMetric>>> getKpiMetrics(@Query("timeFrame") String timeFrame);

    @GET("dashboard/sales-trend")
    Call<ApiResponse<SalesTrend>> getSalesTrend(@Query("timeFrame") String timeFrame);

    @GET("dashboard/category-breakdown")
    Call<ApiResponse<List<CategorySales>>> getCategoryBreakdown(@Query("timeFrame") String timeFrame);

    @GET("orders")
    Call<ApiResponse<List<Order>>> getOrders(
            @Query("query") String query,
            @Query("status") String status
    );

    @POST("orders")
    Call<ApiResponse<Order>> createOrder(@Body CreateOrderRequest request);

    @POST("sales-report/sales")
    Call<ApiResponse<List<SalesInvoice>>> getSalesReport(@Body SalesReportRequest request);

    final class SalesReportRequest {
        @SerializedName("user_id") public final int userId;
        @SerializedName("username") public final String username;
        @SerializedName("area_id") public final int areaId;
        @SerializedName("fromDate") public final String fromDate;
        @SerializedName("region_id") public final int regionId;
        @SerializedName("sub_area_id") public final int subAreaId;
        @SerializedName("territory_id") public final int territoryId;
        @SerializedName("toDate") public final String toDate;
        @SerializedName("zone_id") public final int zoneId;
        @SerializedName("depot_id") public final int depotId;
        @SerializedName("id") public final int id;

        public SalesReportRequest(int userId, String username, int areaId, String fromDate,
                                  int regionId, int subAreaId, int territoryId, String toDate,
                                  int zoneId, int depotId, int id) {
            this.userId = userId;
            this.username = username;
            this.areaId = areaId;
            this.fromDate = fromDate;
            this.regionId = regionId;
            this.subAreaId = subAreaId;
            this.territoryId = territoryId;
            this.toDate = toDate;
            this.zoneId = zoneId;
            this.depotId = depotId;
            this.id = id;
        }
    }

    final class LoginRequest {
        @SerializedName("username")
        public final String username;
        @SerializedName("password")
        public final String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    final class CreateOrderRequest {
        public final String customerName;
        public final List<OrderLineRequest> items;

        public CreateOrderRequest(String customerName, List<OrderLineRequest> items) {
            this.customerName = customerName;
            this.items = items;
        }
    }

    final class OrderLineRequest {
        public final String productSku;
        public final int quantity;

        public OrderLineRequest(String productSku, int quantity) {
            this.productSku = productSku;
            this.quantity = quantity;
        }
    }
}
