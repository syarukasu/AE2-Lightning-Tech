package com.moakiee.ae2lt.logic.tianshu.loop;

import appeng.api.stacks.GenericStack;
import java.util.List;
import java.util.Objects;

/** Persistent, server-authoritative description of one closed-loop pattern. */
public record ClosedLoopPatternPayload(
        List<ClosedLoopMemberPattern> members,
        List<GenericStack> externalInputs,
        List<GenericStack> outputs,
        boolean enabled) {

    public ClosedLoopPatternPayload {
        members = List.copyOf(Objects.requireNonNull(members, "members"));
        externalInputs = copyStacks(externalInputs, "externalInputs");
        outputs = copyStacks(outputs, "outputs");
        if (members.isEmpty() || members.size() > 27) {
            throw new IllegalArgumentException("closed-loop member count must be between 1 and 27");
        }
        if (outputs.isEmpty() || outputs.size() > 9) {
            throw new IllegalArgumentException("closed-loop output count must be between 1 and 9");
        }
    }

    public ClosedLoopPatternPayload withEnabled(boolean value) {
        return value == enabled ? this : new ClosedLoopPatternPayload(members, externalInputs, outputs, value);
    }

    private static List<GenericStack> copyStacks(List<GenericStack> stacks, String name) {
        Objects.requireNonNull(stacks, name);
        for (GenericStack stack : stacks) {
            if (stack == null || stack.what() == null || stack.amount() <= 0) {
                throw new IllegalArgumentException(name + " contains an invalid stack");
            }
        }
        return List.copyOf(stacks);
    }
}
