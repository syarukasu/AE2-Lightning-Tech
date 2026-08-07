package com.moakiee.ae2lt.logic.craft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatrixStructureModelTest {
    @Test
    void templateHasExact206DimensionsAndRoleCounts() {
        assertEquals(539, MatrixMultiblockTemplate.entries().size());
        assertEquals(MatrixMultiblockRole.CONTROLLER,
                MatrixMultiblockTemplate.roleAt(MatrixMultiblockTemplate.CONTROLLER_LOCAL));
        assertEquals(MatrixMultiblockRole.CRAFTING_BAY,
                MatrixMultiblockTemplate.roleAt(MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL));

        Map<MatrixMultiblockRole, Integer> counts = new EnumMap<>(MatrixMultiblockRole.class);
        for (MatrixMultiblockTemplate.Entry entry : MatrixMultiblockTemplate.entries()) {
            counts.merge(entry.role(), 1, Integer::sum);
        }

        assertEquals(56, counts.get(MatrixMultiblockRole.EMPTY));
        assertEquals(174, counts.get(MatrixMultiblockRole.CASING));
        assertEquals(130, counts.get(MatrixMultiblockRole.CONSTRAINT_FRAME));
        assertEquals(44, counts.get(MatrixMultiblockRole.GLASS));
        assertEquals(1, counts.get(MatrixMultiblockRole.CONTROLLER));
        assertEquals(3, counts.get(MatrixMultiblockRole.PORT_CANDIDATE));
        assertEquals(50, counts.get(MatrixMultiblockRole.PATTERN_BAY));
        assertEquals(81, counts.get(MatrixMultiblockRole.CRAFTING_BAY));
    }

    @Test
    void scanResultBuildsCraftingProfileAndPatternRepository() {
        MatrixMultiblockMember controller = new MatrixMultiblockMember(
                new BlockPos(10, 64, 10),
                MatrixMultiblockTemplate.CONTROLLER_LOCAL,
                MatrixMultiblockRole.CONTROLLER,
                MatrixMultiblockComponent.MATRIX_CONTROLLER);
        MatrixMultiblockMember core = new MatrixMultiblockMember(
                new BlockPos(13, 64, 10),
                MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL,
                MatrixMultiblockRole.CRAFTING_BAY,
                MatrixMultiblockComponent.QUANTUM_MAIN_CORE);
        MatrixMultiblockMember thread = new MatrixMultiblockMember(
                new BlockPos(13, 64, 11),
                MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL.offset(0, 0, 1),
                MatrixMultiblockRole.CRAFTING_BAY,
                MatrixMultiblockComponent.THREAD_UNIT_T1);
        MatrixMultiblockMember storageT1 = new MatrixMultiblockMember(
                new BlockPos(11, 60, 10),
                new BlockPos(1, 1, 1),
                MatrixMultiblockRole.PATTERN_BAY,
                MatrixMultiblockComponent.PATTERN_STORAGE_T1);
        MatrixMultiblockMember storageT2 = new MatrixMultiblockMember(
                new BlockPos(12, 60, 10),
                new BlockPos(2, 1, 1),
                MatrixMultiblockRole.PATTERN_BAY,
                MatrixMultiblockComponent.PATTERN_STORAGE_T2);

        List<MatrixMultiblockMember> members = List.of(controller, core, thread, storageT1, storageT2);
        MatrixMultiblockScanResult result = new MatrixMultiblockScanResult(
                controller.worldPos(),
                Direction.NORTH,
                new BlockPos(10, 59, 7),
                new BlockPos(16, 69, 13),
                new BlockPos(10, 64, 9),
                members,
                List.of(core, thread),
                List.of(storageT1, storageT2));

        assertTrue(result.craftingProfile().isValid());
        assertEquals(MatrixCoreMode.QUANTUM, result.craftingProfile().mode());
        assertEquals(List.of(MatrixPatternStorageTier.T1, MatrixPatternStorageTier.T2), result.patternStorageTiers());
        assertEquals(108, result.createEmptyPatternRepository().capacity());
    }

    @Test
    void clusterFreezesMembershipAndInvalidatesProfileWhenDestroyed() {
        BlockPos controllerPos = new BlockPos(0, 0, 0);
        MatrixMultiblockMember controller = new MatrixMultiblockMember(
                controllerPos,
                MatrixMultiblockTemplate.CONTROLLER_LOCAL,
                MatrixMultiblockRole.CONTROLLER,
                MatrixMultiblockComponent.MATRIX_CONTROLLER);
        MatrixMultiblockMember core = new MatrixMultiblockMember(
                new BlockPos(1, 0, 0),
                MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL,
                MatrixMultiblockRole.CRAFTING_BAY,
                MatrixMultiblockComponent.MULTIDIMENSIONAL_MAIN_CORE);
        ArrayList<MatrixMultiblockMember> sourceMembers = new ArrayList<>(List.of(controller, core));
        MatrixMultiblockScanResult result = new MatrixMultiblockScanResult(
                controllerPos,
                Direction.NORTH,
                BlockPos.ZERO,
                new BlockPos(6, 10, 6),
                new BlockPos(0, 5, 2),
                sourceMembers,
                List.of(core),
                List.of());
        MatrixStructureCluster cluster = new MatrixStructureCluster(result);

        sourceMembers.clear();
        assertTrue(cluster.contains(controllerPos));
        assertTrue(cluster.craftingProfile().isValid());
        assertEquals(MatrixCoreMode.MULTIDIMENSIONAL, cluster.craftingProfile().mode());

        cluster.destroy();
        assertTrue(cluster.isDestroyed());
        assertFalse(cluster.isFormed());
        assertEquals(MatrixCoreMode.NONE, cluster.craftingProfile().mode());
        assertTrue(cluster.patternRepository().units().isEmpty());
    }

    @Test
    void scanAttemptCopiesIssuesAndReportsChunkState() {
        ArrayList<MatrixMultiblockScanIssue> issues = new ArrayList<>();
        issues.add(MatrixMultiblockScanIssue.CHUNKS_UNLOADED);
        MatrixMultiblockScanAttempt attempt = new MatrixMultiblockScanAttempt(Direction.WEST, issues, null);
        issues.clear();

        assertFalse(attempt.formed());
        assertTrue(attempt.chunksUnavailable());
        assertEquals(List.of(MatrixMultiblockScanIssue.CHUNKS_UNLOADED), attempt.issues());
    }

    @Test
    void patternStorageUpgradePreservesCapacityContract() {
        MatrixPatternStorageUnit t1 = MatrixPatternStorageUnit.t1();
        assertEquals(36, t1.capacity());
        assertTrue(t1.canUpgradeToT2());

        MatrixPatternStorageUnit t2 = t1.upgradeToT2();
        assertEquals(MatrixPatternStorageTier.T2, t2.tier());
        assertEquals(72, t2.capacity());
        assertFalse(t2.canUpgradeToT2());
    }
}
