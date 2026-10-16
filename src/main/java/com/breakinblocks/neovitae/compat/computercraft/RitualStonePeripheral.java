package com.breakinblocks.neovitae.compat.computercraft;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.common.datacomponent.Anima;
import com.breakinblocks.neovitae.util.helper.AnimaHelper;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class RitualStonePeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return NeoVitae.MODID + ":master_ritual_stone";
    }

    @Override
    public PeripheralType getType() {
        return PeripheralType.ofAdditional("master_ritual_stone");
    }

    @LuaFunction(mainThread = true)
    public boolean isActive(MasterRitualStoneBlockEntity stone) {
        return stone.isActive();
    }

    @LuaFunction(mainThread = true)
    public boolean isSuspended(MasterRitualStoneBlockEntity stone) {
        return stone.isSuspended();
    }

    @LuaFunction(mainThread = true)
    @Nullable
    public String getRitual(MasterRitualStoneBlockEntity stone) {
        ResourceLocation id = stone.getCurrentRitualId();
        return stone.isActive() && id != null ? id.toString() : null;
    }

    @LuaFunction(mainThread = true)
    public long getRunningTime(MasterRitualStoneBlockEntity stone) {
        return stone.isActive() ? stone.getRunningTime() : 0;
    }

    @LuaFunction(mainThread = true)
    public int getCooldown(MasterRitualStoneBlockEntity stone) {
        return stone.getCooldown();
    }

    @LuaFunction(mainThread = true)
    public int getOwnerAnima(MasterRitualStoneBlockEntity stone) {
        if (stone.getOwner() == null) {
            return 0;
        }
        Anima anima = AnimaHelper.getAnima(stone.getOwner());
        return anima == null ? 0 : anima.getCurrentEV();
    }
}
