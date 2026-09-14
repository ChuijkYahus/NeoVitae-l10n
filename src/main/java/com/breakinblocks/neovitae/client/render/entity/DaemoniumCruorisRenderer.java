package com.breakinblocks.neovitae.client.render.entity;

import com.breakinblocks.neovitae.client.render.entity.model.DaemoniumCruorisModel;
import com.breakinblocks.neovitae.common.entity.mob.DaemoniumCruorisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DaemoniumCruorisRenderer extends NeoVitaeGeoEntityRenderer<DaemoniumCruorisEntity> {

    public DaemoniumCruorisRenderer(EntityRendererProvider.Context context) {
        super(context, new DaemoniumCruorisModel());
        addRenderLayer(new NVEmissiveGeoLayer<>(this));
    }
}
