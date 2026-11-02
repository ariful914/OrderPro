package com.arif.smartordermanagement.ui.operations;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.arif.smartordermanagement.R;
import com.google.android.material.tabs.TabLayout;

/** Builds the destination tabs used by the Operations hub screens. */
final class OperationsTabComponent {
    interface DestinationHandler {
        void onDestinationSelected(String destination);
    }

    private final Context context;
    private final LinearLayout content;
    private int tabIndex = -1;
    private String activeDestination;

    OperationsTabComponent(Context context, LinearLayout content) {
        this.context = context;
        this.content = content;
    }

    void show(String[] labels, String[] destinations, String initialDestination,
              DestinationHandler destinationHandler) {
        TabLayout tabs = new TabLayout(context);
        if (isTargetHub(destinations)) {
            styleTargetTabs(tabs);
        } else {
            tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        }
        for (String label : labels) tabs.addTab(tabs.newTab().setText(label), false);

        tabIndex = content.getChildCount();
        activeDestination = initialDestination;
        content.addView(tabs, matchWrap());
        tabs.selectTab(tabs.getTabAt(indexOf(destinations, initialDestination)));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                activeDestination = destinations[tab.getPosition()];
                destinationHandler.onDestinationSelected(activeDestination);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        destinationHandler.onDestinationSelected(initialDestination);
    }

    int getTabIndex() {
        return tabIndex;
    }

    String getActiveDestination() {
        return activeDestination;
    }

    boolean hasTabs() {
        return tabIndex >= 0;
    }

    private boolean isTargetHub(String[] destinations) {
        return destinations.length > 0
                && OperationsFragment.TARGET_APPROVAL.equals(destinations[0]);
    }

    private void styleTargetTabs(TabLayout tabs) {
        int blue = color(R.color.google_blue);
        int mutedText = color(R.color.on_surface_variant_light);
        int surface = color(R.color.tonal_surface_light);
        float radius = dp(28);

        GradientDrawable track = new GradientDrawable();
        track.setColor(surface);
        track.setCornerRadius(radius);
        tabs.setBackground(track);
        tabs.setClipToOutline(true);
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4));
        tabs.setMinimumHeight(dp(56));

        GradientDrawable selectedTab = new GradientDrawable();
        selectedTab.setColor(blue);
        selectedTab.setCornerRadius(dp(24));
        tabs.setSelectedTabIndicator(selectedTab);
        tabs.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_STRETCH);
        tabs.setTabIndicatorFullWidth(true);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        tabs.setTabTextColors(mutedText, color(android.R.color.white));
        tabs.setTabRippleColorResource(android.R.color.transparent);
    }

    private int indexOf(String[] values, String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) return i;
        }
        return 0;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int color(int resourceId) {
        return context.getColor(resourceId);
    }

    private int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
