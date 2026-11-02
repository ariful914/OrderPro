package com.arif.smartordermanagement.model;

import java.util.List;

public class SalesTrend {
    private final TimeFrame timeFrame;
    private final String totalSalesFormatted;
    private final String averageSalesFormatted;
    private final List<SalesDataPoint> points;

    public SalesTrend(TimeFrame timeFrame, String totalSalesFormatted, String averageSalesFormatted, List<SalesDataPoint> points) {
        this.timeFrame = timeFrame;
        this.totalSalesFormatted = totalSalesFormatted;
        this.averageSalesFormatted = averageSalesFormatted;
        this.points = points;
    }

    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    public String getTotalSalesFormatted() {
        return totalSalesFormatted;
    }

    public String getAverageSalesFormatted() {
        return averageSalesFormatted;
    }

    public List<SalesDataPoint> getPoints() {
        return points;
    }
}
