package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.block.AnchorAura;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.block.RitualAnchorBlock;
import com.breakinblocks.neovitae.common.blockentity.HellfireForgeBlockEntity;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.common.blockentity.RitualAnchorBlockEntity;
import com.breakinblocks.neovitae.common.datacomponent.Anima;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.ritual.EnumRuneType;
import com.breakinblocks.neovitae.ritual.NVRituals;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.breakinblocks.neovitae.ritual.RitualComponent;
import com.breakinblocks.neovitae.ritual.RitualHelper;
import com.breakinblocks.neovitae.ritual.RitualSpiritusTracker;
import com.breakinblocks.neovitae.ritual.types.RitualCelerity;
import com.breakinblocks.neovitae.spiritus.WorldSpiritusHandler;
import com.breakinblocks.neovitae.util.helper.AnimaHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class RitualAnchorTests {


    private static final BlockPos MRS_POS = new BlockPos(5, 1, 12);
    private static final BlockPos ANCHOR_POS = new BlockPos(19, 1, 12);
    private static final UUID OWNER = UUID.fromString("5c1d9e2a-7b43-4f86-9a0d-2e6b81c4f357");

    @GameTest(template = "empty_24x5x24", timeoutTicks = 60)
    public void ritualAnchorRitualWorksAroundAnchor(GameTestHelper helper) {
        MasterRitualStoneBlockEntity mrs = activate(helper, NVRituals.CELERITY.get());
        RitualAnchorBlockEntity anchor = placeAnchor(helper);
        mrs.linkAnchor(helper.absolutePos(ANCHOR_POS));
        HellfireForgeBlockEntity nearAnchor = placeForge(helper, ANCHOR_POS.offset(2, 1, 0));
        HellfireForgeBlockEntity nearStone = placeForge(helper, MRS_POS.offset(2, 1, 0));
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(mrs.getOriginPos().equals(helper.absolutePos(ANCHOR_POS)),
                    "A linked stone should report the anchor as its origin");
            helper.assertTrue(RitualHelper.createContext(mrs).masterPos().equals(helper.absolutePos(ANCHOR_POS)),
                    "Rituals should receive the anchor position in their context");
            mrs.getCurrentRitual().performRitual(mrs);
            helper.assertTrue(nearAnchor.getRitualAcceleration().factor(helper.getLevel()) == RitualCelerity.SPEED,
                    "A forge next to the anchor should be sped up");
            helper.assertTrue(nearStone.getRitualAcceleration().factor(helper.getLevel()) == 1,
                    "A forge next to the stone should not be sped up once the ritual moved to the anchor");
            helper.assertTrue(anchor.getLinks().contains(mrs.getBlockPos()), "The anchor should list the linked stone");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 40)
    public void ritualAnchorUnlinkingReturnsRitualToStone(GameTestHelper helper) {
        MasterRitualStoneBlockEntity mrs = activate(helper, NVRituals.CELERITY.get());
        RitualAnchorBlockEntity anchor = placeAnchor(helper);
        mrs.linkAnchor(helper.absolutePos(ANCHOR_POS));
        helper.runAfterDelay(2, () -> {
            mrs.clearAnchor(false);
            helper.assertTrue(mrs.getOriginPos().equals(mrs.getBlockPos()), "An unlinked stone should work around itself");
            helper.assertTrue(anchor.getLinks().isEmpty(), "Unlinking should remove the stone from the anchor");
            helper.assertTrue(mrs.isActive(), "Unlinking should not stop the ritual");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 40)
    public void ritualAnchorDormantWithoutRunningRitual(GameTestHelper helper) {
        RitualAnchorBlockEntity anchor = placeAnchor(helper);
        MasterRitualStoneBlockEntity mrs = buildLayout(helper, NVRituals.CELERITY.get());
        helper.runAfterDelay(2, () -> {
            anchor.refreshNow();
            helper.assertTrue(aura(helper) == AnchorAura.DORMANT, "An anchor with no links should be dormant");
            mrs.linkAnchor(helper.absolutePos(ANCHOR_POS));
            helper.assertTrue(aura(helper) == AnchorAura.DORMANT, "A link to an idle stone should leave the anchor dormant");
            startRitual(mrs, NVRituals.CELERITY.get());
            helper.assertTrue(aura(helper) == AnchorAura.BLOOD,
                    "A running ritual that draws no Spiritus should glow blood red, got " + aura(helper));
            mrs.clearAnchor(false);
            helper.assertTrue(aura(helper) == AnchorAura.DORMANT, "Unlinking should put the anchor back to sleep");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 40)
    public void ritualAnchorRedstonePausesLinkedRituals(GameTestHelper helper) {
        MasterRitualStoneBlockEntity mrs = activate(helper, NVRituals.CELERITY.get());
        RitualAnchorBlockEntity anchor = placeAnchor(helper);
        mrs.linkAnchor(helper.absolutePos(ANCHOR_POS));
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(aura(helper) == AnchorAura.BLOOD, "A running linked ritual should light the anchor");
            helper.setBlock(ANCHOR_POS.east(), Blocks.REDSTONE_BLOCK);
            helper.assertTrue(mrs.isSuspended(), "Power on the anchor should pause the linked ritual");
            anchor.refreshNow();
            helper.assertTrue(aura(helper) == AnchorAura.DORMANT, "A paused ritual should leave the anchor dark");
            helper.setBlock(ANCHOR_POS.east(), Blocks.AIR);
            helper.setBlock(MRS_POS.above(), Blocks.REDSTONE_BLOCK);
            anchor.refreshNow();
            helper.assertTrue(mrs.isSuspended(), "Power on the stone should still pause its ritual");
            helper.assertTrue(aura(helper) == AnchorAura.DORMANT, "An anchor whose only ritual is powered off should be dark");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 40)
    public void ritualAnchorBreakingAnchorStopsRitual(GameTestHelper helper) {
        MasterRitualStoneBlockEntity mrs = activate(helper, NVRituals.CELERITY.get());
        placeAnchor(helper);
        mrs.linkAnchor(helper.absolutePos(ANCHOR_POS));
        helper.runAfterDelay(2, () -> {
            helper.getLevel().destroyBlock(helper.absolutePos(ANCHOR_POS), false);
            helper.assertTrue(!mrs.isActive(), "Breaking the anchor should stop its rituals");
            helper.assertTrue(mrs.getAnchorPos() == null, "Breaking the anchor should unlink the stone");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void ritualAnchorDungeonRitualsIgnoreAnchor(GameTestHelper helper) {
        helper.assertTrue(!NVRituals.SIMPLE_DUNGEON.get().canUseAnchor(), "Dungeon rituals should not use an anchor");
        helper.assertTrue(NVRituals.CELERITY.get().canUseAnchor(), "Ordinary rituals should use an anchor");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void ritualAnchorAuraFollowsDrainedSpiritus(GameTestHelper helper) {
        helper.assertTrue(AnchorAura.fromMask(false, 0) == AnchorAura.DORMANT, "No running ritual means dormant");
        helper.assertTrue(AnchorAura.fromMask(true, 0) == AnchorAura.BLOOD, "No Spiritus means blood red");
        helper.assertTrue(AnchorAura.fromMask(true, 1 << SpiritusType.RUINA.ordinal()) == AnchorAura.RUINA,
                "One Spiritus type should show that type");
        helper.assertTrue(AnchorAura.fromMask(true, (1 << SpiritusType.RAW.ordinal()) | (1 << SpiritusType.NIHILUM.ordinal()))
                == AnchorAura.BLOOD, "Several Spiritus types should show blood red");

        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        WorldSpiritusHandler.addSpiritusToChunk(helper.getLevel(), pos, SpiritusType.VINDICTA, 10);
        RitualSpiritusTracker.begin();
        WorldSpiritusHandler.drainSpiritusFromChunk(helper.getLevel(), pos, SpiritusType.VINDICTA, 1);
        int mask = RitualSpiritusTracker.end();
        helper.assertTrue(mask == 1 << SpiritusType.VINDICTA.ordinal(),
                "Draining during a ritual should record the Spiritus type, got mask " + mask);
        WorldSpiritusHandler.drainSpiritusFromChunk(helper.getLevel(), pos, SpiritusType.VINDICTA, 1);
        helper.assertTrue(RitualSpiritusTracker.end() == 0, "Drains outside a ritual should not be recorded");
        helper.succeed();
    }


    private static AnchorAura aura(GameTestHelper helper) {
        return helper.getBlockState(ANCHOR_POS).getValue(RitualAnchorBlock.AURA);
    }

    private static RitualAnchorBlockEntity placeAnchor(GameTestHelper helper) {
        helper.setBlock(ANCHOR_POS.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(ANCHOR_POS, NVBlocks.RITUAL_ANCHOR.block().get().defaultBlockState());
        return ((RitualAnchorBlockEntity) helper.getBlockEntity(ANCHOR_POS));
    }

    private static MasterRitualStoneBlockEntity activate(GameTestHelper helper, Ritual ritual) {
        MasterRitualStoneBlockEntity mrs = buildLayout(helper, ritual);
        startRitual(mrs, ritual);
        return mrs;
    }

    private static void startRitual(MasterRitualStoneBlockEntity mrs, Ritual ritual) {
        Anima network = AnimaHelper.getAnima(OWNER);
        if (network.getCurrentEV() < 1000000) {
            network.add(AnimaTicket.create(1000000), 10000000);
        }
        mrs.forceActivateRitual(ritual, null);
        mrs.setOwner(OWNER);
    }

    private static MasterRitualStoneBlockEntity buildLayout(GameTestHelper helper, Ritual ritual) {
        helper.setBlock(MRS_POS.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(MRS_POS, NVBlocks.MASTER_RITUAL_STONE.block().get().defaultBlockState());
        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);
        for (RitualComponent component : components) {
            helper.setBlock(MRS_POS.offset(component.offset()), runeBlock(component.runeType()).defaultBlockState());
        }
        return ((MasterRitualStoneBlockEntity) helper.getBlockEntity(MRS_POS));
    }

    private static HellfireForgeBlockEntity placeForge(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, NVBlocks.HELLFIRE_FORGE.block().get().defaultBlockState());
        return ((HellfireForgeBlockEntity) helper.getBlockEntity(pos));
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
}
