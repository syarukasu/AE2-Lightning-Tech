package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** 閉ループパターン保管枠。AE端末連携は別ポートで実装します。 */
public final class TianshuPatternStorageBlock extends TianshuSupercomputingUnitBlock {
    public TianshuPatternStorageBlock(BlockBehaviour.Properties properties) {
        super(properties, TianshuMultiblockComponent.CLOSED_LOOP_PATTERN_STORAGE);
    }
}
