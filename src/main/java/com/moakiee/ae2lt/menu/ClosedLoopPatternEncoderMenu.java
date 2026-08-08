package com.moakiee.ae2lt.menu;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.util.inv.AppEngInternalInventory;
import com.moakiee.ae2lt.AE2LightningTech;
import com.moakiee.ae2lt.item.ClosedLoopPatternItem;
import com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopMemberPattern;
import com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopPatternPayload;
import com.moakiee.ae2lt.overload.pattern.SourcePatternSnapshot;
import com.moakiee.ae2lt.registry.ModItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Small server-authoritative editor for closed-loop member patterns.
 * The output is only generated after every member decodes successfully.
 */
public final class ClosedLoopPatternEncoderMenu extends AEBaseMenu {
    public static final int MEMBER_SLOTS = 9;
    private static final int RESULT_SLOT = MEMBER_SLOTS;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 103;
    private static final int HOTBAR_Y = 161;
    private static final int SLOT_SPACING = 18;

    public static final MenuType<ClosedLoopPatternEncoderMenu> TYPE = Ae2ltMenuBuilder.buildUnregistered(
            MenuTypeBuilder.create(ClosedLoopPatternEncoderMenu::new, ClosedLoopPatternEncoderHost.class),
            new ResourceLocation(AE2LightningTech.MODID, "closed_loop_pattern_encoder"));

    private final AppEngInternalInventory members;
    private final AppEngInternalInventory result;
    private boolean dirty = true;

    public ClosedLoopPatternEncoderMenu(int id, Inventory playerInventory, ClosedLoopPatternEncoderHost host) {
        super(TYPE, id, playerInventory, host);
        this.members = new AppEngInternalInventory(new InventoryHost(), MEMBER_SLOTS, 1);
        this.result = new AppEngInternalInventory(null, 1, 1);

        for (int slot = 0; slot < MEMBER_SLOTS; slot++) {
            addSlot(new MemberSlot(new InventoryAdapter(members), slot,
                    17 + (slot % 3) * SLOT_SPACING, 18 + (slot / 3) * SLOT_SPACING),
                    SlotSemantics.ENCODED_PATTERN);
        }
        addSlot(new ResultSlot(new InventoryAdapter(result), 0, 116, 36), SlotSemantics.CRAFTING_RESULT);
        addPlayerInventorySlots(playerInventory);
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide() && dirty) {
            dirty = false;
            result.setItemDirect(0, createResult());
        }
        super.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        returnInventory(player, members);
        returnInventory(player, result);
    }

    public ItemStack resultStack() {
        return result.getStackInSlot(0);
    }

    private ItemStack createResult() {
        var decoded = new ArrayList<IPatternDetails>(MEMBER_SLOTS);
        var payloadMembers = new ArrayList<ClosedLoopMemberPattern>();
        for (int slot = 0; slot < MEMBER_SLOTS; slot++) {
            var source = members.getStackInSlot(slot);
            if (source.isEmpty()) {
                continue;
            }
            if (source.getItem() instanceof ClosedLoopPatternItem
                    || !PatternDetailsHelper.isEncodedPattern(source)) {
                return ItemStack.EMPTY;
            }
            var details = PatternDetailsHelper.decodePattern(source, getPlayer().level());
            if (details == null) {
                return ItemStack.EMPTY;
            }
            decoded.add(details);
            payloadMembers.add(new ClosedLoopMemberPattern(SourcePatternSnapshot.fromItemStack(source), 1L));
        }
        if (decoded.isEmpty()) {
            return ItemStack.EMPTY;
        }

        var consumption = new LinkedHashMap<appeng.api.stacks.AEKey, Long>();
        var production = new LinkedHashMap<appeng.api.stacks.AEKey, Long>();
        for (var details : decoded) {
            for (var input : details.getInputs()) {
                var possible = input.getPossibleInputs();
                if (possible.length == 0 || possible[0] == null) {
                    return ItemStack.EMPTY;
                }
                var amount = safeMultiply(possible[0].amount(), input.getMultiplier());
                merge(consumption, possible[0].what(), amount);
            }
            for (var output : details.getOutputs()) {
                merge(production, output.what(), output.amount());
            }
        }

        var externalInputs = new ArrayList<appeng.api.stacks.GenericStack>();
        for (var entry : consumption.entrySet()) {
            long required = Math.max(0L, entry.getValue() - production.getOrDefault(entry.getKey(), 0L));
            if (required > 0) {
                externalInputs.add(new appeng.api.stacks.GenericStack(entry.getKey(), required));
            }
        }
        var outputs = new ArrayList<appeng.api.stacks.GenericStack>();
        for (var entry : production.entrySet()) {
            long amount = Math.max(0L, entry.getValue() - consumption.getOrDefault(entry.getKey(), 0L));
            if (amount > 0) {
                outputs.add(new appeng.api.stacks.GenericStack(entry.getKey(), amount));
            }
        }
        if (externalInputs.isEmpty() || outputs.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try {
            var payload = new ClosedLoopPatternPayload(payloadMembers, externalInputs, outputs, true);
            return ((ClosedLoopPatternItem) ModItems.CLOSED_LOOP_PATTERN.get()).createStack(payload);
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static void merge(Map<appeng.api.stacks.AEKey, Long> map,
            appeng.api.stacks.AEKey key, long amount) {
        map.merge(key, amount, ClosedLoopPatternEncoderMenu::safeAdd);
    }

    // Saturation keeps a malformed test pattern from wrapping into a negative request.
    private static long safeAdd(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }

    // A pattern with a huge multiplier is rejected as a saturated but valid long request.
    private static long safeMultiply(long left, long right) {
        if (left <= 0 || right <= 0) {
            return 0L;
        }
        return left > Long.MAX_VALUE / right ? Long.MAX_VALUE : left * right;
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + row * 9 + 9;
                addSlot(new Slot(playerInventory, index,
                        PLAYER_INV_X + column * SLOT_SPACING, PLAYER_INV_Y + row * SLOT_SPACING),
                        SlotSemantics.PLAYER_INVENTORY);
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column,
                    PLAYER_INV_X + column * SLOT_SPACING, HOTBAR_Y), SlotSemantics.PLAYER_HOTBAR);
        }
    }

    private static void returnInventory(Player player, AppEngInternalInventory inventory) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            var stack = inventory.extractItem(slot, Integer.MAX_VALUE, false);
            if (!stack.isEmpty() && !player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    private final class InventoryHost implements appeng.util.inv.InternalInventoryHost {
        @Override
        public void saveChanges() {
            dirty = true;
        }

        @Override
        public void onChangeInventory(InternalInventory inv, int slot) {
            dirty = true;
        }

        @Override
        public boolean isClientSide() {
            return ClosedLoopPatternEncoderMenu.this.isClientSide();
        }
    }

    private static class InventoryAdapter extends SimpleContainer {
        private final AppEngInternalInventory inventory;

        private InventoryAdapter(AppEngInternalInventory inventory) {
            super(inventory.size());
            this.inventory = inventory;
        }

        @Override public int getContainerSize() { return inventory.size(); }
        @Override public boolean isEmpty() {
            for (int i = 0; i < inventory.size(); i++) if (!inventory.getStackInSlot(i).isEmpty()) return false;
            return true;
        }
        @Override public ItemStack getItem(int slot) { return inventory.getStackInSlot(slot); }
        @Override public ItemStack removeItem(int slot, int amount) { return inventory.extractItem(slot, amount, false); }
        @Override public ItemStack removeItemNoUpdate(int slot) { return inventory.extractItem(slot, Integer.MAX_VALUE, false); }
        @Override public void setItem(int slot, ItemStack stack) { inventory.setItemDirect(slot, stack); }
        @Override public void clearContent() {
            for (int i = 0; i < inventory.size(); i++) inventory.setItemDirect(i, ItemStack.EMPTY);
        }
        @Override public boolean stillValid(Player player) { return true; }
    }

    private final class MemberSlot extends Slot {
        private MemberSlot(InventoryAdapter inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override public boolean mayPlace(ItemStack stack) {
            return !stack.isEmpty() && !(stack.getItem() instanceof ClosedLoopPatternItem)
                    && PatternDetailsHelper.isEncodedPattern(stack);
        }

        @Override public int getMaxStackSize() { return 1; }
    }

    private static final class ResultSlot extends Slot {
        private ResultSlot(InventoryAdapter inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return true; }
        @Override public int getMaxStackSize() { return 1; }
    }
}
