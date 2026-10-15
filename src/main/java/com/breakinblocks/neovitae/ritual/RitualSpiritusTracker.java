package com.breakinblocks.neovitae.ritual;

import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;

public final class RitualSpiritusTracker {

    private static final ThreadLocal<int[]> CURRENT = new ThreadLocal<>();

    private RitualSpiritusTracker() {}

    public static void begin() {
        CURRENT.set(new int[1]);
    }

    public static int end() {
        int[] mask = CURRENT.get();
        CURRENT.remove();
        return mask == null ? 0 : mask[0];
    }

    public static void record(SpiritusType type) {
        int[] mask = CURRENT.get();
        if (mask != null) {
            mask[0] |= 1 << type.ordinal();
        }
    }
}
