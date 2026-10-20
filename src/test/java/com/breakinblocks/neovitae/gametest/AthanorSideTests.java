package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import com.breakinblocks.neovitae.common.block.AthanorBlock;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.AthanorBlockEntity;

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

    private static int extract(IItemHandler handler) {
        int taken = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            taken += handler.extractItem(slot, 64, false).getCount();
        }
        return taken;
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void defaultSidesKeepFurnaceLayout(GameTestHelper helper) {
        AthanorBlockEntity athanor = place(helper);
        athanor.athanorInv.setStackInSlot(AthanorBlockEntity.OUTPUT_SLOT, new ItemStack(Items.IRON_INGOT, 4));

        ItemStack topLeftover = ItemHandlerHelper.insertItemStacked(items(helper, Direction.UP), new ItemStack(Items.RAW_IRON, 3), false);
        helper.assertTrue(topLeftover.getCount() == 3, "The top should not accept inputs");
        ItemStack sideLeftover = ItemHandlerHelper.insertItemStacked(items(helper, Direction.EAST), new ItemStack(Items.RAW_IRON, 3), false);
        helper.assertTrue(sideLeftover.isEmpty(), "The sides should accept inputs, left " + sideLeftover);
        ItemStack bottomLeftover = ItemHandlerHelper.insertItemStacked(items(helper, Direction.DOWN), new ItemStack(Items.RAW_IRON, 3), false);
        helper.assertTrue(bottomLeftover.getCount() == 3, "The bottom should not accept inputs");

        int pulled = extract(items(helper, Direction.DOWN));
        helper.assertTrue(pulled == 4, "The bottom should give only the 4 outputs, gave " + pulled);
        helper.assertTrue(athanor.athanorInv.getStackInSlot(AthanorBlockEntity.INPUT_START).getCount() == 3, "Inputs must stay in place");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void toggledSidesFollowTheSlotGroups(GameTestHelper helper) {
        AthanorBlockEntity athanor = place(helper);
        athanor.getSideConfig().setAllowed(AthanorBlockEntity.GROUP_INPUT, Direction.DOWN, true);
        athanor.getSideConfig().setAllowed(AthanorBlockEntity.GROUP_INPUT, Direction.NORTH, false);
        athanor.athanorInv.setStackInSlot(AthanorBlockEntity.OUTPUT_SLOT, new ItemStack(Items.IRON_INGOT, 2));

        ItemStack northLeftover = ItemHandlerHelper.insertItemStacked(items(helper, Direction.NORTH), new ItemStack(Items.RAW_IRON, 3), false);
        helper.assertTrue(northLeftover.getCount() == 3, "A side closed to the inputs should refuse them");
        ItemStack bottomLeftover = ItemHandlerHelper.insertItemStacked(items(helper, Direction.DOWN), new ItemStack(Items.RAW_IRON, 3), false);
        helper.assertTrue(bottomLeftover.isEmpty(), "The bottom opened to the inputs should accept them, left " + bottomLeftover);
        helper.assertTrue(athanor.athanorInv.getStackInSlot(AthanorBlockEntity.OUTPUT_SLOT).getCount() == 2, "Inputs must not land in the outputs");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 60)
    public void outputsArePushedOutOfTheChosenSide(GameTestHelper helper) {
        AthanorBlockEntity athanor = place(helper);
        helper.setBlock(POS.east(), Blocks.CHEST);
        athanor.setOutputFace(Direction.EAST);
        athanor.athanorInv.setStackInSlot(AthanorBlockEntity.OUTPUT_SLOT + 1, new ItemStack(Items.IRON_INGOT, 5));
        athanor.athanorInv.setStackInSlot(AthanorBlockEntity.INPUT_START, new ItemStack(Items.RAW_IRON, 2));

        helper.succeedWhen(() -> {
            IItemHandler chest = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(POS.east()), Direction.WEST);
            int ingots = 0;
            int raw = 0;
            for (int slot = 0; slot < chest.getSlots(); slot++) {
                ItemStack stack = chest.getStackInSlot(slot);
                if (stack.is(Items.IRON_INGOT)) ingots += stack.getCount();
                if (stack.is(Items.RAW_IRON)) raw += stack.getCount();
            }
            helper.assertTrue(ingots == 5, "Expected 5 pushed ingots, found " + ingots);
            helper.assertTrue(raw == 0, "Inputs must not be pushed");
        });
    }
}
