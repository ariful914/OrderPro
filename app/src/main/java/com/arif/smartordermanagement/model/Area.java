package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Area {
    @SerializedName(value = "area_id", alternate = {"id"})
    private String id;
    @SerializedName(value = "area_name", alternate = {"name"})
    private String name;
    @SerializedName(value = "associated_emp", alternate = {"associatedEmp"})
    private String associatedEmp;
    @SerializedName(value = "region_id", alternate = {"regionId"})
    private String regionId;

    public Area() {}

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

    public String getAssociatedEmp() {
        return associatedEmp;
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
