package com.breakinblocks.neovitae.client.render.entity;

import com.breakinblocks.neovitae.client.render.entity.model.DaemoniumGlaciarisModel;
import com.breakinblocks.neovitae.common.entity.mob.DaemoniumGlaciarisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DaemoniumGlaciarisRenderer extends NeoVitaeGeoEntityRenderer<DaemoniumGlaciarisEntity> {

    public DaemoniumGlaciarisRenderer(EntityRendererProvider.Context context) {
        super(context, new DaemoniumGlaciarisModel());
        addRenderLayer(new NVEmissiveGeoLayer<>(this));
    }
}
