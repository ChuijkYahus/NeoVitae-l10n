// Derived from Blood Magic (https://github.com/WayofTime/BloodMagic), licensed under CC BY 4.0
// SPDX-FileCopyrightText: 2014-2026 WayofTime <https://github.com/WayofTime>
// SPDX-FileCopyrightText: 2024-2026 Saereth <https://github.com/breakinblocks/NeoVitae>
// SPDX-License-Identifier: CC-BY-4.0 AND MIT

package com.breakinblocks.neovitae.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import com.breakinblocks.neovitae.common.block.BlockMasterRitualStone;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.block.BlockInversionPillarEnd;
import com.breakinblocks.neovitae.common.block.type.PillarCapType;
import com.breakinblocks.neovitae.common.blockentity.InversionPillarBlockEntity;
import com.breakinblocks.neovitae.common.dataattachment.NVDataAttachments;
import com.breakinblocks.neovitae.common.dataattachment.DungeonExitData;
import com.breakinblocks.neovitae.common.dimension.DungeonDimensionHelper;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.util.helper.AnimaHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Abstract base class for dungeon rituals containing shared functionality.
 * Provides common methods for portal pillar spawning, rotation, and exit location storage.
 */
public abstract class DungeonRitualBase extends Ritual {

    protected DungeonRitualBase(String name, int crystalLevel, int activationCost, String translationKey) {
        super(name, crystalLevel, activationCost, translationKey);
    }

    @Override
    public boolean canActivate(IMasterRitualStone masterRitualStone, Player player) {
        return getActivationError(masterRitualStone, player) == null;
    }

    @Nullable
    @Override
    public Component getActivationError(IMasterRitualStone masterRitualStone, Player player) {
        if (masterRitualStone.getWorldObj() instanceof ServerLevel level) {
            BoundingBox box = getStructureBounds(masterRitualStone, level);
            if (box == null) {
                return Component.translatable("ritual.neovitae.dungeon.missing_structure");
            }
            int minY = level.dimensionType().minY();
            int maxY = minY + level.dimensionType().height() - 1;
            if (box.minY() < minY || box.maxY() > maxY) {
                return Component.translatable("ritual.neovitae.dungeon.build_height");
            }
            if (!level.getWorldBorder().isWithinBounds(new BlockPos(box.minX(), box.minY(), box.minZ()))
                    || !level.getWorldBorder().isWithinBounds(new BlockPos(box.maxX(), box.maxY(), box.maxZ()))) {
                return Component.translatable("ritual.neovitae.dungeon.world_border");
            }
            List<BlockPos> obstructions = findObstructions(masterRitualStone, level, box);
            if (!obstructions.isEmpty()) {
                showObstructions(level, obstructions);
                BlockPos first = obstructions.getFirst();
                return Component.translatable("ritual.neovitae.dungeon.obstructed", obstructions.size(),
                        first.getX(), first.getY(), first.getZ());
            }
        }
        return null;
    }

    private void showObstructions(ServerLevel level, List<BlockPos> obstructions) {
        // A short burst of particles makes the blocking blocks easy to spot without
        // requiring a client-side payload or leaving persistent world markers.
        obstructions.stream().limit(32).forEach(pos -> level.sendParticles(
                ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                6, 0.25, 0.25, 0.25, 0.01));
    }

    @Override
    public boolean activateRitual(IMasterRitualStone masterRitualStone, Player player, UUID owner) {
        storePlayerExitLocation(player);
        return true;
    }

    @Nullable
    protected BoundingBox getStructureBounds(IMasterRitualStone masterRitualStone, ServerLevel level) {
        Optional<StructureTemplate> templateOpt = level.getStructureManager().get(getStructureId());
        if (templateOpt.isEmpty()) {
            return null;
        }
        StructurePlaceSettings settings = structureSettings(masterRitualStone);
        BlockPos placeOrigin = masterRitualStone.getMasterBlockPos().subtract(ALTERNATOR_LOCAL);
        return templateOpt.get().getBoundingBox(settings, placeOrigin);
    }

    protected List<BlockPos> findObstructions(IMasterRitualStone masterRitualStone, ServerLevel level, BoundingBox box) {
        Set<BlockPos> ritualPositions = ritualPositions(masterRitualStone);
        List<BlockPos> obstructions = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.canBeReplaced() || isRitualStone(level, pos, ritualPositions)) {
                continue;
            }
            // The master stone may be embedded in a floor. Only ordinary full blocks
            // can become the portal's foundation; inventories and bedrock remain protected.
            if (pos.getY() == masterRitualStone.getMasterBlockPos().getY()
                    && !state.hasBlockEntity() && state.isCollisionShapeFullBlock(level, pos)
                    && state.getDestroySpeed(level, pos) >= 0) {
                continue;
            }
            obstructions.add(pos.immutable());
        }
        return obstructions;
    }

    private Set<BlockPos> ritualPositions(IMasterRitualStone masterRitualStone) {
        Set<BlockPos> positions = new HashSet<>();
        positions.add(masterRitualStone.getMasterBlockPos());
        for (RitualComponent component : RitualLayouts.get(masterRitualStone.getWorldObj(), this)) {
            positions.add(masterRitualStone.getMasterBlockPos().offset(
                    rotateOffset(component.offset(), masterRitualStone.getDirection())));
        }
        return positions;
    }

    private static boolean isRitualStone(LevelReader level, BlockPos pos, Set<BlockPos> ritualPositions) {
        Block block = level.getBlockState(pos).getBlock();
        return ritualPositions.contains(pos) && (block instanceof IRitualStone || block instanceof BlockMasterRitualStone);
    }

    private StructurePlaceSettings structureSettings(IMasterRitualStone masterRitualStone) {
        Set<BlockPos> ritualPositions = ritualPositions(masterRitualStone);
        return new StructurePlaceSettings()
                .setRotation(directionToRotation(masterRitualStone.getDirection()))
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(true)
                .setRotationPivot(ALTERNATOR_LOCAL)
                .addProcessor(new BlockIgnoreProcessor(List.of(Blocks.AIR)) {
                    @Nullable
                    @Override
                    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos origin,
                            BlockPos pivot, StructureTemplate.StructureBlockInfo original,
                            StructureTemplate.StructureBlockInfo current, StructurePlaceSettings settings) {
                        // Preserve the floor in template air cells, but still consume the ritual's runes.
                        if (current.pos().getY() == masterRitualStone.getMasterBlockPos().getY()
                                && !isRitualStone(level, current.pos(), ritualPositions)) {
                            return super.processBlock(level, origin, pivot, original, current, settings);
                        }
                        return current;
                    }
                });
    }

    protected void storePlayerExitLocation(Player player) {
        DungeonExitData exitData = DungeonExitData.of(player.level(), player.blockPosition());
        player.setData(NVDataAttachments.DUNGEON_EXIT.get(), exitData);
    }

    protected void storeControllerPosition(IMasterRitualStone masterRitualStone, BlockPos controllerPos) {
        Level world = masterRitualStone.getWorldObj();
        Player player = world.getPlayerByUUID(masterRitualStone.getOwner());
        if (player != null) {
            DungeonExitData exitData = player.getData(NVDataAttachments.DUNGEON_EXIT.get());
            player.setData(NVDataAttachments.DUNGEON_EXIT.get(), exitData.withControllerPos(controllerPos));
        }
    }

    protected static final BlockPos ALTERNATOR_LOCAL = new BlockPos(4, 0, 4);

    protected abstract Identifier getStructureId();

    protected boolean applyRitualStructure(IMasterRitualStone masterRitualStone, ServerLevel level) {
        BlockPos masterPos = masterRitualStone.getMasterBlockPos();

        Optional<StructureTemplate> templateOpt = level.getStructureManager().get(getStructureId());
        if (templateOpt.isEmpty()) {
            return false;
        }

        StructurePlaceSettings settings = structureSettings(masterRitualStone);

        BlockPos placeOrigin = masterPos.subtract(ALTERNATOR_LOCAL);
        if (!templateOpt.get().placeInWorld(level, placeOrigin, ALTERNATOR_LOCAL, settings, level.getRandom(),
                Block.UPDATE_CLIENTS)) {
            return false;
        }

        spawnLightningEffect(level, masterPos);
        AnimaHelper.incrementDungeonCounter();
        return true;
    }

    protected void wireFunctionalInversionPillar(ServerLevel spawnWorld, BlockPos masterPos,
                                                  Level destinationWorld, BlockPos safePlayerPos) {
        BlockEntity tile = spawnWorld.getBlockEntity(masterPos.above(2));
        if (tile instanceof InversionPillarBlockEntity tileInversion) {
            tileInversion.setDestination(destinationWorld, safePlayerPos);
        }
    }

    private static Rotation directionToRotation(Direction direction) {
        return switch (direction) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    protected void spawnLightningEffect(Level world, BlockPos pos) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world, EntitySpawnReason.TRIGGERED);
        if (lightning != null) {
            lightning.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
            lightning.setVisualOnly(true);
            world.addFreshEntity(lightning);
        }
    }

    protected void spawnPortalPillar(Level spawnWorld, Level destinationWorld,
                                      BlockPos pillarPos, BlockPos safePlayerPos) {
        spawnWorld.setBlockAndUpdate(pillarPos, NVBlocks.INVERSION_PILLAR.block().get().defaultBlockState());

        BlockEntity tile = spawnWorld.getBlockEntity(pillarPos);
        if (tile instanceof InversionPillarBlockEntity tileInversion) {
            tileInversion.setDestination(destinationWorld, safePlayerPos);

            spawnWorld.setBlockAndUpdate(pillarPos.below(),
                    NVBlocks.INVERSION_PILLAR_CAP.block().get().defaultBlockState()
                            .setValue(BlockInversionPillarEnd.TYPE, PillarCapType.BOTTOM));
            spawnWorld.setBlockAndUpdate(pillarPos.above(),
                    NVBlocks.INVERSION_PILLAR_CAP.block().get().defaultBlockState()
                            .setValue(BlockInversionPillarEnd.TYPE, PillarCapType.TOP));
            spawnWorld.setBlockAndUpdate(pillarPos.below(2),
                    NVBlocks.BLOODSTONE.block().get().defaultBlockState());
        }

        net.minecraft.util.RandomSource rand = spawnWorld.getRandom();
        int lightCount = 4 + rand.nextInt(6);
        net.minecraft.world.level.block.state.BlockState lightState = NVBlocks.BLOOD_LIGHT.get().defaultBlockState();
        for (int i = 0; i < lightCount; i++) {
            for (int attempt = 0; attempt < 10; attempt++) {
                int dx = rand.nextInt(9) - 4;
                int dy = rand.nextInt(5) - 1;
                int dz = rand.nextInt(9) - 4;
                BlockPos lightPos = pillarPos.offset(dx, dy, dz);
                if (spawnWorld.isEmptyBlock(lightPos)) {
                    spawnWorld.setBlockAndUpdate(lightPos, lightState);
                    break;
                }
            }
        }
    }

    protected BlockPos rotateOffset(BlockPos offset, Direction direction) {
        return switch (direction) {
            case NORTH -> offset;
            case EAST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
            case SOUTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
            case WEST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
            default -> offset;
        };
    }

    protected ServerLevel getDungeonWorld(Level world) {
        return DungeonDimensionHelper.getDungeonWorld(world);
    }

    @Override
    public int getRefreshTime() {
        return 1; // Execute once immediately
    }

    @Override
    public int getRefreshCost() {
        return 0; // One-time activation cost only
    }
}
