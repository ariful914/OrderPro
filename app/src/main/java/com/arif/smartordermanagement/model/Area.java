package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

public class Area {
    private final String id;
    private final String name;
    private final String regionId;

    public Area(String id, String name, String regionId) {
        this.id = id;
        this.name = name;
        this.regionId = regionId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRegionId() {
        return regionId;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
