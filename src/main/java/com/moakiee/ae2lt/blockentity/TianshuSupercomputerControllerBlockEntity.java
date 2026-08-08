package com.moakiee.ae2lt.blockentity;

import com.moakiee.ae2lt.block.MultiblockStateProperties;
import com.moakiee.ae2lt.block.TianshuSupercomputerControllerBlock;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockScanAttempt;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockScanResult;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockScanner;
import com.moakiee.ae2lt.registry.ModBlockEntities;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocator;
import com.moakiee.ae2lt.menu.TianshuSupercomputerControllerMenu;

/**
 * Tianshuの形成ライフサイクルだけを担当します。
 * AE2クラフトCPU・端末・閉ループ在庫は別の統合層で扱います。
 */
public final class TianshuSupercomputerControllerBlockEntity extends BlockEntity {
    private static final String TAG_FORMED = "TianshuFormed";
    // 構造変更直後の同一tick再走査を避けるための待機時間です。
    private static final int RESCAN_DELAY_TICKS = 2;
    // チャンクが未ロードの場合に、強制ロードせず再試行する間隔です。
    private static final int CHUNK_RETRY_DELAY_TICKS = 20;

    private boolean structureCheckQueued = true;
    private int rescanDelay;
    private TianshuMultiblockScanResult formedStructure;

    public TianshuSupercomputerControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TIANSHU_CONTROLLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            TianshuSupercomputerControllerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        if (blockEntity.rescanDelay > 0) {
            --blockEntity.rescanDelay;
        }
        if (blockEntity.structureCheckQueued && blockEntity.rescanDelay <= 0) {
            blockEntity.refreshStructure();
        }
    }

    public void scheduleStructureCheck() {
        structureCheckQueued = true;
        rescanDelay = Math.min(rescanDelay, RESCAN_DELAY_TICKS);
        setChanged();
    }

    public void invalidateStructure() {
        structureCheckQueued = false;
        rescanDelay = 0;
        clearFormedMembers();
        formedStructure = null;
        setControllerFormed(false);
    }

    public boolean isFormed() {
        return formedStructure != null;
    }

    public TianshuMultiblockScanResult formedStructure() {
        return formedStructure;
    }

    public IGrid getGrid() {
        if (formedStructure == null || level == null) {
            return null;
        }
        if (level.getBlockEntity(formedStructure.portPos())
                instanceof TianshuSupercomputerPortBlockEntity port) {
            return port.getMainNode().getGrid();
        }
        return null;
    }

    public long storageCapacity() {
        return formedStructure == null ? 0L : formedStructure.coreProfile().storageBytes();
    }

    public int parallelism() {
        return formedStructure == null ? 1 : Math.max(1, formedStructure.coreProfile().parallelism());
    }

    public boolean isCraftingBusy() {
        if (formedStructure == null || level == null) {
            return false;
        }
        return level.getBlockEntity(formedStructure.portPos())
                instanceof TianshuSupercomputerPortBlockEntity port
                && port.getCraftingCpu().isBusy();
    }

    public void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(TianshuSupercomputerControllerMenu.TYPE, player, locator);
    }

    public void patternsChanged() {
        if (level != null && formedStructure != null
                && level.getBlockEntity(formedStructure.portPos())
                instanceof TianshuSupercomputerPortBlockEntity port) {
            port.refreshCraftingProvider();
        }
        setChanged();
    }

    public List<IPatternDetails> getClosedLoopPatterns() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)
                || formedStructure == null) {
            return List.of();
        }
        List<IPatternDetails> result = new ArrayList<>();
        for (BlockPos storagePos : formedStructure.patternStoragePositions()) {
            if (!(serverLevel.getBlockEntity(storagePos) instanceof TianshuPatternStorageBlockEntity storage)) {
                continue;
            }
            for (var stack : storage.getTerminalPatternInventory()) {
                if (!stack.isEmpty() && PatternDetailsHelper.isEncodedPattern(stack)) {
                    IPatternDetails details = PatternDetailsHelper.decodePattern(stack, serverLevel);
                    if (details != null) {
                        // 専用閉ループはそのまま渡し、通常パターンだけ互換ラッパーへ包みます。
                        if (details instanceof com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopPatternDetails) {
                            result.add(details);
                        } else {
                            result.add(new com.moakiee.ae2lt.logic.tianshu.TianshuClosedLoopPatternDetails(details));
                        }
                    }
                }
            }
        }
        return List.copyOf(result);
    }

    public List<TianshuPatternStorageBlockEntity> getPatternStorageEntities() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)
                || formedStructure == null) {
            return List.of();
        }
        List<TianshuPatternStorageBlockEntity> result = new ArrayList<>();
        for (BlockPos storagePos : formedStructure.patternStoragePositions()) {
            if (serverLevel.getBlockEntity(storagePos) instanceof TianshuPatternStorageBlockEntity storage) {
                result.add(storage);
            }
        }
        return List.copyOf(result);
    }

    public Set<AEKey> getEmitableItems() {
        Set<AEKey> result = new HashSet<>();
        for (IPatternDetails details : getClosedLoopPatterns()) {
            for (GenericStack output : details.getOutputs()) {
                result.add(output.what());
            }
        }
        return Set.copyOf(result);
    }

    private void refreshStructure() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        structureCheckQueued = false;
        Direction orientation = getBlockState().getValue(TianshuSupercomputerControllerBlock.FACING);
        TianshuMultiblockScanAttempt attempt = TianshuMultiblockScanner.scan(
                serverLevel, worldPosition, orientation);
        if (attempt.chunksUnavailable()) {
            // 必要チャンクがない間は既存状態を残し、チケットを発行せずに後で再試行します。
            structureCheckQueued = true;
            rescanDelay = CHUNK_RETRY_DELAY_TICKS;
            return;
        }
        if (!attempt.formed()) {
            clearFormedMembers();
            formedStructure = null;
            setControllerFormed(false);
            setChanged();
            return;
        }

        applyFormedMembers(attempt.result());
        setControllerFormed(true);
        setChanged();
    }

    private void applyFormedMembers(TianshuMultiblockScanResult result) {
        if (formedStructure != null) {
            clearFormedMembers();
        }
        // スキャナが返した7×7×7の構成部品だけを更新します。
        for (BlockPos member : result.members()) {
            setMemberFormed(member, true);
        }
        // バインド処理から参照できるよう、形成結果を先に確定します。
        formedStructure = result;
        bindRuntimeMembers(result);
    }

    private void bindRuntimeMembers(TianshuMultiblockScanResult result) {
        if (level == null) {
            return;
        }
        if (level.getBlockEntity(result.portPos())
                instanceof TianshuSupercomputerPortBlockEntity port) {
            port.bindController(worldPosition);
        }
        for (BlockPos storagePos : result.patternStoragePositions()) {
            if (level.getBlockEntity(storagePos) instanceof TianshuPatternStorageBlockEntity storage) {
                storage.setControllerBinding(worldPosition);
            }
        }
        for (BlockPos storagePos : result.seedStoragePositions()) {
            if (level.getBlockEntity(storagePos) instanceof TianshuSeedStorageBlockEntity storage) {
                storage.setControllerBinding(worldPosition);
            }
        }
    }

    private void clearFormedMembers() {
        if (formedStructure == null) {
            return;
        }
        if (level != null && level.getBlockEntity(formedStructure.portPos())
                instanceof TianshuSupercomputerPortBlockEntity port) {
            port.bindController(null);
        }
        for (BlockPos storagePos : formedStructure.patternStoragePositions()) {
            if (level != null && level.getBlockEntity(storagePos)
                    instanceof TianshuPatternStorageBlockEntity storage) {
                storage.setControllerBinding(null);
            }
        }
        for (BlockPos storagePos : formedStructure.seedStoragePositions()) {
            if (level != null && level.getBlockEntity(storagePos)
                    instanceof TianshuSeedStorageBlockEntity storage) {
                storage.setControllerBinding(null);
            }
        }
        for (BlockPos member : formedStructure.members()) {
            setMemberFormed(member, false);
        }
    }

    private void setControllerFormed(boolean formed) {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(MultiblockStateProperties.FORMED)
                && state.getValue(MultiblockStateProperties.FORMED) != formed) {
            level.setBlock(worldPosition,
                    state.setValue(MultiblockStateProperties.FORMED, formed),
                    Block.UPDATE_CLIENTS);
        }
    }

    private void setMemberFormed(BlockPos pos, boolean formed) {
        if (level == null || !level.hasChunkAt(pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(MultiblockStateProperties.FORMED)
                || state.getValue(MultiblockStateProperties.FORMED) == formed) {
            return;
        }
        level.setBlock(pos, state.setValue(MultiblockStateProperties.FORMED, formed),
                Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_FORMED, isFormed());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // 再起動後は座標から再検証し、古い形成結果をそのまま信頼しません。
        structureCheckQueued = true;
    }
}
