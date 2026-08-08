package com.moakiee.ae2lt.logic.tianshu;

/** Tianshuの形成済みランタイムを参照する共通入口です。 */
public final class TianshuSupercomputerRuntime {
    private TianshuSupercomputerRuntime() {
    }

    public static boolean thunderboltAvailable() {
        return ThunderboltBridge.isAvailable();
    }
}
