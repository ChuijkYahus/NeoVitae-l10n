package com.breakinblocks.neovitae.gametest;

import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.ritual.NVRituals;
import com.breakinblocks.neovitae.ritual.types.RitualMagnetism;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class QuarryBackfillTests {

    private static final BlockPos MRS_POS = new BlockPos(2, 4, 2);

    private static RitualMagnetism activeQuarry(GameTestHelper helper, int cursorY) {
        helper.setBlock(MRS_POS, NVBlocks.MASTER_RITUAL_STONE.block().get());
        MasterRitualStoneBlockEntity mrs = helper.getBlockEntity(MRS_POS);
        mrs.forceActivateRitual(NVRituals.DEUS_MAGNETISM.get(), null);
        if (!(mrs.getCurrentRitual() instanceof RitualMagnetism quarry)) {
            helper.fail("Expected an active Endless Quarry, got " + mrs.getCurrentRitual());
            return null;
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt("lastPosX", -3);
        tag.putInt("lastPosY", cursorY);
        tag.putInt("lastPosZ", -3);
        quarry.readFromNBT(tag);
        return quarry;
    }

    private static int cursorY(RitualMagnetism quarry) {
        CompoundTag tag = new CompoundTag();
        quarry.writeToNBT(tag);
        return tag.contains("lastPosY") ? tag.getInt("lastPosY") : Integer.MIN_VALUE;
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 40)
    public void meteorRewindsPassedScan(GameTestHelper helper) {
        RitualMagnetism quarry = activeQuarry(helper, -40);
        if (quarry == null) return;

        RitualMagnetism.notifyDeposit(helper.getLevel(), helper.absolutePos(MRS_POS.below(3)), 1);
        int cursor = cursorY(quarry);
        if (cursor != -2) {
            helper.fail("A meteor above the scan cursor should rewind it to the meteor's top layer, cursor=" + cursor);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 40)
    public void meteorBelowCursorKeepsScan(GameTestHelper helper) {
        RitualMagnetism quarry = activeQuarry(helper, -1);
        if (quarry == null) return;

        RitualMagnetism.notifyDeposit(helper.getLevel(), helper.absolutePos(MRS_POS.below(3)), 1);
        int cursor = cursorY(quarry);
        if (cursor != -1) {
            helper.fail("A meteor the scan has not reached yet must not move the cursor, cursor=" + cursor);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 60)
    public void backfillMatchesSurroundingStone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hole = new BlockPos(2, 1, 2);
        BlockPos abs = helper.absolutePos(hole);

        helper.setBlock(hole.north(), Blocks.DEEPSLATE);
        helper.setBlock(hole.south(), Blocks.DEEPSLATE);

        BlockState filler = RitualMagnetism.fillerFor(level, abs);
        if (!filler.is(Blocks.DEEPSLATE)) {
            helper.fail("Backfill picked " + filler + " instead of the surrounding deepslate");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 60)
    public void backfillNeverCopiesPlayerBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hole = new BlockPos(2, 1, 2);
        BlockPos abs = helper.absolutePos(hole);

        // Valuables and built blocks must never be duplicated into the hole.
        helper.setBlock(hole.north(), Blocks.DIAMOND_BLOCK);
        helper.setBlock(hole.south(), Blocks.CHEST);
        helper.setBlock(hole.east(), Blocks.OAK_PLANKS);

        BlockState filler = RitualMagnetism.fillerFor(level, abs);
        if (!filler.is(Blocks.STONE)) {
            helper.fail("Backfill copied a non-natural neighbour: " + filler);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 60)
    public void backfillFallsBackToStoneInOpenAir(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockState filler = RitualMagnetism.fillerFor(level, helper.absolutePos(new BlockPos(2, 3, 2)));
        if (!filler.is(Blocks.STONE)) {
            helper.fail("Expected a plain stone fallback, got " + filler);
            return;
        }
        helper.succeed();
    }
}
