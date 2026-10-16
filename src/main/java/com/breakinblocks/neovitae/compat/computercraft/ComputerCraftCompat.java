package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import net.neoforged.fml.ModList;

public final class ComputerCraftCompat {

    private ComputerCraftCompat() {
    }

    public static void init() {
        if (!ModList.get().isLoaded("computercraft")) {
            return;
        }
        try {
            ComputerCraftHooks.register();
            NeoVitae.LOGGER.info("CC: Tweaked found, Neo Vitae blocks can be read by computers and turtles");
        } catch (Throwable t) {
            NeoVitae.LOGGER.warn("CC: Tweaked integration failed to register", t);
        }
    }
}
