package com.arif.smartordermanagement.model;

import androidx.annotation.NonNull;

public class ReportFilter {
    private String fromDate;
    private String toDate;
    private Zone zone;
    private Region region;
    private Area area;
    private SubArea subArea;
    private Territory territory;

    public ReportFilter() {
        this.fromDate = "";
        this.toDate = "";
    }

    public ReportFilter(String fromDate, String toDate, Region region, Area area, Territory territory) {
        this(fromDate, toDate, null, region, area, territory);
    }

    public ReportFilter(String fromDate, String toDate, Zone zone, Region region,
                        Area area, Territory territory) {
        this(fromDate, toDate, zone, region, area, null, territory);
    }

    public ReportFilter(String fromDate, String toDate, Zone zone, Region region,
                        Area area, SubArea subArea, Territory territory) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.zone = zone;
        this.region = region;
        this.area = area;
        this.subArea = subArea;
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

    public Zone getZone() { return zone; }

    public void setZone(Zone zone) { this.zone = zone; }

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

    public SubArea getSubArea() { return subArea; }

    public void setSubArea(SubArea subArea) { this.subArea = subArea; }

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
        if (zone != null && !"ALL".equals(zone.getId())) {
            sb.append("Zone: ").append(zone.getName()).append(" ");
        }
        if (region != null && !"ALL".equals(region.getId())) {
            sb.append("Region: ").append(region.getName()).append(" ");
        }
        if (area != null && !"ALL".equals(area.getId())) {
            sb.append("Area: ").append(area.getName()).append(" ");
        }
        if (subArea != null && !"ALL".equals(subArea.getId())) {
            sb.append("Sub-area: ").append(subArea.getName()).append(" ");
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
