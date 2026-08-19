package com.moakiee.ae2lt.logic.craft;

import com.moakiee.ae2lt.block.MatrixMultiblockComponentBlock;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * World-facing formation scanner for the 2.0.6 Matrix template.
 *
 * The scanner only reads loaded block states. It never calls a chunk-loading
 * API, so a partially loaded structure remains pending instead of forcing
 * server work or changing the chunk ticket set.
 */
public final class MatrixMultiblockScanner {
    private MatrixMultiblockScanner() {
    }

    public static Optional<MatrixMultiblockScanResult> find(BlockPos controllerPos,
            Direction orientation, ComponentResolver resolver) {
        return Optional.ofNullable(scan(controllerPos, orientation, resolver).result());
    }

    public static Optional<MatrixMultiblockScanResult> findInLevel(BlockGetter level,
            BlockPos controllerPos, Direction orientation) {
        return find(controllerPos, orientation, pos -> componentAt(level, pos));
    }

    public static MatrixMultiblockScanAttempt scan(Level level, BlockPos controllerPos,
            Direction orientation) {
        if (!areRequiredChunksLoaded(level, controllerPos, orientation)) {
            return new MatrixMultiblockScanAttempt(orientation,
                    List.of(MatrixMultiblockScanIssue.CHUNKS_UNLOADED), null);
        }
        return scan(controllerPos, orientation, pos -> componentAt(level, pos));
    }

    public static boolean areRequiredChunksLoaded(Level level, BlockPos controllerPos,
            Direction orientation) {
        return areRequiredChunksLoaded(controllerPos, orientation, level::hasChunkAt);
    }

    static boolean areRequiredChunksLoaded(BlockPos controllerPos, Direction orientation,
            Predicate<BlockPos> loaded) {
        if (orientation == null || orientation.getAxis() == Direction.Axis.Y) {
            return false;
        }

        // The four corners are sufficient because the template is one chunk
        // footprint wide in X/Z and chunk availability is checked by column.
        for (BlockPos corner : List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(MatrixMultiblockTemplate.SIZE_X - 1, 0, 0),
                new BlockPos(0, 0, MatrixMultiblockTemplate.SIZE_Z - 1),
                new BlockPos(MatrixMultiblockTemplate.SIZE_X - 1, 0,
                        MatrixMultiblockTemplate.SIZE_Z - 1))) {
            if (!loaded.test(worldPos(controllerPos, corner, orientation))) {
                return false;
            }
        }
        return true;
    }

    public static MatrixMultiblockScanAttempt scan(BlockPos controllerPos,
            Direction orientation, ComponentResolver resolver) {
        EnumSet<MatrixMultiblockScanIssue> issues = EnumSet.noneOf(MatrixMultiblockScanIssue.class);
        if (controllerPos == null || orientation == null || orientation.getAxis() == Direction.Axis.Y) {
            issues.add(MatrixMultiblockScanIssue.UNEXPECTED_COMPONENT);
            return new MatrixMultiblockScanAttempt(orientation, List.copyOf(issues), null);
        }

        ArrayList<MatrixMultiblockMember> members = new ArrayList<>();
        ArrayList<MatrixMultiblockMember> craftingMembers = new ArrayList<>();
        ArrayList<MatrixMultiblockMember> patternMembers = new ArrayList<>();
        BlockPos portPos = null;
        BlockPos corePos = null;
        int coreCount = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        // Each template entry is resolved exactly once; role validation is
        // deterministic and does not inspect recipes, inventories, or chunks.
        for (MatrixMultiblockTemplate.Entry entry : MatrixMultiblockTemplate.entries()) {
            BlockPos localPos = entry.localPos();
            BlockPos worldPos = worldPos(controllerPos, localPos, orientation);
            MatrixMultiblockComponent component = resolver.componentAt(worldPos);
            if (component == null) {
                component = MatrixMultiblockComponent.OTHER;
            }

            if (!accepts(entry.role(), localPos, component)) {
                issues.add(MatrixMultiblockScanIssue.UNEXPECTED_COMPONENT);
                continue;
            }
            if (component == MatrixMultiblockComponent.AIR) {
                continue;
            }

            MatrixMultiblockMember member = new MatrixMultiblockMember(
                    worldPos, localPos, entry.role(), component);
            members.add(member);
            minX = Math.min(minX, worldPos.getX());
            minY = Math.min(minY, worldPos.getY());
            minZ = Math.min(minZ, worldPos.getZ());
            maxX = Math.max(maxX, worldPos.getX());
            maxY = Math.max(maxY, worldPos.getY());
            maxZ = Math.max(maxZ, worldPos.getZ());

            if (component == MatrixMultiblockComponent.MATRIX_PORT) {
                if (portPos != null) {
                    issues.add(MatrixMultiblockScanIssue.MULTIPLE_PORTS);
                } else {
                    portPos = worldPos;
                }
            }
            if (component.isMainCore()) {
                ++coreCount;
                if (corePos == null) {
                    corePos = worldPos;
                }
                if (!localPos.equals(MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL)) {
                    issues.add(MatrixMultiblockScanIssue.MAIN_CORE_OUTSIDE_CENTER);
                }
            }
            if (entry.role() == MatrixMultiblockRole.CRAFTING_BAY) {
                craftingMembers.add(member);
            }
            if (component.isPatternStorage()) {
                patternMembers.add(member);
            }
        }

        if (portPos == null) {
            issues.add(MatrixMultiblockScanIssue.MISSING_PORT);
        }
        if (coreCount == 0 || corePos == null) {
            issues.add(MatrixMultiblockScanIssue.MISSING_MAIN_CORE);
        }
        if (coreCount > 1) {
            issues.add(MatrixMultiblockScanIssue.UNEXPECTED_COMPONENT);
        }
        if (patternMembers.isEmpty()) {
            issues.add(MatrixMultiblockScanIssue.MISSING_PATTERN_STORAGE);
        }

        MatrixCraftingProfile profile = profileFor(craftingMembers);
        if (profile.hasIssue(MatrixProfileIssue.AMPLIFIER_LIMIT_EXCEEDED)) {
            issues.add(MatrixMultiblockScanIssue.AMPLIFIER_LIMIT_EXCEEDED);
        }
        if (profile.hasIssue(MatrixProfileIssue.MISSING_DISPATCH_UNIT)) {
            issues.add(MatrixMultiblockScanIssue.MISSING_DISPATCH_UNIT);
        }
        if (profile.hasIssue(MatrixProfileIssue.AMPLIFIER_NOT_SUPPORTED)) {
            issues.add(MatrixMultiblockScanIssue.AMPLIFIER_NOT_SUPPORTED);
        }
        if (profile.mode() == MatrixCoreMode.MULTIDIMENSIONAL
                && profile.hasIssue(MatrixProfileIssue.CONFLICTING_CORES)) {
            issues.add(MatrixMultiblockScanIssue.MULTIDIMENSIONAL_UNIT_NOT_SUPPORTED);
        }

        if (!issues.isEmpty() || portPos == null || members.isEmpty()) {
            return new MatrixMultiblockScanAttempt(orientation, List.copyOf(issues), null);
        }

        BlockPos minPos = new BlockPos(minX, minY, minZ);
        BlockPos maxPos = new BlockPos(maxX, maxY, maxZ);
        MatrixMultiblockScanResult result = new MatrixMultiblockScanResult(
                controllerPos, orientation, minPos, maxPos, portPos,
                members, craftingMembers, patternMembers);
        return new MatrixMultiblockScanAttempt(orientation, List.of(), result);
    }

    public static BlockPos worldPos(BlockPos controllerPos, BlockPos localPos,
            Direction orientation) {
        if (orientation == null || orientation.getAxis() == Direction.Axis.Y) {
            throw new IllegalArgumentException("Matrix orientation must be horizontal");
        }
        int x = localPos.getX();
        int y = localPos.getY();
        int z = localPos.getZ();
        return switch (orientation) {
            case NORTH -> controllerPos.offset(x, y, z);
            case EAST -> controllerPos.offset(-z, y, x);
            case SOUTH -> controllerPos.offset(-x, y, -z);
            case WEST -> controllerPos.offset(z, y, -x);
            default -> throw new IllegalArgumentException("Matrix orientation must be horizontal");
        };
    }

    public static Set<BlockPos> candidateControllerPositions(BlockPos changedPos) {
        LinkedHashSet<BlockPos> candidates = new LinkedHashSet<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            // Only non-empty template roles can contain a changed component.
            for (MatrixMultiblockTemplate.Entry entry : MatrixMultiblockTemplate.entries()) {
                if (entry.role() != MatrixMultiblockRole.EMPTY) {
                    candidates.add(controllerPosFor(changedPos, entry.localPos(), direction));
                }
            }
        }
        return Set.copyOf(candidates);
    }

    public static BlockPos controllerPosFor(BlockPos worldPos, BlockPos localPos,
            Direction orientation) {
        if (orientation == null || orientation.getAxis() == Direction.Axis.Y) {
            throw new IllegalArgumentException("Matrix orientation must be horizontal");
        }
        int x = localPos.getX();
        int y = localPos.getY();
        int z = localPos.getZ();
        return switch (orientation) {
            case NORTH -> worldPos.offset(-x, -y, -z);
            case EAST -> worldPos.offset(z, -y, -x);
            case SOUTH -> worldPos.offset(x, -y, z);
            case WEST -> worldPos.offset(-z, -y, x);
            default -> throw new IllegalArgumentException("Matrix orientation must be horizontal");
        };
    }

    public static MatrixMultiblockComponent componentAt(BlockGetter level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.isAir()) {
            return MatrixMultiblockComponent.AIR;
        }
        if (state.getBlock() instanceof MatrixMultiblockComponentBlock componentBlock) {
            return componentBlock.matrixComponent(state);
        }
        return MatrixMultiblockComponent.OTHER;
    }

    private static boolean accepts(MatrixMultiblockRole role, BlockPos localPos,
            MatrixMultiblockComponent component) {
        return switch (role) {
            case EMPTY -> component == MatrixMultiblockComponent.AIR;
            case CASING -> component == MatrixMultiblockComponent.MATRIX_CASING;
            case CONSTRAINT_FRAME -> component == MatrixMultiblockComponent.MATRIX_CONSTRAINT_FRAME;
            case GLASS -> component == MatrixMultiblockComponent.MATRIX_GLASS;
            case CONTROLLER -> component == MatrixMultiblockComponent.MATRIX_CONTROLLER;
            case PORT_CANDIDATE -> component == MatrixMultiblockComponent.MATRIX_PORT
                    || component == MatrixMultiblockComponent.MATRIX_CONSTRAINT_FRAME;
            case PATTERN_BAY -> component == MatrixMultiblockComponent.AIR || component.isPatternStorage();
            case CRAFTING_BAY -> localPos.equals(MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL)
                    ? component.isMainCore()
                    : component.isCraftingUnit();
        };
    }

    private static MatrixCraftingProfile profileFor(List<MatrixMultiblockMember> craftingMembers) {
        ArrayList<MatrixCraftingUnit> units = new ArrayList<>();
        for (MatrixMultiblockMember member : craftingMembers) {
            MatrixCraftingUnit unit = member.component().toCraftingUnit(
                    distanceToCraftingCenter(member.localPos()));
            if (unit != null) {
                units.add(unit);
            }
        }
        return MatrixCraftingProfile.fromUnits(units);
    }

    private static int distanceToCraftingCenter(BlockPos localPos) {
        return Math.abs(localPos.getX() - MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL.getX())
                + Math.abs(localPos.getY() - MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL.getY())
                + Math.abs(localPos.getZ() - MatrixMultiblockTemplate.CRAFTING_CENTER_LOCAL.getZ());
    }

    @FunctionalInterface
    public interface ComponentResolver {
        MatrixMultiblockComponent componentAt(BlockPos pos);
    }
}
