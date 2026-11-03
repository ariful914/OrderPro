package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Region {
    @SerializedName(value = "region_id", alternate = {"id"})
    private String id;
    @SerializedName(value = "region_name", alternate = {"name"})
    private String name;
    @SerializedName(value = "associated_emp", alternate = {"associatedEmp"})
    private String associatedEmp;

    public Region() {}

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

    public String getAssociatedEmp() {
        return associatedEmp;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
