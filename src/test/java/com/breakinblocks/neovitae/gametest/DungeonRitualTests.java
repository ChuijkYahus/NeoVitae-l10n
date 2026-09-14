package com.breakinblocks.neovitae.gametest;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.block.BlockMasterRitualStone;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.block.dungeon.DungeonBlocks;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.common.network.RitualObstructionsPayload;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.types.DungeonRitualBase;
import com.breakinblocks.neovitae.util.helper.AnimaHelper;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class DungeonRitualTests {
    private static final BlockPos CENTER = new BlockPos(12, 2, 12);

    @GameTest(template = "empty_24x5x24")
    public void portalsFormInFloorInEveryDirection(GameTestHelper helper) {
        for (Ritual ritual : rituals()) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                MasterRitualStoneBlockEntity stone = build(helper, ritual, direction);
                Probe probe = new Probe(helper, ritual);
                Player player = helper.makeMockPlayer(GameType.SURVIVAL);
                helper.assertTrue(probe.getActivationError(stone, player) == null,
                        "Floor must not obstruct " + ritual.getName() + " facing " + direction);
                BoundingBox bounds = probe.bounds(stone, helper.getLevel());
                helper.assertTrue(bounds.minY() == stone.getBlockPos().getY(), "Bounds must start at the master, not below it");
                helper.assertTrue(bounds.getYSpan() == 7 && bounds.getXSpan() == 9 && bounds.getZSpan() == 9,
                        "Expected the shipped 9x7x9 portal bounds");
                helper.assertTrue(probe.place(stone, helper.getLevel()), "Portal placement must succeed");
                helper.assertBlockPresent(DungeonBlocks.ALTERNATOR.block().get(), CENTER);
                helper.assertBlockPresent(NVBlocks.INVERSION_PILLAR.block().get(), CENTER.above(2));
                helper.assertBlockPresent(Blocks.STONE, CENTER.below());
                // These template air cells are outside both rune layouts, even after rotation.
                helper.assertBlockPresent(Blocks.STONE, CENTER.offset(-4, 0, -4));
                helper.assertBlockPresent(Blocks.STONE, CENTER.offset(4, 0, 4));
                helper.assertTrue(!(helper.getBlockState(CENTER.above(4)).getBlock() instanceof IRitualStone),
                        "Portal placement must consume upper ritual stones");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty_24x5x24")
    public void obstructionBoundsIncludeTopAndSidesButExcludeFloorBelow(GameTestHelper helper) {
        for (Ritual ritual : rituals()) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                MasterRitualStoneBlockEntity stone = build(helper, ritual, direction);
                Probe probe = new Probe(helper, ritual);
                List<BlockPos> blockers = List.of(CENTER.offset(-4, 6, -4), CENTER.offset(4, 1, 4), CENTER.offset(0, 6, 4));
                for (BlockPos pos : blockers) helper.setBlock(pos, Blocks.STONE);
                helper.setBlock(CENTER.offset(5, 1, 0), Blocks.STONE);
                helper.setBlock(CENTER.above(7), Blocks.STONE);
                List<BlockPos> found = probe.obstructions(stone, helper.getLevel());
                helper.assertTrue(found.size() == blockers.size() && blockers.stream().map(helper::absolutePos).allMatch(found::contains),
                        "Must report exactly the obstructions inside the rotated portal bounds");
                for (BlockPos pos : blockers) helper.setBlock(pos, Blocks.AIR);
                helper.assertTrue(probe.obstructions(stone, helper.getLevel()).isEmpty(), "Removing blockers must allow activation");
                helper.setBlock(CENTER.offset(4, 0, 4), Blocks.CHEST);
                helper.setBlock(CENTER.offset(-4, 0, -4), Blocks.BEDROCK);
                helper.setBlock(CENTER.offset(4, 1, 4), NVBlocks.BLOODSTONE.block().get());
                helper.assertTrue(probe.obstructions(stone, helper.getLevel()).size() == 3,
                        "Inventories, bedrock and unrelated NeoVitae blocks must remain obstructions");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty_24x5x24")
    public void activationKeepsSpecificMessageAndDoesNotCharge(GameTestHelper helper) {
        List<Component> messages = new ArrayList<>();
        Player player = new Player(helper.getLevel(), BlockPos.ZERO, 0, new GameProfile(UUID.randomUUID(), "ritual-test")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }
            @Override public void displayClientMessage(Component message, boolean actionBar) { messages.add(message); }
        };
        var anima = AnimaHelper.getAnima(player.getUUID());
        anima.add(AnimaTicket.create(1000000), 10000000);
        for (Ritual ritual : rituals()) {
            messages.clear();
            MasterRitualStoneBlockEntity stone = build(helper, ritual, Direction.NORTH);
            helper.setBlock(CENTER.offset(4, 1, 4), Blocks.STONE);
            double before = anima.getCurrentEV();
            helper.assertTrue(!stone.activateRitual(ritual, player, 2), "Obstructed activation must fail");
            helper.assertTrue(!stone.isActive() && anima.getCurrentEV() == before, "Failure must not activate or spend EV");
            helper.assertTrue(messages.size() == 1 && key(messages.getFirst()).equals("ritual.neovitae.dungeon.obstructed"),
                    "The specific obstruction message must be displayed once without the generic overwrite");
            Object[] args = ((TranslatableContents) messages.getFirst().getContents()).getArgs();
            helper.assertTrue(args[0].equals(1), "Message must report the actual blocker count");
        }
        helper.succeed();
    }

    @GameTest(template = "empty_24x5x24")
    public void missingTemplateHeightAndHighlightPayload(GameTestHelper helper) {
        Ritual ritual = NVRituals.SIMPLE_DUNGEON.get();
        Probe probe = new Probe(helper, ritual);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        MasterRitualStoneBlockEntity stone = build(helper, ritual, Direction.NORTH);
        probe.structureId = NeoVitae.rl("ritual/missing_test_template");
        helper.assertTrue(key(probe.getActivationError(stone, player)).equals("ritual.neovitae.dungeon.missing_structure"),
                "Missing template must fail preflight");
        probe = new Probe(helper, ritual);
        var highStone = new MasterRitualStoneBlockEntity(
                new BlockPos(stone.getBlockPos().getX(), helper.getLevel().getMaxBuildHeight() - 6, stone.getBlockPos().getZ()),
                stone.getBlockState());
        highStone.setLevel(helper.getLevel());
        helper.assertTrue(key(probe.getActivationError(highStone, player)).equals("ritual.neovitae.dungeon.build_height"),
                "Portal crossing build height must fail preflight");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (List<BlockPos> positions : List.of(List.<BlockPos>of(), List.of(new BlockPos(-12, 64, 25), new BlockPos(30, -20, -50)))) {
                var payload = new RitualObstructionsPayload(helper.getLevel().dimension().location(), positions);
                RitualObstructionsPayload.STREAM_CODEC.encode(buffer, payload);
                helper.assertTrue(payload.equals(RitualObstructionsPayload.STREAM_CODEC.decode(buffer)),
                        "Highlight positions, dimension and clearing packet must survive network round trip");
            }
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    private static String key(Component component) {
        return component != null && component.getContents() instanceof TranslatableContents text ? text.getKey() : "";
    }

    private static List<Ritual> rituals() {
        return List.of(NVRituals.SIMPLE_DUNGEON.get(), NVRituals.STANDARD_DUNGEON.get());
    }

    private static MasterRitualStoneBlockEntity build(GameTestHelper helper, Ritual ritual, Direction direction) {
        for (BlockPos offset : BlockPos.betweenClosed(-5, -1, -5, 5, 7, 5)) {
            helper.setBlock(CENTER.offset(offset), offset.getY() <= 0 ? Blocks.STONE : Blocks.AIR);
        }
        helper.setBlock(CENTER, NVBlocks.MASTER_RITUAL_STONE.block().get().defaultBlockState()
                .setValue(BlockMasterRitualStone.FACING, direction));
        for (RitualComponent component : RitualLayouts.get(helper.getLevel(), ritual)) {
            BlockPos offset = component.offset();
            BlockPos rotated = switch (direction) {
                case EAST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
                case SOUTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
                case WEST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
                default -> offset;
            };
            helper.setBlock(CENTER.offset(rotated), runeBlock(component.runeType()));
        }
        var stone = (MasterRitualStoneBlockEntity) helper.getBlockEntity(CENTER);
        helper.assertTrue(stone.getDirection() == direction, "Fixture must use the requested direction");
        return stone;
    }

    private static Block runeBlock(EnumRuneType type) {
        return switch (type) {
            case BLANK -> NVBlocks.BLANK_RITUAL_STONE.block().get();
            case WATER -> NVBlocks.WATER_RITUAL_STONE.block().get();
            case FIRE -> NVBlocks.FIRE_RITUAL_STONE.block().get();
            case EARTH -> NVBlocks.EARTH_RITUAL_STONE.block().get();
            case AIR -> NVBlocks.AIR_RITUAL_STONE.block().get();
            case TENEBRAE -> NVBlocks.TENEBRAE_RITUAL_STONE.block().get();
            case DEUS -> NVBlocks.DEUS_RITUAL_STONE.block().get();
        };
    }

    private static class Probe extends DungeonRitualBase {
        private final List<RitualComponent> components;
        private ResourceLocation structureId;

        Probe(GameTestHelper helper, Ritual ritual) {
            super("dungeon_test", 0, 0, "test");
            components = RitualLayouts.get(helper.getLevel(), ritual);
            structureId = NeoVitae.rl(ritual == NVRituals.SIMPLE_DUNGEON.get()
                    ? "ritual/edge_of_the_hidden_realm" : "ritual/pathway_to_the_endless_realm");
        }

        @Override protected ResourceLocation getStructureId() { return structureId; }
        @Override public void gatherComponents(Consumer<RitualComponent> consumer) { components.forEach(consumer); }
        @Override public Ritual getNewCopy() { return this; }
        @Override public void performRitual(IMasterRitualStone stone) {}
        BoundingBox bounds(IMasterRitualStone stone, ServerLevel level) { return getStructureBounds(stone, level); }
        List<BlockPos> obstructions(IMasterRitualStone stone, ServerLevel level) {
            return findObstructions(stone, level, bounds(stone, level));
        }
        boolean place(IMasterRitualStone stone, ServerLevel level) { return applyRitualStructure(stone, level); }
    }
}
