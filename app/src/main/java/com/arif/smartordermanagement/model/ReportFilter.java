package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

public class ReportFilter {
    private String fromDate;
    private String toDate;
    private Region region;
    private Area area;
    private Territory territory;

    public ReportFilter() {
        this.fromDate = "";
        this.toDate = "";
    }

    public ReportFilter(String fromDate, String toDate, Region region, Area area, Territory territory) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.region = region;
        this.area = area;
        this.territory = territory;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Territory getTerritory() {
        return territory;
    }

    public void setTerritory(Territory territory) {
        this.territory = territory;
    }

    public String getDisplayText() {
        StringBuilder sb = new StringBuilder();
        if (fromDate != null && !fromDate.isEmpty()) {
            sb.append("From: ").append(fromDate).append(" ");
        }
        if (toDate != null && !toDate.isEmpty()) {
            sb.append("To: ").append(toDate).append(" ");
        }
        if (region != null && !"ALL".equals(region.getId())) {
            sb.append("Region: ").append(region.getName()).append(" ");
        }
        if (area != null && !"ALL".equals(area.getId())) {
            sb.append("Area: ").append(area.getName()).append(" ");
        }
        if (territory != null && !"ALL".equals(territory.getId())) {
            sb.append("Territory: ").append(territory.getName()).append(" ");
        }
        return sb.length() == 0 ? "All Records" : sb.toString().trim();
    }

    @NonNull
    @Override
    public String toString() {
        return getDisplayText();
    }
}
