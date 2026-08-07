package com.moakiee.ae2lt.logic.tianshu.maintenance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.moakiee.ae2lt.client.TianshuUploadAliasRules;
import com.moakiee.ae2lt.config.TianshuUploadTrigger;
import org.junit.jupiter.api.Test;

class PortedMaintenancePrimitivesTest {
    @Test
    void fixedPeriodScheduleAccumulatesFractionalCredit() {
        FixedPeriodPollingSchedule schedule = new FixedPeriodPollingSchedule(4);
        assertEquals(0, schedule.checksThisTick(1));
        assertEquals(0, schedule.checksThisTick(1));
        assertEquals(0, schedule.checksThisTick(1));
        assertEquals(1, schedule.checksThisTick(1));
        assertEquals(2, schedule.checksThisTick(8));
        schedule.reset();
        assertEquals(0, schedule.checksThisTick(1));
        assertThrows(IllegalArgumentException.class, () -> new FixedPeriodPollingSchedule(0));
    }

    @Test
    void pollingCursorWrapsAndValidatesRuleCount() {
        FixedPeriodPollingSchedule schedule = new FixedPeriodPollingSchedule(1);
        assertEquals(0, schedule.nextIndex(2));
        assertEquals(1, schedule.nextIndex(2));
        assertEquals(0, schedule.nextIndex(2));
        assertThrows(IllegalArgumentException.class, () -> schedule.nextIndex(0));
    }

    @Test
    void maintenanceAndUploadConstantsMatch206() {
        assertEquals(2048, InventoryMaintenanceLimits.MAX_ENTRIES);
        assertEquals(ReservedStockMatchMode.EXACT, ReservedStockMatchMode.valueOf("EXACT"));
        assertEquals("ae2lt:*", TianshuUploadAliasRules.namespaceGlob(" ae2lt "));
        assertEquals("", TianshuUploadAliasRules.namespaceGlob("  "));
        assertEquals(TianshuUploadTrigger.SHIFT, TianshuUploadTrigger.NO_SHIFT.next());
        assertEquals(TianshuUploadTrigger.NO_SHIFT, TianshuUploadTrigger.MANUAL_ONLY.next());
    }
}
