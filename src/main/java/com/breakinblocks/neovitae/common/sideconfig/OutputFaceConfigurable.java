package com.breakinblocks.neovitae.common.sideconfig;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface OutputFaceConfigurable {
    @Nullable
    Direction getOutputFace();

    void setOutputFace(@Nullable Direction face);
}
