package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.logic.craft.MatrixMultiblockComponent;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class MatrixGlassBlock extends MatrixFormedBlock {
    public MatrixGlassBlock(BlockBehaviour.Properties properties) {
        super(properties, MatrixMultiblockComponent.MATRIX_GLASS);
    }
}
