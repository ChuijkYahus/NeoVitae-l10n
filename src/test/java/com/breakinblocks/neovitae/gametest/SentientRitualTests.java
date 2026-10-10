package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.common.datacomponent.Anima;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.datacomponent.SentientStats;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.common.registry.NVRegistries;
import com.breakinblocks.neovitae.common.sentient.SentientHelper;
import com.breakinblocks.neovitae.common.sentient.SentientUpgrade;
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
public class SentientRitualTests {

    private static MasterRitualStoneBlockEntity placeMrs(GameTestHelper helper, BlockPos pos, UUID owner, Ritual ritual) {
        helper.setBlock(pos.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(pos, NVBlocks.MASTER_RITUAL_STONE.block().get().defaultBlockState());
        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);
        for (RitualComponent component : components) {
            helper.setBlock(pos.offset(component.offset()), runeBlock(component.runeType()).defaultBlockState());
        }
        BlockEntity be = helper.getBlockEntity(pos);
        if (!(be instanceof MasterRitualStoneBlockEntity mrs)) {
            helper.fail("Expected MasterRitualStoneBlockEntity at " + pos);
            return null;
        }
        Anima anima = AnimaHelper.getAnima(owner);
        anima.add(AnimaTicket.create(1000000), 10000000);
        mrs.setOwner(owner);
        return mrs;
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

    private static Player standingWearer(GameTestHelper helper, BlockPos mrsPos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(NVItems.SENTIENT_PLATE.get()));
        Vec3 standAt = helper.absoluteVec(new Vec3(mrsPos.getX() + 0.5, mrsPos.getY() + 1, mrsPos.getZ() + 0.5));
        player.setPos(standAt.x, standAt.y, standAt.z);
        helper.getLevel().addFreshEntity(player);
        return player;
    }

    private static Player standingFullSetWearer(GameTestHelper helper, BlockPos mrsPos, int maxPoints) {
        Player player = standingWearer(helper, mrsPos);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        SentientHelper.setDefaultSentient(chest, helper.getLevel().registryAccess());
        chest.set(NVDataComponents.CURRENT_MAX_UPGRADE_POINTS, maxPoints);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(NVItems.SENTIENT_HELMET.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(NVItems.SENTIENT_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(NVItems.SENTIENT_BOOTS.get()));
        return player;
    }

    private static int mendedPieces(Player player) {
        int count = 0;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            if (player.getItemBySlot(slot).has(NVDataComponents.BLOOD_MENDING)) count++;
        }
        return count;
    }

    private static void throwCatalyst(GameTestHelper helper, Item item) {
        Vec3 at = helper.absoluteVec(new Vec3(10.5, 2.2, 10.5));
        ItemEntity entity = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(item));
        entity.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(entity);
    }

    private static Holder<SentientUpgrade> upgradeHolder(GameTestHelper helper, String path) {
        Registry<SentientUpgrade> registry = helper.getLevel().registryAccess().registryOrThrow(NVRegistries.Keys.SENTIENT_UPGRADES);
        return registry.getHolderOrThrow(ResourceKey.create(NVRegistries.Keys.SENTIENT_UPGRADES, NeoVitae.rl(path)));
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 200)
    public void penanceAppliesDowngradeFromThrownCatalyst(GameTestHelper helper) {
        BlockPos mrsPos = new BlockPos(12, 1, 12);
        UUID owner = UUID.randomUUID();
        MasterRitualStoneBlockEntity mrs = placeMrs(helper, mrsPos, owner, NVRituals.PENANCE.get());
        if (mrs == null) return;
        mrs.forceActivateRitual(NVRituals.PENANCE.get(), null);
        mrs.setOwner(owner);

        Player player = standingWearer(helper, mrsPos);
        throwCatalyst(helper, Items.GLASS_BOTTLE);

        helper.runAfterDelay(100, () -> {
            ItemStack worn = player.getItemBySlot(EquipmentSlot.CHEST);
            SentientStats stats = worn.get(NVDataComponents.UPGRADES);
            Holder<SentientUpgrade> quenched = upgradeHolder(helper, "quenched");
            if (stats == null || stats.upgrades().getOrDefault(quenched, 0f) < 1f) {
                helper.fail("Penance should inscribe Quenched from a thrown glass bottle, stats=" + stats
                        + " active=" + mrs.isActive());
                return;
            }
            Integer usedPoints = worn.get(NVDataComponents.CURRENT_UPGRADE_POINTS);
            if (usedPoints == null || usedPoints != -100) {
                helper.fail("Quenched should free 100 points (used=-100), got " + usedPoints);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 200)
    public void penanceIgnoresItemsWithoutRecipe(GameTestHelper helper) {
        BlockPos mrsPos = new BlockPos(12, 1, 12);
        UUID owner = UUID.randomUUID();
        MasterRitualStoneBlockEntity mrs = placeMrs(helper, mrsPos, owner, NVRituals.PENANCE.get());
        if (mrs == null) return;
        mrs.forceActivateRitual(NVRituals.PENANCE.get(), null);
        mrs.setOwner(owner);

        Player player = standingWearer(helper, mrsPos);
        throwCatalyst(helper, Items.COBBLESTONE);

        helper.runAfterDelay(60, () -> {
            ItemStack worn = player.getItemBySlot(EquipmentSlot.CHEST);
            SentientStats stats = worn.get(NVDataComponents.UPGRADES);
            if (stats != null && !stats.upgrades().isEmpty()) {
                helper.fail("A non-catalyst item should not inscribe anything, stats=" + stats);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 200)
    public void evolveAddsCapacityThenDeactivates(GameTestHelper helper) {
        BlockPos mrsPos = new BlockPos(12, 1, 12);
        UUID owner = UUID.randomUUID();
        MasterRitualStoneBlockEntity mrs = placeMrs(helper, mrsPos, owner, NVRituals.ARMOUR_EVOLVE.get());
        if (mrs == null) return;
        mrs.forceActivateRitual(NVRituals.ARMOUR_EVOLVE.get(), null);
        mrs.setOwner(owner);

        Player player = standingWearer(helper, mrsPos);

        helper.runAfterDelay(100, () -> {
            ItemStack worn = player.getItemBySlot(EquipmentSlot.CHEST);
            Integer maxPoints = worn.get(NVDataComponents.CURRENT_MAX_UPGRADE_POINTS);
            if (maxPoints == null || maxPoints != 200) {
                helper.fail("One evolution should raise capacity to 200, got " + maxPoints);
                return;
            }
            if (mrs.isActive()) {
                helper.fail("Evolution ritual should deactivate after evolving");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 200)
    public void evolveRefusesArmorAtMaxCapacity(GameTestHelper helper) {
        BlockPos mrsPos = new BlockPos(12, 1, 12);
        UUID owner = UUID.randomUUID();
        MasterRitualStoneBlockEntity mrs = placeMrs(helper, mrsPos, owner, NVRituals.ARMOUR_EVOLVE.get());
        if (mrs == null) return;
        mrs.forceActivateRitual(NVRituals.ARMOUR_EVOLVE.get(), null);
        mrs.setOwner(owner);

        Player player = standingWearer(helper, mrsPos);
        ItemStack worn = player.getItemBySlot(EquipmentSlot.CHEST);
        worn.set(NVDataComponents.CURRENT_MAX_UPGRADE_POINTS, 500);

        helper.runAfterDelay(100, () -> {
            Integer maxPoints = player.getItemBySlot(EquipmentSlot.CHEST).get(NVDataComponents.CURRENT_MAX_UPGRADE_POINTS);
            if (maxPoints == null || maxPoints != 500) {
                helper.fail("Capacity should stay at the 500 cap, got " + maxPoints);
                return;
            }
            if (mrs.isActive()) {
                helper.fail("Evolution ritual should deactivate when the armor is already at the cap");
                return;
            }
            helper.succeed();
        });

    }

    private static MasterRitualStoneBlockEntity activeMrs(GameTestHelper helper, BlockPos mrsPos, Ritual ritual) {
        UUID owner = UUID.randomUUID();
        MasterRitualStoneBlockEntity mrs = placeMrs(helper, mrsPos, owner, ritual);
        if (mrs == null) return null;
        mrs.forceActivateRitual(ritual, null);
        mrs.setOwner(owner);
        return mrs;
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 200)
    public void deusEvolveAscendsAndMendsEveryPiece(GameTestHelper helper) {
        BlockPos mrsPos = new BlockPos(12, 1, 12);
        MasterRitualStoneBlockEntity mrs = activeMrs(helper, mrsPos, NVRituals.DEUS_ARMOUR_EVOLVE.get());
        if (mrs == null) return;

        Player player = standingFullSetWearer(helper, mrsPos, 500);

        helper.runAfterDelay(100, () -> {
            Integer maxPoints = player.getItemBySlot(EquipmentSlot.CHEST).get(NVDataComponents.CURRENT_MAX_UPGRADE_POINTS);
            if (maxPoints == null || maxPoints != 600) {
                helper.fail("Deus evolution should raise capacity from 500 to 600, got " + maxPoints);
                return;
            }
            int mended = mendedPieces(player);
            if (mended != 4) {
                helper.fail("Deus evolution should bind Blood Mending into all four pieces, got " + mended);
                return;
            }
            if (mrs.isActive()) {
                helper.fail("Deus evolution should deactivate after evolving");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 200)
    public void deusEvolveRefusesArmorBelow500(GameTestHelper helper) {
        BlockPos mrsPos = new BlockPos(12, 1, 12);
        MasterRitualStoneBlockEntity mrs = activeMrs(helper, mrsPos, NVRituals.DEUS_ARMOUR_EVOLVE.get());
        if (mrs == null) return;

        Player player = standingFullSetWearer(helper, mrsPos, 400);

        helper.runAfterDelay(100, () -> {
            Integer maxPoints = player.getItemBySlot(EquipmentSlot.CHEST).get(NVDataComponents.CURRENT_MAX_UPGRADE_POINTS);
            if (maxPoints == null || maxPoints != 400) {
                helper.fail("Deus evolution should leave armor below 500 untouched, got " + maxPoints);
                return;
            }
            if (mendedPieces(player) != 0) {
                helper.fail("Deus evolution should not mend armor it refuses");
                return;
            }
            if (mrs.isActive()) {
                helper.fail("Deus evolution should deactivate when the armor is not ready");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_24x5x24", timeoutTicks = 200)
    public void evolveRefusesArmorAfterDeusAscension(GameTestHelper helper) {
        BlockPos mrsPos = new BlockPos(12, 1, 12);
        MasterRitualStoneBlockEntity mrs = activeMrs(helper, mrsPos, NVRituals.ARMOUR_EVOLVE.get());
        if (mrs == null) return;

        Player player = standingFullSetWearer(helper, mrsPos, 600);

        helper.runAfterDelay(100, () -> {
            Integer maxPoints = player.getItemBySlot(EquipmentSlot.CHEST).get(NVDataComponents.CURRENT_MAX_UPGRADE_POINTS);
            if (maxPoints == null || maxPoints != 600) {
                helper.fail("Sentient Evolution should not change ascended armor, got " + maxPoints);
                return;
            }
            helper.succeed();
        });
    }
}
