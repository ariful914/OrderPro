package com.arif.smartordermanagement;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

/**
 * Base for report fragments that need to share applied filter selections.
 * The Activity scope keeps the filter available as report destinations change.
 */
public abstract class BaseFilterFragment extends Fragment {
    protected FilterSharedViewModel filterViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filterViewModel = new ViewModelProvider(
                requireActivity(), new FilterSharedViewModelFactory(requireActivity().getApplication()))
                .get(FilterSharedViewModel.class);
    }
}
