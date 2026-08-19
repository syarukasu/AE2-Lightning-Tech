package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.blockentity.TianshuSeedStorageBlockEntity;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** 閉ループ種保管枠です。在庫の正は接続されたAE2ストレージです。 */
public final class TianshuSeedStorageBlock extends TianshuSupercomputingUnitBlock
        implements EntityBlock {
    public TianshuSeedStorageBlock(BlockBehaviour.Properties properties) {
        super(properties, TianshuMultiblockComponent.CLOSED_LOOP_SEED_STORAGE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TianshuSeedStorageBlockEntity(pos, state);
    }
}
