package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

public class Territory {
    private final String id;
    private final String name;
    private final String areaId;

    public Territory(String id, String name, String areaId) {
        this.id = id;
        this.name = name;
        this.areaId = areaId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAreaId() {
        return areaId;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
