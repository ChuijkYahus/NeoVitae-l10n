package com.breakinblocks.neovitae.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.blockentity.SanguineCrafterBlockEntity;

import java.util.List;

public record SanguineCrafterTemplatePayload(BlockPos pos, int mode, List<ItemStack> stacks) implements CustomPacketPayload {

    public static final Type<SanguineCrafterTemplatePayload> TYPE =
            new Type<>(NeoVitae.rl("sanguine_crafter_template"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SanguineCrafterTemplatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SanguineCrafterTemplatePayload::pos,
                    ByteBufCodecs.VAR_INT, SanguineCrafterTemplatePayload::mode,
                    ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(SanguineCrafterBlockEntity.TEMPLATE_SIZE)),
                    SanguineCrafterTemplatePayload::stacks,
                    SanguineCrafterTemplatePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
