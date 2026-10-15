package com.breakinblocks.neovitae.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class RitualAcceleration {

    private int factor = 1;
    private long until = Long.MIN_VALUE;
    private long lastWorked = Long.MIN_VALUE;
    @Nullable
    private BlockPos source;

    public boolean apply(Level level, BlockPos source, int factor, long until) {
        boolean active = level.getGameTime() < this.until;
        if (active && !source.equals(this.source) && this.factor >= factor) {
            return false;
        }
        this.factor = factor;
        this.until = until;
        this.source = source.immutable();
        return true;
    }

    public int factor(@Nullable Level level) {
        return level != null && level.getGameTime() < until ? factor : 1;
    }

    public void markWorked(Level level) {
        lastWorked = level.getGameTime();
    }

    public boolean workedSince(long gameTime) {
        return lastWorked >= gameTime;
    }
}
