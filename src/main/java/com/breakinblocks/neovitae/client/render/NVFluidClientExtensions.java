package com.breakinblocks.neovitae.client.render;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.fluid.NVFluids;
import org.joml.Vector3f;

public final class NVFluidClientExtensions implements IClientFluidTypeExtensions {

    private final ResourceLocation still;
    private final ResourceLocation flowing;
    private final ResourceLocation overlay;
    private final float red;
    private final float green;
    private final float blue;
    private final float fogEnd;

    private NVFluidClientExtensions(String name, float red, float green, float blue, float fogEnd) {
        this.still = NeoVitae.rl("block/" + name + "_still");
        this.flowing = NeoVitae.rl("block/" + name + "_flowing");
        this.overlay = NeoVitae.rl("textures/misc/" + name + "_overlay.png");
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.fogEnd = fogEnd;
    }

    public static void register(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new NVFluidClientExtensions("essentia_vitae", 0.42F, 0.02F, 0.02F, 10.0F),
                NVFluids.ESSENTIA_VITAE_TYPE.get());
        event.registerFluidType(new NVFluidClientExtensions("animated_spiritus", 0.04F, 0.3F, 0.42F, 20.0F),
                NVFluids.ANIMATED_SPIRITUS_TYPE.get());
        event.registerFluidType(new NVFluidClientExtensions("liquified_experience", 0.32F, 0.44F, 0.04F, 20.0F),
                NVFluids.LIQUIFIED_EXPERIENCE_TYPE.get());
    }

    @Override
    public ResourceLocation getStillTexture() {
        return still;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return flowing;
    }

    @Override
    public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
        return overlay;
    }

    @Override
    public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                                   float darkenWorldAmount, Vector3f fluidFogColor) {
        float brightness = Math.max(0.2F,
                LightTexture.getBrightness(level.dimensionType(), level.getMaxLocalRawBrightness(camera.getBlockPosition())));
        return new Vector3f(red * brightness, green * brightness, blue * brightness);
    }

    @Override
    public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick,
                                float nearDistance, float farDistance, FogShape shape) {
        float end = camera.getEntity().isSpectator() ? renderDistance * 0.5F : fogEnd;
        RenderSystem.setShaderFogStart(-2.0F);
        RenderSystem.setShaderFogEnd(end);
    }

    public static boolean isEyeInNeoVitaeFluid(Player player) {
        return IClientFluidTypeExtensions.of(player.getEyeInFluidType()) instanceof NVFluidClientExtensions;
    }

    public static void renderOverlay(Minecraft minecraft, Player player, PoseStack poseStack) {
        IClientFluidTypeExtensions.of(player.getEyeInFluidType()).renderOverlay(minecraft, poseStack);
    }
}
