package com.breakinblocks.neovitae.common.block.dungeon;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class BlockPillarCap extends Block {

    public static final MapCodec<BlockPillarCap> CODEC = simpleCodec(BlockPillarCap::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockPillarCap(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockState support = context.getLevel().getBlockState(context.getClickedPos().relative(face.getOpposite()));
        Direction facing = continuesPillar(support, face.getAxis()) ? face : face.getOpposite();
        if (context.isSecondaryUseActive()) {
            facing = facing.getOpposite();
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    private static boolean continuesPillar(BlockState support, Direction.Axis axis) {
        return support.hasProperty(BlockStateProperties.AXIS) && support.getValue(BlockStateProperties.AXIS) == axis;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
