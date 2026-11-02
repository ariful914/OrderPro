package com.arif.smartordermanagement.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

public final class NetworkLoggingInterceptor implements Interceptor {
    private static final String TAG = "OrderProNetwork";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String method = request.method();
        String url = request.url().toString();
        long startedAt = System.nanoTime();

        Log.i(TAG, "--> " + method + " " + url);
        if (request.body() != null) {
            try {
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                String bodyStr = buffer.readUtf8();
                if (!bodyStr.isEmpty()) {
                    Log.i(TAG, "--> BODY: " + bodyStr);
                }
            } catch (Exception e) {
                Log.i(TAG, "--> BODY: [Could not read request body]");
            }
        }

        try {
            Response response = chain.proceed(request);
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
            String responseBodyStr = "";
            if (response.body() != null) {
                try {
                    responseBodyStr = response.peekBody(1024 * 64).string(); // peek up to 64KB
                } catch (Exception ignored) {}
            }

            Log.i(TAG, "<-- " + response.code() + " " + method + " " + url + " (" + elapsedMs + " ms)");
            if (!responseBodyStr.isEmpty()) {
                Log.i(TAG, "<-- BODY: " + responseBodyStr);
            }
            return response;
        } catch (IOException error) {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
            Log.w(TAG, "<-- network failure " + method + " " + url
                    + " (" + elapsedMs + " ms): " + error.getMessage());
            throw error.getMessage() != null ? error : new IOException(error);
        }
    }
}
