package com.breakinblocks.neovitae.client.render.entity;

import com.breakinblocks.neovitae.client.render.entity.model.DaemoniumCorrodisModel;
import com.breakinblocks.neovitae.common.entity.mob.DaemoniumCorrodisEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DaemoniumCorrodisRenderer extends NeoVitaeGeoEntityRenderer<DaemoniumCorrodisEntity> {

    public DaemoniumCorrodisRenderer(EntityRendererProvider.Context context) {
        super(context, new DaemoniumCorrodisModel());
        addRenderLayer(new NVEmissiveGeoLayer<>(this));
    }
}
