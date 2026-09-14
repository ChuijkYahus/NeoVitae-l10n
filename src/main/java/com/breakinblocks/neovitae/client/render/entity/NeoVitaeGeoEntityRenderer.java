package com.breakinblocks.neovitae.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.NeoForge;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib's renderer replaces EntityRenderer.render, so it must forward the
 * vanilla name-tag event explicitly for overlays such as Mob Plaques.
 */
public abstract class NeoVitaeGeoEntityRenderer<T extends net.minecraft.world.entity.Entity & GeoAnimatable>
        extends GeoEntityRenderer<T> {

    protected NeoVitaeGeoEntityRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        RenderNameTagEvent event = new RenderNameTagEvent(entity, entity.getDisplayName(), this,
                poseStack, bufferSource, packedLight, partialTick);
        NeoForge.EVENT_BUS.post(event);
        if (event.canRender().isTrue() || event.canRender().isDefault() && this.shouldShowName(entity)) {
            this.renderNameTag(entity, event.getContent(), poseStack, bufferSource, packedLight, partialTick);
        }
    }
}
