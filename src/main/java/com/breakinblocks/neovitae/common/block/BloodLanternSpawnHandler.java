package com.breakinblocks.neovitae.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.dimension.DungeonDimensionHelper;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

@EventBusSubscriber(modid = NeoVitae.MODID)
public final class BloodLanternSpawnHandler {

    private static final int RADIUS = 16;
    private static final Map<ServerLevel, LanternIndex> INDICES = new IdentityHashMap<>();

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        MobCategory category = event.getEntity().getType().getCategory();
        if (category == MobCategory.MONSTER) return;

        ServerLevelAccessor levelAcc = event.getLevel();
        if (levelAcc.getLevel().dimension().equals(DungeonDimensionHelper.DUNGEON_DIMENSION)) return;

        BlockPos center = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        ServerLevel level = levelAcc.getLevel();
        LanternIndex index = INDICES.computeIfAbsent(level, ignored -> new LanternIndex());
        index.ensureLoadedChunks(level, center);
        if (index.hasBlockingLantern(level, center)) {
            event.setSpawnCancelled(true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk) {
            indexChunk(level, chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LanternIndex index = INDICES.get(level);
            if (index != null) index.removeChunk(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) INDICES.remove(level);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && isLantern(event.getPlacedBlock())) {
            INDICES.computeIfAbsent(level, ignored -> new LanternIndex()).add(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && isLantern(event.getState())) {
            LanternIndex index = INDICES.get(level);
            if (index != null) index.remove(event.getPos());
        }
    }

    private static boolean isLantern(BlockState state) {
        return state.is(NVBlocks.BLOOD_LANTERN.block().get()) || state.is(NVBlocks.DEMON_LANTERN.block().get());
    }

    private static void indexChunk(ServerLevel level, LevelChunk chunk) {
        LanternIndex index = INDICES.computeIfAbsent(level, ignored -> new LanternIndex());
        index.removeChunk(chunk.getPos());
        Set<Long> positions = new LongOpenHashSet();
        LevelChunkSection[] sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (!section.maybeHas(BloodLanternSpawnHandler::isLantern)) continue;
            int minY = SectionPos.sectionToBlockCoord(level.getMinSection() + sectionIndex);
            for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                BlockState state = section.getBlockState(x, y, z);
                if (isLantern(state)) positions.add(BlockPos.asLong((chunk.getPos().x << 4) + x, minY + y,
                        (chunk.getPos().z << 4) + z));
            }
        }
        index.putChunk(chunk.getPos(), positions);
    }

    private static final class LanternIndex {
        private final Map<Long, Set<Long>> positionsByChunk = new HashMap<>();
        private final Set<Long> indexedChunks = new LongOpenHashSet();

        void ensureLoadedChunks(ServerLevel level, BlockPos center) {
            int chunkX = center.getX() >> 4, chunkZ = center.getZ() >> 4;
            for (int x = chunkX - 1; x <= chunkX + 1; x++) for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                long key = ChunkPos.asLong(x, z);
                if (!indexedChunks.contains(key)) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                    if (chunk != null) indexChunk(level, chunk);
                }
            }
        }

        boolean hasBlockingLantern(ServerLevel level, BlockPos center) {
            int chunkX = center.getX() >> 4, chunkZ = center.getZ() >> 4;
            for (int x = chunkX - 1; x <= chunkX + 1; x++) for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                Set<Long> positions = positionsByChunk.get(ChunkPos.asLong(x, z));
                if (positions == null) continue;
                for (long packed : positions) {
                    BlockPos lantern = BlockPos.of(packed);
                    if (Math.abs(lantern.getX() - center.getX()) + Math.abs(lantern.getY() - center.getY())
                            + Math.abs(lantern.getZ() - center.getZ()) > RADIUS) continue;
                    BlockState state = level.getBlockState(lantern);
                    if (state.is(NVBlocks.BLOOD_LANTERN.block().get())) return true;
                    if (state.is(NVBlocks.DEMON_LANTERN.block().get()) && !level.hasNeighborSignal(lantern)) return true;
                }
            }
            return false;
        }

        void putChunk(ChunkPos pos, Set<Long> positions) {
            long key = pos.toLong(); indexedChunks.add(key);
            if (positions.isEmpty()) positionsByChunk.remove(key); else positionsByChunk.put(key, positions);
        }
        void removeChunk(ChunkPos pos) { long key = pos.toLong(); indexedChunks.remove(key); positionsByChunk.remove(key); }
        void add(BlockPos pos) { long key = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4); indexedChunks.add(key); positionsByChunk.computeIfAbsent(key, ignored -> new LongOpenHashSet()).add(pos.asLong()); }
        void remove(BlockPos pos) { Set<Long> positions = positionsByChunk.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4)); if (positions != null) positions.remove(pos.asLong()); }
    }
}
