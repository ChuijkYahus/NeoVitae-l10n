package com.breakinblocks.neovitae.gametest;

import com.breakinblocks.neovitae.common.item.NVItems;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("neovitae")
@PrefixGameTestTemplate(false)
public class SentientScytheEnchantTests {

    private static final List<ResourceKey<Enchantment>> WEAPON_ENCHANTS = List.of(
            Enchantments.SHARPNESS,
            Enchantments.SMITE,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.LOOTING,
            Enchantments.FIRE_ASPECT,
            Enchantments.KNOCKBACK,
            Enchantments.SWEEPING_EDGE,
            Enchantments.UNBREAKING,
            Enchantments.MENDING);

    private static final List<ResourceKey<Enchantment>> TABLE_ENCHANTS = List.of(
            Enchantments.SHARPNESS,
            Enchantments.SMITE,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.FIRE_ASPECT);

    @GameTest(template = "empty_5x5x7", timeoutTicks = 20)
    public void acceptsWeaponEnchantments(GameTestHelper helper) {
        ItemStack scythe = new ItemStack(NVItems.SENTIENT_SCYTHE.get());
        var lookup = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (ResourceKey<Enchantment> key : WEAPON_ENCHANTS) {
            Holder<Enchantment> enchantment = lookup.getOrThrow(key);
            helper.assertTrue(enchantment.value().isSupportedItem(scythe),
                    "Sentient Scythe should accept " + key.location());
        }
        for (ResourceKey<Enchantment> key : TABLE_ENCHANTS) {
            Holder<Enchantment> enchantment = lookup.getOrThrow(key);
            helper.assertTrue(enchantment.value().isPrimaryItem(scythe),
                    "Sentient Scythe should roll " + key.location() + " at the enchanting table");
        }
        helper.succeed();
    }

}
