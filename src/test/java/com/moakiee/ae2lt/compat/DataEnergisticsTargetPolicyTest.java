package com.moakiee.ae2lt.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DataEnergisticsTargetPolicyTest {
    @Test
    void legacyAe2ltPathingMixinIsAlwaysCancelled() {
        assertTrue(DataEnergisticsTargetPolicy.shouldCancel(
                List.of(),
                "com.fish_dan_.data_energistics.mixin.ae2lt.Ae2ltPathingCalculationCompatMixin"));
    }

    @Test
    void foreignTargetsRemainUntouched() {
        assertFalse(DataEnergisticsTargetPolicy.shouldCancel(
                List.of("appeng.me.pathfinding.PathingCalculation"),
                "com.fish_dan_.data_energistics.mixin.SomeOtherMixin"));
    }
}
