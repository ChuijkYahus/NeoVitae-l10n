package com.breakinblocks.neovitae.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ActiveRitualData extends SavedData {
    public static final String ID = "neovitae_active_rituals";

    public record Entry(UUID owner, ResourceLocation ritual) {}

    private final Map<GlobalPos, Entry> entries = new LinkedHashMap<>();

    public Map<GlobalPos, Entry> entries() {
        return entries;
    }

    public void put(GlobalPos pos, Entry entry) {
        if (!entry.equals(entries.put(pos, entry))) {
            setDirty();
        }
    }

    public void remove(GlobalPos pos) {
        if (entries.remove(pos) != null) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<GlobalPos, Entry> e : entries.entrySet()) {
            CompoundTag element = new CompoundTag();
            element.putString("dimension", e.getKey().dimension().location().toString());
            element.put("pos", NbtUtils.writeBlockPos(e.getKey().pos()));
            element.putUUID("owner", e.getValue().owner());
            element.putString("ritual", e.getValue().ritual().toString());
            list.add(element);
        }
        tag.put("rituals", list);
        return tag;
    }

    public static ActiveRitualData load(CompoundTag tag, HolderLookup.Provider registries) {
        ActiveRitualData data = new ActiveRitualData();
        ListTag list = tag.getList("rituals", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag element = list.getCompound(i);
            ResourceLocation dimension = ResourceLocation.tryParse(element.getString("dimension"));
            ResourceLocation ritual = ResourceLocation.tryParse(element.getString("ritual"));
            BlockPos pos = NbtUtils.readBlockPos(element, "pos").orElse(null);
            if (dimension == null || ritual == null || pos == null || !element.hasUUID("owner")) {
                continue;
            }
            GlobalPos globalPos = GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimension), pos);
            data.entries.put(globalPos, new Entry(element.getUUID("owner"), ritual));
        }
        return data;
    }
}
