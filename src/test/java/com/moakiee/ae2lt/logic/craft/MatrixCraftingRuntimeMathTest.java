package com.moakiee.ae2lt.logic.craft;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatrixCraftingRuntimeMathTest {
    @Test
    void stableProfileUsesThreadContributionsAndRejectsAmplifiers() {
        MatrixCraftingProfile profile = MatrixCraftingProfile.fromUnits(List.of(
                MatrixCraftingUnit.stableCore(),
                MatrixCraftingUnit.t1Threader(),
                MatrixCraftingUnit.t2Threader()));

        assertTrue(profile.isValid());
        assertEquals(2, profile.dispatchUnitCount());
        assertEquals(4_608L, profile.stableBaseOperations());
        assertEquals(4_096L, profile.snapshot(0.0).operationsPerTick());

        MatrixCraftingProfile invalid = MatrixCraftingProfile.fromUnits(List.of(
                MatrixCraftingUnit.stableCore(),
                MatrixCraftingUnit.t1Threader(),
                MatrixCraftingUnit.amplifier()));
        assertTrue(invalid.hasIssue(MatrixProfileIssue.AMPLIFIER_NOT_SUPPORTED));
    }

    @Test
    void multipleCoresAreConflicting() {
        MatrixCraftingProfile profile = MatrixCraftingProfile.fromUnits(List.of(
                MatrixCraftingUnit.quantumCore(),
                MatrixCraftingUnit.overloadCore(),
                MatrixCraftingUnit.t1Threader()));

        assertEquals(MatrixCoreMode.CONFLICT, profile.mode());
        assertTrue(profile.hasIssue(MatrixProfileIssue.CONFLICTING_CORES));
        assertFalse(profile.isValid());
    }

    @Test
    void amplifierPowerIsLimitedToFifteen() {
        MatrixCraftingProfile profile = MatrixCraftingProfile.fromUnits(List.of(
                MatrixCraftingUnit.quantumCore(),
                MatrixCraftingUnit.t1Threader(),
                MatrixCraftingUnit.amplifierPower(16)));

        assertEquals(16, profile.amplifierUnitCount());
        assertEquals(15.0, profile.amplifierPower());
        assertTrue(profile.amplifierLimitExceeded());
        assertTrue(profile.hasIssue(MatrixProfileIssue.AMPLIFIER_LIMIT_EXCEEDED));
    }

    @Test
    void multidimensionalCoreIgnoresPeripheralUnits() {
        MatrixCraftingProfile profile = MatrixCraftingProfile.fromUnits(List.of(
                MatrixCraftingUnit.multidimensionalCore(),
                MatrixCraftingUnit.t2Threader(),
                MatrixCraftingUnit.amplifierPower(4),
                MatrixCraftingUnit.t2Cooler(1)));

        assertTrue(profile.isValid());
        assertEquals(MatrixCoreMode.MULTIDIMENSIONAL, profile.mode());
        assertEquals(0.0, profile.threadPower());
        assertEquals(0, profile.amplifierUnitCount());
        assertEquals(Long.MAX_VALUE, profile.snapshot(999.0).operationsPerTick());
        assertEquals(1.0, profile.snapshot(999.0).efficiencyFactor());
    }

    @Test
    void thermalConstantsAndOverloadCurveMatch206() {
        assertEquals(2048.0, MatrixCraftingMath.heatCapacity(0.0));
        assertEquals(2198.0, MatrixCraftingMath.heatCapacity(1.0));
        assertEquals(8.0E-5, MatrixCraftingMath.coolingRate(0.0));
        assertEquals(1.05E-4, MatrixCraftingMath.coolingRate(1.0));
        assertEquals(1.0, MatrixCraftingMath.overloadHeatCurve(0.5), 1.0E-12);
        assertEquals(0.0, MatrixCraftingMath.overloadHeatCurve(0.0), 1.0E-12);
        assertEquals(0.0, MatrixCraftingMath.overloadHeatCurve(1.0), 1.0E-12);
    }

    @Test
    void stableEfficiencyNeverFallsBelowColdFloor() {
        MatrixCraftingMath.Snapshot cold = MatrixCraftingMath.stableSnapshot(0.0, 4_096L, 0.0);
        assertEquals(4_096L, cold.operationsPerTick());
        assertEquals(1.0, cold.efficiencyFactor());

        MatrixCraftingMath.Snapshot hot = MatrixCraftingMath.stableSnapshot(Double.MAX_VALUE, 4_096L, 0.0);
        assertEquals(0.45, hot.efficiencyFactor(), 1.0E-12);
        assertEquals((long) Math.floor(4_096L * 0.45), hot.operationsPerTick());
    }

    @Test
    void heatAdvanceUsesLoadAndCooling() {
        double unloaded = MatrixCraftingMath.advanceHeatForCompletedTick(
                100.0, MatrixCoreMode.QUANTUM, 0, 100, 10.0, 0.0);
        assertTrue(unloaded < 100.0);

        double loaded = MatrixCraftingMath.advanceHeatForCompletedTick(
                100.0, MatrixCoreMode.QUANTUM, 100, 100, 10.0, 0.0);
        assertTrue(loaded > unloaded);

        double overload = MatrixCraftingMath.advanceHeatForCompletedTick(
                100.0, MatrixCoreMode.OVERLOAD, 100, 100, 10.0, 0.0);
        assertTrue(overload > loaded);
    }
}
