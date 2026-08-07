package com.moakiee.ae2lt.logic.craft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatrixMultiblockComponentTest {
    @Test
    void storageAndCoreClassificationMatches206() {
        assertTrue(MatrixMultiblockComponent.PATTERN_STORAGE_T1.isPatternStorage());
        assertTrue(MatrixMultiblockComponent.PATTERN_STORAGE_T2.isPatternStorage());
        assertFalse(MatrixMultiblockComponent.MATRIX_CASING.isPatternStorage());

        assertTrue(MatrixMultiblockComponent.STABLE_MAIN_CORE.isMainCore());
        assertTrue(MatrixMultiblockComponent.MULTIDIMENSIONAL_MAIN_CORE.isMainCore());
        assertFalse(MatrixMultiblockComponent.THREAD_UNIT_T1.isMainCore());
    }

    @Test
    void craftingUnitMappingPreservesDistanceForCoolers() {
        MatrixCraftingUnit cooler = MatrixMultiblockComponent.THERMAL_CONTROL_UNIT_T2.toCraftingUnit(3);
        assertNotNull(cooler);
        assertEquals(MatrixCraftingUnit.Kind.COOLER, cooler.kind());
        assertEquals(2, cooler.power());
        assertEquals(3, cooler.distance());
        assertEquals(1.0, cooler.adjustedCoolPower());
    }

    @Test
    void patternStorageTierMappingMatchesComponentTier() {
        assertEquals(MatrixPatternStorageTier.T1, MatrixMultiblockComponent.PATTERN_STORAGE_T1.patternStorageTier());
        assertEquals(MatrixPatternStorageTier.T2, MatrixMultiblockComponent.PATTERN_STORAGE_T2.patternStorageTier());
        assertNull(MatrixMultiblockComponent.MATRIX_PORT.patternStorageTier());
    }

    @Test
    void blankUnitIsCraftingUnitButHasNoRuntimeContribution() {
        assertTrue(MatrixMultiblockComponent.BLANK_UNIT.isCraftingUnit());
        assertNull(MatrixMultiblockComponent.BLANK_UNIT.toCraftingUnit(0));
    }
}
