package com.arif.smartordermanagement.ui.common;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.arif.smartordermanagement.FilterSharedViewModel;
import com.arif.smartordermanagement.R;
import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.arif.smartordermanagement.databinding.LayoutReportHeaderFilterBinding;
import com.arif.smartordermanagement.model.ApiResponse;
import com.arif.smartordermanagement.model.Area;
import com.arif.smartordermanagement.model.Region;
import com.arif.smartordermanagement.model.ReportFilter;
import com.arif.smartordermanagement.model.SubArea;
import com.arif.smartordermanagement.model.Territory;
import com.arif.smartordermanagement.model.User;
import com.arif.smartordermanagement.model.Zone;
import com.arif.smartordermanagement.util.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Shared date and role-aware location filters for report screens. */
public class ReportHeaderFilterView extends FrameLayout {
    public interface OnSearchClickListener {
        void onSearch(ReportFilter filter);
    }

    private final Calendar fromCalendar = Calendar.getInstance();
    private final Calendar toCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private final Observer<ApiResponse<List<Region>>> regionsObserver = this::bindRegions;
    private final Observer<ApiResponse<List<Area>>> areasObserver = this::bindAreas;
    private final Observer<ApiResponse<List<SubArea>>> subAreasObserver = this::bindSubAreas;
    private final Observer<ApiResponse<List<Territory>>> territoriesObserver = this::bindTerritories;

    private LayoutReportHeaderFilterBinding binding;
    private OnSearchClickListener onSearchClickListener;
    private FilterSharedViewModel sharedViewModel;
    private ArrayAdapter<Region> regionAdapter;
    private ArrayAdapter<Area> areaAdapter;
    private ArrayAdapter<SubArea> subAreaAdapter;
    private ArrayAdapter<Territory> territoryAdapter;
    private User user;
    private String designation = "";
    private String assignedRegionId = "";
    private String assignedAreaId = "";
    private String assignedSubAreaId = "";
    private String assignedTerritoryId = "";
    private Zone assignedZone;
    private Region selectedRegion;
    private Area selectedArea;
    private SubArea selectedSubArea;
    private boolean restoringSelection;
    private boolean observing;
    private boolean filtersExpanded = true;

    public ReportHeaderFilterView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ReportHeaderFilterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReportHeaderFilterView(@NonNull Context context, @Nullable AttributeSet attrs,
                                  int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = LayoutReportHeaderFilterBinding.inflate(LayoutInflater.from(context), this, true);
        setupDatePickers(context);
        setupSpinners(context);
        binding.filterHeader.setOnClickListener(v -> setFiltersExpanded(!filtersExpanded));
        setFiltersExpanded(true);
        binding.btnSearchReport.setOnClickListener(v -> {
            ReportFilter filter = getFilter();
            if (sharedViewModel != null) sharedViewModel.setCurrentFilter(filter);
            if (onSearchClickListener != null) onSearchClickListener.onSearch(filter);
        });
    }

    private void setFiltersExpanded(boolean expanded) {
        filtersExpanded = expanded;
        binding.filterContent.setVisibility(expanded ? View.VISIBLE : View.GONE);
        binding.filterExpandIcon.animate().cancel();
        binding.filterExpandIcon.animate()
                .rotation(expanded ? 180f : 0f)
                .setDuration(180)
                .start();
        binding.filterHeader.setContentDescription(getContext().getString(
                expanded ? R.string.daily_invoice_filters_expanded
                        : R.string.daily_invoice_filters_collapsed));
    }

    private void setupDatePickers(Context context) {
        binding.etFromDate.setText(dateFormat.format(fromCalendar.getTime()));
        binding.etToDate.setText(dateFormat.format(toCalendar.getTime()));
        binding.etFromDate.setOnClickListener(v ->
                showDatePicker(context, fromCalendar, binding.etFromDate));
        binding.etToDate.setOnClickListener(v ->
                showDatePicker(context, toCalendar, binding.etToDate));
    }

    private void showDatePicker(Context context, Calendar calendar,
                                android.widget.TextView target) {
        new DatePickerDialog(context, (picker, year, month, day) -> {
            calendar.set(year, month, day);
            target.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupSpinners(Context context) {
        regionAdapter = createAdapter(context, new ArrayList<>(), "R", R.color.report_region_badge);
        areaAdapter = createAdapter(context, new ArrayList<>(), "A", R.color.report_area_badge);
        subAreaAdapter = createAdapter(context, new ArrayList<>(), "S", R.color.report_sub_area_badge);
        territoryAdapter = createAdapter(context, new ArrayList<>(), "T", R.color.report_territory_badge);
        binding.spinnerRegion.setAdapter(regionAdapter);
        binding.spinnerArea.setAdapter(areaAdapter);
        binding.spinnerSubArea.setAdapter(subAreaAdapter);
        binding.spinnerTerritory.setAdapter(territoryAdapter);

        binding.spinnerRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (restoringSelection) return;
                selectedRegion = regionAdapter.getItem(position);
                loadAreasForSelection();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (restoringSelection) return;
                selectedArea = areaAdapter.getItem(position);
                loadSubAreasForSelection();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spinnerSubArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (restoringSelection) return;
                selectedSubArea = subAreaAdapter.getItem(position);
                loadTerritoriesForSelection();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private <T> ArrayAdapter<T> createAdapter(Context context, List<T> items,
                                             String badge, int badgeColor) {
        return new LocationSpinnerAdapter<>(context, items, badge, badgeColor);
    }

    /** Connects this filter view to report-shared state and async location lookups. */
    public void setSharedViewModel(@Nullable FilterSharedViewModel viewModel) {
        detachObservers();
        sharedViewModel = viewModel;
        if (viewModel == null) return;

        user = PrefUtils.getInstance(getContext()).getUserInfo(User.class);
        designation = user != null && user.getDesignationName() != null
                ? user.getDesignationName().trim() : "";
        assignedRegionId = idOf(user != null ? user.getRegionId() : null);
        assignedAreaId = idOf(user != null ? user.getAreaId() : null);
        assignedSubAreaId = idOf(user != null ? user.getSubAreaId() : null);
        assignedTerritoryId = idOf(user != null ? user.getTerritoryId() : null);
        assignedZone = user != null && user.getZoneId() != null
                ? new Zone(String.valueOf(user.getZoneId()), "Assigned zone") : null;

        applyRoleVisibility();
        viewModel.getRegions().observeForever(regionsObserver);
        viewModel.getAreas().observeForever(areasObserver);
        viewModel.getSubAreas().observeForever(subAreasObserver);
        viewModel.getTerritories().observeForever(territoriesObserver);
        observing = true;

        ReportFilter saved = viewModel.getCurrentFilter();
        if (saved != null) {
            binding.etFromDate.setText(saved.getFromDate());
            binding.etToDate.setText(saved.getToDate());
            restoreCalendar(fromCalendar, saved.getFromDate());
            restoreCalendar(toCalendar, saved.getToDate());
            selectedRegion = saved.getRegion();
            selectedArea = saved.getArea();
            selectedSubArea = saved.getSubArea();
        }

        if (isDesignation(Constants.PA)) return;
        String zoneId = saved != null && saved.getZone() != null
                ? saved.getZone().getId() : assignedZone != null ? assignedZone.getId() : "";
        if (!zoneId.isEmpty()) viewModel.loadRegions(zoneId, false);
        else if (!assignedRegionId.isEmpty()) viewModel.loadAreas(assignedRegionId, false);
        else if (!assignedAreaId.isEmpty()) viewModel.loadSubAreas(assignedAreaId, false);
        else if (!assignedSubAreaId.isEmpty()) viewModel.loadTerritories(assignedSubAreaId, false);
    }

    private void applyRoleVisibility() {
        boolean regionVisible = isExecutive() || isDesignation(Constants.NSM);
        boolean areaVisible = isExecutive() || isDesignation(Constants.NSM)
                || isDesignation(Constants.RSM);
        boolean territoryVisible = !isDesignation(Constants.PA);

        binding.regionFilterContainer.setVisibility(regionVisible ? View.VISIBLE : View.GONE);
        binding.areaFilterContainer.setVisibility(areaVisible ? View.VISIBLE : View.GONE);
        binding.subAreaFilterContainer.setVisibility(areaVisible ? View.VISIBLE : View.GONE);
        binding.territoryFilterContainer.setVisibility(territoryVisible ? View.VISIBLE : View.GONE);
        binding.btnSearchReport.setVisibility(View.VISIBLE);

        if (regionVisible && areaVisible) {
            setLocationFieldWeight(binding.regionFilterContainer, 1f);
            setLocationFieldWeight(binding.areaFilterContainer, 1f);
        } else if (regionVisible) {
            setLocationFieldWeight(binding.regionFilterContainer, 0f);
        } else if (areaVisible) {
            setLocationFieldWeight(binding.areaFilterContainer, 0f);
        } else {
            binding.regionAreaRow.setVisibility(View.GONE);
        }
    }

    private void setLocationFieldWeight(View field, float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) field.getLayoutParams();
        params.width = weight == 0f ? LayoutParams.MATCH_PARENT : 0;
        params.weight = weight;
        params.setMarginStart(0);
        params.setMarginEnd(0);
        field.setLayoutParams(params);
    }

    private boolean isExecutive() {
        return isDesignation(Constants.CEO) || isDesignation(Constants.ADMIN)
                || isDesignation(Constants.PMD);
    }

    private boolean isDesignation(String expected) {
        return expected != null && expected.equalsIgnoreCase(designation);
    }

    private void bindRegions(ApiResponse<List<Region>> response) {
        if (response == null) return;
        if (response.getStatus() == ApiResponse.Status.ERROR) {
            showLookupError(response);
            return;
        }
        if (response.getStatus() != ApiResponse.Status.SUCCESS || response.getData() == null) return;

        List<Region> options = new ArrayList<>();
        options.add(new Region("ALL", "All Regions"));
        options.addAll(response.getData());
        regionAdapter = createAdapter(getContext(), options, "R", R.color.report_region_badge);
        binding.spinnerRegion.setAdapter(regionAdapter);

        ReportFilter saved = sharedViewModel != null ? sharedViewModel.getCurrentFilter() : null;
        boolean restrictedRegion = isDesignation(Constants.RSM)
                || isDesignation(Constants.ASM) || isDesignation(Constants.PA);
        String wantedId = saved != null && saved.getRegion() != null
                ? saved.getRegion().getId()
                : restrictedRegion ? assignedRegionId
                : !assignedRegionId.isEmpty() ? assignedRegionId : "ALL";
        if (!regionVisible()) wantedId = assignedRegionId;

        int position = positionOfRegion(wantedId);
        if (position < 0) position = 0;
        restoringSelection = true;
        binding.spinnerRegion.setSelection(position);
        selectedRegion = regionAdapter.getItem(position);
        if (!regionVisible() && !assignedRegionId.isEmpty()) {
            selectedRegion = new Region(assignedRegionId, "Assigned region");
        }
        restoringSelection = false;
        loadAreasForSelection();
    }

    private void bindAreas(ApiResponse<List<Area>> response) {
        if (response == null) return;
        if (response.getStatus() == ApiResponse.Status.ERROR) {
            showLookupError(response);
            return;
        }
        if (response.getStatus() != ApiResponse.Status.SUCCESS || response.getData() == null) return;

        List<Area> options = new ArrayList<>();
        options.add(new Area("ALL", "All Areas", selectedRegion != null ? selectedRegion.getId() : ""));
        options.addAll(response.getData());
        areaAdapter = createAdapter(getContext(), options, "A", R.color.report_area_badge);
        binding.spinnerArea.setAdapter(areaAdapter);

        ReportFilter saved = sharedViewModel != null ? sharedViewModel.getCurrentFilter() : null;
        String wantedAreaId = isDesignation(Constants.ASM) ? assignedAreaId
                : saved != null && saved.getArea() != null ? saved.getArea().getId() : "ALL";
        int position = positionOfArea(wantedAreaId);
        if (position < 0) position = 0;
        restoringSelection = true;
        binding.spinnerArea.setSelection(position);
        selectedArea = areaAdapter.getItem(position);
        restoringSelection = false;
        loadSubAreasForSelection();
    }

    private void bindSubAreas(ApiResponse<List<SubArea>> response) {
        if (response == null) return;
        if (response.getStatus() == ApiResponse.Status.ERROR) {
            showLookupError(response);
            return;
        }
        if (response.getStatus() != ApiResponse.Status.SUCCESS || response.getData() == null) return;

        List<SubArea> options = new ArrayList<>();
        options.add(new SubArea("ALL", "All Sub-areas",
                selectedArea != null ? selectedArea.getId() : assignedAreaId));
        options.addAll(response.getData());
        subAreaAdapter = createAdapter(getContext(), options, "S", R.color.report_sub_area_badge);
        binding.spinnerSubArea.setAdapter(subAreaAdapter);

        ReportFilter saved = sharedViewModel != null ? sharedViewModel.getCurrentFilter() : null;
        String wantedId = isDesignation(Constants.ASM) ? assignedSubAreaId
                : saved != null && saved.getSubArea() != null ? saved.getSubArea().getId() : "ALL";
        int position = positionOfSubArea(wantedId);
        if (position < 0) position = 0;
        restoringSelection = true;
        binding.spinnerSubArea.setSelection(position);
        selectedSubArea = subAreaAdapter.getItem(position);
        restoringSelection = false;
        loadTerritoriesForSelection();
    }

    private void bindTerritories(ApiResponse<List<Territory>> response) {
        if (response == null) return;
        if (response.getStatus() == ApiResponse.Status.ERROR) {
            showLookupError(response);
            return;
        }
        if (response.getStatus() != ApiResponse.Status.SUCCESS || response.getData() == null) return;

        List<Territory> options = new ArrayList<>();
        options.add(new Territory("ALL", "All Territories", selectedSubArea != null
                ? selectedSubArea.getId() : assignedSubAreaId));
        options.addAll(response.getData());
        territoryAdapter = createAdapter(getContext(), options, "T", R.color.report_territory_badge);
        binding.spinnerTerritory.setAdapter(territoryAdapter);

        ReportFilter saved = sharedViewModel != null ? sharedViewModel.getCurrentFilter() : null;
        String wantedId = saved != null && saved.getTerritory() != null
                ? saved.getTerritory().getId()
                : isDesignation(Constants.ASM) || isDesignation(Constants.PA)
                ? assignedTerritoryId : "ALL";
        int position = positionOfTerritory(wantedId);
        if (position < 0) position = 0;
        binding.spinnerTerritory.setSelection(position);
    }

    private void loadAreasForSelection() {
        if (sharedViewModel == null) return;
        String regionId = selectedRegion != null ? selectedRegion.getId() : assignedRegionId;
        if (isDesignation(Constants.RSM) || isDesignation(Constants.ASM)) {
            regionId = assignedRegionId;
        }
        if ("ALL".equalsIgnoreCase(regionId)) regionId = "";
        sharedViewModel.loadAreas(regionId, false);
    }

    private void loadSubAreasForSelection() {
        if (sharedViewModel == null) return;
        String areaId = selectedArea != null ? selectedArea.getId() : assignedAreaId;
        if (isDesignation(Constants.ASM)) areaId = assignedAreaId;
        sharedViewModel.loadSubAreas(areaId, false);
    }

    private void loadTerritoriesForSelection() {
        if (sharedViewModel == null) return;
        String subAreaId = selectedSubArea != null ? selectedSubArea.getId() : assignedSubAreaId;
        if (isDesignation(Constants.ASM) || isDesignation(Constants.PA)) {
            subAreaId = assignedSubAreaId;
        }
        sharedViewModel.loadTerritories(subAreaId, false);
    }

    private boolean regionVisible() {
        return isExecutive() || isDesignation(Constants.NSM);
    }

    private void showLookupError(ApiResponse<?> response) {
        Toast.makeText(getContext(), response.safeMessage(), Toast.LENGTH_LONG).show();
    }

    private void detachObservers() {
        if (sharedViewModel != null && observing) {
            sharedViewModel.getRegions().removeObserver(regionsObserver);
            sharedViewModel.getAreas().removeObserver(areasObserver);
            sharedViewModel.getSubAreas().removeObserver(subAreasObserver);
            sharedViewModel.getTerritories().removeObserver(territoriesObserver);
            observing = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        detachObservers();
        super.onDetachedFromWindow();
    }

    private String idOf(Integer id) { return id == null ? "" : String.valueOf(id); }

    private void restoreCalendar(Calendar calendar, String date) {
        try {
            java.util.Date parsed = dateFormat.parse(date);
            if (parsed != null) calendar.setTime(parsed);
        } catch (Exception ignored) {
            // Keep today's date when no valid saved date is available.
        }
    }

    private int positionOfRegion(String id) {
        for (int i = 0; i < regionAdapter.getCount(); i++) {
            Region item = regionAdapter.getItem(i);
            if (item != null && id.equals(item.getId())) return i;
        }
        return -1;
    }

    private int positionOfArea(String id) {
        for (int i = 0; i < areaAdapter.getCount(); i++) {
            Area item = areaAdapter.getItem(i);
            if (item != null && id.equals(item.getId())) return i;
        }
        return -1;
    }

    private int positionOfSubArea(String id) {
        for (int i = 0; i < subAreaAdapter.getCount(); i++) {
            SubArea item = subAreaAdapter.getItem(i);
            if (item != null && id.equals(item.getId())) return i;
        }
        return -1;
    }

    private int positionOfTerritory(String id) {
        for (int i = 0; i < territoryAdapter.getCount(); i++) {
            Territory item = territoryAdapter.getItem(i);
            if (item != null && id.equals(item.getId())) return i;
        }
        return -1;
    }

    public ReportFilter getFilter() {
        String from = binding.etFromDate.getText() == null
                ? "" : binding.etFromDate.getText().toString();
        String to = binding.etToDate.getText() == null
                ? "" : binding.etToDate.getText().toString();

        Region region = regionVisible()
                ? (Region) binding.spinnerRegion.getSelectedItem()
                : !assignedRegionId.isEmpty() ? new Region(assignedRegionId, "Assigned region") : null;
        Area area = (isDesignation(Constants.ASM) || isDesignation(Constants.PA))
                && !assignedAreaId.isEmpty()
                ? new Area(assignedAreaId, "Assigned area", assignedRegionId)
                : (Area) binding.spinnerArea.getSelectedItem();
        SubArea subArea = isDesignation(Constants.ASM) || isDesignation(Constants.PA)
                ? !assignedSubAreaId.isEmpty()
                ? new SubArea(assignedSubAreaId, "Assigned sub-area", assignedAreaId) : null
                : (SubArea) binding.spinnerSubArea.getSelectedItem();
        Territory territory = !isDesignation(Constants.PA)
                ? (Territory) binding.spinnerTerritory.getSelectedItem()
                : !assignedTerritoryId.isEmpty()
                ? new Territory(assignedTerritoryId, "Assigned territory", assignedSubAreaId) : null;

        return new ReportFilter(from, to, assignedZone, region, area, subArea, territory);
    }

    private static final class LocationSpinnerAdapter<T> extends ArrayAdapter<T> {
        private final String badge;
        private final int badgeColor;

        LocationSpinnerAdapter(Context context, List<T> items, String badge, int badgeColor) {
            super(context, 0, items);
            this.badge = badge;
            this.badgeColor = badgeColor;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView,
                            @NonNull ViewGroup parent) {
            return bindRow(position, parent, R.layout.item_report_spinner_selected);
        }

        @NonNull
        @Override
        public View getDropDownView(int position, @Nullable View convertView,
                                    @NonNull ViewGroup parent) {
            return bindRow(position, parent, R.layout.item_report_spinner_dropdown);
        }

        private View bindRow(int position, ViewGroup parent, int layoutId) {
            View row = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            TextView badgeView = row.findViewById(R.id.locationBadge);
            TextView nameView = row.findViewById(R.id.locationName);
            TextView associatedEmpView = row.findViewById(R.id.locationAssociatedEmp);
            badgeView.setText(badge);
            badgeView.setBackgroundTintList(ColorStateList.valueOf(getContext().getColor(badgeColor)));
            T item = getItem(position);
            nameView.setText(item == null ? "" : item.toString());
            String associatedEmp = associatedEmployeeOf(item);
            if (associatedEmp == null || associatedEmp.trim().isEmpty()) {
                associatedEmpView.setVisibility(View.GONE);
            } else {
                associatedEmpView.setText(String.format(Locale.getDefault(), "(%s)", associatedEmp.trim()));
                associatedEmpView.setVisibility(View.VISIBLE);
            }
            return row;
        }

        private String associatedEmployeeOf(T item) {
            if (item instanceof Region) return ((Region) item).getAssociatedEmp();
            if (item instanceof Area) return ((Area) item).getAssociatedEmp();
            if (item instanceof SubArea) return ((SubArea) item).getAssociatedEmp();
            if (item instanceof Territory) return ((Territory) item).getAssociatedEmp();
            return null;
        }
    }

    public void setOnSearchClickListener(OnSearchClickListener listener) {
        onSearchClickListener = listener;
    }
}
