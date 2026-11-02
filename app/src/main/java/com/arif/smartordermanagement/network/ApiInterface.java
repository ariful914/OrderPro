package com.arif.smartordermanagement.network;

import com.arif.smartordermanagement.model.ApiResponse;
import com.arif.smartordermanagement.model.CategorySales;
import com.arif.smartordermanagement.model.KpiMetric;
import com.arif.smartordermanagement.model.Order;
import com.arif.smartordermanagement.model.ProductInfo;
import com.arif.smartordermanagement.model.SalesInvoice;
import com.arif.smartordermanagement.model.SalesTarget;
import com.arif.smartordermanagement.model.SalesTrend;
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

    @POST(Constants.GET_PRODUCT_LIST)
    Call<ApiResponse<List<ProductInfo>>> getProductData(@Body User user);

    @POST(Constants.SUBMIT_SALES_TARGET)
    Call<ApiResponse<SalesTarget>> submitSalesTarget(@Body SalesTarget target);

    @POST(Constants.APPROVE_SALES_TARGET)
    Call<ApiResponse<SalesTarget>> approveSalesTarget(@Body ApproveTargetRequest request);

    @POST(Constants.GET_TARGET_STATUS_REPORT)
    Call<ApiResponse<List<SalesTarget>>> getTargetStatusReport(@Body User user);

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

    final class ApproveTargetRequest {
        @SerializedName("target_id")
        public final String targetId;
        @SerializedName("approved_by")
        public final String approvedBy;
        @SerializedName("remarks")
        public final String remarks;
        @SerializedName("status")
        public final String status;
        @SerializedName("items")
        public final List<ProductInfo> items;

        public ApproveTargetRequest(String targetId, String approvedBy, String remarks,
                                    String status, List<ProductInfo> items) {
            this.targetId = targetId;
            this.approvedBy = approvedBy;
            this.remarks = remarks;
            this.status = status;
            this.items = items;
        }
    }
}
