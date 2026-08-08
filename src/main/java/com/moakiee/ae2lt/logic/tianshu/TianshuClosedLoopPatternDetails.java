package com.moakiee.ae2lt.logic.tianshu;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.level.Level;

/**
 * AE2の通常パターンをTianshuの閉ループ実行対象として扱う薄いビューです。
 * 元の入力・出力・定義をそのまま返すため、AE2の計算結果とレシピの意味は変えません。
 */
public final class TianshuClosedLoopPatternDetails implements IPatternDetails,
        com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopBatchPatternDetails {
    private final IPatternDetails delegate;

    public TianshuClosedLoopPatternDetails(IPatternDetails delegate) {
        this.delegate = delegate;
    }

    public IPatternDetails delegate() {
        return delegate;
    }

    @Override
    public AEItemKey getDefinition() {
        return delegate.getDefinition();
    }

    @Override
    public IInput[] getInputs() {
        return delegate.getInputs();
    }

    @Override
    public GenericStack[] getOutputs() {
        return delegate.getOutputs();
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        return false;
    }

    public boolean isValid(AEKey key, Level level) {
        return key != null && delegate.getInputs().length > 0;
    }
}
