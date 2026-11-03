package com.arif.smartordermanagement;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/** Creates the shared report filter state holder. */
public final class FilterSharedViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public FilterSharedViewModelFactory(@NonNull Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FilterSharedViewModel.class)) {
            //noinspection unchecked
            return (T) new FilterSharedViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
