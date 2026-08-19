package com.moakiee.ae2lt.compat;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/** 2.0.7のData Energistics互換警告境界をForge側へ移植します。 */
public final class DataEnergisticsVersionPolicy {
    // このバージョン以上では、旧互換警告を表示する必要がありません。
    private static final ArtifactVersion SILENT_FROM = new DefaultArtifactVersion("2.4.4");

    private DataEnergisticsVersionPolicy() {
    }

    /**
     * インストール済みData Energisticsが警告対象かを返します。
     * nullは未検出またはバージョン不明として警告対象にします。
     */
    public static boolean shouldWarn(ArtifactVersion installedVersion) {
        // 未検出、または互換境界より古いバージョンは安全側で警告します。
        if (installedVersion == null || installedVersion.compareTo(SILENT_FROM) < 0) {
            return true;
        }
        return false;
    }
}
