package com.moakiee.ae2lt.logic;

import com.moakiee.ae2lt.block.MultiblockStateProperties;
import com.moakiee.ae2lt.celestweave.PhaseFlightControlRules;
import com.moakiee.ae2lt.celestweave.phase.PhaseLockProjectionRules;
import net.minecraft.world.entity.EquipmentSlot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControlAndBlockContractsTest {
    @Test
    void formedPropertyUsesStableName() {
        assertEquals("formed", MultiblockStateProperties.FORMED.getName());
    }

    @Test
    void phaseFlightRejectsOnlyInsideWallLandingToggle() {
        assertTrue(PhaseFlightControlRules.rejectFlightToggle(true, true, false));
        assertFalse(PhaseFlightControlRules.rejectFlightToggle(false, true, false));
        assertFalse(PhaseFlightControlRules.rejectFlightToggle(true, false, false));
        assertFalse(PhaseFlightControlRules.rejectFlightToggle(true, true, true));
        assertTrue(PhaseFlightControlRules.suppressLandingExit(true));
        assertFalse(PhaseFlightControlRules.suppressLandingExit(false));
        assertFalse(PhaseFlightControlRules.intersectsWorldCollision(null));
    }

    @Test
    void phaseLockProjectionMapsArmorSlotsToPlayerInventory() {
        assertEquals(36 + EquipmentSlot.FEET.getIndex(),
                PhaseLockProjectionRules.expectedInventorySlot(EquipmentSlot.FEET));
        assertEquals(36 + EquipmentSlot.HEAD.getIndex(),
                PhaseLockProjectionRules.expectedInventorySlot(EquipmentSlot.HEAD));
        assertEquals(-1, PhaseLockProjectionRules.expectedInventorySlot(EquipmentSlot.MAINHAND));
        assertTrue(PhaseLockProjectionRules.isExpectedSlot(
                EquipmentSlot.CHEST,
                36 + EquipmentSlot.CHEST.getIndex()));
    }

    @Test
    void batchBlockingFollows206ShortCircuitOrder() {
        assertTrue(BatchBlockingPolicy.isBlocked(true, false, false, false, null, null));
        assertFalse(BatchBlockingPolicy.isBlocked(false, false, true, false, null, null));
        assertFalse(BatchBlockingPolicy.isBlocked(false, true, false, false, null, null));
        assertTrue(BatchBlockingPolicy.isBlocked(false, true, true, false, null, null));
        assertFalse(BatchBlockingPolicy.isBlocked(false, true, true, true, null, null));
    }

    @Test
    void batchSamePatternUsesIdentityNotEquality() {
        assertTrue(BatchBlockingPolicy.samePattern(null, null));
    }
}
