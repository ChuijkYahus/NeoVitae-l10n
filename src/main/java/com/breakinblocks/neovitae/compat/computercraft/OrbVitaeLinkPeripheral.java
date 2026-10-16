package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.blockentity.OrbFillingLinkBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;

public final class OrbVitaeLinkPeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return NeoVitae.MODID + ":orb_vitae_link";
    }

    @Override
    public PeripheralType getType() {
        return PeripheralType.ofAdditional("orb_vitae_link");
    }

    @LuaFunction(mainThread = true)
    public int getAnima(OrbFillingLinkBlockEntity link) {
        return link.getAnimaEV();
    }

    @LuaFunction(mainThread = true)
    public int getAnimaCapacity(OrbFillingLinkBlockEntity link) {
        return link.getAnimaCapacity();
    }

    @LuaFunction(mainThread = true)
    public boolean hasOrb(OrbFillingLinkBlockEntity link) {
        return link.hasOrb();
    }

    @LuaFunction(mainThread = true)
    public boolean isLinked(OrbFillingLinkBlockEntity link) {
        return link.isLinked();
    }

    @LuaFunction(mainThread = true)
    public boolean isPaused(OrbFillingLinkBlockEntity link) {
        return link.isPowered();
    }
}
