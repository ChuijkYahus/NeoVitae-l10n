package com.breakinblocks.neovitae.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;

import javax.annotation.Nullable;
import java.util.List;

public class RoomBoundsProcessor extends StructureProcessor {

    private final List<AreaDescriptor> bounds;

    public RoomBoundsProcessor(List<AreaDescriptor> bounds) {
        this.bounds = bounds;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader levelReader, BlockPos blockPos,
                                                        BlockPos blockPos2,
                                                        StructureTemplate.StructureBlockInfo blockInfoBefore,
                                                        StructureTemplate.StructureBlockInfo blockInfoAfter,
                                                        StructurePlaceSettings settings,
                                                        @Nullable StructureTemplate template) {
        if (!blockInfoAfter.state().isAir() || contains(blockInfoAfter.pos())) {
            return blockInfoAfter;
        }
        return null;
    }

    public boolean contains(BlockPos pos) {
        for (AreaDescriptor bound : bounds) {
            if (bound instanceof AreaDescriptor.Rectangle rect) {
                BlockPos min = rect.getMinimumOffset();
                BlockPos max = rect.getMaximumOffset();
                if (pos.getX() >= min.getX() && pos.getX() < max.getX()
                        && pos.getY() >= min.getY() && pos.getY() < max.getY()
                        && pos.getZ() >= min.getZ() && pos.getZ() < max.getZ()) {
                    return true;
                }
            } else if (bound.isWithinArea(pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}
