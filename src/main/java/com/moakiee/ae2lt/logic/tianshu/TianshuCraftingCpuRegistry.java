package com.moakiee.ae2lt.logic.tianshu;

import appeng.api.networking.IGrid;
import java.util.ArrayList;
import java.util.List;

/** AE2のサービス側MixinからTianshu CPUを参照するための軽量な探索窓口です。 */
public final class TianshuCraftingCpuRegistry {
    private TianshuCraftingCpuRegistry() {
    }

    public static List<TianshuCraftingCpu> findForGrid(IGrid grid) {
        List<TianshuCraftingCpu> result = new ArrayList<>();
        if (grid == null) {
            return result;
        }
        // AE2が管理するノードだけを走査し、ワールド全体のBlockEntity探索をしません。
        for (var node : grid.getNodes()) {
            TianshuCraftingCpu cpu = node.getService(TianshuCraftingCpu.class);
            if (cpu != null && cpu.isActive() && cpu.getGrid() == grid) {
                result.add(cpu);
            }
        }
        return result;
    }
}
