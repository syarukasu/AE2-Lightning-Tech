package com.moakiee.ae2lt.logic.tianshu.loop;

import appeng.api.stacks.GenericStack;
import com.moakiee.ae2lt.overload.pattern.SourcePatternSnapshot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** NBT codec kept separate from the runtime model so the payload stays testable. */
public final class ClosedLoopPatternPayloadTagCodec {
    private static final String TAG_MEMBERS = "Members";
    private static final String TAG_PATTERN = "Pattern";
    private static final String TAG_COPIES = "Copies";
    private static final String TAG_INPUTS = "Inputs";
    private static final String TAG_OUTPUTS = "Outputs";
    private static final String TAG_ENABLED = "Enabled";

    private ClosedLoopPatternPayloadTagCodec() {
    }

    public static CompoundTag write(ClosedLoopPatternPayload payload) {
        var tag = new CompoundTag();
        var members = new ListTag();
        for (var member : payload.members()) {
            var memberTag = new CompoundTag();
            memberTag.put(TAG_PATTERN, member.pattern().toTag());
            memberTag.putLong(TAG_COPIES, member.copiesPerCycle());
            members.add(memberTag);
        }
        tag.put(TAG_MEMBERS, members);
        tag.put(TAG_INPUTS, writeStacks(payload.externalInputs()));
        tag.put(TAG_OUTPUTS, writeStacks(payload.outputs()));
        tag.putBoolean(TAG_ENABLED, payload.enabled());
        return tag;
    }

    public static ClosedLoopPatternPayload read(CompoundTag tag) {
        var members = new ArrayList<ClosedLoopMemberPattern>();
        var memberTags = tag.getList(TAG_MEMBERS, Tag.TAG_COMPOUND);
        if (memberTags.size() > 27) {
            throw new IllegalArgumentException("closed-loop payload contains too many members");
        }
        for (int i = 0; i < memberTags.size(); i++) {
            var memberTag = memberTags.getCompound(i);
            if (!memberTag.contains(TAG_PATTERN, Tag.TAG_COMPOUND)) {
                throw new IllegalArgumentException("closed-loop member is missing its pattern");
            }
            members.add(new ClosedLoopMemberPattern(
                    SourcePatternSnapshot.fromTag(memberTag.getCompound(TAG_PATTERN)),
                    Math.max(1L, memberTag.getLong(TAG_COPIES))));
        }
        return new ClosedLoopPatternPayload(
                members,
                readStacks(tag.getList(TAG_INPUTS, Tag.TAG_COMPOUND)),
                readStacks(tag.getList(TAG_OUTPUTS, Tag.TAG_COMPOUND)),
                !tag.contains(TAG_ENABLED, Tag.TAG_BYTE) || tag.getBoolean(TAG_ENABLED));
    }

    private static ListTag writeStacks(Iterable<GenericStack> stacks) {
        var result = new ListTag();
        for (var stack : stacks) {
            result.add(GenericStack.writeTag(stack));
        }
        return result;
    }

    private static List<GenericStack> readStacks(ListTag tags) {
        var result = new ArrayList<GenericStack>(tags.size());
        for (int i = 0; i < tags.size(); i++) {
            var stack = GenericStack.readTag(tags.getCompound(i));
            if (stack == null) {
                throw new IllegalArgumentException("closed-loop payload contains an invalid stack");
            }
            result.add(stack);
        }
        return result;
    }
}
