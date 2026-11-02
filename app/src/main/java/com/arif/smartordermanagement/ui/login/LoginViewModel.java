package com.arif.smartordermanagement.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arif.smartordermanagement.data.AuthRepository;
import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.arif.smartordermanagement.model.User;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<String> userId = new MutableLiveData<>("");
    private final MutableLiveData<String> password = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> rememberMe = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<User> authenticatedUser = new MutableLiveData<>(null);

    public LoginViewModel(@NonNull Application application) {
        super(application);
        PrefUtils prefs = PrefUtils.getInstance(application);
        authRepository = new AuthRepository(application);
        rememberMe.setValue(prefs.isRememberMeEnabled());
        if (Boolean.TRUE.equals(rememberMe.getValue())) {
            userId.setValue(prefs.getSavedUsername());
            password.setValue(prefs.getSavedPassword());
        }
    }

    public LiveData<String> getUserId() { return userId; }
    public void setUserId(String value) { userId.setValue(value); errorMessage.setValue(null); }
    public LiveData<String> getPassword() { return password; }
    public void setPassword(String value) { password.setValue(value); errorMessage.setValue(null); }
    public LiveData<Boolean> getRememberMe() { return rememberMe; }
    public void setRememberMe(boolean value) {
        rememberMe.setValue(value);
        PrefUtils.getInstance(getApplication()).setRememberMeEnabled(value);
    }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<User> getAuthenticatedUser() { return authenticatedUser; }

    public void performLogin() {
        if (Boolean.TRUE.equals(isLoading.getValue())) return;
        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.login(userId.getValue(), password.getValue(),
                Boolean.TRUE.equals(rememberMe.getValue()), new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        isLoading.postValue(false);
                        authenticatedUser.postValue(user);
                    }

                    @Override
                    public void onError(String error) {
                        isLoading.postValue(false);
                        errorMessage.postValue(error);
                    }
                });
    }
}
