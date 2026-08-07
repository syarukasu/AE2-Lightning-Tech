package com.moakiee.ae2lt.logic;

import appeng.api.crafting.IPatternDetails;
import java.util.Objects;

final class TargetPatternKey<T> {
    private final T target;
    private final IPatternDetails pattern;
    private final int hashCode;

    TargetPatternKey(T target, IPatternDetails pattern) {
        this.target = Objects.requireNonNull(target, "target");
        this.pattern = pattern;
        this.hashCode = 31 * target.hashCode() + (pattern == null ? 0 : pattern.hashCode());
    }

    T target() {
        return this.target;
    }

    IPatternDetails pattern() {
        return this.pattern;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TargetPatternKey<?> key)) {
            return false;
        }
        return this.target.equals(key.target) && this.pattern == key.pattern;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
