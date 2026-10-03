package com.breakinblocks.neovitae.compat.arsnouveau;

import com.breakinblocks.neovitae.NeoVitae;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public final class ArsNouveauCompat {

    private static boolean available;

    private ArsNouveauCompat() {
    }

    public static void init() {
        available = ModList.get().isLoaded("ars_nouveau");
        if (available) {
            NeoVitae.LOGGER.info("Ars Nouveau found, enabling storage lectern and containment jar support");
        }
    }

    public static boolean isStorageLectern(Level level, BlockPos pos) {
        if (!available) {
            return false;
        }
        try {
            return ArsNouveauHooks.isStorageLectern(level, pos);
        } catch (Throwable t) {
            disable(t);
            return false;
        }
    }

    public static boolean openStorageLectern(ServerPlayer player, Level level, BlockPos pos) {
        if (!available) {
            return false;
        }
        try {
            return ArsNouveauHooks.openStorageLectern(player, level, pos);
        } catch (Throwable t) {
            disable(t);
            return false;
        }
    }

    @Nullable
    public static EntityType<?> getJarEntityType(@Nullable BlockEntity be) {
        if (!available || be == null) {
            return null;
        }
        try {
            return ArsNouveauHooks.getJarEntityType(be);
        } catch (Throwable t) {
            disable(t);
            return null;
        }
    }

    private static void disable(Throwable t) {
        available = false;
        NeoVitae.LOGGER.warn("Ars Nouveau lookup failed, skipping its blocks from now on", t);
    }
}
