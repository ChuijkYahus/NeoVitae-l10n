package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import com.breakinblocks.neovitae.common.block.AthanorBlock;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.AthanorBlockEntity;
import com.breakinblocks.neovitae.common.blockentity.AthanorSides;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class AthanorSideTests {

    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private static AthanorBlockEntity place(GameTestHelper helper) {
        helper.setBlock(POS, NVBlocks.ATHANOR_BLOCK.block().get().defaultBlockState().setValue(AthanorBlock.FACING, Direction.NORTH));
        return (AthanorBlockEntity) helper.getBlockEntity(POS);
    }

    private static IItemHandler items(GameTestHelper helper, Direction side) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(POS), side);
    }

    private static int extract(IItemHandler handler, int amount) {
        int taken = 0;
        for (int slot = 0; slot < handler.getSlots() && taken < amount; slot++) {
            taken += handler.extractItem(slot, amount - taken, false).getCount();
        }
        return taken;
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void defaultSidesKeepFurnaceLayout(GameTestHelper helper) {
        place(helper);
        helper.assertTrue(items(helper, Direction.UP).getSlots() == 1, "Top should expose only the tool slot");
        helper.assertTrue(items(helper, Direction.DOWN).getSlots() == AthanorBlockEntity.NUM_OUTPUTS, "Bottom should expose the outputs");
        helper.assertTrue(items(helper, Direction.EAST).getSlots() == AthanorBlockEntity.NUM_INPUTS + 2, "Sides should expose inputs and buckets");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void offSideExposesNothing(GameTestHelper helper) {
        AthanorBlockEntity athanor = place(helper);
        while (athanor.getSideMode(AthanorSides.Face.FRONT) != AthanorSides.Mode.OFF) {
            athanor.cycleSideMode(AthanorSides.Face.FRONT, 1);
        }
        helper.assertTrue(items(helper, Direction.NORTH) == null, "An Off front should expose no inventory");
        helper.assertTrue(items(helper, Direction.SOUTH) != null, "The back should be unaffected");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void bothSideInsertsInputsAndExtractsOutputs(GameTestHelper helper) {
        AthanorBlockEntity athanor = place(helper);
        while (athanor.getSideMode(AthanorSides.Face.BOTTOM) != AthanorSides.Mode.BOTH) {
            athanor.cycleSideMode(AthanorSides.Face.BOTTOM, 1);
        }
        IItemHandler handler = items(helper, Direction.DOWN);
        athanor.athanorInv.setStackInSlot(AthanorBlockEntity.OUTPUT_SLOT, new ItemStack(Items.IRON_INGOT, 4));

        ItemStack leftover = ItemHandlerHelper.insertItemStacked(handler, new ItemStack(Items.RAW_IRON, 3), false);
        helper.assertTrue(leftover.isEmpty(), "Inserting through Inputs & Outputs should fill the inputs, left " + leftover);
        helper.assertTrue(athanor.athanorInv.getStackInSlot(AthanorBlockEntity.INPUT_START).is(Items.RAW_IRON), "Raw iron should land in an input slot");
        int pulled = extract(handler, 64);
        helper.assertTrue(pulled == 4, "Only the 4 outputs should be extractable, got " + pulled);
        helper.assertTrue(athanor.athanorInv.getStackInSlot(AthanorBlockEntity.INPUT_START).getCount() == 3, "Inputs must not be pulled back out");
        helper.succeed();
    }
}
