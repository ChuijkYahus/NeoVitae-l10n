package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.block.SanguineCasingBlock;
import com.breakinblocks.neovitae.common.block.SanguineCrafterBlock;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity.ChunkLoading;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity.Mode;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity.Status;
import com.breakinblocks.neovitae.common.datacomponent.Anima;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.util.helper.AnimaHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class SanguineCrafterTests {


    private static final BlockPos CENTER = new BlockPos(4, 3, 4);
    private static final BlockPos CONTROLLER = CENTER.north();

    private record Rig(SanguineCrafterBlockEntity crafter, Player owner, Anima network) {}

    private static Rig build(GameTestHelper helper, Block... upgrades) {
        BlockPos center = helper.absolutePos(CENTER);
        BlockPos controller = helper.absolutePos(CONTROLLER);
        Direction facing = Direction.getNearest(controller.getX() - center.getX(), controller.getY() - center.getY(),
                controller.getZ() - center.getZ());
        int upgradeIndex = 0;
        for (int dy = -1; dy <= 0; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (pos.equals(center) || pos.equals(controller)) continue;
                    Block block = upgradeIndex < upgrades.length && dy == -1
                            ? upgrades[upgradeIndex++]
                            : NVBlocks.SANGUINE_CASING.block().get();
                    helper.getLevel().setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        BlockState state = NVBlocks.SANGUINE_CRAFTER.block().get().defaultBlockState().setValue(SanguineCrafterBlock.FACING, facing);
        helper.getLevel().setBlock(controller, state, Block.UPDATE_ALL);
        SanguineCrafterBlockEntity crafter = (SanguineCrafterBlockEntity) helper.getLevel().getBlockEntity(controller);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Anima network = AnimaHelper.getAnima(owner.getUUID());
        network.add(AnimaTicket.create(10000), 100000);
        crafter.setOwner(owner);
        return new Rig(crafter, owner, network);
    }

    private static List<ItemStack> template(ItemStack... cells) {
        List<ItemStack> list = new ArrayList<>(Collections.nCopies(SanguineCrafterBlockEntity.TEMPLATE_SIZE, ItemStack.EMPTY));
        for (int i = 0; i < cells.length; i++) list.set(i, cells[i]);
        return list;
    }

    private static int outputCount(SanguineCrafterBlockEntity crafter, Item item) {
        int count = 0;
        for (int i = 0; i < SanguineCrafterBlockEntity.OUTPUT_COUNT; i++) {
            ItemStack stack = crafter.inv.getStackInSlot(SanguineCrafterBlockEntity.OUTPUT_START + i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static List<ItemStack> sticksTemplate() {
        return template(new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS));
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 120)
    public void sanguineCrafterFormsAndUnforms(GameTestHelper helper) {
        Rig rig = build(helper);
        BlockPos floorCenter = helper.absolutePos(CENTER.below());
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(rig.crafter().isFormed(), "A complete structure should form");
            BlockState floor = helper.getLevel().getBlockState(floorCenter);
            helper.assertTrue(floor.getValue(SanguineCasingBlock.FORMED) && floor.getValue(SanguineCasingBlock.PX) == 1
                            && floor.getValue(SanguineCasingBlock.PY) == 0 && floor.getValue(SanguineCasingBlock.PZ) == 1,
                    "The floor center casing should be formed at 1,0,1, got " + floor);
            helper.getLevel().setBlock(helper.absolutePos(CENTER.east()), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            helper.runAfterDelay(25, () -> {
                helper.assertTrue(!rig.crafter().isFormed(), "Replacing a ring casing with stone should unform it");
                helper.assertTrue(rig.crafter().getStatus() == Status.NOT_FORMED, "Status should be NOT_FORMED, got " + rig.crafter().getStatus());
                helper.assertTrue(!helper.getLevel().getBlockState(floorCenter).getValue(SanguineCasingBlock.FORMED),
                        "Casings should drop their formed state when the structure breaks");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 140)
    public void sanguineCrafterActiveStateFollowsCrafting(GameTestHelper helper) {
        Rig rig = build(helper);
        rig.crafter().setTemplate(Mode.CRAFTING, sticksTemplate());
        rig.crafter().inv.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 2));
        helper.runAfterDelay(12, () -> {
            helper.assertTrue(rig.crafter().getBlockState().getValue(SanguineCrafterBlock.ACTIVE),
                    "The controller should be active while crafting, status " + rig.crafter().getStatus());
            helper.succeedWhen(() -> {
                helper.assertTrue(outputCount(rig.crafter(), Items.STICK) == 4, "Expected 4 sticks");
                helper.assertTrue(!rig.crafter().getBlockState().getValue(SanguineCrafterBlock.ACTIVE),
                        "The controller should go idle once the planks run out");
            });
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 80)
    public void sanguineCrafterCenterBlockCanBeAnything(GameTestHelper helper) {
        Rig rig = build(helper);
        helper.getLevel().setBlock(helper.absolutePos(CENTER), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(rig.crafter().isFormed(), "A block in the center should not stop the structure forming");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 120)
    public void sanguineCrafterCraftingTemplateCraftsAndChargesEv(GameTestHelper helper) {
        Rig rig = build(helper);
        rig.crafter().setTemplate(Mode.CRAFTING, sticksTemplate());
        rig.crafter().inv.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 2));
        helper.succeedWhen(() -> {
            helper.assertTrue(outputCount(rig.crafter(), Items.STICK) == 4, "Expected 4 sticks, status " + rig.crafter().getStatus());
            helper.assertTrue(rig.crafter().inv.getStackInSlot(0).isEmpty(), "Both planks should be used");
            helper.assertTrue(rig.network().getCurrentEV() == 9980, "One craft should cost 20 EV, network holds " + rig.network().getCurrentEV());
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 100)
    public void sanguineCrafterHellforgedCasingsCraftInParallel(GameTestHelper helper) {
        Rig rig = build(helper, NVBlocks.HELLFORGED_SANGUINE_CASING.block().get(), NVBlocks.HELLFORGED_SANGUINE_CASING.block().get());
        rig.crafter().setTemplate(Mode.CRAFTING, sticksTemplate());
        rig.crafter().inv.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 6));
        helper.runAfterDelay(35, () -> {
            helper.assertTrue(rig.crafter().getParallel() == 3, "Two Hellforged casings should give 3 parallel crafts, got " + rig.crafter().getParallel());
            helper.assertTrue(outputCount(rig.crafter(), Items.STICK) == 12, "Three crafts should finish in one cycle, got " + outputCount(rig.crafter(), Items.STICK));
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 300)
    public void sanguineCrafterTabulaModeUsesOrb(GameTestHelper helper) {
        Rig rig = build(helper);
        ItemStack orb = new ItemStack(NVItems.ORB_APPRENTICE.get());
        orb.set(NVDataComponents.BINDING, new Binding(rig.owner().getUUID(), "CrafterOwner"));
        rig.crafter().setTemplate(Mode.TABULA, template(new ItemStack(Items.WHEAT), new ItemStack(Items.SUGAR)));
        rig.crafter().inv.setStackInSlot(SanguineCrafterBlockEntity.ORB_SLOT, orb);
        rig.crafter().inv.setStackInSlot(0, new ItemStack(Items.WHEAT));
        rig.crafter().inv.setStackInSlot(1, new ItemStack(Items.SUGAR));
        helper.succeedWhen(() -> {
            helper.assertTrue(outputCount(rig.crafter(), Items.BREAD) == 1, "Expected bread, status " + rig.crafter().getStatus());
            helper.assertTrue(rig.network().getCurrentEV() == 9880, "Recipe 100 EV plus 20 base, network holds " + rig.network().getCurrentEV());
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 300)
    public void sanguineCrafterAutoModeCraftsForgeRecipeFromGem(GameTestHelper helper) {
        Rig rig = build(helper);
        ItemStack gem = new ItemStack(NVItems.SPIRITUS_GEM_GREATER.get());
        gem.set(NVDataComponents.SPIRITUS_AMOUNT, 600.0);
        rig.crafter().inv.setStackInSlot(SanguineCrafterBlockEntity.GEM_SLOT, gem);
        rig.crafter().inv.setStackInSlot(0, new ItemStack(NVBlocks.HELLFIRE_FORGE.item().get()));
        rig.crafter().inv.setStackInSlot(1, new ItemStack(Items.DEEPSLATE));
        rig.crafter().inv.setStackInSlot(2, new ItemStack(Items.LAPIS_LAZULI));
        rig.crafter().inv.setStackInSlot(3, new ItemStack(Items.GLASS));
        helper.succeedWhen(() -> {
            helper.assertTrue(outputCount(rig.crafter(), NVBlocks.CRYSTALLARIUM_MALEFICUM.item().get()) == 1,
                    "Expected Crystallarium Maleficum, status " + rig.crafter().getStatus());
            double left = rig.crafter().inv.getStackInSlot(SanguineCrafterBlockEntity.GEM_SLOT).getOrDefault(NVDataComponents.SPIRITUS_AMOUNT, 0D);
            helper.assertTrue(left == 500.0, "The gem should pay the 100 drain, holds " + left);
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 120)
    public void sanguineCrafterPushesResultsOutOfChosenFace(GameTestHelper helper) {
        Rig rig = build(helper);
        BlockPos center = helper.absolutePos(CENTER);
        BlockPos barrelPos = center.east(2);
        helper.getLevel().setBlock(barrelPos, Blocks.BARREL.defaultBlockState(), Block.UPDATE_ALL);
        BarrelBlockEntity barrel = (BarrelBlockEntity) helper.getLevel().getBlockEntity(barrelPos);
        rig.crafter().setOutputFace(Direction.EAST);
        rig.crafter().setTemplate(Mode.CRAFTING, sticksTemplate());
        rig.crafter().inv.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 2));
        helper.succeedWhen(() -> {
            helper.assertTrue(barrel.getItem(0).is(Items.STICK) && barrel.getItem(0).getCount() == 4,
                    "Barrel should receive the 4 sticks, holds " + barrel.getItem(0));
            helper.assertTrue(outputCount(rig.crafter(), Items.STICK) == 0, "Output buffer should be empty after pushing");
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 80)
    public void sanguineCrafterCasingFacesFollowSideGroups(GameTestHelper helper) {
        Rig rig = build(helper);
        helper.runAfterDelay(25, () -> {
            BlockPos casing = helper.absolutePos(CENTER.below());
            IItemHandler top = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, casing, Direction.UP);
            helper.assertTrue(top != null, "A formed casing should expose the crafter's inventory");
            ItemStack plankLeft = top.insertItem(SanguineCrafterBlockEntity.INPUT_START, new ItemStack(Items.OAK_PLANKS), true);
            helper.assertTrue(plankLeft.isEmpty(), "The top face should accept input by default");
            ItemStack orbLeft = top.insertItem(SanguineCrafterBlockEntity.ORB_SLOT, new ItemStack(NVItems.ORB_APPRENTICE.get()), true);
            helper.assertTrue(!orbLeft.isEmpty(), "The orb slot should stay hidden from the top face by default");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 80)
    public void sanguineCrafterChunkLoadingWaitsForOwnerOnline(GameTestHelper helper) {
        Rig rig = build(helper);
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(rig.crafter().getChunkLoading() == ChunkLoading.OWNER_OFFLINE,
                    "A mock owner is not in the player list, expected OWNER_OFFLINE, got " + rig.crafter().getChunkLoading());
            helper.succeed();
        });
    }

}
