package com.arif.smartordermanagement;

import com.arif.smartordermanagement.data.SalesRepository;
import com.arif.smartordermanagement.model.CategorySales;
import com.arif.smartordermanagement.model.KpiMetric;
import com.arif.smartordermanagement.model.Order;
import com.arif.smartordermanagement.model.OrderStatus;
import com.arif.smartordermanagement.model.SalesTrend;
import com.arif.smartordermanagement.model.TimeFrame;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SalesRepositoryTest {

    private SalesRepository salesRepository;

    @Before
    public void setUp() {
        salesRepository = new SalesRepository();
    }

    @Test
    public void getKpiMetrics_returns4MetricsForTimeframe() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<KpiMetric>> resultRef = new AtomicReference<>();

        salesRepository.getKpiMetrics(TimeFrame.THIS_WEEK, metrics -> {
            resultRef.set(metrics);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        List<KpiMetric> metrics = resultRef.get();
        assertNotNull(metrics);
        assertEquals(4, metrics.size());
        assertEquals("Total Revenue", metrics.get(0).getTitle());
        assertEquals("৳24,850.00", metrics.get(0).getValue());
    }

    @Test
    public void getSalesTrend_returnsPointsForThisWeek() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<SalesTrend> trendRef = new AtomicReference<>();

        salesRepository.getSalesTrend(TimeFrame.THIS_WEEK, trend -> {
            trendRef.set(trend);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        SalesTrend trend = trendRef.get();
        assertNotNull(trend);
        assertEquals(TimeFrame.THIS_WEEK, trend.getTimeFrame());
        assertEquals("৳24,850.00", trend.getTotalSalesFormatted());
        assertEquals(7, trend.getPoints().size());
    }

    @Test
    public void getRecentOrders_filtersByQueryAndStatus() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Order>> ordersRef = new AtomicReference<>();

        salesRepository.getRecentOrders("TechCorp", OrderStatus.COMPLETED, orders -> {
            ordersRef.set(orders);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        List<Order> orders = ordersRef.get();
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals("TechCorp Systems", orders.get(0).getCustomerName());
    }
}
