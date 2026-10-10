package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.breakinblocks.neovitae.common.dataattachment.NVDataAttachments;
import com.breakinblocks.neovitae.common.event.BurdenGroundingHandler;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class BurdenGroundingTests {

    private static final BlockPos FLOOR = new BlockPos(2, 0, 2);
    private static final Vec3 HOVER = new Vec3(2.5, 4.0, 2.5);
    private static final double CAP_ABOVE_FLOOR_TOP = 1.5;

    @GameTest(template = "empty_5x5x7", timeoutTicks = 60)
    public void markedFlyerIsPulledDown(GameTestHelper helper) {
        Mob flyer = spawnHoveringFlyer(helper);
        BurdenGroundingHandler.mark(flyer, helper.getLevel().getGameTime() + 100);
        double cap = floorTop(helper) + CAP_ABOVE_FLOOR_TOP + 0.01;

        helper.succeedWhen(() -> {
            if (flyer.getY() > cap) {
                helper.fail("Marked flyer still at y=" + flyer.getY() + ", cap is " + cap);
            }
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 40)
    public void unmarkedFlyerKeepsAltitude(GameTestHelper helper) {
        Mob flyer = spawnHoveringFlyer(helper);
        double startY = flyer.getY();

        helper.runAfterDelay(20, () -> {
            if (flyer.getY() < startY - 0.01) {
                helper.fail("Unmarked flyer dropped from " + startY + " to " + flyer.getY());
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 40)
    public void expiredMarkIsCleared(GameTestHelper helper) {
        Mob flyer = spawnHoveringFlyer(helper);
        double startY = flyer.getY();
        BurdenGroundingHandler.mark(flyer, helper.getLevel().getGameTime() - 1);

        helper.runAfterDelay(10, () -> {
            if (flyer.hasData(NVDataAttachments.BURDEN_GROUNDED_UNTIL)) {
                helper.fail("Expired grounding mark was not removed");
                return;
            }
            if (flyer.getY() < startY - 0.01) {
                helper.fail("Flyer with an expired mark dropped from " + startY + " to " + flyer.getY());
                return;
            }
            helper.succeed();
        });
    }

    private static Mob spawnHoveringFlyer(GameTestHelper helper) {
        helper.setBlock(FLOOR, Blocks.STONE.defaultBlockState());
        Mob flyer = helper.spawn(EntityType.ALLAY, HOVER);
        flyer.setNoAi(true);
        flyer.setNoGravity(true);
        flyer.setDeltaMovement(Vec3.ZERO);
        return flyer;
    }

    private static double floorTop(GameTestHelper helper) {
        return helper.absolutePos(FLOOR).getY() + 1.0;
    }
}
