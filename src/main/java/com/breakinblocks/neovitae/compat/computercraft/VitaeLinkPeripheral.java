package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.blockentity.VitaeLinkBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;

public final class VitaeLinkPeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return NeoVitae.MODID + ":vitae_link";
    }

    @Override
    public PeripheralType getType() {
        return PeripheralType.ofAdditional("vitae_link");
    }

    @LuaFunction(mainThread = true)
    public int getTier(VitaeLinkBlockEntity link) {
        return link.getCraftTier();
    }

    @LuaFunction(mainThread = true)
    public int getMaxTier(VitaeLinkBlockEntity link) {
        return Math.max(0, link.getMaxLinkTier());
    }

    @LuaFunction(mainThread = true)
    public boolean isLinked(VitaeLinkBlockEntity link) {
        return link.isLinked();
    }

    @LuaFunction(mainThread = true)
    public boolean isCrafting(VitaeLinkBlockEntity link) {
        return link.isClientCrafting();
    }

    @LuaFunction(mainThread = true)
    public int getEssenceProgress(VitaeLinkBlockEntity link) {
        return link.getProgress();
    }

    @LuaFunction(mainThread = true)
    public int getEssenceRequired(VitaeLinkBlockEntity link) {
        return link.getEssenceRequired();
    }
}
