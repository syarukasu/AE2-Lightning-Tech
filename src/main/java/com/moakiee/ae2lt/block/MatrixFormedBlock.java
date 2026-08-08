package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.blockentity.MatrixControllerBlockEntity;
import com.moakiee.ae2lt.logic.craft.MatrixMultiblockComponent;
import com.moakiee.ae2lt.logic.craft.MatrixMultiblockScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** Common formed-state and structure-change notification for Matrix parts. */
public class MatrixFormedBlock extends Block implements MatrixMultiblockComponentBlock {
    private final MatrixMultiblockComponent component;

    public MatrixFormedBlock(BlockBehaviour.Properties properties, MatrixMultiblockComponent component) {
        super(properties);
        this.component = component;
        registerDefaultState(defaultBlockState().setValue(MultiblockStateProperties.FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MultiblockStateProperties.FORMED);
    }

    @Override
    public MatrixMultiblockComponent matrixComponent(BlockState state) {
        return component;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        notifyMatrixControllers(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            notifyMatrixControllers(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        notifyMatrixControllers(level, pos);
    }

    protected static void notifyMatrixControllers(Level level, BlockPos changedPos) {
        if (level.isClientSide()) {
            return;
        }

        // Each candidate is checked only when a component changes; there is no
        // world-wide polling loop and no chunk is loaded by this notification.
        for (BlockPos candidate : MatrixMultiblockScanner.candidateControllerPositions(changedPos)) {
            if (!level.hasChunkAt(candidate)) {
                continue;
            }
            if (level.getBlockEntity(candidate) instanceof MatrixControllerBlockEntity controller) {
                controller.scheduleStructureCheck();
            }
        }
    }
}
