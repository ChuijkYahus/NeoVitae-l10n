package com.breakinblocks.neovitae.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class SanguineCasingBlock extends Block {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final IntegerProperty PX = IntegerProperty.create("px", 0, 2);
    public static final IntegerProperty PY = IntegerProperty.create("py", 0, 1);
    public static final IntegerProperty PZ = IntegerProperty.create("pz", 0, 2);

    public SanguineCasingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FORMED, false).setValue(PX, 0).setValue(PY, 0).setValue(PZ, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED, PX, PY, PZ);
    }

    public static BlockState formedAt(BlockState state, int px, int py, int pz) {
        return state.setValue(FORMED, true).setValue(PX, px).setValue(PY, py).setValue(PZ, pz);
    }

    public static BlockState unformed(BlockState state) {
        return state.setValue(FORMED, false).setValue(PX, 0).setValue(PY, 0).setValue(PZ, 0);
    }
}
