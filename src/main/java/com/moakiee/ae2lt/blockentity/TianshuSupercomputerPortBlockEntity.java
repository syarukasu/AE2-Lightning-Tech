package com.moakiee.ae2lt.blockentity;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.me.helpers.MachineSource;
import com.google.common.collect.ImmutableSet;
import com.moakiee.ae2lt.block.TianshuSupercomputerPortBlock;
import com.moakiee.ae2lt.logic.tianshu.TianshuCraftingCpu;
import com.moakiee.ae2lt.logic.tianshu.TianshuPatternInventoryView;
import com.moakiee.ae2lt.registry.ModBlockEntities;
import com.moakiee.ae2lt.registry.ModBlocks;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** TianshuをAE2ネットワークへ公開する唯一の接続点です。 */
public final class TianshuSupercomputerPortBlockEntity extends AENetworkBlockEntity
        implements ICraftingProvider, ICraftingRequester, PatternContainer {
    private static final String TAG_CONTROLLER = "ControllerPos";
    private final IActionSource actionSource = new MachineSource(this::getActionableNode);
    private final TianshuCraftingCpu craftingCpu = new TianshuCraftingCpu(this);
    private final InternalInventory terminalPatternInventory = new TianshuPatternInventoryView(this);
    @Nullable
    private BlockPos controllerPos;
    private boolean formed;

    public TianshuSupercomputerPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TIANSHU_PORT.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            TianshuSupercomputerPortBlockEntity port) {
        if (level.isClientSide()) {
            return;
        }
        // CPUのtickはAE2 CraftingServiceから一度だけ呼ぶため、ここではリンク状態だけを保ちます。
        if (port.formed && port.getController() == null) {
            port.bindController(null);
        }
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return super.createMainNode()
                .setTagName("tianshu_supercomputer_port")
                .setVisualRepresentation(ModBlocks.TIANSHU_PORT.get())
                .setIdlePowerUsage(8.0D)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(ICraftingProvider.class, this)
                .addService(ICraftingRequester.class, this)
                .addService(TianshuCraftingCpu.class, craftingCpu);
    }

    @Override
    public AECableType getCableConnectionType(Direction direction) {
        return formed ? AECableType.DENSE_SMART : AECableType.NONE;
    }

    @Override
    public Set<Direction> getGridConnectableSides(appeng.api.orientation.BlockOrientation orientation) {
        return formed ? EnumSet.allOf(Direction.class) : Collections.emptySet();
    }

    @Override
    public IGrid getGrid() {
        return getMainNode().getGrid();
    }

    @Override
    public boolean isVisibleInTerminal() {
        // 未形成またはAE2ノード未接続のポートはPattern Access Terminalへ公開しません。
        return isLinkActive();
    }

    public boolean isLinkActive() {
        return formed && controllerPos != null && getMainNode().isActive()
                && getMainNode().getGrid() != null && getController() != null;
    }

    @Nullable
    public TianshuSupercomputerControllerBlockEntity getController() {
        if (level == null || controllerPos == null || !level.hasChunkAt(controllerPos)) {
            return null;
        }
        if (level.getBlockEntity(controllerPos) instanceof TianshuSupercomputerControllerBlockEntity controller) {
            return controller;
        }
        return null;
    }

    public TianshuCraftingCpu getCraftingCpu() {
        return craftingCpu;
    }

    public long storageCapacity() {
        var controller = getController();
        return controller == null ? 0L : controller.storageCapacity();
    }

    public int parallelism() {
        var controller = getController();
        return controller == null ? 1 : controller.parallelism();
    }

    public void bindController(@Nullable BlockPos newControllerPos) {
        controllerPos = newControllerPos == null ? null : newControllerPos.immutable();
        formed = controllerPos != null;
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.hasProperty(com.moakiee.ae2lt.block.MultiblockStateProperties.FORMED)
                    && state.getValue(com.moakiee.ae2lt.block.MultiblockStateProperties.FORMED) != formed) {
                level.setBlock(worldPosition,
                        state.setValue(com.moakiee.ae2lt.block.MultiblockStateProperties.FORMED, formed),
                        Block.UPDATE_CLIENTS);
            }
            onGridConnectableSidesChanged();
            refreshCraftingProvider();
        }
        setChanged();
    }

    @Nullable
    public BlockPos controllerPos() {
        return controllerPos;
    }

    public void refreshCraftingProvider() {
        IGrid grid = getMainNode().getGrid();
        if (grid != null) {
            grid.getCraftingService().refreshNodeCraftingProvider(getMainNode().getNode());
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        var controller = getController();
        return controller == null ? List.of() : controller.getClosedLoopPatterns();
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return terminalPatternInventory;
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        // AE2のPattern Access Terminalへ、形成済み構造だけを1つの端末グループとして公開します。
        return level == null ? PatternContainerGroup.nothing()
                : PatternContainerGroup.fromMachine(level, worldPosition, Direction.UP);
    }

    @Override
    public int getPatternPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean pushPattern(IPatternDetails pattern, KeyCounter[] inputHolder) {
        // AE2標準CPUから誤って押し込まれた場合も、閉ループ所有権以外は受理しません。
        return pattern instanceof com.moakiee.ae2lt.logic.tianshu.TianshuClosedLoopPatternDetails;
    }

    @Override
    public boolean isBusy() {
        return craftingCpu.isBusy();
    }

    @Override
    public Set<AEKey> getEmitableItems() {
        var controller = getController();
        return controller == null ? Set.of() : controller.getEmitableItems();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.of();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        if (getMainNode().getGrid() == null) {
            return 0L;
        }
        return getMainNode().getGrid().getStorageService().getInventory()
                .insert(what, amount, mode, actionSource);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        setChanged();
    }

    @Override
    public IGridNode getActionableNode() {
        return getMainNode().getNode();
    }

    @Override
    public void loadTag(CompoundTag tag) {
        super.loadTag(tag);
        controllerPos = tag.contains(TAG_CONTROLLER) ? BlockPos.of(tag.getLong(TAG_CONTROLLER)) : null;
        formed = controllerPos != null;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (controllerPos != null) {
            tag.putLong(TAG_CONTROLLER, controllerPos.asLong());
        }
    }
}
