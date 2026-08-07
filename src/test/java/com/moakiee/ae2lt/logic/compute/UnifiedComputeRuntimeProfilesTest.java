package com.moakiee.ae2lt.logic.compute;

import com.moakiee.ae2lt.logic.craft.MatrixCraftingUnit;
import com.moakiee.ae2lt.logic.tianshu.CpuInternalCoreCalculator;
import com.moakiee.ae2lt.logic.tianshu.CpuInternalCoreProfile;
import com.moakiee.ae2lt.logic.tianshu.CpuMainCoreTier;
import com.moakiee.ae2lt.logic.tianshu.TianshuFunctionProfile;
import com.moakiee.ae2lt.logic.tianshu.terminal.ProcessingPatternEncodingType;
import com.moakiee.ae2lt.logic.tianshu.terminal.TianshuTerminalAction;
import com.moakiee.ae2lt.logic.tianshu.terminal.TianshuTerminalCapabilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnifiedComputeRuntimeProfilesTest {
    @Test
    void baselineCpuEnvelopeMatches206Constants() {
        CraftingComputeEnvelope envelope = UnifiedCraftingComputeCalculator.cpuEnvelope(
                ComputeTier.BASELINE,
                new ComputingUnitTotals(4, 0, 2, 0));

        assertEquals(512, envelope.successfulDispatchesPerTick());
        assertEquals(1_048_576L + 2L * 67_108_864L, envelope.storageBytes());
        assertEquals(1_024L, envelope.maxCopiesPerTick());
        assertTrue(envelope.dispatchCapped());
        assertFalse(envelope.unboundedBatch());
    }

    @Test
    void overloadAmplifiersUseSquaredStorageAndCopyGain() {
        ComputingUnitTotals units = new ComputingUnitTotals(1, 2, 1, 0);
        CraftingComputeEnvelope envelope = UnifiedCraftingComputeCalculator.cpuEnvelope(ComputeTier.OVERLOAD, units);

        assertEquals(768, envelope.successfulDispatchesPerTick());
        assertEquals(6L, UnifiedCraftingComputeCalculator.dispatchGain(ComputeTier.OVERLOAD, 2));
        assertEquals(36L, UnifiedCraftingComputeCalculator.storageGain(ComputeTier.OVERLOAD, 2));
        assertEquals(9, UnifiedCraftingComputeCalculator.copyGain(ComputeTier.OVERLOAD, 2));
        assertEquals(6_912L, envelope.maxCopiesPerTick());
    }

    @Test
    void multidimensionalCpuRejectsPeripheralUnits() {
        assertThrows(IllegalArgumentException.class, () -> UnifiedCraftingComputeCalculator.cpuEnvelope(
                ComputeTier.MULTIDIMENSIONAL,
                new ComputingUnitTotals(1, 0, 0, 0)));

        CraftingComputeEnvelope envelope = UnifiedCraftingComputeCalculator.cpuEnvelope(
                ComputeTier.MULTIDIMENSIONAL,
                new ComputingUnitTotals(0, 0, 0, 0));
        assertEquals(Long.MAX_VALUE, envelope.storageBytes());
        assertEquals(Long.MAX_VALUE, envelope.maxCopiesPerTick());
        assertTrue(envelope.unboundedBatch());
    }

    @Test
    void tianshuCpuCalculatorEnforcesPeripheralBudget() {
        CpuInternalCoreProfile profile = CpuInternalCoreCalculator.calculate(CpuMainCoreTier.QUANTUM, 2, 3, 1);
        assertEquals(CpuMainCoreTier.QUANTUM, profile.mainCore());
        assertEquals(2, profile.storageUnitCount());
        assertEquals(3, profile.parallelUnitCount());
        assertEquals(1, profile.amplifierUnitCount());
        assertTrue(profile.successfulDispatchesPerTick() > 0);

        assertThrows(IllegalArgumentException.class,
                () -> CpuInternalCoreCalculator.calculate(CpuMainCoreTier.BASELINE, 10, 10, 7));
    }

    @Test
    void matrixUnitCoolingAndStableOperationsMatch206() {
        assertEquals(1.0, MatrixCraftingUnit.coolingDecay(1));
        assertEquals(0.75, MatrixCraftingUnit.coolingDecay(2));
        assertEquals(0.5, MatrixCraftingUnit.coolingDecay(3));
        assertEquals(0.25, MatrixCraftingUnit.coolingDecay(4));
        assertEquals(0.0, MatrixCraftingUnit.coolingDecay(5));
        assertEquals(1_024L, MatrixCraftingUnit.t1Threader().stableOperationContribution());
        assertEquals(3_584L, MatrixCraftingUnit.t2Threader().stableOperationContribution());
    }

    @Test
    void processingPatternConfigDefensivelyCopiesArrays() {
        int[] directions = {1, 9};
        ProcessingPatternEncodingType.AdvancedConfig config = new ProcessingPatternEncodingType.AdvancedConfig(directions);
        directions[0] = 6;
        assertEquals(1, config.direction(0));
        assertEquals(6, config.direction(1));

        int[] returned = config.directions();
        returned[0] = 4;
        assertEquals(1, config.direction(0));
    }

    @Test
    void tianshuTerminalCapabilitiesFollowFunctionProfile() {
        TianshuTerminalCapabilities absent = TianshuTerminalCapabilities.forTianshu(false, TianshuFunctionProfile.empty());
        assertFalse(absent.hasTianshu());
        assertFalse(absent.allows(TianshuTerminalAction.UPLOAD_CLOSED_LOOP_PATTERN));

        TianshuTerminalCapabilities withoutSeed = TianshuTerminalCapabilities.forTianshu(true, new TianshuFunctionProfile(1, 0));
        assertTrue(withoutSeed.hasTianshu());
        assertFalse(withoutSeed.closedLoopUploadAvailable());
        assertTrue(withoutSeed.inventoryMaintenanceAvailable());

        TianshuTerminalCapabilities complete = TianshuTerminalCapabilities.forTianshu(true, new TianshuFunctionProfile(1, 1));
        assertTrue(complete.closedLoopUploadAvailable());
        assertTrue(complete.allows(TianshuTerminalAction.UPLOAD_CLOSED_LOOP_PATTERN));
        assertTrue(complete.allows(TianshuTerminalAction.CONFIGURE_RESERVED_STOCK));
    }
}
