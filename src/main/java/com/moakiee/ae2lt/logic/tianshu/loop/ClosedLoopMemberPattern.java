package com.moakiee.ae2lt.logic.tianshu.loop;

import com.moakiee.ae2lt.overload.pattern.SourcePatternSnapshot;

/** One ordinary AE2 pattern and the number of times it runs in one loop cycle. */
public record ClosedLoopMemberPattern(SourcePatternSnapshot pattern, long copiesPerCycle) {
    public ClosedLoopMemberPattern {
        if (pattern == null) {
            throw new IllegalArgumentException("closed-loop member pattern is required");
        }
        if (copiesPerCycle <= 0) {
            throw new IllegalArgumentException("closed-loop member copies must be positive");
        }
    }
}
