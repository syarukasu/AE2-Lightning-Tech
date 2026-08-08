package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** 閉ループ種保管枠。実インベントリは未接続なので形成判定だけを担当します。 */
public final class TianshuSeedStorageBlock extends TianshuSupercomputingUnitBlock {
    public TianshuSeedStorageBlock(BlockBehaviour.Properties properties) {
        super(properties, TianshuMultiblockComponent.CLOSED_LOOP_SEED_STORAGE);
    }
}
