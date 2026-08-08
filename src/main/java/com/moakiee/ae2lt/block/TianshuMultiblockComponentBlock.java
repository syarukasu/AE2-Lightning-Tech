package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import net.minecraft.world.level.block.state.BlockState;

/** Tianshuスキャナへブロックの構成要素種別を渡す共通契約です。 */
public interface TianshuMultiblockComponentBlock {
    TianshuMultiblockComponent tianshuComponent(BlockState state);
}
