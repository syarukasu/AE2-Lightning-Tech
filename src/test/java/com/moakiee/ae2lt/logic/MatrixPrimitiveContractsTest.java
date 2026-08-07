package com.moakiee.ae2lt.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.moakiee.ae2lt.logic.craft.MatrixCoreMode;
import com.moakiee.ae2lt.logic.craft.MatrixCraftingEnergy;
import com.moakiee.ae2lt.logic.craft.MatrixPatternStorageTier;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MatrixPrimitiveContractsTest {
    @Test
    void matrixEnumsMatch206Contract() {
        assertEquals(0, MatrixPatternStorageTier.CUSTOM.capacity());
        assertEquals(36, MatrixPatternStorageTier.T1.capacity());
        assertEquals(72, MatrixPatternStorageTier.T2.capacity());
        assertEquals(6, MatrixCoreMode.values().length);
        assertEquals(MatrixCoreMode.CONFLICT, MatrixCoreMode.valueOf("CONFLICT"));
        assertEquals(PatternInputAcceptance.COMPLETE_BATCH, PatternInputAcceptance.valueOf("COMPLETE_BATCH"));
    }

    @Test
    void unlimitedMatrixEnergyNeverRejectsPositiveOperations() {
        assertEquals(0L, MatrixCraftingEnergy.UNLIMITED.affordableOperations(-1));
        assertEquals(0L, MatrixCraftingEnergy.UNLIMITED.affordableOperations(0));
        assertEquals(123456789L, MatrixCraftingEnergy.UNLIMITED.affordableOperations(123456789L));
        MatrixCraftingEnergy.UNLIMITED.consumeOperations(Long.MAX_VALUE);
    }

    @Test
    void dueQueueUsesLatestScheduleForAKey() {
        DueTaskQueue<String> queue = new DueTaskQueue<>();
        queue.schedule("provider", 100);
        queue.schedule("provider", 20);

        assertEquals(1, queue.size());
        assertEquals(20L, queue.nextDueTick());
        assertNull(queue.pollDue(19));
        assertEquals("provider", queue.pollDue(20));
        assertFalse(queue.contains("provider"));
        assertEquals(Long.MAX_VALUE, queue.nextDueTick());
    }

    @Test
    void dueQueuePreservesSequenceForEqualTicksAndDropsStaleEntries() {
        DueTaskQueue<String> queue = new DueTaskQueue<>();
        queue.schedule("a", 10);
        queue.schedule("b", 10);
        queue.schedule("stale", 1);
        queue.remove("stale");

        assertEquals("a", queue.pollDue(10));
        assertEquals("b", queue.pollDue(10));
        assertNull(queue.pollDue(10));
    }

    @Test
    void dueQueueRetainsOnlyLiveKeys() {
        DueTaskQueue<String> queue = new DueTaskQueue<>();
        queue.schedule("a", 5);
        queue.schedule("b", 6);
        queue.schedule("c", 7);
        queue.retainAll(Set.of("b", "c"));

        assertFalse(queue.contains("a"));
        assertTrue(queue.contains("b"));
        assertTrue(queue.contains("c"));
        assertEquals(2, queue.size());
        assertEquals("b", queue.pollDue(6));
        queue.clear();
        assertEquals(0, queue.size());
        assertEquals(Long.MAX_VALUE, queue.nextDueTick());
    }
}
