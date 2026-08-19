package com.moakiee.ae2lt.logic.tianshu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

/** 2.0.6 Tianshu Supercomputing Array の固定構造定義です。 */
public final class TianshuMultiblockTemplate {
    public static final int SIZE = 7;
    public static final BlockPos CONTROLLER = new BlockPos(6, 0, 3);
    public static final BlockPos LOWER_PORT = new BlockPos(3, 0, 3);
    public static final BlockPos UPPER_PORT = new BlockPos(3, 6, 3);
    public static final BlockPos CORE_CENTER = new BlockPos(3, 3, 3);

    private TianshuMultiblockTemplate() {
    }

    public static TianshuMultiblockRole roleAt(BlockPos localPos) {
        return roleAt(localPos.getX(), localPos.getY(), localPos.getZ());
    }

    public static TianshuMultiblockRole roleAt(int x, int y, int z) {
        // 構造外は読み飛ばし、7×7×7の範囲だけを検査します。
        if (!inside(x) || !inside(y) || !inside(z)) {
            return TianshuMultiblockRole.IGNORED;
        }
        // コントローラとポート候補は外周の通常ブロックより先に判定します。
        if (new BlockPos(x, y, z).equals(CONTROLLER)) {
            return TianshuMultiblockRole.CONTROLLER;
        }
        if (new BlockPos(x, y, z).equals(LOWER_PORT)
                || new BlockPos(x, y, z).equals(UPPER_PORT)) {
            return TianshuMultiblockRole.PORT_CANDIDATE;
        }
        // 3×3×3の中央はメインコアと周辺ユニットの区画です。
        if (betweenTwoAndFour(x) && betweenTwoAndFour(y) && betweenTwoAndFour(z)) {
            return TianshuMultiblockRole.CORE_RESERVED;
        }
        // 内側の5×5×5の面は透明ガラスです。
        if (betweenOneAndFive(x) && betweenOneAndFive(y) && betweenOneAndFive(z)
                && (x == 1 || x == 5 || y == 1 || y == 5 || z == 1 || z == 5)) {
            return TianshuMultiblockRole.GLASS;
        }
        // 上下の中央3×3面は冷却または閉ループストレージ区画です。
        if ((y == 0 || y == 6) && betweenTwoAndFour(x) && betweenTwoAndFour(z)) {
            return TianshuMultiblockRole.COOLING;
        }
        // 外周の角・辺は構造ケーシングです。
        if (boundary(x) + boundary(y) + boundary(z) >= 2 || y == 0 || y == 6) {
            return TianshuMultiblockRole.CASING;
        }
        return TianshuMultiblockRole.IGNORED;
    }

    public static List<Entry> entries() {
        ArrayList<Entry> entries = new ArrayList<>(SIZE * SIZE * SIZE);
        // 全座標を一度だけ列挙して、スキャン側の分岐を固定します。
        for (int y = 0; y < SIZE; ++y) {
            for (int z = 0; z < SIZE; ++z) {
                for (int x = 0; x < SIZE; ++x) {
                    TianshuMultiblockRole role = roleAt(x, y, z);
                    if (role != TianshuMultiblockRole.IGNORED) {
                        entries.add(new Entry(new BlockPos(x, y, z), role));
                    }
                }
            }
        }
        return List.copyOf(entries);
    }

    private static boolean inside(int value) {
        return value >= 0 && value < SIZE;
    }

    private static boolean betweenOneAndFive(int value) {
        return value >= 1 && value <= 5;
    }

    private static boolean betweenTwoAndFour(int value) {
        return value >= 2 && value <= 4;
    }

    private static int boundary(int value) {
        return value == 0 || value == SIZE - 1 ? 1 : 0;
    }

    public record Entry(BlockPos localPos, TianshuMultiblockRole role) {
    }
}
