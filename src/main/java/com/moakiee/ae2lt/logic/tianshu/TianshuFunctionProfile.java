package com.moakiee.ae2lt.logic.tianshu;

public record TianshuFunctionProfile(int closedLoopPatternStorageCount, int closedLoopSeedStorageCount) {
    public static final int PATTERNS_PER_CLOSED_LOOP_STORAGE = 36;

    public TianshuFunctionProfile {
        if (closedLoopPatternStorageCount < 0 || closedLoopSeedStorageCount < 0) {
            throw new IllegalArgumentException("Tianshu function unit counts cannot be negative");
        }
    }

    public static TianshuFunctionProfile empty() {
        return new TianshuFunctionProfile(0, 0);
    }

    public boolean supportsInventoryMaintenance() {
        return true;
    }

    public boolean supportsClosedLoopPatterns() {
        return true;
    }

    public boolean supportsClosedLoopSeeds() {
        return this.closedLoopSeedStorageCount > 0;
    }

    public int maintenanceRuleCapacity() {
        return 2048;
    }

    public int closedLoopPatternCapacity() {
        return saturatingMultiply(this.closedLoopPatternStorageCount, PATTERNS_PER_CLOSED_LOOP_STORAGE);
    }

    private static int saturatingMultiply(int count, int perUnit) {
        long result = (long) count * (long) perUnit;
        return result >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }
}
