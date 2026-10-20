package com.breakinblocks.neovitae.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class SplitItemHandler implements IItemHandler {

    private final IItemHandler insertInto;
    private final IItemHandler extractFrom;

    public SplitItemHandler(IItemHandler insertInto, IItemHandler extractFrom) {
        this.insertInto = insertInto;
        this.extractFrom = extractFrom;
    }

    private boolean isInsertSlot(int slot) {
        return slot < insertInto.getSlots();
    }

    private int extractSlot(int slot) {
        return slot - insertInto.getSlots();
    }

    @Override
    public int getSlots() {
        return insertInto.getSlots() + extractFrom.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return isInsertSlot(slot) ? insertInto.getStackInSlot(slot) : extractFrom.getStackInSlot(extractSlot(slot));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return isInsertSlot(slot) ? insertInto.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return isInsertSlot(slot) ? ItemStack.EMPTY : extractFrom.extractItem(extractSlot(slot), amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return isInsertSlot(slot) ? insertInto.getSlotLimit(slot) : extractFrom.getSlotLimit(extractSlot(slot));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return isInsertSlot(slot) && insertInto.isItemValid(slot, stack);
    }
}
