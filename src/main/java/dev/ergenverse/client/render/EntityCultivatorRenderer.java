package dev.ergenverse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ergenverse.client.PerceptionBridge;
import dev.ergenverse.client.model.CultivatorRobeModel;
import dev.ergenverse.client.model.SpiritBeastModelLayers;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.network.ClientCultivationCache;
import dev.ergenverse.perception.PerceptionResult;
import dev.ergenverse.perception.PerceptionTier;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * EntityCultivatorRenderer — renders cultivator NPCs with custom robe model + perception-tier aura.
 *
 * <p>v2 change: Now uses {@link CultivatorRobeModel} (custom humanoid with robe skirt,
 * hair bun, flowing sleeves) instead of vanilla HumanoidModel. This eliminates the
 * "recolored Steve" problem — cultivators now visually wear robes and have a topknot.
 *
 * <h2>Prime Directive compliance</h2>
 * <p>The renderer does NOT change what the entity IS — it adds a VISUAL LAYER
 * that represents the observer's understanding. The entity's objective stats
 * come from canon; the renderer is the perception layer.
 *
 * <h2>Aura colors by perception tier</h2>
 * <ul>
 *   <li>Mortal: no aura (alpha=0)</li>
 *   <li>Qi Condensation: faint white shimmer (alpha=0.08)</li>
 *   <li>Foundation: yellow-green aura (alpha=0.12)</li>
 *   <li>Nascent Soul: blue-purple aura (alpha=0.15)</li>
 *   <li>Soul Formation: full-spectrum Dao aura (alpha=0.20)</li>
 *   <li>Ascendant+: intense golden aura (alpha=0.25)</li>
 * </ul>
 */
public class EntityCultivatorRenderer extends MobRenderer<EntityCultivator, CultivatorRobeModel> {

    private static final ResourceLocation DEFAULT_TEXTURE =
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/cultivator/default.png");

    public EntityCultivatorRenderer(EntityRendererProvider.Context context) {
        super(context, new CultivatorRobeModel(context.bakeLayer(SpiritBeastModelLayers.CULTIVATOR_ROBE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultivator entity) {
        // v2: single placeholder texture for all cultivators.
        // v3: switch on entity.getCharacterId() to return per-character textures.
        return DEFAULT_TEXTURE;
    }

    @Override
    public void render(EntityCultivator entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight) {
        // Set pose flags from SYNCED entity state before super.render calls setupAnim.
        // CRON-COMPLETIONIST-3: Previously these were hardcoded false (TODO).
        // Now EntityCultivator has DATA_POSE synced via SynchedEntityData, and
        // isMeditating()/isCasting() read from it. The model's meditation/casting
        // poses actually fire now.
        // CRON-COMPLETIONIST-31: Added observing and guarding poses — the
        // Cultivator Mind's scoring decisions are now VISIBLE. Wang Lin crouches
        // at the treeline (POSE_OBSERVING) instead of standing idle. Da Niu
        // stands combat-ready at the gate (POSE_GUARDING).
        CultivatorRobeModel model = this.getModel();
        model.setMeditating(entity.isMeditating());
        model.setCasting(entity.isCasting());
        model.setObserving(entity.isObserving());
        model.setGuarding(entity.isGuarding());

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // Only render aura if the client has cultivation data
        if (!ClientCultivationCache.isAvailable()) return;

        PerceptionTier observerTier = ClientCultivationCache.getPerceptionTier();
        if (observerTier.order < PerceptionTier.QI_CONDENSATION.order) return;

        PerceptionResult perception = PerceptionBridge.perceiveEntity(entity);
        if (perception != null && perception.concealed) return;

        // v2: Aura conveyed through name tag color (gray/green/yellow/aqua/purple/gold/white)
        // and Divine Sense HUD (V key). A true translucent overlay requires custom RenderType
        // + GLSL shader — deferred to v3.
    }
}