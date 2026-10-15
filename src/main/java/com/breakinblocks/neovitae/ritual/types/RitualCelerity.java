package com.breakinblocks.neovitae.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.api.stream.StreamPresets;
import com.breakinblocks.neovitae.common.blockentity.IRitualAccelerated;
import com.breakinblocks.neovitae.common.blockentity.RitualAcceleration;
import com.breakinblocks.neovitae.ritual.EnumRuneType;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.breakinblocks.neovitae.ritual.RitualComponent;
import com.breakinblocks.neovitae.ritual.RitualHelper;
import com.breakinblocks.neovitae.ritual.RitualHelper.RitualContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RitualCelerity extends Ritual {

    public static final String NAME = "celerity";
    public static final String ACCELERATE_RANGE = "accelerateRange";
    public static final int SPEED = 2;

    private final int speed;

    public RitualCelerity() {
        this(NAME, 1, 25000, SPEED);
        addBlockRange(ACCELERATE_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -1, -5), 11, 7, 11));
        setMaximumVolumeAndDistanceOfRange(ACCELERATE_RANGE, 2000, 13, 13);
    }

    protected RitualCelerity(String name, int crystalLevel, int activationCost, int speed) {
        super(name, crystalLevel, activationCost, "ritual." + NeoVitae.MODID + "." + name);
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        RitualContext ctx = RitualHelper.createContext(masterRitualStone);
        if (ctx == null) return;

        ServerLevel level = ctx.serverLevel();
        BlockPos masterPos = ctx.masterPos();
        AreaDescriptor range = RitualHelper.getEffectiveRange(masterRitualStone, this, ACCELERATE_RANGE);
        if (range == null) return;

        long now = level.getGameTime();
        long until = now + getRefreshTime() + 10;
        long workWindow = now - getRefreshTime();
        int maxCharged = ctx.maxOperations(getRefreshCost());
        int charged = 0;
        List<BlockPos> working = new ArrayList<>();

        for (BlockEntity be : machinesInRange(level, masterPos, range)) {
            RitualAcceleration acceleration = ((IRitualAccelerated) be).getRitualAcceleration();
            boolean isWorking = acceleration.workedSince(workWindow);
            if (isWorking && charged >= maxCharged) continue;
            if (acceleration.apply(level, masterPos, speed, until) && isWorking) {
                charged++;
                working.add(be.getBlockPos());
            }
        }

        if (!working.isEmpty() && level.getRandom().nextInt(3) == 0) {
            BlockPos target = working.get(level.getRandom().nextInt(working.size()));
            StreamPresets.lifePulse(masterPos, target).build().sendToNearby(level, masterPos, 64);
        }

        ctx.syphon(getRefreshCost() * charged);
    }

    private static List<BlockEntity> machinesInRange(ServerLevel level, BlockPos masterPos, AreaDescriptor range) {
        AABB box = range.getAABB(masterPos);
        List<BlockEntity> found = new ArrayList<>();
        int minCx = SectionPos.blockToSectionCoord(box.minX);
        int maxCx = SectionPos.blockToSectionCoord(box.maxX);
        int minCz = SectionPos.blockToSectionCoord(box.minZ);
        int maxCz = SectionPos.blockToSectionCoord(box.maxZ);
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                if (!level.hasChunk(cx, cz)) continue;
                LevelChunk chunk = level.getChunk(cx, cz);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof IRitualAccelerated && range.isWithinArea(be.getBlockPos().subtract(masterPos))) {
                        found.add(be);
                    }
                }
            }
        }
        return found;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 1, 0, EnumRuneType.FIRE);
        addCornerRunes(components, 1, 0, EnumRuneType.AIR);
        addOffsetRunes(components, 2, 1, 0, EnumRuneType.TENEBRAE);
        addParallelRunes(components, 3, 0, EnumRuneType.FIRE);
        addCornerRunes(components, 3, 0, EnumRuneType.AIR);
        addParallelRunes(components, 4, 0, EnumRuneType.TENEBRAE);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualCelerity();
    }
}
