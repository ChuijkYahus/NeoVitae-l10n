package com.breakinblocks.neovitae.common.routing;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.api.routing.IFilterKey;
import com.breakinblocks.neovitae.common.item.routing.BasicFilterKey;
import com.breakinblocks.neovitae.common.item.routing.CollectionTagFilterKey;
import com.breakinblocks.neovitae.common.item.routing.ComponentFilterKey;
import com.breakinblocks.neovitae.common.item.routing.CompositeFilterKey;
import com.breakinblocks.neovitae.common.item.routing.ModFilterKey;
import com.breakinblocks.neovitae.common.item.routing.PropertyFilterKey;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class FilterMatchRules {

    public static final String MATCH_NAMESPACE = "neovitae_match";
    public static final String TAG_NAMESPACE = "neovitae_tag";
    public static final ResourceLocation ANY_ITEM = ResourceLocation.fromNamespaceAndPath(MATCH_NAMESPACE, "any_item");
    public static final ResourceLocation SAME_MOD = ResourceLocation.fromNamespaceAndPath(MATCH_NAMESPACE, "same_mod");

    public enum ItemProperty {
        STACKABLE(stack -> stack.getMaxStackSize() > 1),
        NON_STACKABLE(stack -> stack.getMaxStackSize() <= 1),
        DAMAGED(ItemStack::isDamaged),
        UNDAMAGED(stack -> stack.isDamageableItem() && !stack.isDamaged()),
        ENCHANTED(ItemStack::isEnchanted),
        UNENCHANTED(stack -> !stack.isEnchanted()),
        RENAMED(stack -> stack.has(DataComponents.CUSTOM_NAME));

        private final Predicate<ItemStack> test;

        ItemProperty(Predicate<ItemStack> test) {
            this.test = test;
        }

        public boolean test(ItemStack stack) {
            return test.test(stack);
        }

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public ResourceLocation id() {
            return ResourceLocation.fromNamespaceAndPath(MATCH_NAMESPACE, getName());
        }
    }

    private FilterMatchRules() {}

    public static boolean isRule(ResourceLocation id) {
        return MATCH_NAMESPACE.equals(id.getNamespace()) || TAG_NAMESPACE.equals(id.getNamespace());
    }

    public static ResourceLocation tagRule(TagKey<Item> tag) {
        ResourceLocation location = tag.location();
        return ResourceLocation.fromNamespaceAndPath(TAG_NAMESPACE, location.getNamespace() + "/" + location.getPath());
    }

    public static Optional<TagKey<Item>> tagFromRule(ResourceLocation id) {
        if (!TAG_NAMESPACE.equals(id.getNamespace())) return Optional.empty();
        String path = id.getPath();
        int slash = path.indexOf('/');
        if (slash <= 0 || slash == path.length() - 1) return Optional.empty();
        ResourceLocation tagId = ResourceLocation.tryBuild(path.substring(0, slash), path.substring(slash + 1));
        return tagId == null ? Optional.empty() : Optional.of(TagKey.create(Registries.ITEM, tagId));
    }

    public static Optional<ItemProperty> propertyFromRule(ResourceLocation id) {
        if (!MATCH_NAMESPACE.equals(id.getNamespace())) return Optional.empty();
        for (ItemProperty property : ItemProperty.values()) {
            if (property.getName().equals(id.getPath())) return Optional.of(property);
        }
        return Optional.empty();
    }

    public static IFilterKey buildKey(ItemStack keyStack, Set<ResourceLocation> selected, int count) {
        boolean anyItem = selected.contains(ANY_ITEM);
        boolean sameMod = selected.contains(SAME_MOD);
        List<TagKey<Item>> tags = new ArrayList<>();
        Set<ItemProperty> properties = EnumSet.noneOf(ItemProperty.class);
        Set<DataComponentType<?>> components = new LinkedHashSet<>();
        for (ResourceLocation id : selected) {
            if (isRule(id)) {
                tagFromRule(id).ifPresent(tags::add);
                propertyFromRule(id).ifPresent(properties::add);
            } else {
                DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(id);
                if (type != null) components.add(type);
            }
        }

        List<IFilterKey> parts = new ArrayList<>();
        if (!tags.isEmpty()) {
            parts.add(new CollectionTagFilterKey(tags, count));
        }
        if (sameMod) {
            parts.add(new ModFilterKey(BuiltInRegistries.ITEM.getKey(keyStack.getItem()).getNamespace(), count));
        }
        if (!anyItem && tags.isEmpty() && !sameMod) {
            parts.add(components.isEmpty()
                    ? new BasicFilterKey(keyStack, count)
                    : new ComponentFilterKey(keyStack, components, count));
        }
        if (!properties.isEmpty() || parts.isEmpty()) {
            parts.add(new PropertyFilterKey(properties, count));
        }
        if (parts.size() == 1) return parts.get(0);

        CompositeFilterKey composite = new CompositeFilterKey(count);
        parts.forEach(composite::addFilterKey);
        return composite;
    }
}
