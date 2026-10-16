package com.breakinblocks.neovitae.datagen.content.loot;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.block.BlockShapedExplosive;
import com.breakinblocks.neovitae.common.block.BlockTau;
import com.breakinblocks.neovitae.common.block.dungeon.DungeonBlocks;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.util.helper.BlockWithItemHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MineBlock extends BlockLootSubProvider {
    private final HolderLookup.Provider registries;

    public MineBlock(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, registries);
        this.registries = registries;
        NVBlocks.BASIC_BLOCKS.getEntries().forEach(holder -> dropSelfList.add(holder.get()));

        Set<Block> skipAuto = new HashSet<>();
        skipAuto.addAll(specialDropList);
        skipAuto.addAll(inscribedRitualStones);
        skipAuto.add(NVBlocks.DUNGEON_CONTROLLER.block().get());
        skipAuto.add(NVBlocks.DUNGEON_SEAL.block().get());
        skipAuto.add(NVBlocks.DUNGEON_SEAL_INACCESSIBLE.block().get());
        skipAuto.add(NVBlocks.SPATIAL_RIFT.block().get());
        skipAuto.add(NVBlocks.ALCHEMY_ARRAY.get());
        skipAuto.add(NVBlocks.BLOOD_LIGHT.get());
        skipAuto.add(NVBlocks.SPECTRAL_BLOCK.get());
        skipAuto.add(NVBlocks.PHANTOM_BRIDGE_BLOCK.get());

        NVBlocks.BLOCKS.getEntries().forEach(holder -> {
            Block block = holder.get();
            if (skipAuto.contains(block) || block instanceof BlockShapedExplosive) return;
            dropSelfList.add(block);
        });
        dropSelfList.add(DungeonBlocks.SPIKE_TRAP.block().get());
    }

    private final List<Block> inscribedRitualStones = List.of(
            NVBlocks.AIR_RITUAL_STONE.block().get(),
            NVBlocks.WATER_RITUAL_STONE.block().get(),
            NVBlocks.FIRE_RITUAL_STONE.block().get(),
            NVBlocks.EARTH_RITUAL_STONE.block().get(),
            NVBlocks.TENEBRAE_RITUAL_STONE.block().get(),
            NVBlocks.DEUS_RITUAL_STONE.block().get()
    );

    private final List<Block> specialDropList = List.of(
            NVBlocks.BLOOD_TANK.block().get(),
            NVBlocks.ATHANOR_BLOCK.block().get(),
            NVBlocks.DEMON_LANTERN.block().get(),
            NVBlocks.WEAK_TAU.block().get(),
            NVBlocks.STRONG_TAU.block().get(),
            NVBlocks.INCENSE_ALTAR.block().get(),
            NVBlocks.SPIRIT_CACHE.block().get(),
            NVBlocks.BLOOD_STAINED_GLASS.block().get(),
            NVBlocks.BLOOD_STAINED_GLASS_PANE.block().get(),
            DungeonBlocks.DUNGEON_ORE.block().get()
    );
    private List<Block> dropSelfList = new ArrayList<>();

    @Override
    protected Iterable<Block> getKnownBlocks() {
        List<Block> list = new ArrayList<>();
        list.addAll(specialDropList);
        list.addAll(inscribedRitualStones);
        list.addAll(dropSelfList);
        return list;
    }

    @Override
    protected void generate() {
        dropSelfList.forEach(this::dropSelf);

        inscribedRitualStones.forEach(block -> dropOther(block, NVBlocks.BLANK_RITUAL_STONE.item().get()));

        // Blocks that preserve their contents when broken
        copyComponents(NVBlocks.BLOOD_TANK);
        copyComponents(NVBlocks.ATHANOR_BLOCK);
        copyComponents(NVBlocks.DEMON_LANTERN);
        copyComponents(NVBlocks.SPIRIT_ACCUMULATOR);

        // Tau crops - drop 1 seed always, plus bonus seeds at max age with fortune
        generateTauLoot(NVBlocks.WEAK_TAU);
        generateTauLoot(NVBlocks.STRONG_TAU);

        // Incense Altar - simple drop self
        dropSelf(NVBlocks.INCENSE_ALTAR.block().get());

        // Spirit Cache
        dropSelf(NVBlocks.SPIRIT_CACHE.block().get());

        // Glass blocks - silk touch only
        dropWhenSilkTouch(NVBlocks.BLOOD_STAINED_GLASS.block().get());
        dropWhenSilkTouch(NVBlocks.BLOOD_STAINED_GLASS_PANE.block().get());

        add(DungeonBlocks.DUNGEON_ORE.block().get(),
                createOreDrop(DungeonBlocks.DUNGEON_ORE.block().get(), NVItems.DEMONITE_RAW.get()));
    }

    private void generateTauLoot(BlockWithItemHolder<BlockTau, BlockItem> holder) {
        BlockTau block = holder.block().get();
        HolderLookup.RegistryLookup<Enchantment> enchantmentLookup = registries.lookupOrThrow(Registries.ENCHANTMENT);

        // Drop 1 seed always, plus 0-3 bonus seeds at max age with fortune
        add(block, LootTable.lootTable()
                // Always drop 1 seed
                .withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(holder.item().get()))
                ))
                // Bonus seeds at max age with fortune
                .withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(holder.item().get())
                                .apply(ApplyBonusCount.addBonusBinomialDistributionCount(
                                        enchantmentLookup.getOrThrow(Enchantments.FORTUNE), 0.5714286F, 3)))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(CropBlock.AGE, 7)))
                ))
        );
    }

    private void copyComponents(BlockWithItemHolder<? extends Block, ? extends BlockItem> holder) {
        add(
                holder.block().get(),
                LootTable.lootTable().withPool(
                        this.applyExplosionCondition(holder.block().get(), LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(holder)
                                        .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY))
                                )
                        )
                )
        );
    }
}
