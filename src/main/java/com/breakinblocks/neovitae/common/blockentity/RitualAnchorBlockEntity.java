package com.breakinblocks.neovitae.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neovitae.common.block.AnchorAura;
import com.breakinblocks.neovitae.common.block.RitualAnchorBlock;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RitualAnchorBlockEntity extends BaseBlockEntity {

    private static final int AURA_INTERVAL = 20;

    private final Set<BlockPos> links = new LinkedHashSet<>();
    private boolean removing;

    public RitualAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(NVTiles.RITUAL_ANCHOR_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RitualAnchorBlockEntity anchor) {
        if (level.getGameTime() % AURA_INTERVAL != 0) return;
        anchor.refreshAura(level, pos, state);
    }

    public void refreshAura(Level level, BlockPos pos, BlockState state) {
        boolean anyActive = false;
        int mask = 0;
        boolean pruned = false;
        Iterator<BlockPos> it = links.iterator();
        while (it.hasNext()) {
            BlockPos link = it.next();
            if (!level.isLoaded(link)) continue;
            if (!(level.getBlockEntity(link) instanceof MasterRitualStoneBlockEntity mrs) || !pos.equals(mrs.getAnchorPos())) {
                it.remove();
                pruned = true;
                continue;
            }
            if (mrs.isActive() && mrs.getCurrentRitual() != null && !mrs.isSuspended()) {
                anyActive = true;
                mask |= mrs.getRecentSpiritusMask();
            }
        }
        if (pruned) setChanged();
        AnchorAura aura = AnchorAura.fromMask(anyActive, mask);
        if (state.getValue(RitualAnchorBlock.AURA) != aura) {
            level.setBlock(pos, state.setValue(RitualAnchorBlock.AURA, aura), Block.UPDATE_ALL);
        }
    }

    public Set<BlockPos> getLinks() {
        return links;
    }

    public void addLink(BlockPos pos) {
        if (links.add(pos.immutable())) {
            setChanged();
            refreshNow();
        }
    }

    public void removeLink(BlockPos pos) {
        if (links.remove(pos)) {
            setChanged();
            refreshNow();
        }
    }

    public void refreshNow() {
        if (level != null && !level.isClientSide() && !removing) {
            refreshAura(level, worldPosition, getBlockState());
        }
    }

    public static int auraColor(AnchorAura aura) {
        if (aura.type() == null) return 0xFF3A2A;
        return switch (aura.type()) {
            case RAW -> 0x66E6E6;
            case RUINA -> 0x55EE55;
            case NIHILUM -> 0xFF8844;
            case VINDICTA -> 0xBB55EE;
            case INVICTUS -> 0xEEEEEE;
        };
    }

    public void onBlockRemoved() {
        removing = true;
        if (level != null && !level.isClientSide()) {
            for (BlockPos link : List.copyOf(links)) {
                if (level.isLoaded(link) && level.getBlockEntity(link) instanceof MasterRitualStoneBlockEntity mrs
                        && worldPosition.equals(mrs.getAnchorPos())) {
                    mrs.anchorRemoved();
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLongArray("links", links.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        links.clear();
        for (long link : tag.getLongArray("links")) {
            links.add(BlockPos.of(link));
        }
    }
}
