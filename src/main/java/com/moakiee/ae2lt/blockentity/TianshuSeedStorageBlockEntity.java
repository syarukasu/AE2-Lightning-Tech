package com.moakiee.ae2lt.blockentity;

import com.moakiee.ae2lt.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** 種保管枠の形成・所有者情報を保存します。実行時の在庫はAE2ストレージを正とします。 */
public final class TianshuSeedStorageBlockEntity extends BlockEntity {
    private static final String TAG_CONTROLLER = "ControllerPos";
    @Nullable
    private BlockPos controllerPos;

    public TianshuSeedStorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TIANSHU_SEED_STORAGE.get(), pos, state);
    }

    public void setControllerBinding(@Nullable BlockPos pos) {
        controllerPos = pos == null ? null : pos.immutable();
        setChanged();
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
        controllerPos = tag.contains(TAG_CONTROLLER) ? BlockPos.of(tag.getLong(TAG_CONTROLLER)) : null;
    }
}
