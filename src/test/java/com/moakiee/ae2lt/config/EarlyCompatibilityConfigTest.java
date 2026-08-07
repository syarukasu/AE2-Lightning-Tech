package com.moakiee.ae2lt.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EarlyCompatibilityConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void missingFileDefaultsProtectionToEnabled() {
        assertTrue(EarlyCompatibilityConfig.readDataEnergisticsProtection(tempDir.resolve("missing.toml")));
    }

    @Test
    void compatibilitySectionCanDisableProtection() throws IOException {
        Path file = tempDir.resolve("ae2lt-common.toml");
        Files.writeString(file, "[compatibility]\ndataEnergisticsMixinProtection = false\n");
        assertFalse(EarlyCompatibilityConfig.readDataEnergisticsProtection(file));
    }

    @Test
    void dottedKeyCanEnableProtection() throws IOException {
        Path file = tempDir.resolve("ae2lt-common.toml");
        Files.writeString(file, "compatibility.dataEnergisticsMixinProtection = true # keep enabled\n");
        assertTrue(EarlyCompatibilityConfig.readDataEnergisticsProtection(file));
    }

    @Test
    void invalidValueFailsClosedToEnabled() throws IOException {
        Path file = tempDir.resolve("ae2lt-common.toml");
        Files.writeString(file, "[compatibility]\ndataEnergisticsMixinProtection = maybe\n");
        assertTrue(EarlyCompatibilityConfig.readDataEnergisticsProtection(file));
    }
}
