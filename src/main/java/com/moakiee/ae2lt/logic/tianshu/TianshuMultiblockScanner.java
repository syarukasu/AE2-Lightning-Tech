package com.moakiee.ae2lt.logic.tianshu;

import com.moakiee.ae2lt.block.TianshuMultiblockComponentBlock;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/** 2.0.6 Tianshu構造をロード済みチャンクだけで検査します。 */
public final class TianshuMultiblockScanner {
    private TianshuMultiblockScanner() {
    }

    public static TianshuMultiblockScanAttempt scan(Level level, BlockPos controllerPos,
            Direction orientation) {
        if (!areRequiredChunksLoaded(level, controllerPos, orientation)) {
            return new TianshuMultiblockScanAttempt(null,
                    List.of(TianshuMultiblockScanIssue.CHUNKS_UNLOADED));
        }
        return scan(controllerPos, orientation, pos -> componentAt(level, pos));
    }

    public static boolean areRequiredChunksLoaded(Level level, BlockPos controllerPos,
            Direction orientation) {
        return areRequiredChunksLoaded(controllerPos, orientation, level::hasChunkAt);
    }

    static boolean areRequiredChunksLoaded(BlockPos controllerPos, Direction orientation,
            Predicate<BlockPos> loaded) {
        if (controllerPos == null || orientation == null || orientation.getAxis() == Direction.Axis.Y) {
            return false;
        }
        // 7×7の四隅を確認すれば、この構造が占有するチャンク列を判定できます。
        for (BlockPos corner : List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(TianshuMultiblockTemplate.SIZE - 1, 0, 0),
                new BlockPos(0, 0, TianshuMultiblockTemplate.SIZE - 1),
                new BlockPos(TianshuMultiblockTemplate.SIZE - 1, 0,
                        TianshuMultiblockTemplate.SIZE - 1))) {
            if (!loaded.test(worldPos(controllerPos, corner, orientation))) {
                return false;
            }
        }
        return true;
    }

    public static TianshuMultiblockScanAttempt scan(BlockPos controllerPos,
            Direction orientation, Function<BlockPos, TianshuMultiblockComponent> resolver) {
        EnumSet<TianshuMultiblockScanIssue> issues = EnumSet.noneOf(TianshuMultiblockScanIssue.class);
        if (controllerPos == null || orientation == null || orientation.getAxis() == Direction.Axis.Y) {
            issues.add(TianshuMultiblockScanIssue.INVALID_CONTROLLER);
            return new TianshuMultiblockScanAttempt(null, List.copyOf(issues));
        }

        ArrayList<BlockPos> members = new ArrayList<>();
        ArrayList<BlockPos> corePositions = new ArrayList<>();
        ArrayList<BlockPos> patternStoragePositions = new ArrayList<>();
        ArrayList<BlockPos> seedStoragePositions = new ArrayList<>();
        BlockPos portPos = null;
        int mainCoreCount = 0;
        int storageUnits = 0;
        int parallelUnits = 0;
        int amplifierUnits = 0;
        CpuMainCoreTier mainTier = null;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        // 各テンプレート座標を一度だけ解決し、材料・コア・ポートを同時に集計します。
        for (TianshuMultiblockTemplate.Entry entry : TianshuMultiblockTemplate.entries()) {
            BlockPos localPos = entry.localPos();
            BlockPos worldPosition = worldPos(controllerPos, localPos, orientation);
            TianshuMultiblockComponent component = resolver.apply(worldPosition);
            if (component == null) {
                component = TianshuMultiblockComponent.OTHER;
            }

            if (!accepts(entry.role(), localPos, component)) {
                addOnce(issues, issueFor(entry.role()));
                continue;
            }
            if (component == TianshuMultiblockComponent.AIR) {
                continue;
            }

            members.add(worldPosition);
            minX = Math.min(minX, worldPosition.getX());
            minY = Math.min(minY, worldPosition.getY());
            minZ = Math.min(minZ, worldPosition.getZ());
            maxX = Math.max(maxX, worldPosition.getX());
            maxY = Math.max(maxY, worldPosition.getY());
            maxZ = Math.max(maxZ, worldPosition.getZ());

            // ポート候補は必ず1つだけ実ポートでなければなりません。
            if (component == TianshuMultiblockComponent.PORT) {
                if (portPos != null) {
                    issues.add(TianshuMultiblockScanIssue.MULTIPLE_PORTS);
                } else {
                    portPos = worldPosition;
                }
            }
            if (isMainCore(component)) {
                ++mainCoreCount;
                corePositions.add(worldPosition);
                if (mainTier != null || !localPos.equals(TianshuMultiblockTemplate.CORE_CENTER)) {
                    issues.add(TianshuMultiblockScanIssue.MAIN_CORE_OUTSIDE_CENTER);
                } else {
                    mainTier = mainTier(component);
                }
            }
            if (component == TianshuMultiblockComponent.STORAGE_UNIT) {
                ++storageUnits;
            } else if (component == TianshuMultiblockComponent.PARALLEL_UNIT) {
                ++parallelUnits;
            } else if (component == TianshuMultiblockComponent.AMPLIFIER_UNIT) {
                ++amplifierUnits;
            } else if (component == TianshuMultiblockComponent.CLOSED_LOOP_PATTERN_STORAGE) {
                patternStoragePositions.add(worldPosition);
            } else if (component == TianshuMultiblockComponent.CLOSED_LOOP_SEED_STORAGE) {
                seedStoragePositions.add(worldPosition);
            }
        }

        if (portPos == null) {
            issues.add(TianshuMultiblockScanIssue.MISSING_PORT);
        }
        if (mainCoreCount == 0 || mainTier == null) {
            issues.add(TianshuMultiblockScanIssue.MISSING_MAIN_CORE);
        }
        // メインコア以外の中心区画には並列・増幅・ストレージ以外を許可しません。
        if (mainTier != null && mainTier == CpuMainCoreTier.MULTIDIMENSIONAL
                && (storageUnits > 0 || parallelUnits > 0 || amplifierUnits > 0)) {
            issues.add(TianshuMultiblockScanIssue.INVALID_PERIPHERAL_UNIT);
        }
        if (mainTier != CpuMainCoreTier.MULTIDIMENSIONAL && parallelUnits == 0) {
            issues.add(TianshuMultiblockScanIssue.MISSING_PARALLEL_UNIT);
        }
        if (amplifierUnits > 15) {
            issues.add(TianshuMultiblockScanIssue.TOO_MANY_AMPLIFIER_UNITS);
        }
        // BaselineとMultidimensionalは増幅ユニットを受け付けません。
        if ((mainTier == CpuMainCoreTier.BASELINE || mainTier == CpuMainCoreTier.MULTIDIMENSIONAL)
                && amplifierUnits > 0) {
            issues.add(TianshuMultiblockScanIssue.AMPLIFIER_UNIT_NOT_SUPPORTED);
        }

        // 有限コアだけ計算プロファイルを作り、無限系の不正な補助ユニットを拒否します。
        CpuInternalCoreProfile coreProfile = null;
        if (mainTier != null && issues.stream().noneMatch(issue ->
                issue == TianshuMultiblockScanIssue.INVALID_PERIPHERAL_UNIT
                        || issue == TianshuMultiblockScanIssue.MISSING_PARALLEL_UNIT)) {
            coreProfile = CpuInternalCoreCalculator.calculate(
                    mainTier, storageUnits, parallelUnits, amplifierUnits);
        }
        if (coreProfile == null) {
            coreProfile = CpuInternalCoreProfile.empty();
        }

        if (!issues.isEmpty() || portPos == null || mainTier == null || members.isEmpty()) {
            return new TianshuMultiblockScanAttempt(null, List.copyOf(issues));
        }

        TianshuFunctionProfile functionProfile = new TianshuFunctionProfile(
                patternStoragePositions.size(), seedStoragePositions.size());
        TianshuMultiblockScanResult result = new TianshuMultiblockScanResult(
                controllerPos, orientation,
                new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), portPos,
                members, corePositions, patternStoragePositions, seedStoragePositions,
                coreProfile, functionProfile);
        return new TianshuMultiblockScanAttempt(result, List.of());
    }

    public static TianshuMultiblockComponent componentAt(BlockGetter level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.isAir()) {
            return TianshuMultiblockComponent.AIR;
        }
        if (state.getBlock() instanceof TianshuMultiblockComponentBlock componentBlock) {
            return componentBlock.tianshuComponent(state);
        }
        return TianshuMultiblockComponent.OTHER;
    }

    public static BlockPos worldPos(BlockPos controllerPos, BlockPos localPos,
            Direction orientation) {
        if (orientation == null || orientation.getAxis() == Direction.Axis.Y) {
            throw new IllegalArgumentException("Tianshu controller must face horizontally");
        }
        int x = localPos.getX();
        int y = localPos.getY();
        int z = localPos.getZ();
        return switch (orientation) {
            case NORTH -> controllerPos.offset(x, y, z);
            case EAST -> controllerPos.offset(-z, y, x);
            case SOUTH -> controllerPos.offset(-x, y, -z);
            case WEST -> controllerPos.offset(z, y, -x);
            default -> throw new IllegalArgumentException("Tianshu controller must face horizontally");
        };
    }

    public static Set<BlockPos> candidateControllerPositions(BlockPos changedPos) {
        LinkedHashSet<BlockPos> candidates = new LinkedHashSet<>();
        // 構造部品が変わった時だけ候補を通知し、ワールド全体をポーリングしません。
        for (Direction orientation : Direction.Plane.HORIZONTAL) {
            for (TianshuMultiblockTemplate.Entry entry : TianshuMultiblockTemplate.entries()) {
                candidates.add(controllerPosFor(changedPos, entry.localPos(), orientation));
            }
        }
        return Set.copyOf(candidates);
    }

    public static BlockPos controllerPosFor(BlockPos worldPosition, BlockPos localPos,
            Direction orientation) {
        if (orientation == null || orientation.getAxis() == Direction.Axis.Y) {
            throw new IllegalArgumentException("Tianshu controller must face horizontally");
        }
        int x = localPos.getX();
        int y = localPos.getY();
        int z = localPos.getZ();
        return switch (orientation) {
            case NORTH -> worldPosition.offset(-x, -y, -z);
            case EAST -> worldPosition.offset(z, -y, -x);
            case SOUTH -> worldPosition.offset(x, -y, z);
            case WEST -> worldPosition.offset(-z, -y, x);
            default -> throw new IllegalArgumentException("Tianshu controller must face horizontally");
        };
    }

    private static boolean accepts(TianshuMultiblockRole role, BlockPos localPos,
            TianshuMultiblockComponent component) {
        return switch (role) {
            case CASING -> component == TianshuMultiblockComponent.CASING;
            case GLASS -> component == TianshuMultiblockComponent.GLASS;
            case CONTROLLER -> component == TianshuMultiblockComponent.CONTROLLER;
            case PORT_CANDIDATE -> component == TianshuMultiblockComponent.PORT
                    || component.fillsCoolingPosition();
            case COOLING -> component.fillsCoolingPosition();
            case CORE_RESERVED -> localPos.equals(TianshuMultiblockTemplate.CORE_CENTER)
                    ? isMainCore(component)
                    : component == TianshuMultiblockComponent.BLANK_UNIT
                    || component == TianshuMultiblockComponent.STORAGE_UNIT
                    || component == TianshuMultiblockComponent.PARALLEL_UNIT
                    || component == TianshuMultiblockComponent.AMPLIFIER_UNIT;
            case IGNORED -> component == TianshuMultiblockComponent.AIR;
        };
    }

    private static TianshuMultiblockScanIssue issueFor(TianshuMultiblockRole role) {
        return switch (role) {
            case CASING -> TianshuMultiblockScanIssue.MISSING_CASING;
            case GLASS -> TianshuMultiblockScanIssue.MISSING_GLASS;
            case CONTROLLER -> TianshuMultiblockScanIssue.INVALID_CONTROLLER;
            case COOLING, PORT_CANDIDATE -> TianshuMultiblockScanIssue.MISSING_COOLING;
            case CORE_RESERVED -> TianshuMultiblockScanIssue.INVALID_PERIPHERAL_UNIT;
            case IGNORED -> TianshuMultiblockScanIssue.INVALID_PERIPHERAL_UNIT;
        };
    }

    private static boolean isMainCore(TianshuMultiblockComponent component) {
        return component == TianshuMultiblockComponent.MAIN_BASELINE
                || component == TianshuMultiblockComponent.MAIN_QUANTUM
                || component == TianshuMultiblockComponent.MAIN_OVERLOAD
                || component == TianshuMultiblockComponent.MAIN_MULTIDIMENSIONAL;
    }

    private static CpuMainCoreTier mainTier(TianshuMultiblockComponent component) {
        return switch (component) {
            case MAIN_BASELINE -> CpuMainCoreTier.BASELINE;
            case MAIN_QUANTUM -> CpuMainCoreTier.QUANTUM;
            case MAIN_OVERLOAD -> CpuMainCoreTier.OVERLOAD;
            case MAIN_MULTIDIMENSIONAL -> CpuMainCoreTier.MULTIDIMENSIONAL;
            default -> throw new IllegalArgumentException("Not a Tianshu main core: " + component);
        };
    }

    private static void addOnce(EnumSet<TianshuMultiblockScanIssue> issues,
            TianshuMultiblockScanIssue issue) {
        issues.add(issue);
    }
}
