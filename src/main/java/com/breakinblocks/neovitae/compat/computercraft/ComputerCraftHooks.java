package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.api.altar.IAraVitae;
import com.breakinblocks.neovitae.api.capability.NVCapabilities;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.detail.BlockReference;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import java.util.HashMap;
import java.util.Map;

final class ComputerCraftHooks {

    private ComputerCraftHooks() {
    }

    static void register() {
        ComputerCraftAPI.registerGenericSource(new AraVitaePeripheral());
        ComputerCraftAPI.registerGenericSource(new VitaeLinkPeripheral());
        ComputerCraftAPI.registerGenericSource(new OrbVitaeLinkPeripheral());
        ComputerCraftAPI.registerGenericSource(new RitualStonePeripheral());
        ComputerCraftAPI.registerGenericSource(new SpiritusStoragePeripheral());
        ComputerCraftAPI.registerGenericSource(new AthanorPeripheral());
        ComputerCraftAPI.registerGenericSource(new TabulaVitaePeripheral());
        ComputerCraftAPI.registerGenericSource(new HellfireForgePeripheral());
        ComputerCraftAPI.registerGenericSource(new SanguineCrafterPeripheral());
        VanillaDetailRegistries.BLOCK_IN_WORLD.addProvider(ComputerCraftHooks::provideAltarDetails);
    }

    private static void provideAltarDetails(Map<? super String, Object> details, BlockReference block) {
        IAraVitae altar = block.level().getCapability(NVCapabilities.ARA_VITAE, block.pos(), block.state(), block.blockEntity(), null);
        if (altar == null) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("tier", altar.getTier());
        data.put("essence", altar.getCurrentBlood());
        data.put("capacity", altar.getCapacity());
        data.put("progress", altar.getProgressFloat());
        data.put("essenceRequired", altar.getLiquidRequired());
        details.put("araVitae", data);
    }
}
