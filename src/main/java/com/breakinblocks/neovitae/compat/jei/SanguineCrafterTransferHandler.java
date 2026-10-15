package com.breakinblocks.neovitae.compat.jei;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity;
import com.breakinblocks.neovitae.common.menu.NVMenus;
import com.breakinblocks.neovitae.common.menu.SanguineCrafterMenu;
import com.breakinblocks.neovitae.common.network.SanguineCrafterTemplatePayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SanguineCrafterTransferHandler<R> implements IRecipeTransferHandler<SanguineCrafterMenu, R> {

    private final RecipeType<R> recipeType;
    private final SanguineCrafterBlockEntity.Mode mode;

    public SanguineCrafterTransferHandler(RecipeType<R> recipeType, SanguineCrafterBlockEntity.Mode mode) {
        this.recipeType = recipeType;
        this.mode = mode;
    }

    @Override
    public Class<? extends SanguineCrafterMenu> getContainerClass() {
        return SanguineCrafterMenu.class;
    }

    @Override
    public Optional<MenuType<SanguineCrafterMenu>> getMenuType() {
        return Optional.of(NVMenus.SANGUINE_CRAFTER.get());
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(SanguineCrafterMenu container, R recipe, IRecipeSlotsView recipeSlots,
                                                         Player player, boolean maxTransfer, boolean doTransfer) {
        if (!doTransfer) {
            return null;
        }
        List<ItemStack> stacks = new ArrayList<>(SanguineCrafterBlockEntity.TEMPLATE_SIZE);
        for (IRecipeSlotView view : recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)) {
            if (stacks.size() >= SanguineCrafterBlockEntity.TEMPLATE_SIZE) break;
            stacks.add(view.getDisplayedItemStack().map(stack -> stack.copyWithCount(1)).orElse(ItemStack.EMPTY));
        }
        while (stacks.size() < SanguineCrafterBlockEntity.TEMPLATE_SIZE) {
            stacks.add(ItemStack.EMPTY);
        }
        SanguineCrafterBlockEntity tile = container.tile;
        tile.setTemplate(mode, stacks);
        PacketDistributor.sendToServer(new SanguineCrafterTemplatePayload(tile.getBlockPos(), mode.ordinal(), stacks));
        return null;
    }
}
