package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.blockentity.AthanorBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public final class AthanorPeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return NeoVitae.MODID + ":athanor";
    }

    @Override
    public PeripheralType getType() {
        return PeripheralType.ofAdditional("athanor");
    }

    @LuaFunction(mainThread = true)
    public double getProgress(AthanorBlockEntity athanor) {
        return Math.min(1.0, athanor.getProgress());
    }

    @LuaFunction(mainThread = true)
    @Nullable
    public String getIdleReason(AthanorBlockEntity athanor) {
        AthanorBlockEntity.IdleReason reason = athanor.getIdleReason();
        return reason == AthanorBlockEntity.IdleReason.NONE ? null : reason.name().toLowerCase(Locale.ROOT);
    }
}
