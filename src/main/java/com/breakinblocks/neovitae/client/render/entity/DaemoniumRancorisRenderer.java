package com.breakinblocks.neovitae.client.render.entity;

import com.breakinblocks.neovitae.client.render.entity.model.DaemoniumRancorisModel;
import com.breakinblocks.neovitae.common.entity.mob.DaemoniumRancorisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DaemoniumRancorisRenderer extends NeoVitaeGeoEntityRenderer<DaemoniumRancorisEntity> {

    public DaemoniumRancorisRenderer(EntityRendererProvider.Context context) {
        super(context, new DaemoniumRancorisModel());
        addRenderLayer(new NVEmissiveGeoLayer<>(this));
    }
}
