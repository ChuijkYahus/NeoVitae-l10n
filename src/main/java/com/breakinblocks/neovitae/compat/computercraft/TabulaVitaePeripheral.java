package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.blockentity.TabulaVitaeBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public final class TabulaVitaePeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return NeoVitae.MODID + ":tabula_vitae";
    }

    @Override
    public PeripheralType getType() {
        return PeripheralType.ofAdditional("tabula_vitae");
    }

    @LuaFunction(mainThread = true)
    public double getProgress(TabulaVitaeBlockEntity table) {
        TabulaVitaeBlockEntity master = table.getMaster();
        return master == null ? 0 : Math.min(1.0, master.getProgressForGui());
    }

    @LuaFunction(mainThread = true)
    @Nullable
    public String getIdleReason(TabulaVitaeBlockEntity table) {
        TabulaVitaeBlockEntity master = table.getMaster();
        if (master == null || master.getIdleReason() == TabulaVitaeBlockEntity.IdleReason.NONE) {
            return null;
        }
        return master.getIdleReason().name().toLowerCase(Locale.ROOT);
    }
}
