package com.breakinblocks.neovitae.client.render.entity;

import com.breakinblocks.neovitae.client.render.entity.model.DaemoniumDolorisModel;
import com.breakinblocks.neovitae.common.entity.mob.DaemoniumDolorisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DaemoniumDolorisRenderer extends NeoVitaeGeoEntityRenderer<DaemoniumDolorisEntity> {

    public DaemoniumDolorisRenderer(EntityRendererProvider.Context context) {
        super(context, new DaemoniumDolorisModel());
        addRenderLayer(new NVEmissiveGeoLayer<>(this));
    }
}
