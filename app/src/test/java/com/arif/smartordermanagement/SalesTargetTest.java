package com.arif.smartordermanagement;

import com.arif.smartordermanagement.model.ProductInfo;
import com.arif.smartordermanagement.model.SalesTarget;
import com.arif.smartordermanagement.model.TargetStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SalesTargetTest {

    @Test
    public void productInfo_calculatesTargetAndApprovedAmount() {
        ProductInfo product = new ProductInfo(1, "SKU-101", "Cleansing Gel", "Avene", "Cosmed", "100ml", 1000.0, 1200.0, 20);

        product.setTargetQty(15);
        assertEquals(15000.0, product.getTargetAmount(), 0.001);

        // When approvedQty is not set yet, approvedAmount uses targetQty
        assertEquals(15000.0, product.getApprovedAmount(), 0.001);

        // Senior adjusts quantity to 20
        product.setApprovedQty(20);
        assertEquals(20000.0, product.getApprovedAmount(), 0.001);
    }

    @Test
    public void salesTarget_calculatesTotalsAndTracksSeniorEdits() {
        List<ProductInfo> items = new ArrayList<>();

        ProductInfo p1 = new ProductInfo(1, "SKU-1", "Product 1", "Avene", "Cosmed", "100ml", 100.0, 120.0, 10);
        p1.setTargetQty(10); // 10 * 100 = 1000

        ProductInfo p2 = new ProductInfo(2, "SKU-2", "Product 2", "Carex", "Carex", "200ml", 50.0, 60.0, 20);
        p2.setTargetQty(20); // 20 * 50 = 1000

        items.add(p1);
        items.add(p2);

        SalesTarget target = new SalesTarget(
                "TGT-202611-001",
                101,
                "Kamrul Hasan",
                "Territory Officer",
                "Gulshan",
                "November 2026",
                "02/11/2026",
                TargetStatus.PENDING,
                items,
                "Initial plan"
        );

        assertEquals(30, target.getTotalQty());
        assertEquals(2000.0, target.getTotalAmount(), 0.001);
        assertFalse(target.isSeniorEdited());

        // Senior edits quantity for product 1 to 15
        p1.setApprovedQty(15); // 15 * 100 = 1500
        target.recalculateTotals();

        assertEquals(35, target.getApprovedQty());
        assertEquals(2500.0, target.getApprovedAmount(), 0.001);
        assertTrue(target.isSeniorEdited());
    }

    @Test
    public void targetStatus_verifiesStatusConstants() {
        assertTrue(TargetStatus.isPending("Pending"));
        assertTrue(TargetStatus.isPending("pending"));
        assertTrue(TargetStatus.isApproved("Approved"));
        assertTrue(TargetStatus.isApproved("approved"));
        assertTrue(TargetStatus.isRejected("Rejected"));
        assertTrue(TargetStatus.isRejected("rejected"));
        assertFalse(TargetStatus.isApproved("Pending"));
    }

    @Test
    public void defaultProductsCatalog_returnsNonEmptyList() {
        List<ProductInfo> defaults = ProductInfo.getDefaultProducts();
        assertNotNull(defaults);
        assertFalse(defaults.isEmpty());
        assertTrue(defaults.size() >= 5);
    }
}
