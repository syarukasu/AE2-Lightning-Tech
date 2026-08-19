package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.blockentity.TianshuPatternStorageBlockEntity;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** AE2端末から編集できる閉ループパターン保管枠です。 */
public final class TianshuPatternStorageBlock extends TianshuSupercomputingUnitBlock
        implements EntityBlock {
    public TianshuPatternStorageBlock(BlockBehaviour.Properties properties) {
        super(properties, TianshuMultiblockComponent.CLOSED_LOOP_PATTERN_STORAGE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TianshuPatternStorageBlockEntity(pos, state);
    }
}
