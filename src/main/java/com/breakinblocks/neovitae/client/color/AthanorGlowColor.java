package com.breakinblocks.neovitae.client.color;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neovitae.common.block.AthanorBlock;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;

import org.jetbrains.annotations.Nullable;

public class AthanorGlowColor implements BlockColor {

    public static final int NO_SPIRITUS = 0xFFFF3A2A;

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 0 || !state.hasProperty(AthanorBlock.GLOW)) return -1;
        return blend(state.getValue(AthanorBlock.GLOW));
    }

    public static int blend(int mask) {
        int red = 0;
        int green = 0;
        int blue = 0;
        int count = 0;
        for (SpiritusType type : SpiritusType.values()) {
            if ((mask & (1 << type.ordinal())) == 0) continue;
            int color = color(type);
            red += (color >> 16) & 0xFF;
            green += (color >> 8) & 0xFF;
            blue += color & 0xFF;
            count++;
        }
        if (count == 0) return NO_SPIRITUS;
        return 0xFF000000 | (red / count) << 16 | (green / count) << 8 | blue / count;
    }

    public static int color(SpiritusType type) {
        return switch (type) {
            case RAW -> 0x66E6E6;
            case RUINA -> 0x55EE55;
            case NIHILUM -> 0xFF8844;
            case VINDICTA -> 0xBB55EE;
            case INVICTUS -> 0xEEEEEE;
        };
    }
}
