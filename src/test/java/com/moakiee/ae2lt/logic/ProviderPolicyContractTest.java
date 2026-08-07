package com.moakiee.ae2lt.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.moakiee.ae2lt.api.patternprovider.WirelessPatternProviderPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ProviderPolicyContractTest {
    @AfterEach
    void resetWirelessDistance() {
        WirelessPatternProviderPolicy.setMaxDistanceSupplier(() -> 0);
    }

    @Test
    void wirelessDistanceUsesSupplierAndClampsNegativeValues() {
        WirelessPatternProviderPolicy.setMaxDistanceSupplier(() -> 128);
        assertEquals(128, WirelessPatternProviderPolicy.maxDistance());
        WirelessPatternProviderPolicy.setMaxDistanceSupplier(() -> -10);
        assertEquals(0, WirelessPatternProviderPolicy.maxDistance());
        assertThrows(IllegalArgumentException.class,
                () -> WirelessPatternProviderPolicy.setMaxDistanceSupplier(null));
    }

    @Test
    void outputReturnActivityMatches206SweepContract() {
        assertTrue(OutputReturnResult.EXTRACTED.keepsSweepActive());
        assertTrue(OutputReturnResult.BLOCKED.keepsSweepActive());
        assertFalse(OutputReturnResult.EMPTY.keepsSweepActive());
        assertFalse(OutputReturnResult.UNAVAILABLE.keepsSweepActive());
    }
}
