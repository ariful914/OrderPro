package com.arif.smartordermanagement.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    private int userId;
    @SerializedName("username")
    private String username;
    @SerializedName("role_id")
    private int roleId;
    @SerializedName("role_name")
    private String roleName;
    @SerializedName("employee_name")
    private String employeeName;
    @SerializedName("address")
    private String address;
    @SerializedName("designation_name")
    private String designationName;
    @SerializedName("joining_date")
    private String joiningDate;
    @SerializedName("contact")
    private String contact;
    @SerializedName("zone_id")
    private Integer zoneId;
    @SerializedName("region_id")
    private Integer regionId;
    @SerializedName("area_id")
    private Integer areaId;
    @SerializedName("sub_area_id")
    private Integer subAreaId;
    @SerializedName("territory_id")
    private Integer territoryId;
    @SerializedName("depot_id")
    private Integer depotId;
    @SerializedName("token")
    private String token;
    @SerializedName("version")
    private String version;

    public User() {}

    // Kept for existing demo/sample code that constructs a local user.
    public User(String id, String name, String email, String role, String companyName) {
        try {
            this.userId = Integer.parseInt(id);
        } catch (NumberFormatException ignored) {
            this.userId = 0;
        }
        this.username = email;
        this.employeeName = name;
        this.roleName = role;
        this.designationName = companyName;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }
    public String getEmployeeName() { return employeeName; }
    public String getAddress() { return address; }
    public String getDesignationName() { return designationName; }
    public String getJoiningDate() { return joiningDate; }
    public String getContact() { return contact; }
    public Integer getZoneId() { return zoneId; }
    public Integer getRegionId() { return regionId; }
    public Integer getAreaId() { return areaId; }
    public Integer getSubAreaId() { return subAreaId; }
    public Integer getTerritoryId() { return territoryId; }
    public Integer getDepotId() { return depotId; }
    public String getToken() { return token; }
    public String getVersion() { return version; }

    // Compatibility with the existing dashboard UI.
    public String getId() { return String.valueOf(userId); }
    public String getName() { return employeeName != null ? employeeName : username; }
    public String getEmail() { return username; }
    public String getRole() { return designationName != null ? designationName : roleName; }
    public String getCompanyName() { return roleName != null ? roleName : ""; }
}
