package com.breakinblocks.neovitae.client.color;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neovitae.common.block.RitualAnchorBlock;
import com.breakinblocks.neovitae.common.blockentity.RitualAnchorBlockEntity;

import org.jetbrains.annotations.Nullable;

public class RitualAnchorColor implements BlockColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 0 || !state.hasProperty(RitualAnchorBlock.AURA)) return -1;
        return 0xFF000000 | RitualAnchorBlockEntity.auraColor(state.getValue(RitualAnchorBlock.AURA));
    }
}
