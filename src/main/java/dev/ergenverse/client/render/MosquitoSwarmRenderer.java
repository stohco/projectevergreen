package dev.ergenverse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import dev.ergenverse.entity.MosquitoSwarmEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * MosquitoSwarmRenderer — LOD-based renderer for the composite swarm entity.
 *
 * <h2>Level-of-Detail Strategy</h2>
 * <ul>
 *   <li><b>CLOSE (&lt; 16 blocks):</b> Individual billboard quads with
 *       wing-flapping animation. Render count proportional to log₂(population),
 *       capped at 200 for performance.</li>
 *   <li><b>MEDIUM (16–64 blocks):</b> Single camera-facing billboard textured
 *       quad representing the swarm cloud.</li>
 *   <li><b>FAR (&gt; 64 blocks):</b> Three layered dark planes simulating a
 *       volumetric dark cloud silhouette.</li>
 * </ul>
 *
 * <h2>Fission Interpolation</h2>
 * <p>When a child swarm is freshly split from its parent, the renderer
 * interpolates the visual position from the parent's origin to the child's
 * current position using a smoothstep curve over the fission duration.
 */
public class MosquitoSwarmRenderer extends EntityRenderer<MosquitoSwarmEntity> {

    private static final ResourceLocation SWARM_TEXTURE =
            new ResourceLocation("ergenverse", "textures/entity/mosquito_swarm.png");

    // LOD distance thresholds (squared, in blocks)
    private static final float CLOSE_DIST_SQ = 16.0F * 16.0F;   // 256
    private static final float FAR_DIST_SQ = 64.0F * 64.0F;      // 4096

    public MosquitoSwarmRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MosquitoSwarmEntity swarm, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (swarm.getMosquitoCount() <= 0) return;

        float distSq = (float) swarm.distanceToSqr(this.entityRenderDispatcher.camera.getEntity());

        // ── Fission interpolation ──────────────────────────────────
        // PoseStack is already at the swarm's interpolated position (set by
        // the rendering pipeline). During fission, we offset backward toward
        // the parent's origin so the child visually "emerges" from the parent.
        if (swarm.isChild() && swarm.getFissionTimer() > 0) {
            Vec3 parentOrigin = swarm.getParentOrigin();
            if (parentOrigin != null) {
                float t = 1.0F - ((float) swarm.getFissionTimer() / MosquitoSwarmEntity.FISSION_DURATION);
                t = t * t * (3.0F - 2.0F * t); // smoothstep
                // Offset from current position toward parent origin, scaled by (1-t)
                Vec3 offset = parentOrigin.subtract(
                        swarm.getX(partialTick),
                        swarm.getY(partialTick),
                        swarm.getZ(partialTick)
                ).scale(1.0 - t);
                poseStack.translate(offset.x, offset.y, offset.z);
            }
        }

        // ── LOD selection ──────────────────────────────────────────
        if (distSq < CLOSE_DIST_SQ) {
            renderClose(swarm, poseStack, buffer, packedLight);
        } else if (distSq < FAR_DIST_SQ) {
            renderMedium(swarm, poseStack, buffer, packedLight);
        } else {
            renderFar(swarm, poseStack, buffer, packedLight);
        }
    }

    /**
     * CLOSE (&lt; 16 blocks): Render individual billboard quads with
     * wing-flapping animation. Uses pseudo-random distribution within the
     * swarm sphere for deterministic particle positions.
     */
    private void renderClose(MosquitoSwarmEntity swarm, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight) {
        float radius = swarm.getSwarmRadius();
        // Render representative instances (cap at 200 for performance)
        int renderCount = Math.min((int) (Math.log(swarm.getMosquitoCount()) / Math.log(2)), 200);
        float time = swarm.tickCount * 0.1F;

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(SWARM_TEXTURE));

        long seed = swarm.getId() * 31L;
        for (int i = 0; i < renderCount; i++) {
            // Pseudo-random position within sphere (deterministic per entity + index)
            double theta = ((seed + i * 7919L) % 36000) / 36000.0 * Math.PI * 2;
            double phi = ((seed + i * 6271L) % 36000) / 36000.0 * Math.PI;
            float r = radius * (float) Math.cbrt(((seed + i * 3571L) % 10000) / 10000.0);

            float x = (float) (r * Math.sin(phi) * Math.cos(theta + time * 0.3));
            float y = (float) (r * Math.cos(phi) + Math.sin(time + i) * 0.5F);
            float z = (float) (r * Math.sin(phi) * Math.sin(theta + time * 0.2));

            // Wing flap animation (simulated by varying billboard size)
            float wingFlap = 0.6F + 0.4F * (float) Math.sin(time * 15.0F + i * 0.7F);
            float size = 0.15F * wingFlap;

            // Billboard quad facing camera
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(size, size, size);

            Matrix4f localMatrix = poseStack.last().pose();
            consumer.vertex(localMatrix, -1, -1, 0).color(20, 15, 15, 180).uv(0, 0).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();
            consumer.vertex(localMatrix, 1, -1, 0).color(20, 15, 15, 180).uv(1, 0).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();
            consumer.vertex(localMatrix, 1, 1, 0).color(25, 18, 18, 160).uv(1, 1).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();
            consumer.vertex(localMatrix, -1, 1, 0).color(25, 18, 18, 160).uv(0, 1).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();

            poseStack.popPose();
        }
    }

    /**
     * MEDIUM (16–64 blocks): Render as a single 2D billboard cloud
     * (flat plane facing camera). Much cheaper — single textured quad.
     */
    private void renderMedium(MosquitoSwarmEntity swarm, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight) {
        float radius = swarm.getSwarmRadius();
        float time = swarm.tickCount * 0.05F;

        // Billboard facing camera
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(radius * 2, radius, 1.0F);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(SWARM_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        // Dark cloud with subtle pulsing alpha
        float alpha = 0.7F + 0.1F * (float) Math.sin(time);
        int alphaInt = (int) (alpha * 200);
        int alphaInt2 = (int) (alpha * 180);
        consumer.vertex(matrix, -1, -1, 0).color(15, 10, 10, alphaInt).uv(0, 0).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, 1, -1, 0).color(15, 10, 10, alphaInt).uv(1, 0).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, 1, 1, 0).color(20, 12, 12, alphaInt2).uv(1, 1).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, -1, 1, 0).color(20, 12, 12, alphaInt2).uv(0, 1).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();

        poseStack.popPose();
    }

    /**
     * FAR (&gt; 64 blocks): Render as volumetric dark cloud.
     * Cheapest LOD — three layered dark planes to suggest volume.
     */
    private void renderFar(MosquitoSwarmEntity swarm, PoseStack poseStack,
                           MultiBufferSource buffer, int packedLight) {
        float radius = swarm.getSwarmRadius() * 1.5F; // slightly larger for atmospheric effect
        float time = swarm.tickCount * 0.02F;

        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // Layered dark planes to simulate volume
        for (int layer = 0; layer < 3; layer++) {
            float layerRadius = radius * (1.0F - layer * 0.2F);
            float yOffset = layer * 1.5F + (float) Math.sin(time + layer) * 2.0F;

            poseStack.pushPose();
            poseStack.translate(0, yOffset, 0);
            poseStack.scale(layerRadius, layerRadius * 0.5F, 1.0F);

            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(SWARM_TEXTURE));
            Matrix4f matrix = poseStack.last().pose();

            int alpha = 120 - layer * 30;
            consumer.vertex(matrix, -1, -1, 0).color(10, 5, 5, alpha).uv(0, 0).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();
            consumer.vertex(matrix, 1, -1, 0).color(10, 5, 5, alpha).uv(1, 0).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();
            consumer.vertex(matrix, 1, 1, 0).color(15, 8, 8, alpha).uv(1, 1).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();
            consumer.vertex(matrix, -1, 1, 0).color(15, 8, 8, alpha).uv(0, 1).overlayCoords(0).uv2(packedLight).normal(0, 1, 0).endVertex();

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MosquitoSwarmEntity entity) {
        return SWARM_TEXTURE;
    }
}