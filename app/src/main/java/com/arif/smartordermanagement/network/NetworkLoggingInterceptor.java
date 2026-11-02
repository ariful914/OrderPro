package com.arif.smartordermanagement.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class NetworkLoggingInterceptor implements Interceptor {
    private static final String TAG = "OrderProNetwork";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String method = request.method();
        String path = request.url().encodedPath().replaceAll("/\\d+(?=/|$)", "/{id}");
        long startedAt = System.nanoTime();

        Log.i(TAG, "--> " + method + " " + path);
        try {
            Response response = chain.proceed(request);
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
            Log.i(TAG, "<-- " + response.code() + " " + method + " " + path + " (" + elapsedMs + " ms)");
            return response;
        } catch (IOException error) {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
            Log.w(TAG, "<-- network failure " + method + " " + path
                    + " (" + elapsedMs + " ms): " + error.getClass().getSimpleName());
            throw error;
        }
    }
}
