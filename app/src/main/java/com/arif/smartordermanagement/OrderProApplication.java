package com.arif.smartordermanagement;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.arif.smartordermanagement.data.storage.PrefUtils;

/** Applies the user's display preference before any activity is created. */
public class OrderProApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(PrefUtils.getInstance(this).getThemeMode());
    }
}
