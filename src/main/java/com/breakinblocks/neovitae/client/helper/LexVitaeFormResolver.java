package com.breakinblocks.neovitae.client.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.client.event.LexVitaeBeamHandler;
import com.breakinblocks.neovitae.common.item.soul.LexVitaeForm;
import com.breakinblocks.neovitae.common.item.soul.LexVitaeItem;
import org.jetbrains.annotations.Nullable;

public final class LexVitaeFormResolver {

    private static final double DEFAULT_REACH = 4.5;

    private LexVitaeFormResolver() {}

    public static float resolve(ItemStack stack, @Nullable LivingEntity holder) {
        if (holder == null || !LexVitaeItem.isActive(stack)) return LexVitaeForm.BASE.ordinal();
        if (holder.getMainHandItem() != stack && holder.getOffhandItem() != stack) return LexVitaeForm.BASE.ordinal();
        return LexVitaeForm.forTarget(stack, holder.level(), target(holder, stack)).ordinal();
    }

    @Nullable
    private static HitResult target(LivingEntity holder, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (holder == mc.player) {
            return LexVitaeBeamHandler.isFiring() ? beamTarget(mc.player, stack) : mc.hitResult;
        }
        double reach = holder instanceof Player player ? player.blockInteractionRange() : DEFAULT_REACH;
        return holder.pick(reach, 1.0F, false);
    }

    private static HitResult beamTarget(Player player, ItemStack stack) {
        Vec3 origin = player.getEyePosition();
        Vec3 reach = origin.add(player.getLookAngle().scale(LexVitaeItem.BEAM_RANGE));
        if (LexVitaeItem.canAttack(stack)) {
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player.level(), player, origin, reach,
                    new AABB(origin, reach).inflate(2.0),
                    e -> !e.isSpectator() && e.isPickable() && e != player && e.isAlive());
            if (entityHit != null && entityHit.getEntity() instanceof LivingEntity) return entityHit;
        }
        return player.level().clip(new ClipContext(origin, reach, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }
}
