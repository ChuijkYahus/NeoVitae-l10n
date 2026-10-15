package com.breakinblocks.neovitae.compat.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.client.screen.SanguineCrafterScreen;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity;
import com.breakinblocks.neovitae.common.menu.SanguineCrafterMenu;

import java.util.ArrayList;
import java.util.List;

public class SanguineCrafterGhostHandler implements IGhostIngredientHandler<SanguineCrafterScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(SanguineCrafterScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();
        if (!(ingredient.getIngredient() instanceof ItemStack)) return targets;

        SanguineCrafterMenu menu = screen.getMenu();
        for (int i = 0; i < SanguineCrafterBlockEntity.TEMPLATE_SIZE; i++) {
            final int templateSlot = i;
            Slot slot = menu.slots.get(SanguineCrafterMenu.TEMPLATE_START + i);
            Rect2i area = new Rect2i(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16);
            targets.add(new Target<>() {
                @Override
                public Rect2i getArea() {
                    return area;
                }

                @Override
                public void accept(I value) {
                    if (value instanceof ItemStack stack) {
                        screen.setTemplateSlotFromJei(templateSlot, stack);
                    }
                }
            });
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }
}
