package com.breakinblocks.neovitae.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.breakinblocks.neovitae.NeoVitae;

public record SetOutputFacePayload(BlockPos pos, int face) implements CustomPacketPayload {

    public static final Type<SetOutputFacePayload> TYPE =
            new Type<>(NeoVitae.rl("set_output_face"));

    public static final StreamCodec<FriendlyByteBuf, SetOutputFacePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetOutputFacePayload::pos,
                    ByteBufCodecs.VAR_INT, SetOutputFacePayload::face,
                    SetOutputFacePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
