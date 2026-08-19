package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.blockentity.MatrixPatternStorageBlockEntity;
import com.moakiee.ae2lt.logic.craft.MatrixMultiblockComponent;
import com.moakiee.ae2lt.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class MatrixPatternStorageBlock extends MatrixFormedBlock implements net.minecraft.world.level.block.EntityBlock {
    public MatrixPatternStorageBlock(BlockBehaviour.Properties properties, MatrixMultiblockComponent component) {
        super(properties, component);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MatrixPatternStorageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return null;
    }
}
