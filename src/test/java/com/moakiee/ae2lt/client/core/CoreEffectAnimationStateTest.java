package com.moakiee.ae2lt.client.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoreEffectAnimationStateTest {
    @Test
    void initialSampleUsesTargetActivity() {
        CoreEffectAnimationState state = new CoreEffectAnimationState();
        CoreEffectAnimationState.MotionProfile profile = new CoreEffectAnimationState.MotionProfile(
                1.0, 3.0, 10.0,
                2.0, 4.0, 12.0,
                0.5, 1.5);

        CoreEffectAnimationState.Sample idle = state.sample(40.0, false, profile);
        assertEquals(0.0, idle.activity());
        assertEquals(2.0, idle.primaryPhase(), 1.0E-9);
        assertEquals(4.0, idle.secondaryPhase(), 1.0E-9);
        assertEquals(1.0, idle.glowPhase(), 1.0E-9);
        assertEquals(2.0, idle.ambientTime(), 1.0E-9);
    }

    @Test
    void activityApproachesWorkingStateExponentially() {
        CoreEffectAnimationState state = new CoreEffectAnimationState();
        CoreEffectAnimationState.MotionProfile profile = new CoreEffectAnimationState.MotionProfile(
                1.0, 2.0, 10.0,
                1.0, 2.0, 10.0,
                1.0, 2.0);

        state.sample(0.0, false, profile);
        CoreEffectAnimationState.Sample sample = state.sample(20.0, true, profile);
        assertTrue(sample.activity() > 0.99);
        assertTrue(sample.activity() < 1.0);
    }

    @Test
    void nonPositiveElapsedTimeDoesNotAdvancePhases() {
        CoreEffectAnimationState state = new CoreEffectAnimationState();
        CoreEffectAnimationState.MotionProfile profile = new CoreEffectAnimationState.MotionProfile(
                1.0, 2.0, 10.0,
                1.0, 2.0, 10.0,
                1.0, 2.0);

        CoreEffectAnimationState.Sample first = state.sample(20.0, true, profile);
        CoreEffectAnimationState.Sample sameTick = state.sample(20.0, false, profile);
        assertEquals(first, sameTick);
    }
}
