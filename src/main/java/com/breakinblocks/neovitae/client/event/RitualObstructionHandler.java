package com.breakinblocks.neovitae.client.event;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.client.render.NVRenderTypes;
import com.breakinblocks.neovitae.common.network.RitualObstructionsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT, modid = NeoVitae.MODID)
public final class RitualObstructionHandler {
    private static final List<BlockPos> POSITIONS = new ArrayList<>();
    private static ClientLevel highlightLevel;
    private static long expiresAt;

    private RitualObstructionHandler() {}

    public static void show(RitualObstructionsPayload payload) {
        clear();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(payload.dimension())) return;
        highlightLevel = level;
        expiresAt = level.getGameTime() + 200;
        POSITIONS.addAll(payload.positions());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static void clear() {
        POSITIONS.clear();
        highlightLevel = null;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level != highlightLevel || mc.level.getGameTime() >= expiresAt) {
            clear();
            return;
        }
        POSITIONS.removeIf(pos -> mc.level.getBlockState(pos).isAir()
                || mc.level.getBlockState(pos).canBeReplaced());
        if (POSITIONS.isEmpty()) return;

        var pose = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        var buffers = mc.renderBuffers().bufferSource();
        var lines = buffers.getBuffer(NVRenderTypes.LINES_SEE_THROUGH);
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        for (BlockPos pos : POSITIONS) {
            LevelRenderer.renderLineBox(pose, lines, new AABB(pos).inflate(0.004), 1.0f, 0.15f, 0.1f, 0.9f);
        }
        pose.popPose();
        buffers.endBatch(NVRenderTypes.LINES_SEE_THROUGH);
    }
}
