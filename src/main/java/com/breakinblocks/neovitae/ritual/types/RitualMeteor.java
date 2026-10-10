// Derived from Blood Magic (https://github.com/WayofTime/BloodMagic), licensed under CC BY 4.0
// SPDX-FileCopyrightText: 2022 WayofTime <https://github.com/WayofTime>
// SPDX-FileCopyrightText: 2024-2026 Saereth <https://github.com/breakinblocks/NeoVitae>
// SPDX-License-Identifier: CC-BY-4.0 AND MIT

package com.breakinblocks.neovitae.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.entity.projectile.EntityMeteor;
import com.breakinblocks.neovitae.common.recipe.meteor.MeteorRecipe;
import com.breakinblocks.neovitae.compat.sable.SableCompat;
import com.breakinblocks.neovitae.common.recipe.meteor.MeteorRecipeHelper;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.api.stream.StreamPresets;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.RitualHelper.RitualContext;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Ritual that summons meteors based on item catalysts.
 * Searches for items within the ritual range and matches them to meteor recipes.
 * When a match is found, spawns a meteor entity that falls and generates blocks.
 */
public class RitualMeteor extends Ritual {

    public static final String CHECK_RANGE = "itemRange";
    public static final String CHEST_RANGE = "chestRange";
    private static final int CONTRAPTION_FALL_HEIGHT = 24;
    private static final double MAX_LANDING_OCCUPANCY = 0.1;
    private static final int LANDING_RECHECK_TICKS = 100;

    private long nextLandingCheck;
    private UUID pendingMeteor;

    public RitualMeteor() {
        this("meteor", 1, 250000);
    }

    protected RitualMeteor(String name, int crystalLevel, int activationCost) {
        super(name, crystalLevel, activationCost, "ritual." + NeoVitae.MODID + "." + name);
        addBlockRange(CHECK_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1, 1, 1));
        setMaximumVolumeAndDistanceOfRange(CHECK_RANGE, 27, 10, 10);
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1, 1, 1));
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        RitualContext ctx = RitualHelper.createContext(masterRitualStone, 0);
        if (ctx == null) return;

        BlockPos chestPos = RitualHelper.firstPositionInRange(ctx.master(), this, CHEST_RANGE, ctx.masterPos())
                .orElse(ctx.masterPos().above());
        IItemHandler chest = ctx.level().getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);
        if (chest != null) {
            for (int slot = 0; slot < chest.getSlots(); slot++) {
                ItemStack stack = chest.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                MeteorRecipe recipe = MeteorRecipeHelper.findRecipe(ctx.level(), stack);
                if (recipe == null || chest.extractItem(slot, 1, true).isEmpty()) continue;
                if (callMeteor(ctx, recipe, stack)) {
                    chest.extractItem(slot, 1, false);
                }
                return;
            }
        }

        AreaDescriptor itemRange = RitualHelper.getEffectiveRange(ctx.master(), this, CHECK_RANGE);
        List<ItemEntity> itemList = ctx.level().getEntitiesOfClass(ItemEntity.class,
                itemRange.getAABB(ctx.masterPos()));

        for (ItemEntity entityItem : itemList) {
            if (!entityItem.isAlive()) {
                continue;
            }

            ItemStack stack = entityItem.getItem();
            MeteorRecipe recipe = MeteorRecipeHelper.findRecipe(ctx.level(), stack);
            if (recipe == null) {
                continue;
            }

            if (!callMeteor(ctx, recipe, stack)) {
                entityItem.setUnlimitedLifetime();
                return;
            }

            StreamPresets
                    .demonTether(entityItem, ctx.masterPos()).build()
                    .sendToNearby(ctx.serverLevel(), ctx.masterPos(), 128);

            ItemStack reduced = stack.copy();
            reduced.shrink(1);
            if (reduced.isEmpty()) {
                entityItem.remove(RemovalReason.KILLED);
            } else {
                entityItem.setItem(reduced);
            }
            return;
        }
    }

    protected boolean spawnsOresOnly() {
        return false;
    }

    private boolean callMeteor(RitualContext ctx, MeteorRecipe recipe, ItemStack catalyst) {
        if (pendingMeteor != null) {
            Entity falling = ctx.serverLevel().getEntity(pendingMeteor);
            if (falling != null && falling.isAlive()) {
                return false;
            }
            pendingMeteor = null;
        }

        int syphonAmount = recipe.getSyphon();
        if (ctx.currentEV() < syphonAmount) {
            return false;
        }

        int targetY = ctx.masterPos().getY() + 1 + recipe.getMaxRadius();
        long now = ctx.level().getGameTime();
        if (now < nextLandingCheck) {
            return false;
        }
        BlockPos landing = new BlockPos(ctx.masterPos().getX(), targetY, ctx.masterPos().getZ());
        if (!recipe.isLandingAreaClear(ctx.level(), landing, MAX_LANDING_OCCUPANCY)) {
            nextLandingCheck = now + LANDING_RECHECK_TICKS;
            return false;
        }

        if (syphonAmount > 0) {
            ctx.syphon(syphonAmount);
        }

        double spawnY = ctx.level().getMaxBuildHeight() + 10;
        if (SableCompat.isOnContraption(ctx.level(), Vec3.atCenterOf(ctx.masterPos()))) {
            spawnY = targetY + CONTRAPTION_FALL_HEIGHT;
        }

        EntityMeteor meteor = new EntityMeteor(ctx.level(),
                ctx.masterPos().getX() + 0.5,
                spawnY,
                ctx.masterPos().getZ() + 0.5);
        meteor.setDeltaMovement(0, -0.1, 0);
        meteor.setContainedStack(catalyst.copyWithCount(1));
        meteor.setTargetY(targetY);
        meteor.setOresOnly(spawnsOresOnly());
        ctx.level().addFreshEntity(meteor);
        pendingMeteor = meteor.getUUID();
        return true;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        pendingMeteor = tag.hasUUID("pendingMeteor") ? tag.getUUID("pendingMeteor") : null;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (pendingMeteor != null) {
            tag.putUUID("pendingMeteor", pendingMeteor);
        }
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Large complex ritual pattern
        addCornerRunes(components, 1, 0, EnumRuneType.FIRE);
        addParallelRunes(components, 2, 0, EnumRuneType.FIRE);
        addCornerRunes(components, 2, 0, EnumRuneType.TENEBRAE);
        addParallelRunes(components, 3, 0, EnumRuneType.EARTH);
        addCornerRunes(components, 3, 0, EnumRuneType.TENEBRAE);
        addParallelRunes(components, 4, 0, EnumRuneType.FIRE);
        addCornerRunes(components, 4, 0, EnumRuneType.EARTH);

        // Elevated corners
        addCornerRunes(components, 4, 1, EnumRuneType.TENEBRAE);
        addCornerRunes(components, 4, 2, EnumRuneType.TENEBRAE);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualMeteor();
    }
}
