package com.arif.smartordermanagement.data;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.arif.smartordermanagement.data.storage.PrefUtils;

public final class SessionManager {
    public static final String ACTION_SESSION_EXPIRED = "com.arif.smartordermanagement.SESSION_EXPIRED";
    public static final String ACTION_SESSION_UPDATED = "com.arif.smartordermanagement.SESSION_UPDATED";

    private SessionManager() {}

    public static void notifySessionUpdated(@NonNull Context context) {
        send(context, ACTION_SESSION_UPDATED);
    }

    public static void expireSession(@NonNull Context context) {
        PrefUtils.getInstance(context).clearSession();
        send(context, ACTION_SESSION_EXPIRED);
    }

    private static void send(Context context, String action) {
        Intent intent = new Intent(action).setPackage(context.getPackageName());
        context.getApplicationContext().sendBroadcast(intent);
    }
}
