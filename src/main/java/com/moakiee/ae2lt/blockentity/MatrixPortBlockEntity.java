package com.moakiee.ae2lt.blockentity;

import com.moakiee.ae2lt.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class MatrixPortBlockEntity extends BlockEntity {
    private static final String TAG_CONTROLLER = "MatrixController";
    @Nullable
    private BlockPos controllerPos;

    public MatrixPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATRIX_PORT.get(), pos, state);
    }

    public void setControllerBinding(@Nullable BlockPos controllerPos) {
        this.controllerPos = controllerPos == null ? null : controllerPos.immutable();
        setChanged();
    }

    @Nullable
    public BlockPos controllerPos() {
        return controllerPos;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (controllerPos != null) {
            tag.putLong(TAG_CONTROLLER, controllerPos.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        controllerPos = tag.contains(TAG_CONTROLLER)
                ? BlockPos.of(tag.getLong(TAG_CONTROLLER))
                : null;
    }
}
