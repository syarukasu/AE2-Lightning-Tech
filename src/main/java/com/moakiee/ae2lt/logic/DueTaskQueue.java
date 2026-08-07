package com.moakiee.ae2lt.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

final class DueTaskQueue<K> {
    private static final int MIN_COMPACTION_SIZE = 64;
    private static final int COMPACTION_MULTIPLIER = 4;

    private final Map<K, Schedule> schedules = new HashMap<>();
    private final PriorityQueue<Entry<K>> entries = new PriorityQueue<>((left, right) -> {
        int dueComparison = Long.compare(left.dueTick(), right.dueTick());
        return dueComparison != 0 ? dueComparison : Long.compare(left.sequence(), right.sequence());
    });

    private long nextToken;
    private long nextSequence;

    void schedule(K key, long dueTick) {
        long token = ++this.nextToken;
        this.schedules.put(key, new Schedule(dueTick, token));
        this.entries.add(new Entry<>(key, dueTick, token, this.nextSequence++));
        this.compactIfNeeded();
    }

    boolean contains(K key) {
        return this.schedules.containsKey(key);
    }

    void remove(K key) {
        this.schedules.remove(key);
    }

    void retainAll(Set<K> retainedKeys) {
        this.schedules.keySet().retainAll(retainedKeys);
        this.compactIfNeeded();
    }

    @Nullable
    K pollDue(long gameTick) {
        this.discardStaleHead();
        Entry<K> entry = this.entries.peek();
        if (entry == null || entry.dueTick() > gameTick) {
            return null;
        }
        this.entries.poll();
        this.schedules.remove(entry.key());
        return entry.key();
    }

    long nextDueTick() {
        this.discardStaleHead();
        Entry<K> entry = this.entries.peek();
        return entry != null ? entry.dueTick() : Long.MAX_VALUE;
    }

    int size() {
        return this.schedules.size();
    }

    void clear() {
        this.schedules.clear();
        this.entries.clear();
    }

    private void discardStaleHead() {
        while (!this.entries.isEmpty() && !this.isLive(this.entries.peek())) {
            this.entries.poll();
        }
    }

    private boolean isLive(Entry<K> entry) {
        Schedule schedule = this.schedules.get(entry.key());
        return schedule != null && schedule.token() == entry.token() && schedule.dueTick() == entry.dueTick();
    }

    private void compactIfNeeded() {
        int liveCount = this.schedules.size();
        int threshold = Math.max(MIN_COMPACTION_SIZE, liveCount * COMPACTION_MULTIPLIER);
        if (this.entries.size() <= threshold) {
            return;
        }
        this.entries.clear();
        for (Map.Entry<K, Schedule> scheduled : this.schedules.entrySet()) {
            Schedule value = scheduled.getValue();
            this.entries.add(new Entry<>(scheduled.getKey(), value.dueTick(), value.token(), this.nextSequence++));
        }
    }

    private record Schedule(long dueTick, long token) {
    }

    private record Entry<K>(K key, long dueTick, long token, long sequence) {
    }
}
