package com.breakinblocks.neovitae.common.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.item.soul.LexVitaeGrowth;
import com.breakinblocks.neovitae.common.item.soul.LexVitaeItem;

@EventBusSubscriber(modid = NeoVitae.MODID)
public final class LexVitaeGrowthHandler {

    private LexVitaeGrowthHandler() {}

    @SubscribeEvent
    public static void onGetEnchantmentLevel(GetEnchantmentLevelEvent event) {
        if (!event.isTargetting(Enchantments.FORTUNE)) return;
        if (!(event.getStack() instanceof ItemStack stack)) return;
        if (!(stack.getItem() instanceof LexVitaeItem) || !LexVitaeItem.isActive(stack)) return;
        int bonus = LexVitaeGrowth.fortuneBonus(LexVitaeGrowth.getLevel(stack));
        if (bonus <= 0) return;
        event.getHolder(Enchantments.FORTUNE).ifPresent(fortune ->
                event.getEnchantments().set(fortune, event.getEnchantments().getLevel(fortune) + bonus));
    }
}
