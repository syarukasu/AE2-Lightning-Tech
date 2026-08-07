package com.moakiee.ae2lt.logic.tianshu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.moakiee.ae2lt.logic.craft.MatrixProfileIssue;
import com.moakiee.ae2lt.logic.tianshu.maintenance.InventoryMaintenanceBadge;
import com.moakiee.ae2lt.logic.tianshu.maintenance.InventoryMaintenanceStatus;
import com.moakiee.ae2lt.logic.tianshu.terminal.ClosedLoopDraftStatus;
import com.moakiee.ae2lt.logic.tianshu.terminal.TianshuTerminalAction;
import org.junit.jupiter.api.Test;

class TianshuPrimitiveContractTest {
    @Test
    void functionProfileMatches206CapacityRules() {
        TianshuFunctionProfile empty = TianshuFunctionProfile.empty();
        assertTrue(empty.supportsInventoryMaintenance());
        assertTrue(empty.supportsClosedLoopPatterns());
        assertFalse(empty.supportsClosedLoopSeeds());
        assertEquals(2048, empty.maintenanceRuleCapacity());
        assertEquals(0, empty.closedLoopPatternCapacity());

        TianshuFunctionProfile profile = new TianshuFunctionProfile(3, 1);
        assertEquals(108, profile.closedLoopPatternCapacity());
        assertTrue(profile.supportsClosedLoopSeeds());
        assertThrows(IllegalArgumentException.class, () -> new TianshuFunctionProfile(-1, 0));
        assertEquals(Integer.MAX_VALUE,
                new TianshuFunctionProfile(Integer.MAX_VALUE, 0).closedLoopPatternCapacity());
    }

    @Test
    void multiblockComponentCoolingRulesMatch206() {
        assertTrue(TianshuMultiblockComponent.COOLING.fillsCoolingPosition());
        assertTrue(TianshuMultiblockComponent.CLOSED_LOOP_PATTERN_STORAGE.fillsCoolingPosition());
        assertTrue(TianshuMultiblockComponent.CLOSED_LOOP_SEED_STORAGE.isClosedLoopStorage());
        assertFalse(TianshuMultiblockComponent.CASING.fillsCoolingPosition());
        assertEquals(7, TianshuMultiblockRole.values().length);
        assertEquals(13, TianshuMultiblockScanIssue.values().length);
    }

    @Test
    void maintenanceBadgeMappingMatches206() {
        assertEquals(InventoryMaintenanceBadge.GRAY,
                InventoryMaintenanceBadge.from(InventoryMaintenanceStatus.DISABLED));
        assertEquals(InventoryMaintenanceBadge.GREEN,
                InventoryMaintenanceBadge.from(InventoryMaintenanceStatus.SATISFIED));
        assertEquals(InventoryMaintenanceBadge.RED,
                InventoryMaintenanceBadge.from(InventoryMaintenanceStatus.MISSING_PATTERN));
        assertEquals(InventoryMaintenanceBadge.RED,
                InventoryMaintenanceBadge.from(InventoryMaintenanceStatus.MISSING_INGREDIENTS));
        assertEquals(InventoryMaintenanceBadge.RED,
                InventoryMaintenanceBadge.from(InventoryMaintenanceStatus.OFFLINE));
        assertEquals(InventoryMaintenanceBadge.YELLOW,
                InventoryMaintenanceBadge.from(InventoryMaintenanceStatus.CRAFTING));
        assertEquals(InventoryMaintenanceBadge.YELLOW, InventoryMaintenanceBadge.from(null));
    }

    @Test
    void terminalAndDraftVocabulariesArePinned() {
        assertEquals(6, TianshuTerminalAction.values().length);
        assertEquals(11, ClosedLoopDraftStatus.values().length);
        assertEquals(ClosedLoopDraftStatus.ENCODED, ClosedLoopDraftStatus.valueOf("ENCODED"));
        assertEquals(5, MatrixProfileIssue.values().length);
    }
}
