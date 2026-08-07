package com.moakiee.ae2lt.logic.tianshu.terminal;

public enum ProcessingPatternEncodingType {
    NORMAL(false, false),
    ADVANCED(true, false),
    OVERLOAD(false, true),
    ADVANCED_OVERLOAD(true, true);

    private final boolean advanced;
    private final boolean overload;

    ProcessingPatternEncodingType(boolean advanced, boolean overload) {
        this.advanced = advanced;
        this.overload = overload;
    }

    public boolean hasAdvanced() {
        return this.advanced;
    }

    public boolean hasOverload() {
        return this.overload;
    }

    public boolean includes(ProcessingPatternEncodingType capability) {
        return !(capability.advanced && !this.advanced || capability.overload && !this.overload);
    }

    public static ProcessingPatternEncodingType fromConfigs(AdvancedConfig advancedConfig, OverloadConfig overloadConfig) {
        if (advancedConfig != null) {
            return overloadConfig != null ? ADVANCED_OVERLOAD : ADVANCED;
        }
        return overloadConfig != null ? OVERLOAD : NORMAL;
    }

    public record OverloadConfig(int[] inputIdOnly, int[] outputIdOnly) {
        public OverloadConfig {
            inputIdOnly = inputIdOnly == null ? new int[]{} : inputIdOnly.clone();
            outputIdOnly = outputIdOnly == null ? new int[]{} : outputIdOnly.clone();
        }

        @Override
        public int[] inputIdOnly() {
            return this.inputIdOnly.clone();
        }

        @Override
        public int[] outputIdOnly() {
            return this.outputIdOnly.clone();
        }

        public boolean isInputIdOnly(int slot) {
            return contains(this.inputIdOnly, slot);
        }

        public boolean isOutputIdOnly(int slot) {
            return contains(this.outputIdOnly, slot);
        }

        private static boolean contains(int[] slots, int slot) {
            for (int candidate : slots) {
                if (candidate == slot) {
                    return true;
                }
            }
            return false;
        }
    }

    public record AdvancedConfig(int[] directions) {
        public AdvancedConfig {
            directions = directions == null ? new int[]{} : directions.clone();
        }

        @Override
        public int[] directions() {
            return this.directions.clone();
        }

        public int direction(int slot) {
            return slot >= 0 && slot < this.directions.length
                    ? Math.max(0, Math.min(6, this.directions[slot]))
                    : 0;
        }
    }
}
