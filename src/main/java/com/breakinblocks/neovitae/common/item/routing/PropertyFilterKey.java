package com.breakinblocks.neovitae.common.item.routing;

import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.api.routing.IFilterKey;
import com.breakinblocks.neovitae.common.routing.FilterMatchRules.ItemProperty;

import java.util.Set;

public class PropertyFilterKey implements IFilterKey {

    private final Set<ItemProperty> properties;
    private int count;

    public PropertyFilterKey(Set<ItemProperty> properties, int count) {
        this.properties = properties;
        this.count = count;
    }

    @Override
    public boolean doesStackMatch(ItemStack testStack) {
        if (testStack.isEmpty()) return false;
        for (ItemProperty property : properties) {
            if (!property.test(testStack)) return false;
        }
        return true;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void shrink(int changeAmount) {
        this.count -= changeAmount;
    }

    @Override
    public void grow(int changeAmount) {
        this.count += changeAmount;
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    public Set<ItemProperty> getProperties() {
        return properties;
    }
}
