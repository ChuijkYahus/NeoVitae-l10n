package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.api.altar.IAraVitae;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;

public final class AraVitaePeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return NeoVitae.MODID + ":ara_vitae";
    }

    @Override
    public PeripheralType getType() {
        return PeripheralType.ofAdditional("ara_vitae");
    }

    @LuaFunction(mainThread = true)
    public int getEssence(IAraVitae altar) {
        return altar.getCurrentBlood();
    }

    @LuaFunction(mainThread = true)
    public int getCapacity(IAraVitae altar) {
        return altar.getCapacity();
    }

    @LuaFunction(mainThread = true)
    public int getTier(IAraVitae altar) {
        return altar.getTier();
    }

    @LuaFunction(mainThread = true)
    public double getProgress(IAraVitae altar) {
        return altar.getProgressFloat();
    }

    @LuaFunction(mainThread = true)
    public int getEssenceRequired(IAraVitae altar) {
        return altar.getLiquidRequired();
    }

    @LuaFunction(mainThread = true)
    public int getConsumptionRate(IAraVitae altar) {
        return altar.getConsumptionRate();
    }
}
