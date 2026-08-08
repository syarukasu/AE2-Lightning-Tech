package com.moakiee.ae2lt.blockentity;

import com.moakiee.ae2lt.block.MultiblockStateProperties;
import com.moakiee.ae2lt.block.TianshuSupercomputerControllerBlock;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockScanAttempt;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockScanResult;
import com.moakiee.ae2lt.logic.tianshu.TianshuMultiblockScanner;
import com.moakiee.ae2lt.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
        formedStructure = attempt.result();
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
    }

    private void clearFormedMembers() {
        if (formedStructure == null) {
            return;
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
