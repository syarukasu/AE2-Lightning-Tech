package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.blockentity.MatrixControllerBlockEntity;
import com.moakiee.ae2lt.logic.craft.MatrixMultiblockComponent;
import com.moakiee.ae2lt.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public final class MatrixControllerBlock extends MatrixMultiblockDirectionalBlock implements EntityBlock {
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public MatrixControllerBlock(BlockBehaviour.Properties properties) {
        super(properties, MatrixMultiblockComponent.MATRIX_CONTROLLER);
        registerDefaultState(defaultBlockState()
                .setValue(MultiblockStateProperties.FORMED, false)
                .setValue(WORKING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MatrixControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.MATRIX_CONTROLLER.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                MatrixControllerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (MatrixControllerBlockEntity) blockEntity);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MatrixControllerBlockEntity controller) {
            controller.scheduleStructureCheck();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MatrixControllerBlockEntity controller) {
            controller.invalidateStructure();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
