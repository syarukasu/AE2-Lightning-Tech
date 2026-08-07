package com.moakiee.ae2lt.celestweave;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class PhaseFlightControlRules {
    private PhaseFlightControlRules() {
    }

    public static boolean rejectFlightToggle(boolean phaseModeEnabled, boolean insideWall, boolean requestedFlying) {
        return phaseModeEnabled && insideWall && !requestedFlying;
    }

    public static boolean suppressLandingExit(boolean phaseModeEnabled) {
        return phaseModeEnabled;
    }

    public static boolean intersectsWorldCollision(Player player) {
        return player != null && !player.level().noCollision((Entity) player, player.getBoundingBox());
    }
}
