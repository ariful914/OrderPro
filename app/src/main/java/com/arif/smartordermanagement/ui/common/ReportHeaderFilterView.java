package com.arif.smartordermanagement.ui.common;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arif.smartordermanagement.data.LocationDataStore;
import com.arif.smartordermanagement.databinding.LayoutReportHeaderFilterBinding;
import com.arif.smartordermanagement.model.Area;
import com.arif.smartordermanagement.model.Region;
import com.arif.smartordermanagement.model.ReportFilter;
import com.arif.smartordermanagement.model.Territory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportHeaderFilterView extends FrameLayout {

    public interface OnSearchClickListener {
        void onSearch(ReportFilter filter);
    }

    private LayoutReportHeaderFilterBinding binding;
    private final Calendar fromCalendar = Calendar.getInstance();
    private final Calendar toCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private OnSearchClickListener onSearchClickListener;
    private ArrayAdapter<Region> regionAdapter;
    private ArrayAdapter<Area> areaAdapter;
    private ArrayAdapter<Territory> territoryAdapter;

    public ReportHeaderFilterView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ReportHeaderFilterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReportHeaderFilterView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = LayoutReportHeaderFilterBinding.inflate(LayoutInflater.from(context), this, true);
        setupDatePickers(context);
        setupSpinners(context);
        setupSearchButton();
    }

    private void setupDatePickers(Context context) {
        // Default From Date to today
        binding.etFromDate.setText(dateFormat.format(fromCalendar.getTime()));
        // Default To Date to today
        binding.etToDate.setText(dateFormat.format(toCalendar.getTime()));

        binding.etFromDate.setOnClickListener(v -> showDatePicker(context, fromCalendar, (view, year, month, dayOfMonth) -> {
            fromCalendar.set(Calendar.YEAR, year);
            fromCalendar.set(Calendar.MONTH, month);
            fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.etFromDate.setText(dateFormat.format(fromCalendar.getTime()));
        }));

        binding.etToDate.setOnClickListener(v -> showDatePicker(context, toCalendar, (view, year, month, dayOfMonth) -> {
            toCalendar.set(Calendar.YEAR, year);
            toCalendar.set(Calendar.MONTH, month);
            toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.etToDate.setText(dateFormat.format(toCalendar.getTime()));
        }));
    }

    private void showDatePicker(Context context, Calendar calendar, DatePickerDialog.OnDateSetListener listener) {
        new DatePickerDialog(
                context,
                listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void setupSpinners(Context context) {
        List<Region> regions = LocationDataStore.getRegions();
        regionAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, regions);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRegion.setAdapter(regionAdapter);

        binding.spinnerRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Region selectedRegion = regionAdapter.getItem(position);
                updateAreas(context, selectedRegion != null ? selectedRegion.getId() : "ALL");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Area selectedArea = areaAdapter != null ? areaAdapter.getItem(position) : null;
                updateTerritories(context, selectedArea != null ? selectedArea.getId() : "ALL");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateAreas(context, "ALL");
    }

    private void updateAreas(Context context, String regionId) {
        List<Area> areas = LocationDataStore.getAreasForRegion(regionId);
        areaAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, areas);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerArea.setAdapter(areaAdapter);

        Area selectedArea = areas.get(0);
        updateTerritories(context, selectedArea.getId());
    }

    private void updateTerritories(Context context, String areaId) {
        List<Territory> territories = LocationDataStore.getTerritoriesForArea(areaId);
        territoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, territories);
        territoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTerritory.setAdapter(territoryAdapter);
    }

    private void setupSearchButton() {
        binding.btnSearchReport.setOnClickListener(v -> {
            if (onSearchClickListener != null) {
                onSearchClickListener.onSearch(getFilter());
            }
        });
    }

    public ReportFilter getFilter() {
        String fromDate = binding.etFromDate.getText() != null ? binding.etFromDate.getText().toString() : "";
        String toDate = binding.etToDate.getText() != null ? binding.etToDate.getText().toString() : "";

        Region region = (Region) binding.spinnerRegion.getSelectedItem();
        Area area = (Area) binding.spinnerArea.getSelectedItem();
        Territory territory = (Territory) binding.spinnerTerritory.getSelectedItem();

        return new ReportFilter(fromDate, toDate, region, area, territory);
    }

    public void setOnSearchClickListener(OnSearchClickListener listener) {
        this.onSearchClickListener = listener;
    }
}
