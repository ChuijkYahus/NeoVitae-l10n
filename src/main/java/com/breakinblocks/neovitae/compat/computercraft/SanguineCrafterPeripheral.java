package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;
import java.util.Locale;

public final class SanguineCrafterPeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return NeoVitae.MODID + ":sanguine_crafter";
    }

    @Override
    public PeripheralType getType() {
        return PeripheralType.ofAdditional("sanguine_crafter");
    }

    @LuaFunction(mainThread = true)
    public double getProgress(SanguineCrafterBlockEntity crafter) {
        int craftTime = crafter.getCraftTime();
        return craftTime <= 0 ? 0 : Math.min(1.0, (double) crafter.getProgress() / craftTime);
    }

    @LuaFunction(mainThread = true)
    public String getStatus(SanguineCrafterBlockEntity crafter) {
        return crafter.getStatus().name().toLowerCase(Locale.ROOT);
    }

    @LuaFunction(mainThread = true)
    public String getMode(SanguineCrafterBlockEntity crafter) {
        return crafter.getMode().name().toLowerCase(Locale.ROOT);
    }

    @LuaFunction(mainThread = true)
    public boolean isFormed(SanguineCrafterBlockEntity crafter) {
        return crafter.isFormed();
    }
}
