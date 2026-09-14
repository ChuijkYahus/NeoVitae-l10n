package com.breakinblocks.neovitae.common.network;

import com.breakinblocks.neovitae.NeoVitae;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record RitualObstructionsPayload(ResourceLocation dimension, List<BlockPos> positions)
        implements CustomPacketPayload {
    public static final int MAX_POSITIONS = 1024;
    public static final Type<RitualObstructionsPayload> TYPE = new Type<>(NeoVitae.rl("ritual_obstructions"));
    public static final StreamCodec<FriendlyByteBuf, RitualObstructionsPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, RitualObstructionsPayload::dimension,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_POSITIONS)), RitualObstructionsPayload::positions,
            RitualObstructionsPayload::new);

    public RitualObstructionsPayload {
        positions = List.copyOf(positions);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
