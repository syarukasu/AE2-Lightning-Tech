package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class TianshuSupercomputerGlassBlock extends TianshuSupercomputerStructureBlock {
    public TianshuSupercomputerGlassBlock(BlockBehaviour.Properties properties) {
        super(properties, TianshuMultiblockComponent.GLASS);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, net.minecraft.core.Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
