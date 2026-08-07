package com.moakiee.ae2lt.logic.craft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MatrixMultiblockPrimitiveContractTest {
    @Test
    void templateKeysMatch206() {
        assertEquals(MatrixMultiblockRole.EMPTY, MatrixMultiblockRole.fromTemplateKey('.'));
        assertEquals(MatrixMultiblockRole.CASING, MatrixMultiblockRole.fromTemplateKey('S'));
        assertEquals(MatrixMultiblockRole.CONSTRAINT_FRAME, MatrixMultiblockRole.fromTemplateKey('P'));
        assertEquals(MatrixMultiblockRole.GLASS, MatrixMultiblockRole.fromTemplateKey('G'));
        assertEquals(MatrixMultiblockRole.CONTROLLER, MatrixMultiblockRole.fromTemplateKey('C'));
        assertEquals(MatrixMultiblockRole.PORT_CANDIDATE, MatrixMultiblockRole.fromTemplateKey('I'));
        assertEquals(MatrixMultiblockRole.PATTERN_BAY, MatrixMultiblockRole.fromTemplateKey('T'));
        assertEquals(MatrixMultiblockRole.CRAFTING_BAY, MatrixMultiblockRole.fromTemplateKey('H'));
        assertThrows(IllegalArgumentException.class, () -> MatrixMultiblockRole.fromTemplateKey('X'));
    }

    @Test
    void portRolesMatch206FormationRules() {
        assertFalse(MatrixMultiblockPortRole.CONTROLLER.exposesAeNetwork());
        assertTrue(MatrixMultiblockPortRole.CONTROLLER.requiredForFormation());
        assertTrue(MatrixMultiblockPortRole.INTERFACE.exposesAeNetwork());
        assertTrue(MatrixMultiblockPortRole.INTERFACE.requiredForFormation());
        assertFalse(MatrixMultiblockPortRole.STRUCTURE.exposesAeNetwork());
        assertFalse(MatrixMultiblockPortRole.STRUCTURE.requiredForFormation());
    }

    @Test
    void scanIssueVocabularyIsPinned() {
        assertEquals(11, MatrixMultiblockScanIssue.values().length);
        assertEquals(MatrixMultiblockScanIssue.CHUNKS_UNLOADED,
                MatrixMultiblockScanIssue.valueOf("CHUNKS_UNLOADED"));
        assertEquals(MatrixMultiblockScanIssue.MULTIDIMENSIONAL_UNIT_NOT_SUPPORTED,
                MatrixMultiblockScanIssue.valueOf("MULTIDIMENSIONAL_UNIT_NOT_SUPPORTED"));
    }
}
