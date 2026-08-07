package com.moakiee.ae2lt.celestweave.phase;

import net.minecraft.world.entity.EquipmentSlot;

public final class PhaseLockProjectionRules {
    private PhaseLockProjectionRules() {
    }

    public static int expectedInventorySlot(EquipmentSlot slot) {
        if (slot == null || slot.getType() != EquipmentSlot.Type.ARMOR) {
            return -1;
        }
        return 36 + slot.getIndex();
    }

    public static boolean isExpectedSlot(EquipmentSlot slot, int slotId) {
        return slotId == expectedInventorySlot(slot);
    }
}
