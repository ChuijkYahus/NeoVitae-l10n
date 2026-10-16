package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.blockentity.HellfireForgeBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;

public final class HellfireForgePeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return NeoVitae.MODID + ":hellfire_forge";
    }

    @Override
    public PeripheralType getType() {
        return PeripheralType.ofAdditional("hellfire_forge");
    }

    @LuaFunction(mainThread = true)
    public double getProgress(HellfireForgeBlockEntity forge) {
        return Math.min(1.0, forge.getProgressForGui());
    }
}
