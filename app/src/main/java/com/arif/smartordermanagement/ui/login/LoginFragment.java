package com.arif.smartordermanagement.ui.login;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.databinding.FragmentLoginBinding;
import com.arif.smartordermanagement.ui.dashboard.DashboardFragment;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;
    private View[] entranceViews;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupTextListeners();
        setupClickListeners();
        observeViewModel();
        animateEntrance();
    }

    private void animateEntrance() {
        entranceViews = new View[]{
                binding.loginBrand,
                binding.loginHeading,
                binding.loginSubtitle,
                binding.layoutEmail,
                binding.layoutPassword,
                binding.checkRememberMe,
                binding.signInContainer,
                binding.loginFooter
        };

        long startDelay = 0L;
        for (View animatedView : entranceViews) {
            animatedView.setAlpha(0f);
            animatedView.setTranslationY(18f);
            animatedView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(startDelay)
                    .setDuration(360L)
                    .setInterpolator(new DecelerateInterpolator(1.6f))
                    .start();
            startDelay += 55L;
        }
    }

    private void setupTextListeners() {
        binding.etEmail.setText(viewModel.getUserId().getValue());
        binding.etPassword.setText(viewModel.getPassword().getValue());
        binding.checkRememberMe.setChecked(Boolean.TRUE.equals(viewModel.getRememberMe().getValue()));

        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setUserId(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setPassword(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        binding.etEmail.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etPassword.requestFocus();
                return true;
            }
            return false;
        });

        binding.checkRememberMe.setOnCheckedChangeListener((button, checked) -> viewModel.setRememberMe(checked));

        binding.etPassword.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitLogin();
                return true;
            }
            return false;
        });

        binding.btnSignIn.setOnClickListener(v -> submitLogin());
    }

    private void submitLogin() {
        if (binding == null) {
            return;
        }

        InputMethodManager inputMethodManager =
                (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        }
        viewModel.performLogin();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            binding.btnSignIn.setEnabled(!loading);
            binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnSignIn.setText(loading ? "" : getString(R.string.login_title));
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.cardError.setVisibility(View.VISIBLE);
                binding.tvErrorMessage.setText(error);
            } else {
                binding.cardError.setVisibility(View.GONE);
            }
        });

        viewModel.getAuthenticatedUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.screen_enter, R.anim.screen_exit, R.anim.screen_pop_enter, R.anim.screen_pop_exit)
                        .replace(R.id.fragment_container, new DashboardFragment())
                        .commit();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (entranceViews != null) {
            for (View animatedView : entranceViews) {
                animatedView.animate().cancel();
            }
            entranceViews = null;
        }
        super.onDestroyView();
        binding = null;
    }
}
