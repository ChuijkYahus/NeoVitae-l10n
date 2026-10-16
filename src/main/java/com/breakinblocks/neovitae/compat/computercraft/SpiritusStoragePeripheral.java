package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.api.spiritus.ISpiritusStorage;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;
import org.jetbrains.annotations.Nullable;

public final class SpiritusStoragePeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return NeoVitae.MODID + ":spiritus_storage";
    }

    @Override
    public PeripheralType getType() {
        return PeripheralType.ofAdditional("spiritus_storage");
    }

    @LuaFunction(mainThread = true)
    public double getSpiritus(ISpiritusStorage storage) {
        return storage.getStored();
    }

    @LuaFunction(mainThread = true)
    public double getSpiritusCapacity(ISpiritusStorage storage) {
        return storage.getCapacity();
    }

    @LuaFunction(mainThread = true)
    @Nullable
    public String getSpiritusType(ISpiritusStorage storage) {
        SpiritusType type = storage.getStoredType();
        return type == null ? null : type.getSerializedName();
    }
}
