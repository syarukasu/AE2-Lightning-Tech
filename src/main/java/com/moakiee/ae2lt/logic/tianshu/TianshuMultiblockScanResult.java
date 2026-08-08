package com.moakiee.ae2lt.logic.tianshu;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** 形成済みTianshuの座標と計算プロファイルを保持します。 */
public record TianshuMultiblockScanResult(
        BlockPos controllerPos,
        Direction orientation,
        BlockPos minPos,
        BlockPos maxPos,
        BlockPos portPos,
        List<BlockPos> members,
        List<BlockPos> corePositions,
        List<BlockPos> patternStoragePositions,
        List<BlockPos> seedStoragePositions,
        CpuInternalCoreProfile coreProfile,
        TianshuFunctionProfile functionProfile) {

    public TianshuMultiblockScanResult {
        controllerPos = controllerPos.immutable();
        minPos = minPos.immutable();
        maxPos = maxPos.immutable();
        portPos = portPos.immutable();
        members = List.copyOf(members);
        corePositions = List.copyOf(corePositions);
        patternStoragePositions = List.copyOf(patternStoragePositions);
        seedStoragePositions = List.copyOf(seedStoragePositions);
    }
}
