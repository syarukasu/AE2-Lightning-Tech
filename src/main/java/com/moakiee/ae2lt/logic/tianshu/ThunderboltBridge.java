package com.moakiee.ae2lt.logic.tianshu;

import java.util.Set;

/**
 * Thunderboltの任意連携点です。
 * Forge 1.20.1版にはThunderbolt本体を必須依存として持たせず、存在時だけ状態を公開します。
 */
public final class ThunderboltBridge {
    private static final Set<String> OPTIONAL_TYPES = Set.of(
            "com.moakiee.thunderbolt.ae2.timewheel.TimeWheelCraftingCpuPool",
            "com.moakiee.thunderbolt.ae2.crafting.PatternFiringExpander");

    private ThunderboltBridge() {
    }

    public static boolean isAvailable() {
        for (String name : OPTIONAL_TYPES) {
            try {
                Class.forName(name, false, ThunderboltBridge.class.getClassLoader());
            } catch (ClassNotFoundException missing) {
                return false;
            }
        }
        return true;
    }

    public static String statusKey() {
        return isAvailable() ? "thunderbolt.available" : "thunderbolt.not_loaded";
    }
}
