package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/** Zone returned by the location lookup API. */
public class Zone {
    @SerializedName(value = "zone_id", alternate = {"id"})
    private String id;
    @SerializedName(value = "zone_name", alternate = {"name"})
    private String name;

    public Zone() {}

    public Zone(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    @NonNull
    @Override
    public String toString() { return name == null ? "" : name; }
}
