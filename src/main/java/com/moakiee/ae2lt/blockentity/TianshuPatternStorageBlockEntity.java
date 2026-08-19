package com.moakiee.ae2lt.blockentity;

import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import com.moakiee.ae2lt.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * AE2のPatternProvider実装を再利用した36スロット保管です。
 * インベントリ・端末同期・パターンデコードを独自実装せず、AE2の既存処理を正とします。
 */
public final class TianshuPatternStorageBlockEntity extends PatternProviderBlockEntity {
    // 1保管ユニットのAE2標準パターンスロット数です。
    public static final int SLOT_COUNT = 36;
    private static final String TAG_CONTROLLER = "ControllerPos";
    @Nullable
    private BlockPos controllerPos;

    public TianshuPatternStorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TIANSHU_PATTERN_STORAGE.get(), pos, state);
    }

    @Override
    protected PatternProviderLogic createLogic() {
        // AE2標準の9枠ではなく、Tianshuの1ユニット分36枠を使います。
        return new PatternProviderLogic(getMainNode(), this, SLOT_COUNT);
    }

    @Override
    public Set<Direction> getGridConnectableSides(appeng.api.orientation.BlockOrientation orientation) {
        // 構造内部の保管枠は独立ケーブルを持たず、コントローラのグリッドを参照します。
        return Collections.emptySet();
    }

    @Override
    public EnumSet<Direction> getTargets() {
        return EnumSet.noneOf(Direction.class);
    }

    @Override
    public IGrid getGrid() {
        if (level != null && controllerPos != null
                && level.getBlockEntity(controllerPos) instanceof TianshuSupercomputerControllerBlockEntity controller) {
            return controller.getGrid();
        }
        return null;
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return getLogic().getPatternInv();
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        return level == null ? PatternContainerGroup.nothing()
                : PatternContainerGroup.fromMachine(level, worldPosition, Direction.UP);
    }

    @Override
    public PatternProviderLogic getLogic() {
        return super.getLogic();
    }

    public void setControllerBinding(@Nullable BlockPos pos) {
        controllerPos = pos == null ? null : pos.immutable();
        setChanged();
    }

    @Nullable
    public BlockPos controllerPos() {
        return controllerPos;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (controllerPos != null) {
            tag.putLong(TAG_CONTROLLER, controllerPos.asLong());
        }
    }

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
        controllerPos = tag.contains(TAG_CONTROLLER) ? BlockPos.of(tag.getLong(TAG_CONTROLLER)) : null;
    }
}
