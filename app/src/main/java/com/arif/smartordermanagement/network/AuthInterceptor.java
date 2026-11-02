package com.arif.smartordermanagement.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.arif.smartordermanagement.data.SessionManager;
import com.arif.smartordermanagement.data.storage.PrefUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthInterceptor implements Interceptor {
    private final Context appContext;
    private final PrefUtils preferences;

    public AuthInterceptor(@NonNull Context context) {
        appContext = context.getApplicationContext();
        preferences = PrefUtils.getInstance(appContext);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();
        boolean isLoginRequest = path.endsWith("/sign-in") || path.endsWith("/auth/login");

        Request.Builder requestBuilder = request.newBuilder()
                .header("Accept", "application/json");

        if (!isLoginRequest && preferences.isLoggedIn()) {
            if (!preferences.hasValidSession()) {
                SessionManager.expireSession(appContext);
                throw new IOException("Your session has expired. Please sign in again.");
            }
            requestBuilder.header("Authorization", "Bearer " + preferences.getToken());
        }

        Response response = chain.proceed(requestBuilder.build());
        if (!isLoginRequest && (response.code() == 401 || response.code() == 419)) {
            SessionManager.expireSession(appContext);
            return response;
        }
        return response;
    }
}
