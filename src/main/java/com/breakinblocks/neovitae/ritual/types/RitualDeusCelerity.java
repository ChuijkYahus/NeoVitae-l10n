package com.breakinblocks.neovitae.ritual.types;

import net.minecraft.core.BlockPos;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.breakinblocks.neovitae.ritual.RitualComponent;

import java.util.function.Consumer;

public class RitualDeusCelerity extends RitualCelerity {

    public static final String NAME = "deus_celerity";
    public static final int SPEED = 10;

    public RitualDeusCelerity() {
        super(NAME, 2, 250000, SPEED);
        addBlockRange(ACCELERATE_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-13, -2, -13), 27, 11, 27));
        setMaximumVolumeAndDistanceOfRange(ACCELERATE_RANGE, 10500, 31, 16);
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addDeusCrown(components, super::gatherComponents);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualDeusCelerity();
    }
}
