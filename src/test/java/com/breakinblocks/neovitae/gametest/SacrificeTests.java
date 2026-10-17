package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.breakinblocks.neovitae.common.datamap.EntitySacrificeHelper;
import com.breakinblocks.neovitae.util.AltarUtil;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class SacrificeTests {

    private static AraVitaeTile placeAltar(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(pos, NVBlocks.ARA_VITAE.block().get().defaultBlockState());
        BlockEntity be = helper.getBlockEntity(pos);
        if (!(be instanceof AraVitaeTile altar)) {
            helper.fail("Expected AraVitaeTile at " + pos);
            return null;
        }
        return altar;
    }

    // ==================== Altar Finding ====================

    @GameTest(template = "empty_5x5x7", timeoutTicks = 30)
    public void altarUtilFindsNearbyAltar(GameTestHelper helper) {
        BlockPos altarPos = new BlockPos(3, 1, 2);
        placeAltar(helper, altarPos);

        helper.runAfterDelay(3, () -> {
            BlockPos searchFrom = helper.absolutePos(new BlockPos(3, 1, 4));
            BlockPos absAltarPos = helper.absolutePos(altarPos);
            BlockPos found = AltarUtil.findAltar(helper.getLevel(), searchFrom, 3);
            if (found == null) {
                helper.fail("AltarUtil.findAltar should find altar within radius 3");
                return;
            }
            if (!found.equals(absAltarPos)) {
                helper.fail("Found altar at wrong position: " + found + " expected " + absAltarPos);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 30)
    public void altarUtilReturnsNullWhenTooFar(GameTestHelper helper) {
        BlockPos altarPos = new BlockPos(1, 1, 1);
        placeAltar(helper, altarPos);

        helper.runAfterDelay(3, () -> {
            // Search from a position far outside radius 2
            BlockPos searchFrom = helper.absolutePos(new BlockPos(4, 1, 4));
            BlockPos found = AltarUtil.findAltar(helper.getLevel(), searchFrom, 2);
            if (found != null) {
                helper.fail("AltarUtil.findAltar should return null when altar is out of range");
                return;
            }
            helper.succeed();
        });
    }

    // ==================== Sacrifice EV ====================

    @GameTest(template = "empty_5x5x7", timeoutTicks = 30)
    public void sacrificeEVAddsToMainTank(GameTestHelper helper) {
        AraVitaeTile altar = placeAltar(helper, new BlockPos(3, 1, 2));

        helper.runAfterDelay(3, () -> {
            if (altar == null) return;
            int before = altar.getMainTank();
            altar.addSacrificeEV(500, true);
            int after = altar.getMainTank();
            if (after - before < 500) {
                helper.fail("addSacrificeEV(500, true) should add at least 500, added " + (after - before));
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 30)
    public void selfSacrificeEVAddsToMainTank(GameTestHelper helper) {
        AraVitaeTile altar = placeAltar(helper, new BlockPos(3, 1, 2));

        helper.runAfterDelay(3, () -> {
            if (altar == null) return;
            int before = altar.getMainTank();
            altar.addSacrificeEV(200, false);
            int after = altar.getMainTank();
            if (after - before < 200) {
                helper.fail("addSacrificeEV(200, false) should add at least 200, added " + (after - before));
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 30)
    public void sacrificeEVRespectsCapacity(GameTestHelper helper) {
        AraVitaeTile altar = placeAltar(helper, new BlockPos(3, 1, 2));

        helper.runAfterDelay(3, () -> {
            if (altar == null) return;
            int capacity = altar.getMainCapacity();
            altar.addSacrificeEV(capacity + 10000, true);
            int after = altar.getMainTank();
            if (after > capacity) {
                helper.fail("Main tank " + after + " should not exceed capacity " + capacity);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 40)
    public void sacrificeOverflowGoesToReserveAndRefillsBasin(GameTestHelper helper) {
        AraVitaeTile altar = placeAltar(helper, new BlockPos(3, 1, 2));

        helper.runAfterDelay(3, () -> {
            if (altar == null) return;
            int capacity = altar.getMainCapacity();
            if (capacity <= 0) {
                helper.fail("Altar capacity should be positive, got " + capacity);
                return;
            }
            altar.drainMainTank(altar.getMainTank());
            altar.addSacrificeEV(capacity * 3, true);
            if (altar.getMainTank() != capacity) {
                helper.fail("Main tank should be full at " + capacity + ", got " + altar.getMainTank());
                return;
            }
            int reserve = altar.getReserveTank();
            if (reserve < capacity * 2) {
                helper.fail("Reserve should hold at least the overflow " + (capacity * 2) + ", got " + reserve);
                return;
            }
            altar.drainMainTank(capacity);
            helper.runAfterDelay(2, () -> {
                if (altar.getMainTank() != capacity) {
                    helper.fail("Reserve should refill the basin to " + capacity + ", got " + altar.getMainTank());
                    return;
                }
                if (altar.getReserveTank() > reserve - capacity) {
                    helper.fail("Reserve should drop to at most " + (reserve - capacity) + ", got " + altar.getReserveTank());
                    return;
                }
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 30)
    public void sacrificeReserveRespectsCapacity(GameTestHelper helper) {
        AraVitaeTile altar = placeAltar(helper, new BlockPos(3, 1, 2));

        helper.runAfterDelay(3, () -> {
            if (altar == null) return;
            altar.addSacrificeEV(Integer.MAX_VALUE, true);
            if (altar.getReserveTank() != altar.getReserveCapacity()) {
                helper.fail("Reserve " + altar.getReserveTank() + " should cap at " + altar.getReserveCapacity());
                return;
            }
            helper.succeed();
        });
    }

    // ==================== Entity Sacrifice Values ====================

    @GameTest(template = "empty_5x5x7", timeoutTicks = 60)
    public void cowHasSacrificeValue(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 2);
        helper.setBlock(new BlockPos(3, 0, 2), Blocks.STONE.defaultBlockState());

        helper.runAfterDelay(3, () -> {
            BlockPos absPos = helper.absolutePos(pos);
            Cow cow = EntityType.COW.create(helper.getLevel());
            cow.setPos(absPos.getX() + 0.5, absPos.getY(), absPos.getZ() + 0.5);
            helper.getLevel().addFreshEntity(cow);

            int value = EntitySacrificeHelper.getEvPerDamage(cow);
            if (value <= 0) {
                helper.fail("Cow should have a positive sacrifice value, got " + value);
                return;
            }
            cow.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 60)
    public void chickenHasSacrificeValue(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 2);
        helper.setBlock(new BlockPos(3, 0, 2), Blocks.STONE.defaultBlockState());

        helper.runAfterDelay(3, () -> {
            BlockPos absPos = helper.absolutePos(pos);
            Chicken chicken = EntityType.CHICKEN.create(helper.getLevel());
            chicken.setPos(absPos.getX() + 0.5, absPos.getY(), absPos.getZ() + 0.5);
            helper.getLevel().addFreshEntity(chicken);

            int value = EntitySacrificeHelper.getEvPerDamage(chicken);
            if (value <= 0) {
                helper.fail("Chicken should have a positive sacrifice value, got " + value);
                return;
            }
            chicken.discard();
            helper.succeed();
        });
    }
}
