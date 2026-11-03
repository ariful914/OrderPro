package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/** Sub-area returned by the location lookup API. */
public class SubArea {
    @SerializedName(value = "sub_area_id", alternate = {"id"})
    private String id;
    @SerializedName(value = "sub_area_name", alternate = {"name"})
    private String name;
    @SerializedName(value = "associated_emp", alternate = {"associatedEmp"})
    private String associatedEmp;
    @SerializedName(value = "area_id", alternate = {"areaId"})
    private String areaId;

    public SubArea() {}

    public SubArea(String id, String name, String areaId) {
        this.id = id;
        this.name = name;
        this.areaId = areaId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAssociatedEmp() { return associatedEmp; }
    public String getAreaId() { return areaId; }

    @NonNull
    @Override
    public String toString() { return name == null ? "" : name; }
}
