package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Territory {
    @SerializedName(value = "territory_id", alternate = {"id"})
    private String id;
    @SerializedName(value = "territory_name", alternate = {"name"})
    private String name;
    @SerializedName(value = "associated_emp", alternate = {"associatedEmp"})
    private String associatedEmp;
    @SerializedName(value = "area_id", alternate = {"areaId"})
    private String areaId;
    @SerializedName(value = "sub_area_id", alternate = {"subAreaId"})
    private String subAreaId;

    public Territory() {}

    public Territory(String id, String name, String subAreaId) {
        this.id = id;
        this.name = name;
        this.areaId = subAreaId;
        this.subAreaId = subAreaId;
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

    public String getSubAreaId() {
        return subAreaId;
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
