package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

public class Region {
    private final String id;
    private final String name;

    public Region(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
