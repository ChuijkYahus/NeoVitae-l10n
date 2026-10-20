package com.breakinblocks.neovitae.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.item.soul.LexVitaeItem;

@EventBusSubscriber(modid = NeoVitae.MODID)
public final class LexVitaeAoeHandler {

    private static final ThreadLocal<Boolean> AOE_BREAKING = ThreadLocal.withInitial(() -> false);

    public static boolean isAoeBreaking() {
        return AOE_BREAKING.get();
    }

    private LexVitaeAoeHandler() {}

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (AOE_BREAKING.get()) return;
        Player player = event.getPlayer();
        if (player == null) return;
        if (!player.canInteractWithBlock(event.getPos(), 1.0)) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof LexVitaeItem)) return;
        if (!LexVitaeItem.isActive(stack)) return;
        if (LexVitaeItem.isBeaming(player)) return;

        if (LexVitaeItem.attackedRecently(player)) {
            event.setCanceled(true);
            return;
        }

        Level level = (Level) event.getLevel();
        BlockPos center = event.getPos();
        if (event.getState().getDestroySpeed(level, center) <= 0) return;

        if (LexVitaeItem.canFell(player, event.getState())) {
            AOE_BREAKING.set(true);
            try {
                if (LexVitaeItem.fellTree(level, player, stack, center)) return;
            } finally {
                AOE_BREAKING.set(false);
            }
        }

        int radius = stack.getOrDefault(NVDataComponents.LEX_RADIUS.get(), 0);
        if (radius == 0) return;

        Direction.Axis depth = targetedFaceAxis(player, level, center);
        if (depth == null) {
            depth = dominantAxis(player.getLookAngle());
        }

        AOE_BREAKING.set(true);
        try {
            LexVitaeItem.breakAoe(level, player, stack, center, depth);
        } finally {
            AOE_BREAKING.set(false);
        }
    }

    private static Direction.Axis targetedFaceAxis(Player player, Level level, BlockPos center) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(player.blockInteractionRange() + 1.0));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(center)) {
            return hit.getDirection().getAxis();
        }
        return null;
    }

    private static Direction.Axis dominantAxis(Vec3 look) {
        double ax = Math.abs(look.x);
        double ay = Math.abs(look.y);
        double az = Math.abs(look.z);
        if (ay >= ax && ay >= az) return Direction.Axis.Y;
        if (ax >= az) return Direction.Axis.X;
        return Direction.Axis.Z;
    }
}
