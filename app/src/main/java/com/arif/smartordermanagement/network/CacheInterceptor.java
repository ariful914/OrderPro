package com.arif.smartordermanagement.network;

import android.content.Context;

import androidx.annotation.NonNull;


import com.arif.smartordermanagement.util.NetworkUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CacheInterceptor implements Interceptor {

    private final Context context;

    public CacheInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        boolean isGet = "GET".equalsIgnoreCase(request.method());

        if (isGet && !NetworkUtils.isConnectingToInternet(context)) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }

        Response response = chain.proceed(request);

        if (isGet) {
            CacheControl cacheControl = NetworkUtils.isConnectingToInternet(context)
                    ? new CacheControl.Builder().maxAge(5, TimeUnit.MINUTES).build()
                    : new CacheControl.Builder().onlyIfCached().maxStale(7, TimeUnit.DAYS).build();

            return response.newBuilder()
                    .header("Cache-Control", cacheControl.toString())
                    .removeHeader("Pragma")
                    .build();
        }

        return response;
    }
}
