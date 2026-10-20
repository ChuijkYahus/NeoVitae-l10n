package com.breakinblocks.neovitae.common.item.soul;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum LexVitaeForm {
    BASE,
    PICKAXE,
    AXE,
    SHOVEL,
    HOE,
    SWORD;

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static LexVitaeForm forTarget(ItemStack stack, Level level, @Nullable HitResult hit) {
        if (!LexVitaeItem.isActive(stack) || hit == null) return BASE;
        if (hit instanceof EntityHitResult entityHit) {
            return entityHit.getEntity() instanceof LivingEntity && LexVitaeItem.canAttack(stack) ? SWORD : BASE;
        }
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK && LexVitaeItem.canMine(stack)) {
            return forBlock(level.getBlockState(blockHit.getBlockPos()));
        }
        return BASE;
    }

    public static LexVitaeForm forBlock(BlockState state) {
        if (state.is(BlockTags.CROPS) || state.is(BlockTags.MAINTAINS_FARMLAND) || state.is(Blocks.FARMLAND)) return HOE;
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) return AXE;
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return PICKAXE;
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return SHOVEL;
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) return HOE;
        return BASE;
    }
}
