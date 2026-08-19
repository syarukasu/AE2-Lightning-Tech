package com.moakiee.ae2lt.blockentity;

import com.moakiee.ae2lt.block.MatrixControllerBlock;
import com.moakiee.ae2lt.block.MultiblockStateProperties;
import com.moakiee.ae2lt.logic.craft.MatrixMultiblockMember;
import com.moakiee.ae2lt.logic.craft.MatrixMultiblockScanAttempt;
import com.moakiee.ae2lt.logic.craft.MatrixMultiblockScanResult;
import com.moakiee.ae2lt.logic.craft.MatrixMultiblockScanner;
import com.moakiee.ae2lt.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Owns the Matrix formation lifecycle. Crafting/network integration is kept
 * out of this BE until its dedicated bridge is available.
 */
public final class MatrixControllerBlockEntity extends BlockEntity {
    private static final String TAG_ORIENTATION = "MatrixOrientation";
    private static final int RESCAN_DELAY_TICKS = 2;
    private static final int CHUNK_RETRY_DELAY_TICKS = 20;

    private boolean structureCheckQueued = true;
    private int rescanDelay;
    private MatrixMultiblockScanResult formedStructure;

    public MatrixControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATRIX_CONTROLLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            MatrixControllerBlockEntity blockEntity) {
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
    }

    public boolean isFormed() {
        return formedStructure != null;
    }

    public MatrixMultiblockScanResult formedStructure() {
        return formedStructure;
    }

    private void refreshStructure() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        structureCheckQueued = false;
        Direction orientation = getBlockState().getValue(MatrixControllerBlock.FACING);
        MatrixMultiblockScanAttempt attempt = MatrixMultiblockScanner.scan(
                serverLevel, worldPosition, orientation);
        if (attempt.chunksUnavailable()) {
            // Keep a valid previous structure while a required chunk is absent.
            // Retry later without creating a chunk ticket or force-loading it.
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

    private void applyFormedMembers(MatrixMultiblockScanResult result) {
        if (formedStructure != null) {
            clearFormedMembers();
        }

        // Only the 7x11x7 members returned by the scanner are touched.
        for (MatrixMultiblockMember member : result.members()) {
            setMemberFormed(member.worldPos(), true);
            bindMember(member.worldPos(), true);
        }
    }

    private void clearFormedMembers() {
        if (formedStructure == null) {
            return;
        }
        for (MatrixMultiblockMember member : formedStructure.members()) {
            setMemberFormed(member.worldPos(), false);
            bindMember(member.worldPos(), false);
        }
    }

    private void setControllerFormed(boolean formed) {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (state.hasProperty(MultiblockStateProperties.FORMED)
                && state.getValue(MultiblockStateProperties.FORMED) != formed) {
            BlockState changed = state.setValue(MultiblockStateProperties.FORMED, formed);
            level.setBlock(worldPosition, changed, Block.UPDATE_CLIENTS);
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
        level.setBlock(pos, state.setValue(MultiblockStateProperties.FORMED, formed), Block.UPDATE_CLIENTS);
    }

    private void bindMember(BlockPos pos, boolean formed) {
        if (level == null || !level.hasChunkAt(pos)) {
            return;
        }
        BlockEntity member = level.getBlockEntity(pos);
        if (member instanceof MatrixPortBlockEntity port) {
            port.setControllerBinding(formed ? worldPosition : null);
        } else if (member instanceof MatrixPatternStorageBlockEntity storage) {
            storage.setControllerBinding(formed ? worldPosition : null);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_ORIENTATION, getBlockState().getValue(MatrixControllerBlock.FACING).getName());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        structureCheckQueued = true;
    }
}
