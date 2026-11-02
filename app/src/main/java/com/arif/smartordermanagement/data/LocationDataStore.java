package com.arif.smartordermanagement.data;

import com.arif.smartordermanagement.model.Area;
import com.arif.smartordermanagement.model.Region;
import com.arif.smartordermanagement.model.Territory;

import java.util.ArrayList;
import java.util.List;

public class LocationDataStore {

    public static final Region ALL_REGIONS = new Region("ALL", "All Regions");
    public static final Area ALL_AREAS = new Area("ALL", "All Areas", "ALL");
    public static final Territory ALL_TERRITORIES = new Territory("ALL", "All Territories", "ALL");

    private static final List<Region> REGIONS = new ArrayList<>();
    private static final List<Area> AREAS = new ArrayList<>();
    private static final List<Territory> TERRITORIES = new ArrayList<>();

    static {
        REGIONS.add(ALL_REGIONS);
        REGIONS.add(new Region("REG_DHAKA", "Dhaka Region"));
        REGIONS.add(new Region("REG_CTG", "Chittagong Region"));
        REGIONS.add(new Region("REG_SYLHET", "Sylhet Region"));
        REGIONS.add(new Region("REG_RAJSHAHI", "Rajshahi Region"));

        AREAS.add(ALL_AREAS);
        // Dhaka Region Areas
        AREAS.add(new Area("AREA_DHK_N", "Dhaka North", "REG_DHAKA"));
        AREAS.add(new Area("AREA_DHK_S", "Dhaka South", "REG_DHAKA"));
        // Chittagong Region Areas
        AREAS.add(new Area("AREA_CTG_M", "Chittagong Metro", "REG_CTG"));
        AREAS.add(new Area("AREA_COX", "Cox's Bazar Area", "REG_CTG"));
        // Sylhet Region Areas
        AREAS.add(new Area("AREA_SYL_S", "Sylhet Sadar", "REG_SYLHET"));
        // Rajshahi Region Areas
        AREAS.add(new Area("AREA_RAJ_C", "Rajshahi Central", "REG_RAJSHAHI"));

        TERRITORIES.add(ALL_TERRITORIES);
        // Dhaka North Territories
        TERRITORIES.add(new Territory("TERR_GULSHAN", "Gulshan Territory", "AREA_DHK_N"));
        TERRITORIES.add(new Territory("TERR_BANANI", "Banani Territory", "AREA_DHK_N"));
        TERRITORIES.add(new Territory("TERR_UTTARA", "Uttara Territory", "AREA_DHK_N"));
        // Dhaka South Territories
        TERRITORIES.add(new Territory("TERR_DHANMONDI", "Dhanmondi Territory", "AREA_DHK_S"));
        TERRITORIES.add(new Territory("TERR_MOTIJHEEL", "Motijheel Territory", "AREA_DHK_S"));
        // Chittagong Metro Territories
        TERRITORIES.add(new Territory("TERR_AGRABAD", "Agrabad Territory", "AREA_CTG_M"));
        TERRITORIES.add(new Territory("TERR_NASIRABAD", "Nasirabad Territory", "AREA_CTG_M"));
        // Cox's Bazar Territories
        TERRITORIES.add(new Territory("TERR_BEACH", "Beach Area Territory", "AREA_COX"));
        // Sylhet Sadar Territories
        TERRITORIES.add(new Territory("TERR_ZINDABAZAR", "Zindabazar Territory", "AREA_SYL_S"));
        // Rajshahi Central Territories
        TERRITORIES.add(new Territory("TERR_SAHEB", "Saheb Bazar Territory", "AREA_RAJ_C"));
    }

    private LocationDataStore() {}

    public static List<Region> getRegions() {
        return new ArrayList<>(REGIONS);
    }

    public static List<Area> getAreasForRegion(String regionId) {
        List<Area> filtered = new ArrayList<>();
        filtered.add(ALL_AREAS);
        if (regionId != null && !"ALL".equals(regionId)) {
            for (Area area : AREAS) {
                if (area.getRegionId().equals(regionId)) {
                    filtered.add(area);
                }
            }
        }
        return filtered;
    }

    public static List<Territory> getTerritoriesForArea(String areaId) {
        List<Territory> filtered = new ArrayList<>();
        filtered.add(ALL_TERRITORIES);
        if (areaId != null && !"ALL".equals(areaId)) {
            for (Territory territory : TERRITORIES) {
                if (territory.getAreaId().equals(areaId)) {
                    filtered.add(territory);
                }
            }
        }
        return filtered;
    }
}
