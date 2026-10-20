package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import com.breakinblocks.neovitae.api.routing.IFilterKey;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.common.routing.FilterMatchRules;
import com.breakinblocks.neovitae.common.routing.FilterMatchRules.ItemProperty;

import java.util.Set;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class RoutingFilterRuleTests {

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void plainSlotMatchesExactItem(GameTestHelper helper) {
        IFilterKey key = FilterMatchRules.buildKey(new ItemStack(Items.STONE), Set.of(), 1);
        helper.assertTrue(key.doesStackMatch(new ItemStack(Items.STONE)), "Stone should match stone");
        helper.assertTrue(!key.doesStackMatch(new ItemStack(Items.DIRT)), "Dirt should not match stone");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void anyItemNonStackable(GameTestHelper helper) {
        IFilterKey key = FilterMatchRules.buildKey(new ItemStack(Items.STONE),
                Set.of(FilterMatchRules.ANY_ITEM, ItemProperty.NON_STACKABLE.id()), 1);
        helper.assertTrue(key.doesStackMatch(new ItemStack(Items.DIAMOND_SWORD)), "A sword is non-stackable");
        helper.assertTrue(!key.doesStackMatch(new ItemStack(Items.COBBLESTONE)), "Cobblestone stacks");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void tagRuleMatchesTagMembers(GameTestHelper helper) {
        IFilterKey key = FilterMatchRules.buildKey(new ItemStack(Items.OAK_LOG),
                Set.of(FilterMatchRules.tagRule(ItemTags.LOGS)), 1);
        helper.assertTrue(key.doesStackMatch(new ItemStack(Items.BIRCH_LOG)), "Birch log is in #minecraft:logs");
        helper.assertTrue(!key.doesStackMatch(new ItemStack(Items.STONE)), "Stone is not a log");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void sameModMatchesNamespace(GameTestHelper helper) {
        IFilterKey key = FilterMatchRules.buildKey(new ItemStack(Items.STONE), Set.of(FilterMatchRules.SAME_MOD), 1);
        helper.assertTrue(key.doesStackMatch(new ItemStack(Items.DIRT)), "Dirt is from minecraft");
        helper.assertTrue(!key.doesStackMatch(new ItemStack(NVItems.LEX_VITAE.get())), "The Lex Vitae is not from minecraft");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void tagRuleRoundTrips(GameTestHelper helper) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/iron"));
        helper.assertTrue(FilterMatchRules.tagFromRule(FilterMatchRules.tagRule(tag)).orElse(null) != null
                        && FilterMatchRules.tagFromRule(FilterMatchRules.tagRule(tag)).get().equals(tag),
                "A nested tag path should survive the rule encoding");
        helper.succeed();
    }
}
