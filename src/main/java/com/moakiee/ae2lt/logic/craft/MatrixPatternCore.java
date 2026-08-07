package com.moakiee.ae2lt.logic.craft;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import java.util.List;
import java.util.Objects;

public interface MatrixPatternCore {
    List<IPatternDetails> getAvailablePatterns();

    default boolean hasPattern(IPatternDetails details) {
        if (details == null) {
            return false;
        }
        for (IPatternDetails pattern : this.getAvailablePatterns()) {
            if (samePattern(pattern, details)) {
                return true;
            }
        }
        return false;
    }

    static boolean samePattern(IPatternDetails stored, IPatternDetails requested) {
        if (stored == requested) {
            return true;
        }
        if (stored == null || requested == null) {
            return false;
        }
        AEItemKey storedDefinition = stored.getDefinition();
        AEItemKey requestedDefinition = requested.getDefinition();
        if (storedDefinition != null
                && requestedDefinition != null
                && Objects.equals(storedDefinition, requestedDefinition)) {
            return true;
        }
        return stored.equals(requested);
    }
}
