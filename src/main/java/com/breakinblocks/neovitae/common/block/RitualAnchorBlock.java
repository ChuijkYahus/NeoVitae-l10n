package com.breakinblocks.neovitae.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.breakinblocks.neovitae.client.particle.ColoredParticleOptions;
import com.breakinblocks.neovitae.common.blockentity.NVTiles;
import com.breakinblocks.neovitae.common.blockentity.RitualAnchorBlockEntity;
import com.breakinblocks.neovitae.common.item.ItemRitualReader;
import com.breakinblocks.neovitae.common.particle.NVParticles;
import com.breakinblocks.neovitae.util.helper.BlockEntityHelper;

import javax.annotation.Nullable;

public class RitualAnchorBlock extends BaseEntityBlock {

    public static final MapCodec<RitualAnchorBlock> CODEC = simpleCodec(RitualAnchorBlock::new);
    public static final EnumProperty<AnchorAura> AURA = EnumProperty.create("aura", AnchorAura.class);

    private static final VoxelShape SHAPE = Shapes.or(
            box(1, 0, 1, 15, 2, 15),
            box(3, 2, 3, 13, 4, 13),
            box(1, 2, 1, 3, 5, 3),
            box(13, 2, 1, 15, 5, 3),
            box(1, 2, 13, 3, 5, 15),
            box(13, 2, 13, 15, 5, 15),
            box(6, 6, 6, 10, 16, 10));

    public RitualAnchorBlock(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.STONE)
                .strength(2.0F, 1200.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .noOcclusion()
                .lightLevel(state -> state.getValue(AURA) == AnchorAura.DORMANT ? 0 : 9));
        registerDefaultState(stateDefinition.any().setValue(AURA, AnchorAura.DORMANT));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AURA);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualAnchorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return BlockEntityHelper.getTicker(type, NVTiles.RITUAL_ANCHOR_TYPE.get(), RitualAnchorBlockEntity::tick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ItemRitualReader) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof RitualAnchorBlockEntity anchor)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.translatable("chat.neovitae.ritual_anchor.links", anchor.getLinks().size()), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof RitualAnchorBlockEntity anchor) {
            anchor.onBlockRemoved();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        AnchorAura aura = state.getValue(AURA);
        if (aura == AnchorAura.DORMANT || random.nextInt(3) != 0) return;
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        double y = pos.getY() + 0.45 + random.nextDouble() * 0.5;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        level.addParticle(new ColoredParticleOptions(NVParticles.RUNE_GLOW.get(), RitualAnchorBlockEntity.auraColor(aura)),
                x, y, z, 0, 0.01, 0);
    }
}
