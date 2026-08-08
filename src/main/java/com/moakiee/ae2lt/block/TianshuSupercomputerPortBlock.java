package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Tianshuの形成ポート。AEネットワーク接続は別の実装段階です。 */
public final class TianshuSupercomputerPortBlock extends TianshuSupercomputerStructureBlock {
    public TianshuSupercomputerPortBlock(BlockBehaviour.Properties properties) {
        super(properties, TianshuMultiblockComponent.PORT);
    }
}
