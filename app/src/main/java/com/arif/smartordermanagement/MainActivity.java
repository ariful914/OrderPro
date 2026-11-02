package com.arif.smartordermanagement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.arif.smartordermanagement.data.SessionManager;
import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.arif.smartordermanagement.databinding.ActivityMainBinding;
import com.arif.smartordermanagement.ui.login.LoginFragment;
import com.arif.smartordermanagement.ui.splash.SplashFragment;

public class MainActivity extends AppCompatActivity {
    private final Handler sessionHandler = new Handler(Looper.getMainLooper());
    private final Runnable sessionExpiryRunnable = () -> {
        PrefUtils prefs = PrefUtils.getInstance(this);
        if (prefs.isLoggedIn() && !prefs.hasValidSession()) {
            SessionManager.expireSession(this);
        } else {
            scheduleSessionExpiry();
        }
    };
    private boolean sessionReceiverRegistered;

    private final BroadcastReceiver sessionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SessionManager.ACTION_SESSION_EXPIRED.equals(intent.getAction())) {
                sessionHandler.removeCallbacks(sessionExpiryRunnable);
                showLogin();
            } else if (SessionManager.ACTION_SESSION_UPDATED.equals(intent.getAction())) {
                scheduleSessionExpiry();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        int initialLeft = root.getPaddingLeft();
        int initialTop = root.getPaddingTop();
        int initialRight = root.getPaddingRight();
        int initialBottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets safeInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(
                    initialLeft + safeInsets.left,
                    initialTop + safeInsets.top,
                    initialRight + safeInsets.right,
                    initialBottom + safeInsets.bottom);
            return new WindowInsetsCompat.Builder(windowInsets)
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.NONE)
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.NONE)
                    .build();
        });

        setContentView(root);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), root);
        boolean isDarkMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        insetsController.setAppearanceLightStatusBars(!isDarkMode);
        insetsController.setAppearanceLightNavigationBars(!isDarkMode);
        ViewCompat.requestApplyInsets(root);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SplashFragment())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SessionManager.ACTION_SESSION_EXPIRED);
        filter.addAction(SessionManager.ACTION_SESSION_UPDATED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(sessionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            androidx.core.content.ContextCompat.registerReceiver(
                    this,
                    sessionReceiver,
                    filter,
                    androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);
        }
        sessionReceiverRegistered = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrefUtils prefs = PrefUtils.getInstance(this);
        if (prefs.isLoggedIn() && !prefs.hasValidSession()) {
            SessionManager.expireSession(this);
        } else {
            scheduleSessionExpiry();
        }
    }

    @Override
    protected void onPause() {
        sessionHandler.removeCallbacks(sessionExpiryRunnable);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (sessionReceiverRegistered) {
            unregisterReceiver(sessionReceiver);
            sessionReceiverRegistered = false;
        }
        super.onStop();
    }

    private void scheduleSessionExpiry() {
        sessionHandler.removeCallbacks(sessionExpiryRunnable);
        PrefUtils prefs = PrefUtils.getInstance(this);
        if (!prefs.isLoggedIn()) return;
        long remaining = prefs.getSessionExpiresAt() - System.currentTimeMillis();
        if (remaining <= 0L) {
            SessionManager.expireSession(this);
        } else {
            sessionHandler.postDelayed(sessionExpiryRunnable, remaining);
        }
    }

    private void showLogin() {
        FragmentManager manager = getSupportFragmentManager();
        if (isFinishing() || manager.isStateSaved()) return;
        Fragment current = manager.findFragmentById(R.id.fragment_container);
        if (current instanceof LoginFragment) return;
        manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        manager.beginTransaction()
                .setCustomAnimations(R.anim.screen_enter, R.anim.screen_exit, R.anim.screen_pop_enter, R.anim.screen_pop_exit)
                .replace(R.id.fragment_container, new LoginFragment())
                .commitAllowingStateLoss();
    }
}
