package com.moakiee.ae2lt.menu;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Menu anchor for the hand-held closed-loop pattern encoder. */
public final class ClosedLoopPatternEncoderHost extends ItemMenuHost {
    public ClosedLoopPatternEncoderHost(Player player, int inventorySlot, ItemStack stack) {
        super(player, inventorySlot, stack);
    }
}
