package com.breakinblocks.neovitae.common.block;

import net.minecraft.util.StringRepresentable;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;

import javax.annotation.Nullable;
import java.util.Locale;

public enum AnchorAura implements StringRepresentable {
    DORMANT(null),
    BLOOD(null),
    RAW(SpiritusType.RAW),
    RUINA(SpiritusType.RUINA),
    NIHILUM(SpiritusType.NIHILUM),
    VINDICTA(SpiritusType.VINDICTA),
    INVICTUS(SpiritusType.INVICTUS);

    @Nullable
    private final SpiritusType type;

    AnchorAura(@Nullable SpiritusType type) {
        this.type = type;
    }

    @Nullable
    public SpiritusType type() {
        return type;
    }

    public static AnchorAura fromMask(boolean anyActive, int mask) {
        if (!anyActive) return DORMANT;
        if (Integer.bitCount(mask) != 1) return BLOOD;
        SpiritusType only = SpiritusType.values()[Integer.numberOfTrailingZeros(mask)];
        for (AnchorAura aura : values()) {
            if (aura.type == only) return aura;
        }
        return BLOOD;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
