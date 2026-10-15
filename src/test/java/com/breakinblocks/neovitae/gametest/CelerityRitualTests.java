package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.block.SanguineCrafterBlock;
import com.breakinblocks.neovitae.common.blockentity.HellfireForgeBlockEntity;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity.Mode;
import com.breakinblocks.neovitae.common.blockentity.TabulaVitaeBlockEntity;
import com.breakinblocks.neovitae.common.datacomponent.Anima;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.ritual.EnumRuneType;
import com.breakinblocks.neovitae.ritual.NVRituals;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.breakinblocks.neovitae.ritual.RitualComponent;
import com.breakinblocks.neovitae.ritual.RitualLayouts;
import com.breakinblocks.neovitae.ritual.RitualRegistry;
import com.breakinblocks.neovitae.ritual.types.RitualCelerity;
import com.breakinblocks.neovitae.ritual.types.RitualDeusCelerity;
import com.breakinblocks.neovitae.util.helper.AnimaHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class CelerityRitualTests {


    private static final BlockPos MRS_POS = new BlockPos(12, 1, 12);
    private static final UUID RITUAL_OWNER = UUID.fromString("e2b7c4a1-6d93-4f58-8a0e-3c71d5f9b246");
    private static final UUID ORB_OWNER = UUID.fromString("9a4f1e7c-2b85-4d36-b0c9-6e13a8d27f54");

    @GameTest(template = "empty_24x5x24", timeoutTicks = 40)
    public void celerityLayoutsActivateTheirOwnRitual(GameTestHelper helper) {
        for (Ritual ritual : List.of(NVRituals.CELERITY.get(), NVRituals.DEUS_CELERITY.get())) {
            MasterRitualStoneBlockEntity mrs = buildLayout(helper, MRS_POS, ritual);
            Ritual best = null;
            int bestSize = 0;
            for (Ritual candidate : RitualRegistry.getAllRituals()) {
                if (mrs.checkStructure(candidate)) {
                    int size = RitualLayouts.get(helper.getLevel(), candidate).size();
                    if (size > bestSize) {
                        bestSize = size;
                        best = candidate;
                    }
                }
            }
            helper.assertTrue(best != null && best.getName().equals(ritual.getName()),
                    ritual.getName() + " layout activated " + (best == null ? "nothing" : best.getName()));
        }
        helper.succeed();
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 60)
    public void celeritySpeedsMachinesInRangeOnly(GameTestHelper helper) {
        MasterRitualStoneBlockEntity mrs = activate(helper, MRS_POS, NVRituals.CELERITY.get());
        HellfireForgeBlockEntity inside = placeForge(helper, MRS_POS.offset(2, 1, 0));
        HellfireForgeBlockEntity outside = placeForge(helper, MRS_POS.offset(7, 1, 0));
        helper.runAfterDelay(2, () -> {
            mrs.getCurrentRitual().performRitual(mrs);
            helper.assertTrue(inside.getRitualAcceleration().factor(helper.getLevel()) == RitualCelerity.SPEED,
                    "A forge in range should work at x" + RitualCelerity.SPEED);
            helper.assertTrue(outside.getRitualAcceleration().factor(helper.getLevel()) == 1,
                    "A forge out of range should not be sped up");
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 60)
    public void celerityStrongestRitualWins(GameTestHelper helper) {
        BlockPos deusPos = new BlockPos(8, 1, 12);
        BlockPos basePos = new BlockPos(19, 1, 12);
        MasterRitualStoneBlockEntity deus = activate(helper, deusPos, NVRituals.DEUS_CELERITY.get());
        MasterRitualStoneBlockEntity base = activate(helper, basePos, NVRituals.CELERITY.get());
        HellfireForgeBlockEntity forge = placeForge(helper, new BlockPos(16, 2, 12));
        helper.runAfterDelay(2, () -> {
            deus.getCurrentRitual().performRitual(deus);
            base.getCurrentRitual().performRitual(base);
            helper.assertTrue(forge.getRitualAcceleration().factor(helper.getLevel()) == RitualDeusCelerity.SPEED,
                    "The Deus ritual should win over the base ritual, got x" + forge.getRitualAcceleration().factor(helper.getLevel()));
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 60)
    public void celerityChargesOnlyForWorkingMachines(GameTestHelper helper) {
        MasterRitualStoneBlockEntity mrs = activate(helper, MRS_POS, NVRituals.CELERITY.get());
        HellfireForgeBlockEntity working = placeForge(helper, MRS_POS.offset(2, 1, 0));
        placeForge(helper, MRS_POS.offset(-2, 1, 0));
        placeForge(helper, MRS_POS.offset(0, 1, 2));
        helper.runAfterDelay(2, () -> {
            working.getRitualAcceleration().markWorked(helper.getLevel());
            Anima network = AnimaHelper.getAnima(RITUAL_OWNER);
            int before = network.getCurrentEV();
            mrs.getCurrentRitual().performRitual(mrs);
            int spent = before - network.getCurrentEV();
            int cost = mrs.getCurrentRitual().getRefreshCost();
            helper.assertTrue(spent == cost, "One working machine should cost " + cost + " EV, spent " + spent);
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 80)
    public void celerityDeusForgeCraftsTenTimesFaster(GameTestHelper helper) {
        MasterRitualStoneBlockEntity mrs = activate(helper, MRS_POS, NVRituals.DEUS_CELERITY.get());
        HellfireForgeBlockEntity forge = placeForge(helper, MRS_POS.offset(2, 1, 0));
        helper.runAfterDelay(2, () -> {
            mrs.getCurrentRitual().performRitual(mrs);
            forge.inv.setStackInSlot(0, new ItemStack(Items.REDSTONE));
            forge.inv.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT));
            forge.inv.setStackInSlot(2, new ItemStack(Items.GLASS));
            forge.inv.setStackInSlot(3, new ItemStack(Items.LAPIS_LAZULI));
            ItemStack gem = new ItemStack(NVItems.SPIRITUS_GEM_PETTY.get());
            gem.set(NVDataComponents.SPIRITUS_AMOUNT, 10.0);
            forge.inv.setStackInSlot(HellfireForgeBlockEntity.GEM_SLOT, gem);
            helper.runAfterDelay(15, () -> {
                helper.assertTrue(forge.inv.getStackInSlot(HellfireForgeBlockEntity.OUTPUT_SLOT).is(NVItems.SPIRITUS_GEM_PETTY.get()),
                        "A 100 tick forge craft should finish within 15 ticks at x10, progress " + forge.getProgress());
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 80)
    public void celerityTabulaCostsTheSameEvWhenSpedUp(GameTestHelper helper) {
        MasterRitualStoneBlockEntity mrs = activate(helper, MRS_POS, NVRituals.DEUS_CELERITY.get());
        BlockPos tablePos = MRS_POS.offset(2, 1, 0);
        helper.setBlock(tablePos, NVBlocks.TABULA_VITAE.block().get().defaultBlockState());
        TabulaVitaeBlockEntity table = ((TabulaVitaeBlockEntity) helper.getBlockEntity(tablePos));
        Anima orbNetwork = AnimaHelper.getAnima(ORB_OWNER);
        helper.runAfterDelay(2, () -> {
            mrs.getCurrentRitual().performRitual(mrs);
            while (orbNetwork.getCurrentEV() > 0) {
                orbNetwork.syphon(AnimaTicket.create(orbNetwork.getCurrentEV()));
            }
            orbNetwork.add(AnimaTicket.create(10000), 100000);
            ItemStack orb = new ItemStack(NVItems.ORB_MAGICIAN.get());
            orb.set(NVDataComponents.BINDING, new Binding(ORB_OWNER, "CelerityOrbOwner"));
            table.inv.setStackInSlot(0, new ItemStack(Items.GRAVEL));
            table.inv.setStackInSlot(1, new ItemStack(Items.FLINT));
            table.inv.setStackInSlot(TabulaVitaeBlockEntity.ORB_SLOT, orb);
            helper.succeedWhen(() -> {
                helper.assertTrue(!table.inv.getStackInSlot(TabulaVitaeBlockEntity.OUTPUT_SLOT).isEmpty(), "Table should craft");
                int spent = 10000 - orbNetwork.getCurrentEV();
                helper.assertTrue(spent == 50, "A sped up 50 EV recipe should still cost 50 EV, cost " + spent);
            });
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 80)
    public void celeritySanguineCrafterFinishesSeveralRoundsATick(GameTestHelper helper) {
        MasterRitualStoneBlockEntity mrs = activate(helper, MRS_POS, NVRituals.DEUS_CELERITY.get());
        SanguineCrafterBlockEntity crafter = buildCrafter(helper, new BlockPos(4, 3, 4));
        helper.runAfterDelay(2, () -> {
            mrs.getCurrentRitual().performRitual(mrs);
            List<ItemStack> template = new ArrayList<>(Collections.nCopies(SanguineCrafterBlockEntity.TEMPLATE_SIZE, ItemStack.EMPTY));
            template.set(0, new ItemStack(Items.OAK_PLANKS));
            template.set(3, new ItemStack(Items.OAK_PLANKS));
            crafter.setTemplate(Mode.CRAFTING, template);
            crafter.inv.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 16));
            helper.runAfterDelay(5, () -> {
                int sticks = 0;
                for (int i = 0; i < SanguineCrafterBlockEntity.OUTPUT_COUNT; i++) {
                    ItemStack stack = crafter.inv.getStackInSlot(SanguineCrafterBlockEntity.OUTPUT_START + i);
                    if (stack.is(Items.STICK)) sticks += stack.getCount();
                }
                helper.assertTrue(sticks == 32, "Eight crafts should finish within 5 ticks at x10, got " + sticks + " sticks, status " + crafter.getStatus());
                helper.assertTrue(crafter.getEffectiveSpeedMultiplier() == crafter.getSpeedMultiplier() * RitualDeusCelerity.SPEED,
                        "The crafter should report the ritual's speed");
                helper.succeed();
            });
        });
    }


    private static MasterRitualStoneBlockEntity activate(GameTestHelper helper, BlockPos pos, Ritual ritual) {
        MasterRitualStoneBlockEntity mrs = buildLayout(helper, pos, ritual);
        Anima network = AnimaHelper.getAnima(RITUAL_OWNER);
        if (network.getCurrentEV() < 1000000) {
            network.add(AnimaTicket.create(1000000), 10000000);
        }
        mrs.forceActivateRitual(ritual, null);
        mrs.setOwner(RITUAL_OWNER);
        return mrs;
    }

    private static MasterRitualStoneBlockEntity buildLayout(GameTestHelper helper, BlockPos pos, Ritual ritual) {
        helper.setBlock(pos.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(pos, NVBlocks.MASTER_RITUAL_STONE.block().get().defaultBlockState());
        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);
        for (RitualComponent component : components) {
            helper.setBlock(pos.offset(component.offset()), runeBlock(component.runeType()).defaultBlockState());
        }
        return ((MasterRitualStoneBlockEntity) helper.getBlockEntity(pos));
    }

    private static HellfireForgeBlockEntity placeForge(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, NVBlocks.HELLFIRE_FORGE.block().get().defaultBlockState());
        return ((HellfireForgeBlockEntity) helper.getBlockEntity(pos));
    }

    private static SanguineCrafterBlockEntity buildCrafter(GameTestHelper helper, BlockPos centerRel) {
        BlockPos center = helper.absolutePos(centerRel);
        BlockPos controller = helper.absolutePos(centerRel.north());
        for (int dy = -1; dy <= 0; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (pos.equals(center) || pos.equals(controller)) continue;
                    helper.getLevel().setBlock(pos, NVBlocks.SANGUINE_CASING.block().get().defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        BlockState state = NVBlocks.SANGUINE_CRAFTER.block().get().defaultBlockState().setValue(SanguineCrafterBlock.FACING, Direction.NORTH);
        helper.getLevel().setBlock(controller, state, Block.UPDATE_ALL);
        SanguineCrafterBlockEntity crafter = (SanguineCrafterBlockEntity) helper.getLevel().getBlockEntity(controller);
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        AnimaHelper.getAnima(owner.getUUID()).add(AnimaTicket.create(10000), 100000);
        crafter.setOwner(owner);
        return crafter;
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
