package com.moakiee.ae2lt.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import com.moakiee.ae2lt.blockentity.TianshuSupercomputerControllerBlockEntity;
import com.moakiee.ae2lt.logic.tianshu.TianshuSupercomputerRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/** 形成状態・CPU容量・閉ループパターン数を表示する端末GUIのサーバー側モデルです。 */
public final class TianshuSupercomputerControllerMenu extends AEBaseMenu {
    public static final MenuType<TianshuSupercomputerControllerMenu> TYPE = MenuTypeBuilder
            .create(TianshuSupercomputerControllerMenu::new, TianshuSupercomputerControllerBlockEntity.class)
            .withMenuTitle(host -> Component.translatable("block.ae2lt.tianshu_supercomputer_controller"))
            .build("tianshu_supercomputer_controller");

    @GuiSync(0)
    public int formed;
    @GuiSync(1)
    public int patternCount;
    @GuiSync(2)
    public int parallelism;
    @GuiSync(3)
    public long storageBytes;
    @GuiSync(4)
    public int thunderbolt;
    @GuiSync(5)
    public int busy;

    private final TianshuSupercomputerControllerBlockEntity host;

    public TianshuSupercomputerControllerMenu(int id, Inventory playerInventory,
            TianshuSupercomputerControllerBlockEntity host) {
        super(TYPE, id, playerInventory, host);
        this.host = host;
        updateValues();
    }

    private void updateValues() {
        formed = host.isFormed() ? 1 : 0;
        patternCount = host.getClosedLoopPatterns().size();
        parallelism = host.parallelism();
        storageBytes = host.storageCapacity();
        thunderbolt = TianshuSupercomputerRuntime.thunderboltAvailable() ? 1 : 0;
        busy = host.isCraftingBusy() ? 1 : 0;
    }

    @Override
    public void broadcastChanges() {
        updateValues();
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return !host.isRemoved() && player.distanceToSqr(host.getBlockPos().getCenter()) <= 64.0D;
    }
}
