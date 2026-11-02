package com.arif.smartordermanagement.model;

import com.google.gson.annotations.SerializedName;

/**
 * Unified model representing User profile information and authentication response,
 * used across the app and in API request bodies (e.g. product list, target reports).
 */
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

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

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
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }

    public String getJoiningDate() { return joiningDate; }
    public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public Integer getZoneId() { return zoneId; }
    public void setZoneId(Integer zoneId) { this.zoneId = zoneId; }

    public Integer getRegionId() { return regionId; }
    public void setRegionId(Integer regionId) { this.regionId = regionId; }

    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }

    public Integer getSubAreaId() { return subAreaId; }
    public void setSubAreaId(Integer subAreaId) { this.subAreaId = subAreaId; }

    public Integer getTerritoryId() { return territoryId; }
    public void setTerritoryId(Integer territoryId) { this.territoryId = territoryId; }

    public Integer getDepotId() { return depotId; }
    public void setDepotId(Integer depotId) { this.depotId = depotId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    // Compatibility with the existing dashboard UI.
    public String getId() { return String.valueOf(userId); }
    public String getName() { return employeeName != null ? employeeName : username; }
    public String getEmail() { return username; }
    public String getRole() { return designationName != null ? designationName : roleName; }
    public String getCompanyName() { return roleName != null ? roleName : ""; }
}
