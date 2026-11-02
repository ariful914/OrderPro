package com.arif.smartordermanagement.network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Objects;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    private static final String BASE_URL = "https://aa-groupbd.com/api/";
    private static volatile Retrofit retrofit;
    private static String configuredBaseUrl;

    private RetrofitClient() {}

    @NonNull
    public static Retrofit getInstance(@NonNull String baseUrl) {
        return getOrBuild(baseUrl, null);
    }

    @NonNull
    private static Retrofit getOrBuild(@NonNull String baseUrl, Context context) {
        if (baseUrl.trim().isEmpty() || !baseUrl.endsWith("/")) {
            throw new IllegalArgumentException("API base URL must be non-empty and end with '/'.");
        }

        Retrofit instance = retrofit;
        if (instance == null) {
            synchronized (RetrofitClient.class) {
                instance = retrofit;
                if (instance == null) {
                    OkHttpClient.Builder http = new OkHttpClient.Builder();
                    if (context != null) {
                        http.addInterceptor(new NetworkLoggingInterceptor());
                        http.addInterceptor(new AuthInterceptor(context));
                    }
                    Gson gson = new GsonBuilder().create();
                    instance = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(http.build())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                    configuredBaseUrl = baseUrl;
                    retrofit = instance;
                }
            }
        }
        if (!Objects.equals(configuredBaseUrl, baseUrl)) {
            throw new IllegalStateException("RetrofitClient is already configured for a different API base URL.");
        }
        return instance;
    }

    @NonNull
    public static <T> T createAuthenticatedService(@NonNull Context context, @NonNull Class<T> serviceClass) {
        return getOrBuild(BASE_URL, context.getApplicationContext()).create(serviceClass);
    }

    @NonNull
    public static <T> T createService(@NonNull String baseUrl, @NonNull Class<T> serviceClass) {
        return getInstance(baseUrl).create(serviceClass);
    }
}
