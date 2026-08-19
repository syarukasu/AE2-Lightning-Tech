package com.moakiee.ae2lt.logic.tianshu.loop;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.IPatternDetailsDecoder;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import com.moakiee.ae2lt.item.ClosedLoopPatternItem;
import java.util.ArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Decoder registered with AE2 so the custom item can enter ordinary AE2 planning. */
public final class ClosedLoopPatternDecoder implements IPatternDetailsDecoder {
    public static final ClosedLoopPatternDecoder INSTANCE = new ClosedLoopPatternDecoder();

    private ClosedLoopPatternDecoder() {
    }

    @Override
    public boolean isEncodedPattern(ItemStack stack) {
        return stack.getItem() instanceof ClosedLoopPatternItem item && item.hasPayload(stack);
    }

    @Override
    public @Nullable IPatternDetails decodePattern(ItemStack stack, Level level, boolean tryRecovery) {
        if (!(stack.getItem() instanceof ClosedLoopPatternItem item)) {
            return null;
        }
        return decode(item, AEItemKey.of(stack), level);
    }

    @Override
    public @Nullable IPatternDetails decodePattern(AEItemKey key, Level level) {
        if (!(key.getItem() instanceof ClosedLoopPatternItem item)) {
            return null;
        }
        return decode(item, key, level);
    }

    private static IPatternDetails decode(ClosedLoopPatternItem item, AEItemKey key, Level level) {
        var payload = item.readPayload(key.toStack()).orElse(null);
        if (payload == null || !payload.enabled()) {
            return null;
        }
        var decoded = new ArrayList<IPatternDetails>(payload.members().size());
        for (var member : payload.members()) {
            var details = PatternDetailsHelper.decodePattern(member.pattern().toItemStack(), level);
            if (details == null || details instanceof ClosedLoopBatchPatternDetails) {
                return null;
            }
            decoded.add(details);
        }
        return new ClosedLoopPatternDetails(key, payload, decoded);
    }
}
