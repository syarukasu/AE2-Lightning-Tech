package com.moakiee.ae2lt.item;

import appeng.crafting.pattern.EncodedPatternItem;
import com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopPatternDecoder;
import com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopPatternPayload;
import com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopPatternPayloadTagCodec;
import com.moakiee.ae2lt.menu.ClosedLoopPatternEncoderHost;
import com.moakiee.ae2lt.menu.ClosedLoopPatternEncoderMenu;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;

/**
 * AE2 encoded-pattern item that owns a list of ordinary member patterns.
 * It is intentionally separate from the ordinary AE2 pattern item.
 */
public final class ClosedLoopPatternItem extends EncodedPatternItem {
    private static final String TAG_PAYLOAD = "ClosedLoopPattern";

    public ClosedLoopPatternItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public boolean hasPayload(ItemStack stack) {
        return stack.getItem() == this
                && readRootTag(stack).contains(TAG_PAYLOAD, CompoundTag.TAG_COMPOUND);
    }

    public Optional<ClosedLoopPatternPayload> readPayload(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != this || !hasPayload(stack)) {
            return Optional.empty();
        }
        try {
            return Optional.of(ClosedLoopPatternPayloadTagCodec.read(
                    readRootTag(stack).getCompound(TAG_PAYLOAD)));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public void writePayload(ItemStack stack, ClosedLoopPatternPayload payload) {
        if (stack.isEmpty() || stack.getItem() != this) {
            throw new IllegalArgumentException("payload target is not a closed-loop pattern");
        }
        com.moakiee.ae2lt.util.ItemStackTagSupport.updateTag(stack,
                root -> root.put(TAG_PAYLOAD, ClosedLoopPatternPayloadTagCodec.write(payload)));
    }

    public ItemStack createStack(ClosedLoopPatternPayload payload) {
        var stack = new ItemStack(this);
        writePayload(stack, payload);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            MenuOpener.open(ClosedLoopPatternEncoderMenu.TYPE, player, MenuLocators.forHand(player, hand));
        }
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
    }

    public appeng.api.crafting.IPatternDetails decode(ItemStack stack, Level level) {
        return stack.getItem() == this ? ClosedLoopPatternDecoder.INSTANCE.decodePattern(stack, level, false) : null;
    }

    public appeng.api.crafting.IPatternDetails decode(ItemStack stack, Level level, boolean tryRecovery) {
        return decode(stack, level);
    }

    @Override
    public appeng.api.crafting.IPatternDetails decode(appeng.api.stacks.AEItemKey key, Level level) {
        return key != null && key.getItem() == this
                ? ClosedLoopPatternDecoder.INSTANCE.decodePattern(key, level) : null;
    }

    private static CompoundTag readRootTag(ItemStack stack) {
        return com.moakiee.ae2lt.util.ItemStackTagSupport.getTagCopy(stack);
    }
}
