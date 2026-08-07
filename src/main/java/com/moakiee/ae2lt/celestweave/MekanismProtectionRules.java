package com.moakiee.ae2lt.celestweave;

public final class MekanismProtectionRules {
    public static final int RADIATION_REGEN_INTERVAL_TICKS = 20;
    public static final float MIN_RADIATION_HEALING = 2.0f;
    public static final float MAX_RADIATION_HEALING = 10.0f;

    private MekanismProtectionRules() {
    }

    public static boolean shouldRegenerate(
            long gameTime,
            double radiationLevel,
            double minimumRadiation,
            float health,
            float maximumHealth) {
        return gameTime % RADIATION_REGEN_INTERVAL_TICKS == 0L
                && Double.isFinite(radiationLevel)
                && Double.isFinite(minimumRadiation)
                && radiationLevel >= Math.max(0.0, minimumRadiation)
                && health > 0.0f
                && health < maximumHealth;
    }

    public static float radiationHealing(double scaledSeverity) {
        double severity = Double.isFinite(scaledSeverity) ? Math.max(0.0, Math.min(1.0, scaledSeverity)) : 0.0;
        return (float) (MIN_RADIATION_HEALING
                + (MAX_RADIATION_HEALING - MIN_RADIATION_HEALING) * severity);
    }

    public static long absorbedJoules(long availableJoules, double dissipationPercent) {
        if (availableJoules <= 0L || !Double.isFinite(dissipationPercent) || dissipationPercent <= 0.0) {
            return 0L;
        }
        if (dissipationPercent >= 1.0) {
            return availableJoules;
        }
        return Math.max(0L, (long) Math.floor((double) availableJoules * dissipationPercent));
    }

    public static long joulesToForgeEnergy(long joules, double joulesPerForgeEnergy) {
        if (joules <= 0L || !Double.isFinite(joulesPerForgeEnergy) || joulesPerForgeEnergy <= 0.0) {
            return 0L;
        }
        double converted = (double) joules / joulesPerForgeEnergy;
        if (converted >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, (long) Math.floor(converted));
    }
}
