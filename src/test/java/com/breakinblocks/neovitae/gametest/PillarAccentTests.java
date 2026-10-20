package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.breakinblocks.neovitae.common.block.BlockInversionPillarEnd;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.block.PillarAccent;
import com.breakinblocks.neovitae.common.block.type.PillarCapType;

import java.util.List;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class PillarAccentTests {

    private static void buildColumn(GameTestHelper helper) {
        helper.setBlock(new BlockPos(2, 1, 2), NVBlocks.INVERSION_PILLAR_CAP.block().get().defaultBlockState()
                .setValue(BlockInversionPillarEnd.TYPE, PillarCapType.BOTTOM));
        helper.setBlock(new BlockPos(2, 2, 2), NVBlocks.INVERSION_PILLAR.block().get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 3, 2), NVBlocks.INVERSION_PILLAR.block().get().defaultBlockState());
        helper.setBlock(new BlockPos(2, 4, 2), NVBlocks.INVERSION_PILLAR_CAP.block().get().defaultBlockState()
                .setValue(BlockInversionPillarEnd.TYPE, PillarCapType.TOP));
    }

    private static PillarAccent accentAt(GameTestHelper helper, int y) {
        return helper.getBlockState(new BlockPos(2, y, 2)).getValue(PillarAccent.PROPERTY);
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void dyeColorsWholeColumn(GameTestHelper helper) {
        buildColumn(helper);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack dye = new ItemStack(Items.RED_DYE, 2);

        InteractionResult result = PillarAccent.dyeColumn(dye, helper.getLevel(), helper.absolutePos(new BlockPos(2, 3, 2)), player);
        helper.assertTrue(result != null && result.consumesAction(), "Dyeing a pillar should succeed, got " + result);
        for (int y = 1; y <= 4; y++) {
            helper.assertTrue(accentAt(helper, y) == PillarAccent.RED, "Column block at y=" + y + " should be red, is " + accentAt(helper, y));
        }
        helper.assertTrue(dye.getCount() == 1, "Dyeing a whole column should use one dye, left " + dye.getCount());

        InteractionResult again = PillarAccent.dyeColumn(dye, helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2)), player);
        helper.assertTrue(again == InteractionResult.PASS && dye.getCount() == 1, "Re-dyeing the same color should do nothing");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void dyedDropKeepsAccent(GameTestHelper helper) {
        buildColumn(helper);
        BlockPos abs = helper.absolutePos(new BlockPos(2, 2, 2));
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);

        List<ItemStack> plain = Block.getDrops(helper.getLevel().getBlockState(abs), helper.getLevel(), abs, null, null, pickaxe);
        helper.assertTrue(plain.size() == 1 && !plain.get(0).has(DataComponents.BLOCK_STATE),
                "An undyed pillar should drop a plain item that stacks with new ones");

        BlockState dyed = helper.getLevel().getBlockState(abs).setValue(PillarAccent.PROPERTY, PillarAccent.LIME);
        helper.getLevel().setBlockAndUpdate(abs, dyed);
        List<ItemStack> drops = Block.getDrops(dyed, helper.getLevel(), abs, helper.getLevel().getBlockEntity(abs), null, pickaxe);
        PillarAccent carried = drops.isEmpty() ? null
                : drops.get(0).getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(PillarAccent.PROPERTY);
        helper.assertTrue(carried == PillarAccent.LIME, "A dyed pillar should drop with its accent, got " + carried);
        helper.succeed();
    }
}
