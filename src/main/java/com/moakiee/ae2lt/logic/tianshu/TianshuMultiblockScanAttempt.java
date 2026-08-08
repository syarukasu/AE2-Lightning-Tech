package com.moakiee.ae2lt.logic.tianshu;

import java.util.List;

/** 構造スキャンの成功・失敗・チャンク待機状態を区別します。 */
public record TianshuMultiblockScanAttempt(
        TianshuMultiblockScanResult result,
        List<TianshuMultiblockScanIssue> issues) {

    public TianshuMultiblockScanAttempt {
        issues = List.copyOf(issues);
    }

    public boolean formed() {
        return result != null && issues.isEmpty();
    }

    public boolean chunksUnavailable() {
        return issues.contains(TianshuMultiblockScanIssue.CHUNKS_UNLOADED);
    }
}
