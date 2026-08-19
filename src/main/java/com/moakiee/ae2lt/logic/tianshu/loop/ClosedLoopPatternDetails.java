package com.moakiee.ae2lt.logic.tianshu.loop;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.moakiee.ae2lt.item.ClosedLoopPatternItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** AE2 view of a closed-loop payload. The CPU consumes the member list server-side. */
public final class ClosedLoopPatternDetails implements IPatternDetails, ClosedLoopBatchPatternDetails {
    private final AEItemKey definition;
    private final ClosedLoopPatternPayload payload;
    private final IPatternDetails[] members;
    private final IInput[] inputs;
    private final GenericStack[] outputs;

    public ClosedLoopPatternDetails(AEItemKey definition, ClosedLoopPatternPayload payload,
            List<IPatternDetails> decodedMembers) {
        if (decodedMembers.size() != payload.members().size()) {
            throw new IllegalArgumentException("closed-loop member count mismatch");
        }
        this.definition = definition;
        this.payload = payload;
        this.members = decodedMembers.toArray(IPatternDetails[]::new);
        this.inputs = payload.externalInputs().stream().map(ExactInput::new).toArray(IInput[]::new);
        this.outputs = payload.outputs().toArray(GenericStack[]::new);
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return inputs.clone();
    }

    @Override
    public GenericStack[] getOutputs() {
        return outputs.clone();
    }

    public ClosedLoopPatternPayload payload() {
        return payload;
    }

    public IPatternDetails member(int index) {
        return members[index];
    }

    public int memberCount() {
        return members.length;
    }

    public long memberCopies(int index) {
        return payload.members().get(index).copiesPerCycle();
    }

    public boolean isEnabled() {
        return payload.enabled();
    }

    private static final class ExactInput implements IInput {
        private final GenericStack stack;

        private ExactInput(GenericStack stack) {
            this.stack = stack;
        }

        @Override
        public GenericStack[] getPossibleInputs() {
            return new GenericStack[] {stack};
        }

        @Override
        public long getMultiplier() {
            return 1L;
        }

        @Override
        public boolean isValid(AEKey input, Level level) {
            return stack.what().equals(input);
        }

        @Override
        public @Nullable AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
