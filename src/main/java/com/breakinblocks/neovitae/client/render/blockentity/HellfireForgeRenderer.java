package com.breakinblocks.neovitae.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.common.blockentity.HellfireForgeBlockEntity;

public class HellfireForgeRenderer implements BlockEntityRenderer<HellfireForgeBlockEntity> {

    public HellfireForgeRenderer(BlockEntityRendererProvider.Context context) {}

    private static final float CORNER_OFFSET = 1 + 3/16F;
    private static final float GEM_HEIGHT = 1.1F;
    private static final float GEM_SCALE = 0.6F;
    private static final float GEM_BOB = 0.04F;
    private static final float GEM_SWAY_DEGREES = 20.0F;

    @Override
    public void render(HellfireForgeBlockEntity forge, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = forge.getLevel();
        renderSlot(forge.inv.getStackInSlot(HellfireForgeBlockEntity.SOUTH), 1/16F, CORNER_OFFSET, 15/16F, level, poseStack, bufferSource, packedLight, packedOverlay);
        renderSlot(forge.inv.getStackInSlot(HellfireForgeBlockEntity.WEST), 1/16F, CORNER_OFFSET, 1/16F, level, poseStack, bufferSource, packedLight, packedOverlay);
        renderSlot(forge.inv.getStackInSlot(HellfireForgeBlockEntity.NORTH), 15/16F, CORNER_OFFSET, 1/16F, level, poseStack, bufferSource, packedLight, packedOverlay);
        renderSlot(forge.inv.getStackInSlot(HellfireForgeBlockEntity.EAST), 15/16F, CORNER_OFFSET, 15/16F, level, poseStack, bufferSource, packedLight, packedOverlay);

        if (!forge.inv.getStackInSlot(HellfireForgeBlockEntity.GEM_SLOT).isEmpty()) {
            renderGem(forge, forge.inv.getStackInSlot(HellfireForgeBlockEntity.GEM_SLOT), level, poseStack, bufferSource, packedLight, packedOverlay);
        }
        if (!forge.inv.getStackInSlot(HellfireForgeBlockEntity.OUTPUT_SLOT).isEmpty()) {
            renderSlot(forge.inv.getStackInSlot(HellfireForgeBlockEntity.OUTPUT_SLOT), 0.5F, 1.5F, 0.5F, level, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private void renderGem(HellfireForgeBlockEntity forge, ItemStack itemStack, Level level, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float seconds = (System.currentTimeMillis() % 3_600_000L) / 1000.0F;
        double dx = camera.x - (forge.getBlockPos().getX() + 0.5);
        double dz = camera.z - (forge.getBlockPos().getZ() + 0.5);
        float yaw = (float) Math.toDegrees(Math.atan2(dx, dz)) + GEM_SWAY_DEGREES * (float) Math.sin(seconds * 1.5F);
        poseStack.pushPose();
        poseStack.translate(0.5F, GEM_HEIGHT + GEM_BOB * (float) Math.sin(seconds * 2.0F), 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.scale(GEM_SCALE, GEM_SCALE, GEM_SCALE);
        BakedModel bakedModel = renderer.getModel(itemStack, level, null, 1);
        renderer.render(itemStack, ItemDisplayContext.FIXED, true, poseStack, bufferSource, packedLight, packedOverlay, bakedModel);
        poseStack.popPose();
    }

    private void renderSlot(ItemStack itemStack, float x, float y, float z, Level level, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        float rotation = (720.0F * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        BakedModel bakedModel = renderer.getModel(itemStack, level, null, 1);
        renderer.render(itemStack, ItemDisplayContext.FIXED, true, poseStack, bufferSource, packedLight, packedOverlay, bakedModel);
        poseStack.popPose();
    }
}
