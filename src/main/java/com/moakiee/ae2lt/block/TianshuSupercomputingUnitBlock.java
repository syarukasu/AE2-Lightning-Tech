package com.moakiee.ae2lt.block;

import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockComponent;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** コア室と冷却室で使う、形成状態付きのTianshuユニットです。 */
public class TianshuSupercomputingUnitBlock extends TianshuSupercomputerStructureBlock {
    public TianshuSupercomputingUnitBlock(BlockBehaviour.Properties properties,
            TianshuMultiblockComponent component) {
        super(properties, component);
    }
}
