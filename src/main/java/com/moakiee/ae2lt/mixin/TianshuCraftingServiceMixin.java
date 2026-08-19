package com.moakiee.ae2lt.mixin;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.me.service.CraftingService;
import com.google.common.collect.ImmutableSet;
import com.moakiee.ae2lt.logic.tianshu.TianshuCraftingCpu;
import com.moakiee.ae2lt.logic.tianshu.TianshuCraftingCpuRegistry;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** AE2のCPU列挙・提出・tickへTianshu CPUを追加します。 */
@Mixin(value = CraftingService.class, remap = false)
public abstract class TianshuCraftingServiceMixin {
    @Shadow
    private IGrid grid;

    @Inject(method = "getCpus", at = @At("RETURN"), cancellable = true)
    private void ae2lt$appendTianshuCpus(CallbackInfoReturnable<ImmutableSet<ICraftingCPU>> callback) {
        ImmutableSet.Builder<ICraftingCPU> builder = ImmutableSet.builder();
        builder.addAll(callback.getReturnValue());
        builder.addAll(TianshuCraftingCpuRegistry.findForGrid(grid));
        callback.setReturnValue(builder.build());
    }

    @Inject(method = "submitJob", at = @At("HEAD"), cancellable = true)
    private void ae2lt$submitTianshuJob(ICraftingPlan plan, ICraftingRequester requester,
            ICraftingCPU target, boolean prioritizePower, IActionSource source,
            CallbackInfoReturnable<ICraftingSubmitResult> callback) {
        TianshuCraftingCpu cpu = target instanceof TianshuCraftingCpu tianshu
                ? tianshu : null;
        if (cpu == null && target == null) {
            // AE2自動選択が明示CPUを持たない場合だけTianshuを候補にします。
            List<TianshuCraftingCpu> candidates = TianshuCraftingCpuRegistry.findForGrid(grid);
            for (TianshuCraftingCpu candidate : candidates) {
                if (candidate.supportsPlan(plan)) {
                    cpu = candidate;
                    break;
                }
            }
        }
        if (cpu == null) {
            return;
        }
        callback.setReturnValue(cpu.submitJob(grid, plan, source, requester));
    }

    @Inject(method = "insertIntoCpus", at = @At("HEAD"), cancellable = true)
    private void ae2lt$insertIntoTianshuCpus(AEKey what, long amount, Actionable mode,
            CallbackInfoReturnable<Long> callback) {
        long remaining = amount;
        for (TianshuCraftingCpu cpu : TianshuCraftingCpuRegistry.findForGrid(grid)) {
            long accepted = cpu.insert(what, remaining, mode);
            remaining -= accepted;
            if (remaining <= 0) {
                callback.setReturnValue(amount);
                return;
            }
        }
        if (remaining != amount) {
            callback.setReturnValue(amount - remaining);
        }
    }

    @Inject(method = "hasCpu", at = @At("HEAD"), cancellable = true)
    private void ae2lt$hasTianshuCpu(ICraftingCPU cpu, CallbackInfoReturnable<Boolean> callback) {
        if (cpu instanceof TianshuCraftingCpu tianshu
                && TianshuCraftingCpuRegistry.findForGrid(grid).contains(tianshu)) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "onServerEndTick", at = @At("HEAD"))
    private void ae2lt$tickTianshuCpus(CallbackInfo callback) {
        for (TianshuCraftingCpu cpu : TianshuCraftingCpuRegistry.findForGrid(grid)) {
            cpu.tick();
        }
    }
}
