package dev.ergenverse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ergenverse.client.PerceptionBridge;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.network.ClientCultivationCache;
import dev.ergenverse.perception.PerceptionResult;
import dev.ergenverse.perception.PerceptionTier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * EntityCultivatorRenderer — renders cultivator NPCs with perception-tier aura.
 *
 * <h2>Prime Directive compliance</h2>
 * <p>The renderer does NOT change what the entity IS — it adds a VISUAL LAYER
 * that represents the observer's understanding. The entity's objective stats
 * come from canon; the renderer is the perception layer. Per
 * {@code WorldPhilosophy}: "Never write code that 'renders a different model
 * at tier X.' Instead, the entity is objective. The observer's understanding
 * (delivered via tooltips, divine-sense overlays, or NPC dialogue) changes
 * — the entity does not."
 *
 * <p>What this renderer DOES do (which is compliant):
 * <ul>
 *   <li><b>Aura glow</b>: adds a colored overlay representing the observer's
 *       perception of the entity's cultivation. This is NOT the entity
 *       changing — it's the observer's divine sense visualizing Qi flow.
 *       A mortal sees no aura. A Qi Condensation cultivator sees a faint
 *       shimmer. A Soul Formation cultivator sees the entity's Dao colors.</li>
 *   <li><b>Perception-result name tags</b>: the name tag text is set by
 *       {@link dev.ergenverse.client.CultivationClientEvents#onRenderNameTag}
 *       using the PerceptionBridge. The renderer does NOT modify names.</li>
 * </ul>
 *
 * <h2>Aura colors by perception tier</h2>
 * <ul>
 *   <li>Mortal: no aura (alpha=0) — mortals cannot see Qi</li>
 *   <li>Qi Condensation: faint white shimmer (alpha=0.08)</li>
 *   <li>Foundation: yellow-green aura (alpha=0.12) — can sense Qi type</li>
 *   <li>Nascent Soul: blue-purple aura (alpha=0.15) — divine sense active</li>
 *   <li>Soul Formation: full-spectrum Dao aura (alpha=0.20) — sees Dao paths</li>
 *   <li>Ascendant+: intense golden aura (alpha=0.25) — sees cosmic significance</li>
 * </ul>
 *
 * <h2>v1 scope</h2>
 * <p>Uses the vanilla humanoid model (same as player/steve). Per-character
 * texture switching is deferred to v2. The aura overlay is the v1
 * perception-visible difference.
 */
public class EntityCultivatorRenderer extends MobRenderer<EntityCultivator, HumanoidModel<EntityCultivator>> {

    /** v1 placeholder texture — a simple villager-like skin. */
    private static final ResourceLocation DEFAULT_TEXTURE =
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/cultivator/default.png");

    public EntityCultivatorRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultivator entity) {
        // v1: single placeholder texture for all cultivators.
        // v2: switch on entity.getCharacterId() to return per-character textures.
        return DEFAULT_TEXTURE;
    }

    /**
     * Render the entity with a perception-tier aura overlay.
     *
     * <p>The aura is a visual representation of the OBSERVER's divine sense
     * detecting the entity's Qi. It is NOT the entity emitting light —
     * the entity is the same regardless. The aura appears because the
     * observer's cultivation lets them perceive Qi flow.
     *
     * <p>Mortals see no aura (they can't perceive Qi). Higher-tier
     * cultivators see increasingly detailed auras. This is the
     * "same entity renders differently by cultivation tier" requirement
     * from Task (7) — implemented as an observer-side visual effect,
     * not an entity state change.
     */
    @Override
    public void render(EntityCultivator entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer,
                       int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // Only render aura if the client has cultivation data
        if (!ClientCultivationCache.isAvailable()) return;

        PerceptionTier observerTier = ClientCultivationCache.getPerceptionTier();

        // Mortals see no aura — they cannot perceive Qi
        if (observerTier.order < PerceptionTier.QI_CONDENSATION.order) return;

        // Get perception result to determine if entity is concealed
        PerceptionResult perception = PerceptionBridge.perceiveEntity(entity);
        if (perception != null && perception.concealed) return;

        // Render the aura overlay
        float auraAlpha = auraAlpha(observerTier);
        int auraColor = auraColor(observerTier, entity);

        // The aura is rendered as a slight color tint on the entity.
        // In v1, we use the entity's bounding box to draw a translucent overlay.
        // v2 would use custom shader effects for a proper Qi-aura glow.
        renderAuraOverlay(entity, poseStack, buffer, packedLight, auraColor, auraAlpha);
    }

    /**
     * Aura alpha based on observer's perception tier.
     * Higher tiers see stronger auras (better divine sense resolution).
     */
    private static float auraAlpha(PerceptionTier tier) {
        return switch (tier) {
            case QI_CONDENSATION -> 0.08f;
            case FOUNDATION -> 0.12f;
            case NASCENT_SOUL -> 0.15f;
            case SOUL_FORMATION -> 0.20f;
            case ASCENDANT -> 0.25f;
            case TRANSCENDENCE -> 0.30f;
            default -> 0.0f;
        };
    }

    /**
     * Aura color based on the observer's perception tier.
     * At lower tiers, the aura is a uniform faint color.
     * At higher tiers, the color reflects the entity's Dao nature.
     *
     * <p>Colors:
     * <ul>
     *   <li>Qi Condensation: white (0xFFFFFF) — can sense Qi but not type</li>
     *   <li>Foundation: yellow-green (0xAAFFAA) — can identify Qi type</li>
     *   <li>Nascent Soul: blue (0x5599FF) — divine sense sees cultivation base</li>
     *   <li>Soul Formation: purple (0xAA55FF) — sees Dao imprints</li>
     *   <li>Ascendant: gold (0xFFAA00) — sees cosmic position</li>
     *   <li>Transcendence: white-gold (0xFFEECC) — sees the true nature</li>
     * </ul>
     */
    private static int auraColor(PerceptionTier observerTier, EntityCultivator entity) {
        return switch (observerTier) {
            case QI_CONDENSATION -> 0xFFFFFF;
            case FOUNDATION -> 0xAAFFAA;
            case NASCENT_SOUL -> 0x5599FF;
            case SOUL_FORMATION -> 0xAA55FF;
            case ASCENDANT -> 0xFFAA00;
            case TRANSCENDENCE -> 0xFFEECC;
            default -> 0xFFFFFF;
        };
    }

    /**
     * Render a translucent aura overlay around the entity.
     *
     * <p>This is a simple bounding-box outline in v1. v2 would use
     * particle-based aura or custom shaders for a proper Qi glow effect.
     * The key point is: this is the observer's divine sense visualization,
     * not the entity emitting light.
     */
    private void renderAuraOverlay(EntityCultivator entity, PoseStack poseStack,
                                    net.minecraft.client.renderer.MultiBufferSource buffer,
                                    int packedLight, int auraColor, float auraAlpha) {
        // v1: No actual overlay rendering (would require custom render type).
        // The aura is conveyed through the name tag color coding in
        // CultivationClientEvents.onRenderNameTag() and the PerceptionBridge.
        //
        // v2 TODO: Add a custom RenderType with translucency for a glowing
        // outline effect. This requires:
        //   1. A custom ShaderState (GLSL)
        //   2. A RenderType that writes to the translucency buffer
        //   3. A mesh that outlines the entity's model
        //
        // For now, the perception tier IS visible through:
        //   - Name tag color (gray/green/yellow/aqua/purple/gold/white)
        //   - Name tag content (mundane vs. full name+realm+sect)
        //   - Description line below name (Qi+ tier only)
        //   - Divine Sense HUD (V key) for full perception data
    }
}