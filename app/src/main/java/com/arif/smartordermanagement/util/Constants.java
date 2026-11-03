package com.arif.smartordermanagement.util;

/** API endpoint and system constants for network operations. */
public final class Constants {
    private Constants() {}

    //designation
    public static final String ADMIN="Managing Director";
    public static final String HR="Human Resources Manager";
    public static final String NSM="National Sells Manager";
    public static final String RSM="Regional Sells Manager";
    public static final String ASM="Area Manager";
    public static final String TM="Territory Manager";
    public static final String PA="Product Associate";
    public static final String MANAGER="Manager";
    public static final String PMD="Product Development Head";
    public static final String UNAUTHENTICATE ="Unauthenticated.";
    public static final String CEO="CEO";


    public static final String GET_PRODUCT_LIST = "stock/depot-stock-products";
    public static final String SUBMIT_SALES_TARGET = "sales-target/submit";
    public static final String APPROVE_SALES_TARGET = "sales-target/approve";
    public static final String GET_TARGET_STATUS_REPORT = "sales-target/status-report";
}
