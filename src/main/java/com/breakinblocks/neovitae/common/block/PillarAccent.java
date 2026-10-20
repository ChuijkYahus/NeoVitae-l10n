package com.breakinblocks.neovitae.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum PillarAccent implements StringRepresentable {
    DEFAULT(null),
    WHITE(DyeColor.WHITE),
    ORANGE(DyeColor.ORANGE),
    MAGENTA(DyeColor.MAGENTA),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),
    YELLOW(DyeColor.YELLOW),
    LIME(DyeColor.LIME),
    PINK(DyeColor.PINK),
    GRAY(DyeColor.GRAY),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),
    CYAN(DyeColor.CYAN),
    PURPLE(DyeColor.PURPLE),
    BLUE(DyeColor.BLUE),
    BROWN(DyeColor.BROWN),
    GREEN(DyeColor.GREEN),
    RED(DyeColor.RED),
    BLACK(DyeColor.BLACK);

    public static final EnumProperty<PillarAccent> PROPERTY = EnumProperty.create("accent", PillarAccent.class);
    public static final int DEFAULT_COLOR = 0x90FEE9;
    private static final int MAX_COLUMN = 64;

    @Nullable
    private final DyeColor dye;

    PillarAccent(@Nullable DyeColor dye) {
        this.dye = dye;
    }

    @Override
    public String getSerializedName() {
        return dye == null ? "default" : dye.getSerializedName();
    }

    public int color() {
        return dye == null ? DEFAULT_COLOR : dye.getTextColor();
    }

    public static PillarAccent of(DyeColor dye) {
        for (PillarAccent accent : values()) {
            if (accent.dye == dye) return accent;
        }
        return DEFAULT;
    }

    public static boolean isPillarPart(BlockState state) {
        return state.getBlock() instanceof BlockInversionPillar || state.getBlock() instanceof BlockInversionPillarEnd;
    }

    public static List<BlockPos> column(Level level, BlockPos pos) {
        List<BlockPos> column = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (column.size() < MAX_COLUMN && isPillarPart(level.getBlockState(cursor))) {
            column.add(cursor.immutable());
            cursor.move(Direction.UP);
        }
        cursor.set(pos).move(Direction.DOWN);
        while (column.size() < MAX_COLUMN && isPillarPart(level.getBlockState(cursor))) {
            column.add(cursor.immutable());
            cursor.move(Direction.DOWN);
        }
        return column;
    }

    @Nullable
    public static InteractionResult dyeColumn(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!(stack.getItem() instanceof DyeItem dyeItem)) return null;
        DyeColor dye = dyeItem.getDyeColor();
        PillarAccent accent = of(dye);
        boolean changed = false;
        for (BlockPos part : column(level, pos)) {
            BlockState state = level.getBlockState(part);
            if (state.getValue(PROPERTY) == accent) continue;
            changed = true;
            if (!level.isClientSide()) {
                level.setBlock(part, state.setValue(PROPERTY, accent), Block.UPDATE_ALL);
            }
        }
        if (!changed) return InteractionResult.PASS;
        level.playSound(player, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!level.isClientSide()) {
            stack.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }
}
