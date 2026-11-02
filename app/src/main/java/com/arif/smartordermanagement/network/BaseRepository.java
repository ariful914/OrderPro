package com.arif.smartordermanagement.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;


import com.arif.smartordermanagement.model.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseRepository {

    private static final int MAX_RETRIES = 2;
    private static final Gson GSON = new GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create();

    protected <T> MutableLiveData<ApiResponse<T>> handleApiCall(Call<ApiResponse<T>> call) {
        MutableLiveData<ApiResponse<T>> liveData = new MutableLiveData<>();
        liveData.postValue(ApiResponse.loading(null));
        executeWithRetry(call, liveData, 0);
        return liveData;
    }

    private <T> void executeWithRetry(
            Call<ApiResponse<T>> call,
            MutableLiveData<ApiResponse<T>> liveData,
            int attempt
    ) {
        call.clone().enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<T>> call,
                    @NonNull Response<ApiResponse<T>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                    return;
                }

                String error = parseError(response);

                if (shouldRetry(attempt, response)) {
                    retryWithBackoff(call, liveData, attempt + 1);
                } else {
                    liveData.postValue(ApiResponse.error(error, null));
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse<T>> call,
                    @NonNull Throwable t) {
                String error = (t.getMessage() != null) ? t.getMessage() : "Network Error";
                if (shouldRetry(attempt, null)) {
                    retryWithBackoff(call, liveData, attempt + 1);
                } else {
                    liveData.postValue(ApiResponse.error(error, null));
                }
            }
        });
    }

    private boolean shouldRetry(int attempt, Response<?> response) {
        if (attempt >= MAX_RETRIES) return false;
        if (response == null) return true;
        int code = response.code();
        return code >= 500 || code == 408 || code == 429;
    }

    private <T> void retryWithBackoff(
            Call<ApiResponse<T>> call,
            MutableLiveData<ApiResponse<T>> liveData,
            int nextAttempt
    ) {
        long delay = 500L * nextAttempt * nextAttempt;
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> executeWithRetry(call, liveData, nextAttempt),
                delay
        );
    }

    /**
     * Safe error parser with lenient JSON support
     */
    private String parseError(Response<?> response) {
        try (ResponseBody body = response.errorBody()) {
            if (body == null) {
                return "Server Error (" + response.code() + ")";
            }

            String raw = body.string().trim();
            if (raw.isEmpty()) {
                return "Empty Error (" + response.code() + ")";
            }

            try {
                // Use Gson instance with lenient strictness
                JsonElement element = GSON.fromJson(raw, JsonElement.class);

                if (element != null && element.isJsonObject()) {
                    JsonObject json = element.getAsJsonObject();
                    if (json.has("ERROR")) return json.get("ERROR").getAsString();
                    if (json.has("message")) return json.get("message").getAsString();
                } else if (element != null && element.isJsonPrimitive()) {
                    return element.getAsString();
                }
            } catch (Exception e) {
                // Return raw string if short, else generic error
                if (raw.length() < 120) return raw;
            }
            return "Server Error (" + response.code() + ")";
        } catch (Exception e) {
            Log.e("BaseRepository", "parseError failed", e);
            return "Unknown Error (" + response.code() + ")";
        }
    }
}
