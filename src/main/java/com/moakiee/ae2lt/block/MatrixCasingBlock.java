package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.logic.craft.MatrixMultiblockComponent;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class MatrixCasingBlock extends MatrixFormedBlock {
    public MatrixCasingBlock(BlockBehaviour.Properties properties) {
        super(properties, MatrixMultiblockComponent.MATRIX_CASING);
    }
}
