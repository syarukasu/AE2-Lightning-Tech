package com.moakiee.ae2lt.logic.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ComputeContractsTest {
    @Test
    void computeTierValuesMatch206Contract() {
        assertEquals(0, ComputeTier.BASELINE.maxAmplifierUnits());
        assertEquals(512, ComputeTier.BASELINE.dispatchCap());
        assertEquals(0x100000L, ComputeTier.BASELINE.internalStorage());
        assertEquals(1024L, ComputeTier.BASELINE.copyCap());
        assertFalse(ComputeTier.BASELINE.multidimensional());

        assertEquals(15, ComputeTier.QUANTUM.maxAmplifierUnits());
        assertEquals(6144, ComputeTier.QUANTUM.dispatchCap());
        assertEquals(0x10000000L, ComputeTier.QUANTUM.internalStorage());
        assertEquals(20480L, ComputeTier.QUANTUM.copyCap());

        assertEquals(15, ComputeTier.OVERLOAD.maxAmplifierUnits());
        assertEquals(16384, ComputeTier.OVERLOAD.dispatchCap());
        assertEquals(0x1000000000L, ComputeTier.OVERLOAD.internalStorage());
        assertEquals(0x400000L, ComputeTier.OVERLOAD.copyCap());

        assertEquals(Long.MAX_VALUE, ComputeTier.MULTIDIMENSIONAL.internalStorage());
        assertEquals(Long.MAX_VALUE, ComputeTier.MULTIDIMENSIONAL.copyCap());
        assertTrue(ComputeTier.MULTIDIMENSIONAL.multidimensional());
    }

    @Test
    void computingUnitTotalsRejectNegativeCounts() {
        assertEquals(new ComputingUnitTotals(1, 2, 3, 4), new ComputingUnitTotals(1, 2, 3, 4));
        assertThrows(IllegalArgumentException.class, () -> new ComputingUnitTotals(-1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ComputingUnitTotals(0, -1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ComputingUnitTotals(0, 0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new ComputingUnitTotals(0, 0, 0, -1));
    }

    @Test
    void matrixEnvelopeClamps206Boundaries() {
        MatrixComputeEnvelope envelope = new MatrixComputeEnvelope(-10, 50000, Double.NaN, false);
        assertEquals(0L, envelope.operationsPerTick());
        assertEquals(MatrixComputeEnvelope.MAX_PROVIDER_CALLS_PER_TICK, envelope.maxProviderCallsPerTick());
        assertEquals(0.0, envelope.thermalEfficiency());

        MatrixComputeEnvelope negative = new MatrixComputeEnvelope(12, -2, -0.5, true);
        assertEquals(12L, negative.operationsPerTick());
        assertEquals(0, negative.maxProviderCallsPerTick());
        assertEquals(0.0, negative.thermalEfficiency());
        assertTrue(negative.unboundedOperations());
    }

    @Test
    void craftingEnvelopeIsAnImmutableCarrier() {
        CraftingComputeEnvelope envelope = new CraftingComputeEnvelope(1024L, 8, 16L, true, false);
        assertEquals(1024L, envelope.storageBytes());
        assertEquals(8, envelope.successfulDispatchesPerTick());
        assertEquals(16L, envelope.maxCopiesPerTick());
        assertTrue(envelope.unboundedBatch());
        assertFalse(envelope.dispatchCapped());
    }
}
