package com.breakinblocks.neovitae.compat.arsnouveau;

import com.hollingsworth.arsnouveau.common.block.tile.MobJarTile;
import com.hollingsworth.arsnouveau.common.block.tile.StorageLecternTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

final class ArsNouveauHooks {

    private ArsNouveauHooks() {
    }

    static boolean isStorageLectern(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof StorageLecternTile;
    }

    static boolean openStorageLectern(ServerPlayer player, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof StorageLecternTile lectern && lectern.openMenu(player);
    }

    static EntityType<?> getJarEntityType(BlockEntity be) {
        if (!(be instanceof MobJarTile jar)) {
            return null;
        }
        Entity entity = jar.getEntity();
        if (!(entity instanceof LivingEntity) || entity instanceof Player) {
            return null;
        }
        return entity.getType();
    }
}
