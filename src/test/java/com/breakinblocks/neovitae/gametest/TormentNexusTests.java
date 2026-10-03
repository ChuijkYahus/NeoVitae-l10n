package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.common.datacomponent.Anima;
import com.breakinblocks.neovitae.compat.arsnouveau.ArsNouveauCompat;
import com.breakinblocks.neovitae.ritual.EnumRuneType;
import com.breakinblocks.neovitae.ritual.NVRituals;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.breakinblocks.neovitae.ritual.RitualComponent;
import com.breakinblocks.neovitae.util.helper.AnimaHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class TormentNexusTests {

    private static final int FUNDED_EV = 1_000_000;

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

    @GameTest(template = "empty_24x5x24", timeoutTicks = 100)
    public void tormentNexusRunsWithoutArsNouveau(GameTestHelper helper) {
        if (ModList.get().isLoaded("ars_nouveau")) {
            helper.fail("This test checks the Torment Nexus without Ars Nouveau, but Ars Nouveau is loaded");
            return;
        }

        Ritual ritual = NVRituals.TORMENT_NEXUS.get();
        BlockPos mrsPos = new BlockPos(12, 2, 12);
        helper.setBlock(mrsPos, NVBlocks.MASTER_RITUAL_STONE.block().get().defaultBlockState());
        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);
        for (RitualComponent component : components) {
            helper.setBlock(mrsPos.offset(component.offset()), runeBlock(component.runeType()).defaultBlockState());
        }

        BlockPos spawnerPos = mrsPos.east(4);
        helper.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState());
        if (helper.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(EntityType.ZOMBIE, helper.getLevel().getRandom());
        } else {
            helper.fail("Expected a spawner block entity at " + spawnerPos);
            return;
        }

        BlockPos decoyPos = mrsPos.west(4);
        helper.setBlock(decoyPos, Blocks.CHEST.defaultBlockState());
        BlockEntity decoy = helper.getBlockEntity(decoyPos);
        if (ArsNouveauCompat.getJarEntityType(decoy) != null) {
            helper.fail("A chest must not be read as an Ars Nouveau containment jar");
            return;
        }

        if (!(helper.getBlockEntity(mrsPos) instanceof MasterRitualStoneBlockEntity mrs)) {
            helper.fail("Expected a Master Ritual Stone at " + mrsPos);
            return;
        }
        UUID owner = UUID.randomUUID();
        Anima anima = AnimaHelper.getAnima(owner);
        anima.add(AnimaTicket.create(FUNDED_EV), FUNDED_EV * 10);
        mrs.forceActivateRitual(ritual, null);
        mrs.setOwner(owner);

        for (int i = 0; i < 30; i++) {
            mrs.performRitual();
        }

        if (!mrs.isActive()) {
            helper.fail("The Torment Nexus stopped while running without Ars Nouveau");
            return;
        }
        if (anima.getCurrentEV() >= FUNDED_EV) {
            helper.fail("The Torment Nexus should have simulated spawner kills and charged EV, but the network is still at "
                    + anima.getCurrentEV());
            return;
        }
        helper.succeed();
    }
}
