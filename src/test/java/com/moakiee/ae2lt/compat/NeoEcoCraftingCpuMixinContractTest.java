package com.moakiee.ae2lt.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class NeoEcoCraftingCpuMixinContractTest {

    private static final Path MIXIN_SOURCE = Path.of(
            "src/main/java/com/moakiee/ae2lt/mixin/ECOCraftingCpuLogicMixin.java");

    @Test
    void supportsNeoEco20_3And20_4ProviderPushLocations() throws Exception {
        String source = Files.readString(MIXIN_SOURCE);

        assertTrue(source.contains("method = \"tryPushSlowPattern\""),
                "NeoECO 20.3.x provider-push entry point is missing");
        assertTrue(source.contains("method = \"executeCrafting\""),
                "NeoECO 20.4.x provider-push entry point is missing");

        // Issue #43: one NeoECO version never contains both methods, so both selectors must remain optional.
        assertEquals(2, countOccurrences(source, "require = 0)"),
                "both version-specific provider-push injections must be optional");
        assertEquals(2, countOccurrences(source,
                        "return ae2lt$pushProviderAndRegisterExpectedOutputs(provider, details, inputHolder, original);"),
                "both version-specific entry points must delegate to the same accounting implementation");
    }

    private static int countOccurrences(String source, String expected) {
        int count = 0;
        int offset = 0;

        // Count every non-overlapping annotation or delegation occurrence in the source contract.
        while (true) {
            int found = source.indexOf(expected, offset);
            if (found < 0) {
                return count;
            }
            count++;
            offset = found + expected.length();
        }
    }
}
