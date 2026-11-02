package com.arif.smartordermanagement.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.arif.smartordermanagement.data.storage.PrefUtils;
import com.arif.smartordermanagement.model.ProductInfo;
import com.arif.smartordermanagement.model.SalesTarget;
import com.arif.smartordermanagement.model.TargetStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Thread-safe persistent and in-memory store for Sales Targets.
 * Maintains targets state across app sessions and supports demo initial data.
 */
public final class SalesTargetStore {

    private static final String PREF_KEY_SALES_TARGETS = "sales_targets_cache";
    private static volatile SalesTargetStore instance;

    private final Context appContext;
    private final Gson gson = new Gson();
    private final List<SalesTarget> targets = new ArrayList<>();
    private int nextTargetSequence = 104;

    private SalesTargetStore(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        loadFromPreferencesOrDefaults();
    }

    public static SalesTargetStore getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (SalesTargetStore.class) {
                if (instance == null) {
                    instance = new SalesTargetStore(context);
                }
            }
        }
        return instance;
    }

    private synchronized void loadFromPreferencesOrDefaults() {
        android.content.SharedPreferences prefs = appContext.getSharedPreferences("orderpro_targets", Context.MODE_PRIVATE);
        String json = prefs.getString(PREF_KEY_SALES_TARGETS, null);
        if (json != null && !json.trim().isEmpty()) {
            try {
                List<SalesTarget> loaded = gson.fromJson(json, new TypeToken<List<SalesTarget>>(){}.getType());
                if (loaded != null && !loaded.isEmpty()) {
                    targets.clear();
                    targets.addAll(loaded);
                    return;
                }
            } catch (Exception ignored) {}
        }

        // Initialize realistic starter targets for demo & review
        saveToPreferences();
    }



    private synchronized void saveToPreferences() {
        try {
            android.content.SharedPreferences prefs = appContext.getSharedPreferences("orderpro_targets", Context.MODE_PRIVATE);
            prefs.edit().putString(PREF_KEY_SALES_TARGETS, gson.toJson(targets)).apply();
        } catch (Exception ignored) {}
    }

    public synchronized List<SalesTarget> getTargets() {
        List<SalesTarget> copy = new ArrayList<>();
        for (SalesTarget t : targets) {
            copy.add(t.clone());
        }
        return copy;
    }

    public synchronized SalesTarget getTargetById(String targetId) {
        if (targetId == null) return null;
        for (SalesTarget t : targets) {
            if (targetId.equalsIgnoreCase(t.getTargetId())) {
                return t.clone();
            }
        }
        return null;
    }

    public synchronized SalesTarget submitTarget(SalesTarget target) {
        if (target == null) return null;
        if (target.getTargetId() == null || target.getTargetId().trim().isEmpty()) {
            String monthCode = new SimpleDateFormat("yyyyMM", Locale.US).format(new Date());
            target.setTargetId("TGT-" + monthCode + "-" + (nextTargetSequence++));
        }
        if (target.getSubmissionDate() == null || target.getSubmissionDate().trim().isEmpty()) {
            target.setSubmissionDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date()));
        }
        target.setStatus(TargetStatus.PENDING);
        target.recalculateTotals();

        // Check if exists
        int existingIndex = -1;
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i).getTargetId().equalsIgnoreCase(target.getTargetId())) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex >= 0) {
            targets.set(existingIndex, target.clone());
        } else {
            targets.add(0, target.clone());
        }
        saveToPreferences();
        return target.clone();
    }

    public synchronized SalesTarget seniorApproveTarget(String targetId, String seniorName,
                                                        String remarks, List<ProductInfo> editedProducts) {
        for (SalesTarget t : targets) {
            if (t.getTargetId().equalsIgnoreCase(targetId)) {
                if (editedProducts != null && !editedProducts.isEmpty()) {
                    t.setItems(editedProducts);
                }
                t.setStatus(TargetStatus.APPROVED);
                t.setSeniorRemarks(remarks != null ? remarks : "Target approved by senior authority.");
                t.setApprovedBy(seniorName != null && !seniorName.isEmpty() ? seniorName : "Senior Authority");
                t.setApprovalDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date()));
                t.recalculateTotals();
                saveToPreferences();
                return t.clone();
            }
        }
        return null;
    }

    public synchronized SalesTarget seniorRejectTarget(String targetId, String seniorName, String remarks) {
        for (SalesTarget t : targets) {
            if (t.getTargetId().equalsIgnoreCase(targetId)) {
                t.setStatus(TargetStatus.REJECTED);
                t.setSeniorRemarks(remarks != null ? remarks : "Target rejected for revision.");
                t.setApprovedBy(seniorName != null && !seniorName.isEmpty() ? seniorName : "Senior Authority");
                t.setApprovalDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date()));
                saveToPreferences();
                return t.clone();
            }
        }
        return null;
    }

    public synchronized SalesTarget updateTargetProducts(String targetId, List<ProductInfo> updatedProducts) {
        for (SalesTarget t : targets) {
            if (t.getTargetId().equalsIgnoreCase(targetId)) {
                t.setItems(updatedProducts);
                t.recalculateTotals();
                saveToPreferences();
                return t.clone();
            }
        }
        return null;
    }
}
