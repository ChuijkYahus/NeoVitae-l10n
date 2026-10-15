package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.block.dungeon.BlockPillarCap;
import com.breakinblocks.neovitae.common.block.dungeon.DungeonBlocks;
import com.breakinblocks.neovitae.common.block.dungeon.DungeonVariant;

import java.util.Optional;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class DungeonBlockRotationTests {


    private static final BlockPos SUPPORT = new BlockPos(2, 2, 2);

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void dungeonBlocksCapOnTheFloorPointsDown(GameTestHelper helper) {
        helper.setBlock(SUPPORT, Blocks.STONE);
        assertCap(helper, place(helper, cap(), SUPPORT, Direction.UP, false), Direction.DOWN, "A cap set on the floor starts a pillar above it");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void dungeonBlocksCapOnAPillarPointsAwayFromIt(GameTestHelper helper) {
        helper.setBlock(SUPPORT, pillar().defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y));
        assertCap(helper, place(helper, cap(), SUPPORT, Direction.UP, false), Direction.UP, "A cap set on top of a pillar tops it");
        helper.setBlock(SUPPORT, pillar().defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z));
        assertCap(helper, place(helper, cap(), SUPPORT, Direction.NORTH, false), Direction.NORTH, "A cap at the end of a horizontal pillar points along it");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void dungeonBlocksCapOnAWallPointsIntoIt(GameTestHelper helper) {
        helper.setBlock(SUPPORT, Blocks.STONE);
        assertCap(helper, place(helper, cap(), SUPPORT, Direction.EAST, false), Direction.WEST, "A cap set on a wall starts a horizontal pillar");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void dungeonBlocksSneakingFlipsTheCap(GameTestHelper helper) {
        helper.setBlock(SUPPORT, Blocks.STONE);
        assertCap(helper, place(helper, cap(), SUPPORT, Direction.UP, true), Direction.UP, "Sneaking should flip a floor cap to point up");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void dungeonBlocksCapRotatesWithStructures(GameTestHelper helper) {
        BlockState north = cap().defaultBlockState().setValue(BlockPillarCap.FACING, Direction.NORTH);
        helper.assertTrue(north.rotate(Rotation.CLOCKWISE_90).getValue(BlockPillarCap.FACING) == Direction.EAST,
                "A north cap rotated clockwise should face east");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void dungeonBlocksEyeTurnsWithTheClickedFace(GameTestHelper helper) {
        helper.setBlock(SUPPORT, Blocks.STONE);
        Block eye = DungeonBlocks.DUNGEON_EYE.get(DungeonVariant.RAW).block().get();
        BlockState placed = place(helper, eye, SUPPORT, Direction.EAST, false);
        helper.assertTrue(placed != null && placed.getValue(BlockStateProperties.AXIS) == Direction.Axis.X,
                "An eye placed on an east face should lie along the x axis, got " + placed);
        helper.assertTrue(placed.getLightEmission() == 15, "The eye should still glow");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void dungeonBlocksDungeonPillarsHaveBottomCaps(GameTestHelper helper) {
        Optional<StructureTemplate> template = helper.getLevel().getStructureManager()
                .get(NeoVitae.rl("standard/tall_spiral_staircase"));
        helper.assertTrue(template.isPresent(), "The spiral staircase structure should load");
        int down = 0;
        for (StructureTemplate.StructureBlockInfo info : template.get().filterBlocks(BlockPos.ZERO,
                new StructurePlaceSettings(), cap())) {
            if (info.state().getValue(BlockPillarCap.FACING) == Direction.DOWN) down++;
        }
        helper.assertTrue(down > 0, "Pillars in the spiral staircase should stand on downward caps");
        helper.succeed();
    }


    private static BlockPillarCap cap() {
        return DungeonBlocks.DUNGEON_PILLAR_CAP.get(DungeonVariant.RAW).block().get();
    }

    private static Block pillar() {
        return DungeonBlocks.DUNGEON_PILLAR_CENTER.get(DungeonVariant.RAW).block().get();
    }

    private static BlockState place(GameTestHelper helper, Block block, BlockPos support, Direction face, boolean sneaking) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(sneaking);
        BlockPos abs = helper.absolutePos(support);
        Vec3 hit = Vec3.atCenterOf(abs).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
        BlockPlaceContext context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new ItemStack(block), new BlockHitResult(hit, face, abs, false));
        return block.getStateForPlacement(context);
    }

    private static void assertCap(GameTestHelper helper, BlockState state, Direction expected, String message) {
        helper.assertTrue(state != null && state.getValue(BlockPillarCap.FACING) == expected,
                message + ": expected " + expected + ", got " + (state == null ? "nothing" : state.getValue(BlockPillarCap.FACING)));
    }
}
