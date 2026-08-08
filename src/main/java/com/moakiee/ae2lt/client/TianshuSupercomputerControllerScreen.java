package com.moakiee.ae2lt.client;

import com.moakiee.ae2lt.menu.TianshuSupercomputerControllerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** TianshuのAE2接続状態とCPU実効値を表示する簡潔な端末画面です。 */
public final class TianshuSupercomputerControllerScreen
        extends AbstractContainerScreen<TianshuSupercomputerControllerMenu> {
    public TianshuSupercomputerControllerScreen(TianshuSupercomputerControllerMenu menu,
            Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 220;
        imageHeight = 120;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF20232B);
        graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4,
                0xFF343844);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 10, 10, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable(
                menu.formed == 1 ? "ae2lt.tianshu.gui.formed" : "ae2lt.tianshu.gui.unformed"),
                10, 28, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("ae2lt.tianshu.gui.patterns", menu.patternCount),
                10, 43, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("ae2lt.tianshu.gui.parallelism", menu.parallelism),
                10, 58, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("ae2lt.tianshu.gui.storage", menu.storageBytes),
                10, 73, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable(
                menu.thunderbolt == 1 ? "ae2lt.tianshu.gui.thunderbolt.available"
                        : "ae2lt.tianshu.gui.thunderbolt.unavailable"),
                10, 88, 0xFFFFFF, false);
    }
}
