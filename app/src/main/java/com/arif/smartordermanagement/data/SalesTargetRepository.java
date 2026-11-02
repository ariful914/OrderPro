package com.arif.smartordermanagement.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.arif.smartordermanagement.model.ApiResponse;
import com.arif.smartordermanagement.model.ProductInfo;
import com.arif.smartordermanagement.model.SalesTarget;
import com.arif.smartordermanagement.model.User;
import com.arif.smartordermanagement.network.ApiInterface;
import com.arif.smartordermanagement.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Repository coordinating sales target network requests with local storage and fallbacks.
 */
public class SalesTargetRepository {

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final Context appContext;
    private final ApiInterface api;
    private final SalesTargetStore targetStore;

    public SalesTargetRepository(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.targetStore = SalesTargetStore.getInstance(appContext);
        this.api = RetrofitClient.createAuthenticatedService(appContext, ApiInterface.class);
    }

    /**
     * Fetches product list for target submission and quantity editing.
     * Invokes: @POST(Constants.GET_PRODUCT_LIST)
     *          Call<ApiResponse<List<ProductInfo>>> getProductData(@Body User user);
     */
    public Call<ApiResponse<List<ProductInfo>>> getProductData(User user,
                                                               @NonNull RepositoryCallback<List<ProductInfo>> callback) {
        User info = user != null ? user : getCurrentUser();
        info.setDepotId(2);
        Call<ApiResponse<List<ProductInfo>>> call = api.getProductData(info);
        call.enqueue(new retrofit2.Callback<ApiResponse<List<ProductInfo>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ProductInfo>>> call,
                                   @NonNull Response<ApiResponse<List<ProductInfo>>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null
                        && !response.body().getData().isEmpty()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    // Graceful fallback to default product catalog for reliable offline / demo operation
                    callback.onSuccess(ProductInfo.getDefaultProducts());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ProductInfo>>> call, @NonNull Throwable t) {
                // If network fails, serve local catalog so user can continue editing targets
                callback.onSuccess(ProductInfo.getDefaultProducts());
            }
        });
        return call;
    }

    /** Submits a newly created sales target for senior approval. */
    public void submitSalesTarget(SalesTarget target, @NonNull RepositoryCallback<SalesTarget> callback) {
        // Persist locally first so user immediately sees it in status report
        SalesTarget saved = targetStore.submitTarget(target);

        // Also fire background network sync
        try {
            api.submitSalesTarget(saved).enqueue(new retrofit2.Callback<ApiResponse<SalesTarget>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<SalesTarget>> call,
                                       @NonNull Response<ApiResponse<SalesTarget>> response) {
                    // Network sync completed
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<SalesTarget>> call, @NonNull Throwable t) {
                    // Local copy already saved
                }
            });
        } catch (Exception ignored) {}

        callback.onSuccess(saved);
    }

    /** Approves a target with optional senior adjustments to product quantities. */
    public void seniorApproveTarget(String targetId, String seniorName, String remarks,
                                    List<ProductInfo> editedProducts,
                                    @NonNull RepositoryCallback<SalesTarget> callback) {
        SalesTarget approved = targetStore.seniorApproveTarget(targetId, seniorName, remarks, editedProducts);
        if (approved == null) {
            callback.onError("Target not found with ID: " + targetId);
            return;
        }

        try {
            ApiInterface.ApproveTargetRequest req = new ApiInterface.ApproveTargetRequest(
                    targetId, seniorName, remarks, "Approved", editedProducts);
            api.approveSalesTarget(req).enqueue(new retrofit2.Callback<ApiResponse<SalesTarget>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<SalesTarget>> call,
                                       @NonNull Response<ApiResponse<SalesTarget>> response) {}
                @Override
                public void onFailure(@NonNull Call<ApiResponse<SalesTarget>> call, @NonNull Throwable t) {}
            });
        } catch (Exception ignored) {}

        callback.onSuccess(approved);
    }

    /** Rejects a submitted target with senior remarks. */
    public void seniorRejectTarget(String targetId, String seniorName, String remarks,
                                   @NonNull RepositoryCallback<SalesTarget> callback) {
        SalesTarget rejected = targetStore.seniorRejectTarget(targetId, seniorName, remarks);
        if (rejected == null) {
            callback.onError("Target not found with ID: " + targetId);
            return;
        }
        callback.onSuccess(rejected);
    }

    /** Loads list of all targets for the Target Status Report. */
    public void getTargetStatusReport(User user, @NonNull RepositoryCallback<List<SalesTarget>> callback) {
        // Return current stored targets
        List<SalesTarget> list = targetStore.getTargets();
        callback.onSuccess(list);

        // Optionally query network for updates
        try {
            User info = user != null ? user : getCurrentUser();
            api.getTargetStatusReport(info).enqueue(new retrofit2.Callback<ApiResponse<List<SalesTarget>>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<List<SalesTarget>>> call,
                                       @NonNull Response<ApiResponse<List<SalesTarget>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        for (SalesTarget remote : response.body().getData()) {
                            targetStore.submitTarget(remote);
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ApiResponse<List<SalesTarget>>> call, @NonNull Throwable t) {}
            });
        } catch (Exception ignored) {}
    }

    public User getCurrentUser() {
        return PrefUtils.getInstance(appContext).getUserInfo(User.class);
    }
}
