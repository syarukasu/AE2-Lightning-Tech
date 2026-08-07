package com.moakiee.ae2lt.celestweave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.moakiee.ae2lt.compat.DataEnergisticsTargetPolicy;
import com.moakiee.ae2lt.config.PhaseLockTeleportMode;
import java.util.List;
import org.junit.jupiter.api.Test;

class PortedPolicyRulesTest {
    @Test
    void movementAssistMatches206FallbackRules() {
        assertEquals(1.0, MovementAssistRules.movementMultiplier(true, false, true, 2.0, 3.0, 0.5));
        assertEquals(0.5, MovementAssistRules.movementMultiplier(false, true, true, 2.0, 3.0, 0.5));
        assertEquals(3.0, MovementAssistRules.movementMultiplier(false, false, true, 2.0, 3.0, 0.5));
        assertEquals(2.0, MovementAssistRules.movementMultiplier(false, false, false, 2.0, 3.0, 0.5));
        assertEquals(1.0, MovementAssistRules.movementMultiplier(false, false, false, Double.NaN, 3.0, 0.5));
        assertEquals(1.0, MovementAssistRules.speedModifierAmount(2.0));
        assertEquals(0.0, MovementAssistRules.stepHeightModifierAmount(0.4));
        assertEquals(0.4, MovementAssistRules.stepHeightModifierAmount(1.0), 1.0e-9);
    }

    @Test
    void mekanismProtectionMathMatches206Boundaries() {
        assertTrue(MekanismProtectionRules.shouldRegenerate(20, 2.0, 1.0, 10.0f, 20.0f));
        assertFalse(MekanismProtectionRules.shouldRegenerate(21, 2.0, 1.0, 10.0f, 20.0f));
        assertFalse(MekanismProtectionRules.shouldRegenerate(20, Double.NaN, 1.0, 10.0f, 20.0f));
        assertEquals(2.0f, MekanismProtectionRules.radiationHealing(-1.0));
        assertEquals(10.0f, MekanismProtectionRules.radiationHealing(2.0));
        assertEquals(25L, MekanismProtectionRules.absorbedJoules(100L, 0.25));
        assertEquals(100L, MekanismProtectionRules.absorbedJoules(100L, 2.0));
        assertEquals(25L, MekanismProtectionRules.joulesToForgeEnergy(100L, 4.0));
        assertEquals(0L, MekanismProtectionRules.joulesToForgeEnergy(100L, 0.0));
    }

    @Test
    void dataEnergisticsPolicyOnlyCancelsItsMixinsAgainstAe2ltOwnedTargets() {
        assertTrue(DataEnergisticsTargetPolicy.shouldCancel(
                List.of("com.moakiee.ae2lt.SomeTarget"),
                "com.fish_dan_.data_energistics.mixin.ExampleMixin"));
        assertTrue(DataEnergisticsTargetPolicy.isOwnedTarget("Lcom/moakiee/ae2lt/SomeTarget;"));
        assertFalse(DataEnergisticsTargetPolicy.shouldCancel(
                List.of("appeng.SomeTarget"),
                "com.fish_dan_.data_energistics.mixin.ExampleMixin"));
        assertFalse(DataEnergisticsTargetPolicy.shouldCancel(
                List.of("com.moakiee.ae2lt.SomeTarget"),
                "example.OtherMixin"));
    }

    @Test
    void phaseLockConfigParsingMatches206Defaults() {
        assertEquals(PhaseLockTeleportMode.IGNORE_ALL, PhaseLockTeleportMode.fromConfigValue("ignore-all"));
        assertEquals(PhaseLockTeleportMode.IGNORE_NONE, PhaseLockTeleportMode.fromConfigValue("ignore-none"));
        assertEquals(PhaseLockTeleportMode.IGNORE_COMMAND, PhaseLockTeleportMode.fromConfigValue("unknown"));
        assertTrue(PhaseLockTeleportMode.IGNORE_ALL.disablesProtection());
        assertTrue(PhaseLockTeleportMode.IGNORE_COMMAND.ignoresPrivilegedCommands());
        assertTrue(PhaseLockTeleportMode.isValidConfigValue("ignore-none"));
        assertFalse(PhaseLockTeleportMode.isValidConfigValue(1));
    }
}
