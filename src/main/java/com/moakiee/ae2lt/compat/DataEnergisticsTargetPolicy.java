package com.moakiee.ae2lt.compat;

import java.util.List;

public final class DataEnergisticsTargetPolicy {
    private static final String DATA_ENERGISTICS_MIXIN_PACKAGE = "com.fish_dan_.data_energistics.mixin.";
    // 2.0.7で追加された旧AE2LT経路は対象クラス一覧を持たないため、名前だけで無効化します。
    private static final String LEGACY_AE2LT_PATHING_MIXIN =
            "com.fish_dan_.data_energistics.mixin.ae2lt.Ae2ltPathingCalculationCompatMixin";
    private static final String OWNED_TARGET_PACKAGE = "com.moakiee.";

    private DataEnergisticsTargetPolicy() {
    }

    public static boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        // Data Energistics以外のMixinはこの互換ポリシーの対象外です。
        if (mixinClassName == null || !mixinClassName.startsWith(DATA_ENERGISTICS_MIXIN_PACKAGE)) {
            return false;
        }
        // 旧AE2LTの経路計算MixinはForgeポート側の経路計算と競合するため、常に停止します。
        if (LEGACY_AE2LT_PATHING_MIXIN.equals(mixinClassName)) {
            return true;
        }
        // 対象クラスが提供されないMixinは、キャンセルせずData Energistics側へ委譲します。
        if (targetClassNames == null) {
            return false;
        }
        // AE2LT所有クラスへ注入するMixinだけを停止し、他MODの対象は維持します。
        for (String targetClassName : targetClassNames) {
            if (isOwnedTarget(targetClassName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOwnedTarget(String targetClassName) {
        if (targetClassName == null) {
            return false;
        }
        String normalized = targetClassName.replace('/', '.');
        if (normalized.startsWith("L") && normalized.endsWith(";")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized.startsWith(OWNED_TARGET_PACKAGE);
    }
}
