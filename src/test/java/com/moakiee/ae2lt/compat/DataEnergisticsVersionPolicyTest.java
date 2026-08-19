package com.moakiee.ae2lt.compat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.jupiter.api.Test;

class DataEnergisticsVersionPolicyTest {
    @Test
    void missingOrOlderVersionWarns() {
        assertTrue(DataEnergisticsVersionPolicy.shouldWarn(null));
        assertTrue(DataEnergisticsVersionPolicy.shouldWarn(new DefaultArtifactVersion("2.4.3")));
    }

    @Test
    void silentBoundaryDoesNotWarn() {
        assertFalse(DataEnergisticsVersionPolicy.shouldWarn(new DefaultArtifactVersion("2.4.4")));
        assertFalse(DataEnergisticsVersionPolicy.shouldWarn(new DefaultArtifactVersion("2.5.0")));
    }
}
