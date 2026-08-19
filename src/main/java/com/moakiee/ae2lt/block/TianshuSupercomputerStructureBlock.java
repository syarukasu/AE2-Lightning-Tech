package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.blockentity.TianshuSupercomputerControllerBlockEntity;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** 共通の形成状態と構造変更通知を持つTianshu構成ブロックです。 */
public class TianshuSupercomputerStructureBlock extends Block
        implements TianshuMultiblockComponentBlock {
    private final TianshuMultiblockComponent component;

    public TianshuSupercomputerStructureBlock(BlockBehaviour.Properties properties,
            TianshuMultiblockComponent component) {
        super(properties);
        this.component = component;
        registerDefaultState(defaultBlockState().setValue(MultiblockStateProperties.FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MultiblockStateProperties.FORMED);
    }

    @Override
    public TianshuMultiblockComponent tianshuComponent(BlockState state) {
        return component;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState,
            boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        notifyTianshuControllers(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
            boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            notifyTianshuControllers(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        notifyTianshuControllers(level, pos);
    }

    protected static void notifyTianshuControllers(Level level, BlockPos changedPos) {
        if (level.isClientSide()) {
            return;
        }
        // 部品変更時だけ候補を調べ、ワールド全体の定期走査を避けます。
        for (BlockPos candidate : TianshuMultiblockScanner.candidateControllerPositions(changedPos)) {
            if (!level.hasChunkAt(candidate)) {
                continue;
            }
            if (level.getBlockEntity(candidate) instanceof TianshuSupercomputerControllerBlockEntity controller) {
                controller.scheduleStructureCheck();
            }
        }
    }
}
