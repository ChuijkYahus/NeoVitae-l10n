package com.breakinblocks.neovitae.client.color;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neovitae.common.block.PillarAccent;

import org.jetbrains.annotations.Nullable;

public class PillarAccentColor implements BlockColor, ItemColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex != 0 || !state.hasProperty(PillarAccent.PROPERTY)) return -1;
        return 0xFF000000 | state.getValue(PillarAccent.PROPERTY).color();
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) return -1;
        PillarAccent accent = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(PillarAccent.PROPERTY);
        return 0xFF000000 | (accent == null ? PillarAccent.DEFAULT_COLOR : accent.color());
    }
}
