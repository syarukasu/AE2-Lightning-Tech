package com.moakiee.ae2lt.logic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.crafting.IPatternDetails;

/**
 * Runtime scheduling owner for the provider's adjacent physical targets.
 *
 * <p>Direct mode splits every batch instantly and evenly across the targets
 * that can receive right now (at most one copy of difference per round). It
 * keeps no cross-tick fairness history; only the per target-pattern rejection
 * penalty survives between calls so a failing target is probed at most once
 * per tick and with exponential backoff afterwards.</p>
 */
final class ProviderNormalDispatch {
    private static final int INITIAL_COOLDOWN = 5;
    private static final int MAX_COOLDOWN = 40;

    private final Map<Direction, ProviderTarget> targets =
            new EnumMap<>(Direction.class);
    private final Map<ProviderTarget, Map<IPatternDetails, Penalty>> penalties =
            new HashMap<>();
    private final DueTaskQueue<TargetPatternKey<ProviderTarget>> expirations =
            new DueTaskQueue<>();
    private int cursor;

    ProviderTarget target(
            ServerLevel level,
            BlockPos providerPos,
            Direction pushDirection) {
        var targetPos = providerPos.relative(pushDirection);
        var targetFace = pushDirection.getOpposite();
        return targets.compute(pushDirection, (direction, current) -> {
            if (current == null
                    || !current.dimension().equals(level.dimension())
                    || !current.pos().equals(targetPos)
                    || current.boundFace() != targetFace) {
                return new ProviderTarget(
                        level.dimension(), targetPos, targetFace);
            }
            return current;
        });
    }

    void restore(Direction pushDirection, ProviderTarget target) {
        targets.put(pushDirection, target);
    }

    List<Direction> dispatchOrder(List<Direction> directions) {
        if (directions.isEmpty()) {
            return List.of();
        }
        int start = Math.floorMod(cursor, directions.size());
        var ordered = new ArrayList<Direction>(directions.size());
        for (int i = 0; i < directions.size(); i++) {
            ordered.add(directions.get((start + i) % directions.size()));
        }
        cursor = (cursor + 1) % directions.size();
        return ordered;
    }

    /**
     * Splits {@code maxCopies} evenly across the targets that are receivable
     * right now. Each round recomputes equal shares over the surviving
     * targets, so two targets never differ by more than one copy within a
     * round and copies rejected by full targets flow to the remaining ones.
     */
    long dispatchBatch(
            IPatternDetails pattern,
            List<ProviderTarget> orderedTargets,
            long maxCopies,
            long gameTick,
            BatchAttempt attempt) {
        var eligible = new ArrayList<ProviderTarget>(orderedTargets.size());
        for (var target : orderedTargets) {
            if (retryAfter(target, pattern, gameTick) <= gameTick) {
                eligible.add(target);
            }
        }

        long remaining = maxCopies;
        while (remaining > 0L && !eligible.isEmpty()) {
            int count = eligible.size();
            long base = remaining / count;
            long extra = remaining % count;
            long acceptedThisRound = 0L;
            var survivors = new ArrayList<ProviderTarget>(count);
            for (int i = 0; i < count && remaining > 0L; i++) {
                var target = eligible.get(i);
                long share = Math.min(
                        remaining, base + (i < extra ? 1L : 0L));
                if (share <= 0L) {
                    continue;
                }
                var result = attempt.push(target, share);
                if (result.ownedCopies() <= 0L) {
                    if (result.globalAbort()) {
                        return remaining;
                    }
                    recordRejection(target, pattern, gameTick);
                    continue;
                }

                recordSuccess(target, pattern);
                remaining -= result.ownedCopies();
                acceptedThisRound += result.ownedCopies();
                if (result.globalAbort() || result.stop()) {
                    return remaining;
                }
                if (result.ownedCopies() >= share) {
                    survivors.add(target);
                }
            }
            if (acceptedThisRound <= 0L) {
                break;
            }
            eligible = survivors;
        }
        return remaining;
    }

    long retryAfter(
            ProviderTarget target,
            IPatternDetails pattern,
            long gameTick) {
        purgeExpired(gameTick);
        var byPattern = penalties.get(target);
        if (byPattern == null) {
            return Long.MIN_VALUE;
        }
        var penalty = byPattern.get(pattern);
        return penalty == null ? Long.MIN_VALUE : penalty.retryAfter;
    }

    long recordRejection(
            ProviderTarget target,
            IPatternDetails pattern,
            long gameTick) {
        purgeExpired(gameTick);
        var byPattern = penalties.computeIfAbsent(
                target, ignored -> CanonicalPatternMaps.create());
        var previous = byPattern.get(pattern);
        int cooldown = previous == null
                ? INITIAL_COOLDOWN
                : Math.min(MAX_COOLDOWN, previous.cooldown * 2);
        long retryAfter = gameTick + cooldown;
        byPattern.put(pattern, new Penalty(retryAfter, cooldown));
        expirations.schedule(
                new TargetPatternKey<>(target, pattern), retryAfter);
        return retryAfter;
    }

    void recordSuccess(ProviderTarget target, IPatternDetails pattern) {
        var byPattern = penalties.get(target);
        if (byPattern == null) {
            return;
        }
        byPattern.remove(pattern);
        expirations.remove(new TargetPatternKey<>(target, pattern));
        if (byPattern.isEmpty()) {
            penalties.remove(target);
        }
    }

    void patternsChanged() {
        penalties.clear();
        expirations.clear();
        for (var target : targets.values()) {
            target.clearBatchHistory();
        }
    }

    void clearRuntimeState() {
        for (var target : targets.values()) {
            target.clearRuntimeState();
        }
        patternsChanged();
    }

    void clear() {
        clearRuntimeState();
        targets.clear();
        cursor = 0;
    }

    private void purgeExpired(long gameTick) {
        TargetPatternKey<ProviderTarget> expired;
        while ((expired = expirations.pollDue(gameTick)) != null) {
            var byPattern = penalties.get(expired.target());
            if (byPattern == null) {
                continue;
            }
            var penalty = byPattern.get(expired.pattern());
            if (penalty == null || penalty.retryAfter > gameTick) {
                continue;
            }
            byPattern.remove(expired.pattern());
            if (byPattern.isEmpty()) {
                penalties.remove(expired.target());
            }
        }
    }

    private record Penalty(long retryAfter, int cooldown) {
    }

    @FunctionalInterface
    interface BatchAttempt {
        BatchAttemptResult push(ProviderTarget target, long maxCopies);
    }

    record BatchAttemptResult(
            long ownedCopies, boolean globalAbort, boolean stop) {
    }
}
