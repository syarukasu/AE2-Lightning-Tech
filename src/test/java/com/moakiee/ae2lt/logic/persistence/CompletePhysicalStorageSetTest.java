package com.moakiee.ae2lt.logic.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CompletePhysicalStorageSetTest {
    @Test
    void resolvesOnlyWhenEveryPhysicalPositionIsPresent() {
        Optional<List<String>> result = CompletePhysicalStorageSet.resolve(
                List.of(1, 2, 3),
                value -> "storage-" + value);

        assertTrue(result.isPresent());
        assertEquals(List.of("storage-1", "storage-2", "storage-3"), result.orElseThrow());
    }

    @Test
    void missingAnyStorageInvalidatesWholeSet() {
        Optional<String> unused = Optional.empty();
        assertTrue(unused.isEmpty());
        assertTrue(CompletePhysicalStorageSet.resolve(
                List.of(1, 2, 3),
                value -> value == 2 ? null : "storage-" + value).isEmpty());
    }

    @Test
    void nullInputsAreRejectedAsIncomplete() {
        assertTrue(CompletePhysicalStorageSet.<Integer, String>resolve(null, Object::toString).isEmpty());
        assertTrue(CompletePhysicalStorageSet.<Integer, String>resolve(List.of(1), null).isEmpty());
    }

    @Test
    void resultIsImmutableSnapshot() {
        List<String> result = CompletePhysicalStorageSet.resolve(List.of(1), value -> "storage").orElseThrow();
        assertThrows(UnsupportedOperationException.class, () -> result.add("other"));
    }
}
