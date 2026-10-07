package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.datacomponent.EffectHolder;
import com.breakinblocks.neovitae.common.datacomponent.FlaskEffects;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.common.item.potion.ItemAlchemyFlask;

import java.util.List;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class FlaskRinseTests {

    private static ItemStack brewedFlask(int dosesUsed) {
        ItemStack stack = new ItemStack(NVItems.ALCHEMY_FLASK.get());
        ItemAlchemyFlask.setFlaskEffects(stack, new FlaskEffects(List.of(EffectHolder.create(MobEffects.MOVEMENT_SPEED, 3600, 0))));
        stack.setDamageValue(dosesUsed);
        return stack;
    }

    private static InteractionResult useOnBlock(GameTestHelper helper, Player player, ItemStack stack, BlockPos relPos) {
        BlockPos pos = helper.absolutePos(relPos);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        return stack.onItemUseFirst(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit));
    }

    private static boolean isRinsed(ItemStack stack) {
        return !stack.has(NVDataComponents.FLASK_EFFECTS.get())
                && !stack.has(DataComponents.POTION_CONTENTS)
                && stack.getDamageValue() == 0;
    }

    private static void placeFullCauldron(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void drainedFlaskRinsesInWaterCauldron(GameTestHelper helper) {
        BlockPos cauldron = new BlockPos(2, 1, 2);
        placeFullCauldron(helper, cauldron);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack flask = brewedFlask(ItemAlchemyFlask.MAX_USES);

        InteractionResult result = useOnBlock(helper, player, flask, cauldron);
        if (!result.consumesAction()) {
            helper.fail("A drained flask should rinse in a water cauldron, got " + result);
            return;
        }
        if (!isRinsed(flask)) {
            helper.fail("Flask should be empty with full doses after rinsing: " + flask.getComponents());
            return;
        }
        helper.assertBlockProperty(cauldron, LayeredCauldronBlock.LEVEL, 2);
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void brewedFlaskOnlyRinsesWhileSneaking(GameTestHelper helper) {
        BlockPos cauldron = new BlockPos(2, 1, 2);
        placeFullCauldron(helper, cauldron);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack flask = brewedFlask(2);

        if (useOnBlock(helper, player, flask, cauldron).consumesAction() || isRinsed(flask)) {
            helper.fail("A flask with doses left must not rinse without sneaking");
            return;
        }
        helper.assertBlockProperty(cauldron, LayeredCauldronBlock.LEVEL, 3);

        player.setShiftKeyDown(true);
        if (!useOnBlock(helper, player, flask, cauldron).consumesAction() || !isRinsed(flask)) {
            helper.fail("A sneaking player should rinse a flask that still has doses");
            return;
        }
        helper.assertBlockProperty(cauldron, LayeredCauldronBlock.LEVEL, 2);
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void freshFlaskIsNotRinsed(GameTestHelper helper) {
        BlockPos cauldron = new BlockPos(2, 1, 2);
        placeFullCauldron(helper, cauldron);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);

        if (useOnBlock(helper, player, new ItemStack(NVItems.ALCHEMY_FLASK.get()), cauldron).consumesAction()) {
            helper.fail("An unused flask should not draw water");
            return;
        }
        helper.assertBlockProperty(cauldron, LayeredCauldronBlock.LEVEL, 3);
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 30)
    public void rinseDrawsWaterFromFluidHandler(GameTestHelper helper) {
        BlockPos tankPos = new BlockPos(2, 1, 2);
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.STONE.defaultBlockState());
        helper.setBlock(tankPos, NVBlocks.BLOOD_TANK.block().get().defaultBlockState());

        helper.runAfterDelay(1, () -> {
            IFluidHandler tank = helper.getLevel()
                    .getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(tankPos), null);
            if (tank == null) {
                helper.fail("Blood tank exposes no fluid handler");
                return;
            }
            int filled = tank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (filled != 1000) {
                helper.fail("Tank should take 1000 mB of water, took " + filled);
                return;
            }

            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack flask = brewedFlask(ItemAlchemyFlask.MAX_USES);
            if (!useOnBlock(helper, player, flask, tankPos).consumesAction() || !isRinsed(flask)) {
                helper.fail("A drained flask should rinse from a tank holding water");
                return;
            }
            int left = tank.getFluidInTank(0).getAmount();
            if (left != 1000 - ItemAlchemyFlask.RINSE_AMOUNT) {
                helper.fail("Rinsing should take " + ItemAlchemyFlask.RINSE_AMOUNT + " mB, tank has " + left);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void waterBucketRecipeRinsesFlask(GameTestHelper helper) {
        ItemStack flask = brewedFlask(5);
        CraftingInput input = CraftingInput.of(2, 1, List.of(flask, new ItemStack(Items.WATER_BUCKET)));
        var match = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (match.isEmpty()) {
            helper.fail("Flask + Water Bucket should match a crafting recipe");
            return;
        }
        ItemStack result = match.get().value().assemble(input, helper.getLevel().registryAccess());
        if (!result.is(NVItems.ALCHEMY_FLASK.get()) || !isRinsed(result)) {
            helper.fail("Recipe should give an empty Alchemy Flask, got " + result + " " + result.getComponents());
            return;
        }
        helper.succeed();
    }
}
