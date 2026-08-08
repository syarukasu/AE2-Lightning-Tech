package com.moakiee.ae2lt.client;

import com.moakiee.ae2lt.menu.ClosedLoopPatternEncoderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Minimal vanilla-style screen; all validation and encoding stay server-side. */
public final class ClosedLoopPatternEncoderScreen
        extends AbstractContainerScreen<ClosedLoopPatternEncoderMenu> {
    public ClosedLoopPatternEncoderScreen(ClosedLoopPatternEncoderMenu menu,
            Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 191;
        inventoryLabelX = 8;
        inventoryLabelY = 91;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF20232B);
        graphics.fill(leftPos + 5, topPos + 5, leftPos + imageWidth - 5, topPos + 84, 0xFF343844);
        graphics.fill(leftPos + 110, topPos + 30, leftPos + 146, topPos + 66, 0xFF17191E);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 7, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("ae2lt.tianshu.closed_loop.members"),
                8, 68, 0xD0D0D0, false);
        graphics.drawString(font, Component.translatable("ae2lt.tianshu.closed_loop.result"),
                111, 18, 0xD0D0D0, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFF, false);
    }
}
