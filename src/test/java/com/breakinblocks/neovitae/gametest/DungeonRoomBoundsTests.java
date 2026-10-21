package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.structures.DungeonRoom;
import com.breakinblocks.neovitae.structures.DungeonRoomRegistry;
import com.breakinblocks.neovitae.structures.RoomBoundsProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class DungeonRoomBoundsTests {

    @GameTest(template = "empty_5x5x7", timeoutTicks = 40)
    public void rotatedRoomBoundsMatchTemplate(GameTestHelper helper) {
        int checked = 0;
        for (Map.Entry<ResourceLocation, DungeonRoom> entry : DungeonRoomRegistry.getDungeonRoomMap().entrySet()) {
            DungeonRoom room = entry.getValue();
            if (room.getStructureMap().size() != 1) continue;
            List<AreaDescriptor> base = room.getAreaDescriptors(new StructurePlaceSettings(), BlockPos.ZERO);
            if (base.size() != 1 || !(base.get(0) instanceof AreaDescriptor.Rectangle baseRect)) continue;

            String structure = room.getStructureMap().keySet().iterator().next();
            Optional<StructureTemplate> template = helper.getLevel().getStructureManager().get(ResourceLocation.parse(structure));
            if (template.isEmpty()) continue;
            if (!baseRect.getMinimumOffset().equals(BlockPos.ZERO)
                    || !baseRect.getMaximumOffset().equals(new BlockPos(template.get().getSize()))) continue;

            for (Rotation rotation : Rotation.values()) {
                StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(rotation);
                AreaDescriptor.Rectangle rect = (AreaDescriptor.Rectangle) room.getAreaDescriptors(settings, BlockPos.ZERO).get(0);
                BoundingBox placed = template.get().getBoundingBox(settings, BlockPos.ZERO);
                BlockPos placedMin = new BlockPos(placed.minX(), placed.minY(), placed.minZ());
                BlockPos placedMax = new BlockPos(placed.maxX() + 1, placed.maxY() + 1, placed.maxZ() + 1);
                helper.assertTrue(rect.getMinimumOffset().equals(placedMin) && rect.getMaximumOffset().equals(placedMax),
                        entry.getKey() + " rotated " + rotation + " declares " + rect.getMinimumOffset() + ".."
                                + rect.getMaximumOffset() + " but its blocks span " + placedMin + ".." + placedMax);
            }
            checked++;
        }
        helper.assertTrue(checked > 0, "No box-shaped dungeon rooms were checked");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void doorFillCoversDoorwayInEveryFacing(GameTestHelper helper) {
        DungeonRoom room = new DungeonRoom();
        for (Rotation rotation : Rotation.values()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(rotation);
                Direction worldFacing = rotation.rotate(facing);
                Direction side = worldFacing.getClockWise();
                Set<BlockPos> expected = new HashSet<>();
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        expected.add(BlockPos.ZERO.relative(side, i).above(j));
                    }
                }
                Set<BlockPos> filled = new HashSet<>(room.getDoorFillDescriptor(settings, BlockPos.ZERO, BlockPos.ZERO, facing)
                        .getContainedPositions(BlockPos.ZERO));
                helper.assertTrue(filled.equals(expected),
                        "Door facing " + worldFacing + " (rotation " + rotation + ") fills " + filled + " instead of " + expected);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void roomAirStaysInsideItsBounds(GameTestHelper helper) {
        RoomBoundsProcessor processor = new RoomBoundsProcessor(
                List.of(new AreaDescriptor.Rectangle(BlockPos.ZERO, new BlockPos(2, 2, 2))));
        StructurePlaceSettings settings = new StructurePlaceSettings();
        StructureTemplate.StructureBlockInfo insideAir = new StructureTemplate.StructureBlockInfo(new BlockPos(1, 1, 1), Blocks.AIR.defaultBlockState(), null);
        StructureTemplate.StructureBlockInfo edgeAir = new StructureTemplate.StructureBlockInfo(new BlockPos(2, 0, 0), Blocks.AIR.defaultBlockState(), null);
        StructureTemplate.StructureBlockInfo outsideStone = new StructureTemplate.StructureBlockInfo(new BlockPos(5, 5, 5), Blocks.STONE.defaultBlockState(), null);

        helper.assertTrue(processor.process(helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, insideAir, insideAir, settings, null) != null,
                "Air inside the room should still be placed");
        helper.assertTrue(processor.process(helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, edgeAir, edgeAir, settings, null) == null,
                "Air just past the room's edge must not be placed");
        helper.assertTrue(processor.process(helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, outsideStone, outsideStone, settings, null) != null,
                "Solid blocks are left to the room's own data");
        helper.succeed();
    }
}
