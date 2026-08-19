package com.moakiee.ae2lt.logic.tianshu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class TianshuMultiblockScannerTest {
    @Test
    void validTemplateFormsWithOnePortAndCoolingPositions() {
        Map<BlockPos, TianshuMultiblockComponent> components = validComponents();

        TianshuMultiblockScanAttempt attempt = TianshuMultiblockScanner.scan(
                BlockPos.ZERO, Direction.NORTH,
                pos -> components.getOrDefault(pos, TianshuMultiblockComponent.AIR));

        assertTrue(attempt.formed(), () -> "unexpected issues: " + attempt.issues());
        assertEquals(1, attempt.result().portPos().equals(new BlockPos(3, 0, 3)) ? 1 : 0);
        assertEquals(1, attempt.result().patternStoragePositions().size());
    }

    @Test
    void missingParallelUnitDoesNotFormFiniteCore() {
        Map<BlockPos, TianshuMultiblockComponent> components = validComponents();
        components.entrySet().removeIf(entry -> entry.getValue() == TianshuMultiblockComponent.PARALLEL_UNIT);

        TianshuMultiblockScanAttempt attempt = TianshuMultiblockScanner.scan(
                BlockPos.ZERO, Direction.NORTH,
                pos -> components.getOrDefault(pos, TianshuMultiblockComponent.AIR));

        assertTrue(attempt.issues().contains(TianshuMultiblockScanIssue.MISSING_PARALLEL_UNIT));
        assertTrue(!attempt.formed());
    }

    @Test
    void coordinateTransformsRoundTripForAllHorizontalDirections() {
        BlockPos controller = new BlockPos(40, 80, -12);
        BlockPos local = new BlockPos(6, 3, 5);
        // 回転後の座標を逆変換して、方向別の基準点を固定します。
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos world = TianshuMultiblockScanner.worldPos(controller, local, direction);
            assertEquals(controller,
                    TianshuMultiblockScanner.controllerPosFor(world, local, direction));
        }
    }

    private static Map<BlockPos, TianshuMultiblockComponent> validComponents() {
        Map<BlockPos, TianshuMultiblockComponent> components = new HashMap<>();
        boolean portPlaced = false;
        boolean patternStoragePlaced = false;
        boolean parallelPlaced = false;
        for (TianshuMultiblockTemplate.Entry entry : TianshuMultiblockTemplate.entries()) {
            TianshuMultiblockComponent component = switch (entry.role()) {
                case CASING -> TianshuMultiblockComponent.CASING;
                case GLASS -> TianshuMultiblockComponent.GLASS;
                case CONTROLLER -> TianshuMultiblockComponent.CONTROLLER;
                case COOLING -> TianshuMultiblockComponent.COOLING;
                case PORT_CANDIDATE -> {
                    if (!portPlaced) {
                        portPlaced = true;
                        yield TianshuMultiblockComponent.PORT;
                    }
                    yield TianshuMultiblockComponent.COOLING;
                }
                case CORE_RESERVED -> {
                    if (entry.localPos().equals(TianshuMultiblockTemplate.CORE_CENTER)) {
                        yield TianshuMultiblockComponent.MAIN_QUANTUM;
                    }
                    if (!parallelPlaced) {
                        parallelPlaced = true;
                        yield TianshuMultiblockComponent.PARALLEL_UNIT;
                    }
                    yield TianshuMultiblockComponent.BLANK_UNIT;
                }
                case IGNORED -> TianshuMultiblockComponent.AIR;
            };
            if (entry.role() == TianshuMultiblockRole.COOLING && !patternStoragePlaced) {
                component = TianshuMultiblockComponent.CLOSED_LOOP_PATTERN_STORAGE;
                patternStoragePlaced = true;
            }
            components.put(entry.localPos(), component);
        }
        return components;
    }
}
