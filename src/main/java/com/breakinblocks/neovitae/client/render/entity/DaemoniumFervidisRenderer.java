package com.breakinblocks.neovitae.client.render.entity;

import com.breakinblocks.neovitae.client.render.entity.model.DaemoniumFervidisModel;
import com.breakinblocks.neovitae.common.entity.mob.DaemoniumFervidisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DaemoniumFervidisRenderer extends NeoVitaeGeoEntityRenderer<DaemoniumFervidisEntity> {

    public DaemoniumFervidisRenderer(EntityRendererProvider.Context context) {
        super(context, new DaemoniumFervidisModel());
        addRenderLayer(new NVEmissiveGeoLayer<>(this));
    }
}
