package com.moakiee.ae2lt.logic.tianshu;

import appeng.api.config.Actionable;
import appeng.api.config.CpuSelectionMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.crafting.CraftingJobStatus;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.crafting.inv.ListCraftingInventory;
import com.moakiee.ae2lt.blockentity.TianshuSupercomputerPortBlockEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * TianshuポートがAE2のCPU一覧に公開するCPUです。
 * AE2の計算結果を受け取り、初期在庫の抽出とCraftingLinkをAE2へ委譲します。
 */
public final class TianshuCraftingCpu implements ICraftingCPU, IGridNodeService {
    // 1tickに処理する閉ループ段数。大きくしすぎてTPSを占有しないための上限です。
    private static final int MAX_RECIPE_BATCHES_PER_TICK = 256;

    private final TianshuSupercomputerPortBlockEntity port;
    @Nullable
    private Job job;

    public TianshuCraftingCpu(TianshuSupercomputerPortBlockEntity port) {
        this.port = port;
    }

    public IGrid getGrid() {
        return port.getMainNode().getGrid();
    }

    public boolean isActive() {
        return port.isLinkActive();
    }

    public void tick() {
        Job current = job;
        if (current == null) {
            return;
        }
        if (current.cpuLink.isCanceled() || current.requesterLink.isCanceled()) {
            cancelJob();
            return;
        }
        int batches = 0;
        // 依存順がAE2のMap順と異なっても、入力が揃った段だけを繰り返し進めます。
        while (batches < MAX_RECIPE_BATCHES_PER_TICK && current.hasRemainingTasks()) {
            boolean progressed = false;
            for (var entry : current.tasks.entrySet()) {
                if (entry.getValue() <= 0) {
                    continue;
                }
                long completed = executeOneBatch(current, entry.getKey(), entry.getValue());
                if (completed > 0) {
                    entry.setValue(entry.getValue() - completed);
                    batches++;
                    progressed = true;
                    if (batches >= MAX_RECIPE_BATCHES_PER_TICK) {
                        break;
                    }
                }
            }
            if (!progressed) {
                break;
            }
        }
        if (!current.hasRemainingTasks()) {
            finish(current);
        }
    }

    /** AE2の外部機械出力を、待機中ジョブの内部在庫へ受け入れます。 */
    public long insert(AEKey what, long amount, Actionable mode) {
        if (job == null || amount <= 0) {
            return 0L;
        }
        if (mode == Actionable.SIMULATE) {
            return amount;
        }
        job.inventory.insert(what, amount, Actionable.MODULATE);
        return amount;
    }

    private long executeOneBatch(Job current, IPatternDetails details, long remaining) {
        IPatternDetails actual = details instanceof TianshuClosedLoopPatternDetails wrapped
                ? wrapped.delegate() : details;
        if (actual.getInputs().length == 0 || actual.getOutputs().length == 0) {
            return 0;
        }
        long operations = Math.min(remaining, Long.MAX_VALUE);
        Map<AEKey, Long> selected = new LinkedHashMap<>();
        Map<AEKey, Long> returnedContainers = new LinkedHashMap<>();
        for (IPatternDetails.IInput input : actual.getInputs()) {
            GenericStack candidate = firstAvailableInput(current.inventory, input);
            if (candidate == null || input.getMultiplier() <= 0) {
                return 0;
            }
            long unit = safeMultiply(input.getMultiplier(), candidate.amount());
            if (unit <= 0) {
                return 0;
            }
            long available = current.inventory.list.get(candidate.what());
            long fit = available / unit;
            operations = Math.min(operations, fit);
            selected.merge(candidate.what(), unit, (left, right) -> safeAdd(left, right));
            AEKey remainingKey = input.getRemainingKey(candidate.what());
            if (remainingKey != null) {
                // 返却容器は1回のパターン実行ごとの倍率で戻し、入力量そのものを二重計上しません。
                returnedContainers.merge(remainingKey, input.getMultiplier(), TianshuCraftingCpu::safeAdd);
            }
        }
        if (operations <= 0) {
            return 0;
        }
        for (var input : selected.entrySet()) {
            long amount = safeMultiply(input.getValue(), operations);
            long extracted = current.inventory.extract(input.getKey(), amount, Actionable.MODULATE);
            if (extracted != amount) {
                return 0;
            }
        }
        for (GenericStack output : actual.getOutputs()) {
            long amount = safeMultiply(output.amount(), operations);
            current.inventory.insert(output.what(), amount, Actionable.MODULATE);
        }
        for (var returned : returnedContainers.entrySet()) {
            // バケツ等の返却物を同じバッチへ戻し、AE2標準の容器会計を欠落させません。
            current.inventory.insert(returned.getKey(), safeMultiply(returned.getValue(), operations),
                    Actionable.MODULATE);
        }
        return operations;
    }

    @Nullable
    private static GenericStack firstAvailableInput(ListCraftingInventory inventory,
            IPatternDetails.IInput input) {
        for (GenericStack candidate : input.getPossibleInputs()) {
            if (candidate != null && inventory.list.get(candidate.what()) >= candidate.amount()) {
                return candidate;
            }
        }
        return null;
    }

    private void finish(Job current) {
        GenericStack finalOutput = current.plan.finalOutput();
        if (current.inventory.list.get(finalOutput.what()) < finalOutput.amount()) {
            // 計画と実在庫が一致しない場合はリンクをキャンセルし、内部在庫を返却します。
            cancelJob();
            return;
        }
        long amount = current.inventory.extract(finalOutput.what(), finalOutput.amount(), Actionable.MODULATE);
        if (amount > 0) {
            long accepted = current.cpuLink.insert(finalOutput.what(), amount, Actionable.MODULATE);
            if (accepted < amount && getGrid() != null) {
                getGrid().getStorageService().getInventory().insert(
                        finalOutput.what(), amount - accepted, Actionable.MODULATE, current.source);
            }
        }
        for (var entry : current.inventory.list) {
            if (entry.getLongValue() > 0 && getGrid() != null) {
                getGrid().getStorageService().getInventory().insert(
                        entry.getKey(), entry.getLongValue(), Actionable.MODULATE, current.source);
            }
        }
        current.cpuLink.markDone();
        current.requesterLink.markDone();
        job = null;
        port.refreshCraftingProvider();
    }

    private static long safeMultiply(long left, long right) {
        if (left <= 0 || right <= 0) {
            return 0;
        }
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    private static long safeAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    public ICraftingSubmitResult submitJob(IGrid grid, ICraftingPlan plan,
            IActionSource source, ICraftingRequester requester) {
        if (job != null) {
            return CraftingSubmitResult.CPU_BUSY;
        }
        if (!isActive()) {
            return CraftingSubmitResult.CPU_OFFLINE;
        }
        if (plan == null || plan.simulation() || plan.finalOutput() == null) {
            return CraftingSubmitResult.INCOMPLETE_PLAN;
        }
        if (!supportsPlan(plan)) {
            return CraftingSubmitResult.simpleError(
                    appeng.api.networking.crafting.CraftingSubmitErrorCode.NO_SUITABLE_CPU_FOUND);
        }
        if (plan.bytes() > getAvailableStorage()) {
            return CraftingSubmitResult.CPU_TOO_SMALL;
        }
        ListCraftingInventory inventory = new ListCraftingInventory(ignored -> {
            // 同一ジョブ内のKeyCounterだけを更新し、tick外のネットワーク走査はしません。
        });
        GenericStack missing = CraftingCpuHelper.tryExtractInitialItems(plan, grid, inventory, source);
        if (missing != null) {
            return CraftingSubmitResult.missingIngredient(missing);
        }
        UUID id = UUID.randomUUID();
        CompoundTag data = CraftingCpuHelper.generateLinkData(id, false, true);
        CraftingLink requesterLink = new CraftingLink(data, requester);
        CraftingLink cpuLink = new CraftingLink(data, this);
        // addLinkはAE2の具象CraftingServiceで公開されているため、APIの読み取り専用型を絞ります。
        var service = (appeng.me.service.CraftingService) grid.getCraftingService();
        service.addLink(requesterLink);
        service.addLink(cpuLink);
        job = new Job(plan, source, requesterLink, cpuLink, inventory);
        return CraftingSubmitResult.successful(requesterLink);
    }

    public boolean supportsPlan(ICraftingPlan plan) {
        for (IPatternDetails details : plan.patternTimes().keySet()) {
            if (!(details instanceof TianshuClosedLoopPatternDetails)) {
                return false;
            }
        }
        return !plan.patternTimes().isEmpty();
    }

    @Override
    public boolean isBusy() {
        return job != null;
    }

    @Override
    public CraftingJobStatus getJobStatus() {
        Job current = job;
        if (current == null) {
            return null;
        }
        long total = current.plan.finalOutput().amount();
        long progress = Math.max(0L, total - current.remainingFinalOutput());
        return new CraftingJobStatus(current.plan.finalOutput(), total, progress, 0L);
    }

    private long remainingFinalOutput() {
        return job == null ? 0L : job.remainingFinalOutput();
    }

    @Override
    public void cancelJob() {
        Job current = job;
        if (current == null) {
            return;
        }
        if (getGrid() != null) {
            for (var entry : current.inventory.list) {
                getGrid().getStorageService().getInventory().insert(
                        entry.getKey(), entry.getLongValue(), Actionable.MODULATE, current.source);
            }
        }
        current.cpuLink.cancel();
        current.requesterLink.cancel();
        job = null;
    }

    @Override
    public long getAvailableStorage() {
        long capacity = port.storageCapacity();
        return job == null ? capacity : Math.max(0L, capacity - job.plan.bytes());
    }

    @Override
    public int getCoProcessors() {
        return port.parallelism() - 1;
    }

    @Override
    public Component getName() {
        return Component.translatable("block.ae2lt.tianshu_supercomputer_controller");
    }

    @Override
    public CpuSelectionMode getSelectionMode() {
        return CpuSelectionMode.ANY;
    }

    private static final class Job {
        private final ICraftingPlan plan;
        private final IActionSource source;
        private final CraftingLink requesterLink;
        private final CraftingLink cpuLink;
        private final ListCraftingInventory inventory;
        private final Map<IPatternDetails, Long> tasks = new LinkedHashMap<>();

        private Job(ICraftingPlan plan, IActionSource source, CraftingLink requesterLink,
                CraftingLink cpuLink, ListCraftingInventory inventory) {
            this.plan = plan;
            this.source = source;
            this.requesterLink = requesterLink;
            this.cpuLink = cpuLink;
            this.inventory = inventory;
            tasks.putAll(plan.patternTimes());
        }

        private boolean hasRemainingTasks() {
            for (long count : tasks.values()) {
                if (count > 0) {
                    return true;
                }
            }
            return false;
        }

        private long remainingFinalOutput() {
            return inventory.list.get(plan.finalOutput().what());
        }
    }
}
