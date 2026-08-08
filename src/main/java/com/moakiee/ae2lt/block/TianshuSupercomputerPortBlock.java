package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.blockentity.TianshuSupercomputerPortBlockEntity;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import com.moakiee.ae2lt.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** TianshuをAE2へ公開する形成ポートです。 */
public final class TianshuSupercomputerPortBlock extends TianshuSupercomputerStructureBlock
        implements EntityBlock {
    public TianshuSupercomputerPortBlock(BlockBehaviour.Properties properties) {
        super(properties, TianshuMultiblockComponent.PORT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TianshuSupercomputerPortBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.TIANSHU_PORT.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                TianshuSupercomputerPortBlockEntity.serverTick(
                        tickLevel, tickPos, tickState,
                        (TianshuSupercomputerPortBlockEntity) blockEntity);
    }
}
