package com.breakinblocks.neovitae.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.common.menu.RitualLedgerMenu;
import com.breakinblocks.neovitae.common.world.ActiveRituals;
import com.breakinblocks.neovitae.common.world.ActiveRituals.LedgerEntry;

import java.util.List;

public class ItemRitualLedger extends Item {

    private static final int USE_COOLDOWN = 20;

    public ItemRitualLedger() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            List<LedgerEntry> entries = ActiveRituals.collect(serverPlayer.server, serverPlayer.getUUID());
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new RitualLedgerMenu(id, inv, hand, entries),
                    Component.translatable("container.neovitae.ritual_ledger")
            ), buf -> RitualLedgerMenu.write(buf, hand, entries));
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.0f);
            player.getCooldowns().addCooldown(this, USE_COOLDOWN);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.neovitae.ritual_ledger").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
