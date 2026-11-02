package com.arif.smartordermanagement.ui.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.arif.smartordermanagement.databinding.FragmentSplashBinding;
import com.arif.smartordermanagement.ui.dashboard.DashboardFragment;
import com.arif.smartordermanagement.ui.login.LoginFragment;

public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable navigateRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded()) {
                return;
            }

            androidx.fragment.app.FragmentManager fragmentManager = getParentFragmentManager();
            if (!isResumed() || fragmentManager.isStateSaved()) {
                handler.postDelayed(this, 250);
                return;
            }

            PrefUtils preferences = PrefUtils.getInstance(requireContext());
            boolean hasValidSession = preferences.hasValidSession();
            if (preferences.isLoggedIn() && !hasValidSession) {
                preferences.clearSession();
            }
            Fragment destination = hasValidSession
                    ? new DashboardFragment()
                    : new LoginFragment();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.screen_enter, R.anim.screen_exit, R.anim.screen_pop_enter, R.anim.screen_pop_exit)
                    .replace(R.id.fragment_container, destination)
                    .commit();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Post 1.2s delay to transition to LoginFragment
        handler.postDelayed(navigateRunnable, 1200);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(navigateRunnable);
        binding = null;
    }
}
