package com.moakiee.ae2lt.logic.craft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class MatrixMultiblockScannerTest {
    @Test
    void validTemplateFormsWithOnePortAndOnePatternStorage() {
        Map<BlockPos, MatrixMultiblockComponent> components = validComponents();

        MatrixMultiblockScanAttempt attempt = MatrixMultiblockScanner.scan(
                BlockPos.ZERO, Direction.NORTH, pos -> components.getOrDefault(pos, MatrixMultiblockComponent.AIR));

        assertTrue(attempt.formed(), () -> "unexpected issues: " + attempt.issues());
        assertEquals(BlockPos.ZERO, attempt.result().controllerPos());
        assertEquals(1, attempt.result().patternStorageTiers().size());
    }

    @Test
    void missingPortDoesNotForm() {
        Map<BlockPos, MatrixMultiblockComponent> components = validComponents();
        components.entrySet().removeIf(entry -> entry.getValue() == MatrixMultiblockComponent.MATRIX_PORT);

        MatrixMultiblockScanAttempt attempt = MatrixMultiblockScanner.scan(
                BlockPos.ZERO, Direction.NORTH, pos -> components.getOrDefault(pos, MatrixMultiblockComponent.AIR));

        assertTrue(attempt.issues().contains(MatrixMultiblockScanIssue.MISSING_PORT));
        assertTrue(!attempt.formed());
    }

    @Test
    void coordinateTransformsRoundTripForAllHorizontalDirections() {
        BlockPos controller = new BlockPos(40, 80, -12);
        BlockPos local = new BlockPos(6, 10, 5);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos world = MatrixMultiblockScanner.worldPos(controller, local, direction);
            assertEquals(controller, MatrixMultiblockScanner.controllerPosFor(world, local, direction));
        }
    }

    private static Map<BlockPos, MatrixMultiblockComponent> validComponents() {
        Map<BlockPos, MatrixMultiblockComponent> components = new HashMap<>();
        boolean portPlaced = false;
        boolean storagePlaced = false;
        for (MatrixMultiblockTemplate.Entry entry : MatrixMultiblockTemplate.entries()) {
            MatrixMultiblockComponent component = switch (entry.role()) {
                case EMPTY -> MatrixMultiblockComponent.AIR;
                case CASING -> MatrixMultiblockComponent.MATRIX_CASING;
                case CONSTRAINT_FRAME -> MatrixMultiblockComponent.MATRIX_CONSTRAINT_FRAME;
                case GLASS -> MatrixMultiblockComponent.MATRIX_GLASS;
                case CONTROLLER -> MatrixMultiblockComponent.MATRIX_CONTROLLER;
                case PORT_CANDIDATE -> {
                    if (!portPlaced) {
                        portPlaced = true;
                        yield MatrixMultiblockComponent.MATRIX_PORT;
                    }
                    yield MatrixMultiblockComponent.MATRIX_CONSTRAINT_FRAME;
                }
                case PATTERN_BAY -> {
                    if (!storagePlaced) {
                        storagePlaced = true;
                        yield MatrixMultiblockComponent.PATTERN_STORAGE_T1;
                    }
                    yield MatrixMultiblockComponent.AIR;
                }
                case CRAFTING_BAY -> entry.localPos().equals(MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL)
                        ? MatrixMultiblockComponent.QUANTUM_MAIN_CORE
                        : MatrixMultiblockComponent.THREAD_UNIT_T1;
            };
            if (component != MatrixMultiblockComponent.AIR) {
                components.put(entry.localPos(), component);
            }
        }
        return components;
    }
}
