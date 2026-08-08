package com.moakiee.ae2lt.logic.tianshu;

import appeng.api.inventories.InternalInventory;
import com.moakiee.ae2lt.blockentity.TianshuPatternStorageBlockEntity;
import com.moakiee.ae2lt.blockentity.TianshuSupercomputerControllerBlockEntity;
import com.moakiee.ae2lt.blockentity.TianshuSupercomputerPortBlockEntity;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** 複数のTianshuパターン保管ユニットをAE2端末向けに1つへ束ねるビューです。 */
public final class TianshuPatternInventoryView implements InternalInventory {
    private final TianshuSupercomputerPortBlockEntity port;

    public TianshuPatternInventoryView(TianshuSupercomputerPortBlockEntity port) {
        this.port = port;
    }

    @Override
    public int size() {
        return storageEntities().size() * TianshuPatternStorageBlockEntity.SLOT_COUNT;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        SlotRef ref = resolve(slot);
        return ref == null ? ItemStack.EMPTY : ref.inventory().getStackInSlot(ref.localSlot());
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        SlotRef ref = resolve(slot);
        if (ref == null) {
            return;
        }
        ref.inventory().setItemDirect(ref.localSlot(), stack);
        ref.storage().setChanged();
        TianshuSupercomputerControllerBlockEntity controller = port.getController();
        if (controller != null) {
            controller.patternsChanged();
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.isEmpty() || appeng.api.crafting.PatternDetailsHelper.isEncodedPattern(stack);
    }

    private List<TianshuPatternStorageBlockEntity> storageEntities() {
        TianshuSupercomputerControllerBlockEntity controller = port.getController();
        return controller == null ? List.of() : controller.getPatternStorageEntities();
    }

    private SlotRef resolve(int slot) {
        if (slot < 0) {
            return null;
        }
        List<TianshuPatternStorageBlockEntity> storages = storageEntities();
        int storageIndex = slot / TianshuPatternStorageBlockEntity.SLOT_COUNT;
        int localSlot = slot % TianshuPatternStorageBlockEntity.SLOT_COUNT;
        if (storageIndex >= storages.size()) {
            return null;
        }
        TianshuPatternStorageBlockEntity storage = storages.get(storageIndex);
        return new SlotRef(storage, storage.getTerminalPatternInventory(), localSlot);
    }

    private record SlotRef(TianshuPatternStorageBlockEntity storage,
            InternalInventory inventory, int localSlot) {
    }
}
