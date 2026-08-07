package com.moakiee.ae2lt.logic.tianshu;

import com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopValidationResult;
import com.moakiee.ae2lt.logic.tianshu.maintenance.InventoryMaintenanceDecision;
import com.moakiee.ae2lt.logic.tianshu.maintenance.InventoryMaintenanceRule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TianshuDataModelsTest {
    @Test
    void scanAttemptIsFormedOnlyWithResultAndNoIssues() {
        TianshuMultiblockScanResult result = new TianshuMultiblockScanResult(
                BlockPos.ZERO,
                Direction.NORTH,
                BlockPos.ZERO,
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                CpuInternalCoreProfile.empty(),
                TianshuFunctionProfile.empty());

        assertTrue(new TianshuMultiblockScanAttempt(result, List.of()).formed());
        assertFalse(new TianshuMultiblockScanAttempt(null, List.of()).formed());
        assertFalse(new TianshuMultiblockScanAttempt(result, List.of(TianshuMultiblockScanIssue.INVALID_SIZE)).formed());
    }

    @Test
    void scanAttemptReportsChunkUnavailability() {
        TianshuMultiblockScanAttempt attempt = new TianshuMultiblockScanAttempt(
                null,
                List.of(TianshuMultiblockScanIssue.CHUNKS_UNLOADED));
        assertTrue(attempt.chunksUnavailable());
    }

    @Test
    void closedLoopValidationOnlyAcceptsValidStatus() {
        assertTrue(new ClosedLoopValidationResult(ClosedLoopValidationResult.Status.VALID, null).valid());
        assertFalse(new ClosedLoopValidationResult(
                ClosedLoopValidationResult.Status.DECLARATION_MISMATCH,
                null).valid());
    }

    @Test
    void maintenanceRuleRequiresIdentityAndKey() {
        UUID id = UUID.randomUUID();
        assertThrows(NullPointerException.class,
                () -> new InventoryMaintenanceRule(id, null, 0, 1, 1, true, false, null));
        assertThrows(NullPointerException.class,
                () -> new InventoryMaintenanceRule(null, null, 0, 1, 1, true, false, null));
    }

    @Test
    void nullMaintenanceRuleNeverRequestsCrafting() {
        assertEquals(new InventoryMaintenanceDecision(false, 0L),
                InventoryMaintenanceDecision.evaluate(null, 100L, false));
    }
}
