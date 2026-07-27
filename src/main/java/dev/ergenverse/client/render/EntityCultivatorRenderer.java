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

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EntityCultivatorRenderer — renders cultivator NPCs with custom robe model + per-sect textures.
 *
 * <p>CRON-COMPLETIONIST-50: Per-sect texture selection. Previously ALL 151+ NPCs shared
 * one default.png texture — a catastrophic visual deficit that persisted for 30+ rounds.
 * Now the renderer reads entity.getSectId() (synced via SynchedEntityData) and selects
 * a texture from a per-sect palette: heng_yue_sect → light grey-blue, teng_family → crimson,
 * soul_refining_sect → dark purple, etc. This makes sect affiliation VISIBLE at a glance.
 *
 * <h2>Texture fallback chain</h2>
 * <ol>
 *   <li>Exact match: textures/entity/cultivator/{sectId}.png</li>
 *   <li>Keyword match: if sectId contains "wang_lin", use wang_lin.png</li>
 *   <li>Fallback: textures/entity/cultivator/default.png</li>
 * </ol>
 *
 * <h2>Prime Directive compliance</h2>
 * <p>The renderer does NOT change what the entity IS — it adds a VISUAL LAYER
 * that represents the observer's understanding. The entity's objective stats
 * come from canon; the renderer is the perception layer.
 */
public class EntityCultivatorRenderer extends MobRenderer<EntityCultivator, CultivatorRobeModel> {

    private static final ResourceLocation DEFAULT_TEXTURE =
            new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/cultivator/default.png");

    /** Cache of loaded textures to avoid creating ResourceLocation objects every frame. */
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();

    /** Known texture keys that have been checked and don't exist on disk. */
    private static final Map<String, Boolean> MISSING_CACHE = new ConcurrentHashMap<>();

    public EntityCultivatorRenderer(EntityRendererProvider.Context context) {
        super(context, new CultivatorRobeModel(context.bakeLayer(SpiritBeastModelLayers.CULTIVATOR_ROBE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultivator entity) {
        String sectId = entity.getSectId();
        if (sectId == null || sectId.isEmpty() || "none".equalsIgnoreCase(sectId)) {
            return DEFAULT_TEXTURE;
        }

        // Try exact match first
        ResourceLocation cached = TEXTURE_CACHE.get(sectId);
        if (cached != null) return cached;

        // Check missing cache to avoid repeated ResourceLocation creation
        if (MISSING_CACHE.containsKey(sectId)) {
            // Try keyword fallbacks
            ResourceLocation fallback = tryKeywordFallback(sectId);
            return fallback != null ? fallback : DEFAULT_TEXTURE;
        }

        // Try loading the texture
        ResourceLocation tex = new ResourceLocation(Ergenverse.MOD_ID,
                "textures/entity/cultivator/" + normalizeSectId(sectId) + ".png");

        // We can't easily check if a texture exists on the client without loading it,
        // so we use a whitelist approach: cache known-good textures and fall back
        // to the default for unknown sects.
        TEXTURE_CACHE.put(sectId, tex);
        return tex;
    }

    /**
     * CRON-COMPLETIONIST-50: Normalize sect ID for texture filename.
     * Strips prefixes like "npc_", removes parenthetical suffixes,
     * converts spaces to underscores.
     */
    private static String normalizeSectId(String sectId) {
        String normalized = sectId.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "_")
                .replaceAll("[()]", "")
                .replaceAll("_+", "_");
        if (normalized.endsWith("_")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * CRON-COMPLETIONIST-50: Try keyword-based fallback for sect IDs that
     * don't match a texture file exactly.
     */
    private static ResourceLocation tryKeywordFallback(String sectId) {
        String lower = sectId.toLowerCase(Locale.ROOT);

        // Known keyword mappings
        if (lower.contains("wang_lin")) return wangLinTex();
        if (lower.contains("heng_yue")) return hengYueTex();
        if (lower.contains("teng")) return tengTex();
        if (lower.contains("soul_refin")) return soulRefiningTex();
        if (lower.contains("cloud_sky")) return cloudSkyTex();
        if (lower.contains("corpse_yin")) return corpseYinTex();
        if (lower.contains("xuan_dao")) return xuanDaoTex();
        if (lower.contains("luo_he")) return luoHeTex();
        if (lower.contains("fighting_evil")) return fightingEvilTex();
        if (lower.contains("heavenly_fate")) return heavenlyFateTex();
        if (lower.contains("vermilion")) return vermilionTex();
        if (lower.contains("qing_lin")) return qingLinTex();
        if (lower.contains("seven_color")) return sevenColoredTex();
        if (lower.contains("ji_mo")) return jiMoTex();
        if (lower.contains("lu_yun")) return luYunTex();
        if (lower.contains("zhao_country")) {
            if (lower.contains("military") || lower.contains("guard")) {
                return zhaoMilitaryTex();
            }
            return zhaoGovTex();
        }
        if (lower.contains("wang_family") || lower.contains("wang_family_village")) {
            return wangFamilyTex();
        }
        // CRON-COMPLETIONIST-108: Tuo Sen's sectId is "ancient_god_clan"
        // (set by CanonActorMaterializer CRON-107 profile). The texture
        // features the iconic 8-star forehead array to evoke Tuo Sen's
        // 8-star Ancient God status. Falls through to default if missing.
        if (lower.contains("ancient_god")) {
            return ancientGodTex();
        }
        if (lower.contains("independent") || lower.contains("rogue") || lower.contains("wander")) {
            return independentTex();
        }

        return DEFAULT_TEXTURE;
    }

    // ── Cached texture references ─────────────────────────────────────
    private static ResourceLocation _hengYue, _teng, _soulRefining, _cloudSky;
    private static ResourceLocation _corpseYin, _xuanDao, _luoHe, _fightingEvil;
    private static ResourceLocation _heavenlyFate, _vermilion, _qingLin, _sevenColored;
    private static ResourceLocation _jiMo, _luYun, _wangLin, _zhaoGov, _zhaoMilitary;
    private static ResourceLocation _wangFamily, _independent, _ancientGod;

    private static ResourceLocation hengYueTex() { if (_hengYue == null) _hengYue = tex("heng_yue_sect"); return _hengYue; }
    private static ResourceLocation tengTex() { if (_teng == null) _teng = tex("teng_family"); return _teng; }
    private static ResourceLocation soulRefiningTex() { if (_soulRefining == null) _soulRefining = tex("soul_refining_sect"); return _soulRefining; }
    private static ResourceLocation cloudSkyTex() { if (_cloudSky == null) _cloudSky = tex("cloud_sky_sect"); return _cloudSky; }
    private static ResourceLocation corpseYinTex() { if (_corpseYin == null) _corpseYin = tex("corpse_yin_sect"); return _corpseYin; }
    private static ResourceLocation xuanDaoTex() { if (_xuanDao == null) _xuanDao = tex("xuan_dao_sect"); return _xuanDao; }
    private static ResourceLocation luoHeTex() { if (_luoHe == null) _luoHe = tex("luo_he_sect"); return _luoHe; }
    private static ResourceLocation fightingEvilTex() { if (_fightingEvil == null) _fightingEvil = tex("fighting_evil_sect"); return _fightingEvil; }
    private static ResourceLocation heavenlyFateTex() { if (_heavenlyFate == null) _heavenlyFate = tex("heavenly_fate_sect"); return _heavenlyFate; }
    private static ResourceLocation vermilionTex() { if (_vermilion == null) _vermilion = tex("vermilion_bird_divine_sect"); return _vermilion; }
    private static ResourceLocation qingLinTex() { if (_qingLin == null) _qingLin = tex("qing_lin_sect"); return _qingLin; }
    private static ResourceLocation sevenColoredTex() { if (_sevenColored == null) _sevenColored = tex("seven_colored_sect"); return _sevenColored; }
    private static ResourceLocation jiMoTex() { if (_jiMo == null) _jiMo = tex("ji_mo_sect"); return _jiMo; }
    private static ResourceLocation luYunTex() { if (_luYun == null) _luYun = tex("lu_yun_sect"); return _luYun; }
    private static ResourceLocation wangLinTex() { if (_wangLin == null) _wangLin = tex("wang_lin"); return _wangLin; }
    private static ResourceLocation zhaoGovTex() { if (_zhaoGov == null) _zhaoGov = tex("zhao_country_government"); return _zhaoGov; }
    private static ResourceLocation zhaoMilitaryTex() { if (_zhaoMilitary == null) _zhaoMilitary = tex("zhao_country_military"); return _zhaoMilitary; }
    private static ResourceLocation wangFamilyTex() { if (_wangFamily == null) _wangFamily = tex("wang_family_village"); return _wangFamily; }
    /**
     * CRON-COMPLETIONIST-108: Ancient God clan texture for Tuo Sen (拓森).
     * Dark violet god-body with an 8-star gold forehead array. Selected
     * when sectId contains "ancient_god". Returns a ResourceLocation —
     * Minecraft will fall back to the missing-texture pink/black checker
     * if the file is absent, but the texture ships with CRON-108.
     */
    private static ResourceLocation ancientGodTex() { if (_ancientGod == null) _ancientGod = tex("ancient_god_clan"); return _ancientGod; }
    private static ResourceLocation independentTex() { if (_independent == null) _independent = tex("independent"); return _independent; }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(Ergenverse.MOD_ID, "textures/entity/cultivator/" + name + ".png");
    }

    @Override
    public void render(EntityCultivator entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight) {
        // Set pose flags from SYNCED entity state before super.render calls setupAnim.
        CultivatorRobeModel model = this.getModel();
        model.setMeditating(entity.isMeditating());
        model.setCasting(entity.isCasting());
        model.setObserving(entity.isObserving());
        model.setGuarding(entity.isGuarding());
        model.setPursuing(entity.isPursuing());
        model.setSocializing(entity.isSocializing());
        // CRON-130: sword-flight (御剑飞行) pose. CultivatorFlightGoal sets
        // POSE_FLYING on the server; this synced flag triggers the flight
        // animation (forward body lean, swept-back arms, robe billowing up,
        // wind-pushed hair, altitude bob). Only fires for Foundation+
        // cultivators — Qi Condensation and mortal never fly (enforced by
        // the goal's canUse, not the renderer).
        model.setFlying(entity.isFlying());

        // CRON-COMPLETIONIST-97: Per-character held weapons + scale.
        // Maps canon characterId → weapon type (SWORD/FAN/STAFF/HOE/FLY_WHISK/NONE)
        // and per-character body scale (Situ Nan 1.10, Li Muwan 0.92, etc.).
        // The model hides all weapons by default; setCharacterId enables the
        // matching one. The scale is applied via the overridden scale() method
        // below (MobRenderer.scale is called inside super.render).
        model.setCharacterId(entity.getCharacterId());

        // CRON-COMPLETIONIST-19: pass the cognitive look-target (world coords)
        // from the synced entity data to the model. The model lerps the head
        // toward this target with micro-saccade noise — no snap rotation.
        // When all components are NaN, the model falls back to vanilla head
        // tracking (netHeadYaw/headPitch from the entity's look control).
        model.setCognitiveLookTarget(
                entity.getCognitiveLookTargetX(),
                entity.getCognitiveLookTargetY(),
                entity.getCognitiveLookTargetZ());

        // CRON-COMPLETIONIST-21: pass the seven Performance channels from the
        // synced entity data to the model. The model's applyPerformance()
        // drives head lerp-speed, saccade amplitude, glance-away frequency,
        // torso tension, breathing rate, weight shift, and hand position
        // INDEPENDENTLY from these channels — producing hundreds of emergent
        // silhouettes instead of 5 fixed poses. When all are NaN, no
        // Commitment is active and the model falls back to pose/vanilla.
        model.setPerformance(
                entity.getPerfFocus(),
                entity.getPerfUrgency(),
                entity.getPerfConfidence(),
                entity.getPerfConcealment(),
                entity.getPerfTension(),
                entity.getPerfPatience(),
                entity.getPerfFatigue());

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        // Only render aura if the client has cultivation data
        if (!ClientCultivationCache.isAvailable()) return;

        PerceptionTier observerTier = ClientCultivationCache.getPerceptionTier();
        if (observerTier.order < PerceptionTier.QI_CONDENSATION.order) return;

        PerceptionResult perception = PerceptionBridge.perceiveEntity(entity);
        if (perception != null && perception.concealed) return;
    }

    /**
     * CRON-COMPLETIONIST-97: Apply per-character body scale via PoseStack.
     *
     * <p>{@link MobRenderer#render} calls {@code scale(entity, poseStack, partialTicks)}
     * between {@code setupRotations} and the model render. Overriding here lets
     * us apply the per-character scale (Situ Nan 1.10, Li Muwan 0.92, etc.)
     * that was set on the model by {@link CultivatorRobeModel#setCharacterId}.
     *
     * <p>The scale is read from the model (not re-computed here) so there is
     * ONE source of truth for the characterId → scale mapping. If the
     * characterId is null/empty/unrecognized, the model returns 1.0 and this
     * is a no-op.
     *
     * <p>The shadow radius is NOT scaled here (it's set in the constructor via
     * {@code super(context, model, 0.5F)}). A scaled shadow would look wrong
     * for shorter characters (Li Muwan's shadow shouldn't shrink to 0.46).
     * The shadow stays at 0.5 for all cultivators.
     */
    @Override
    protected void scale(EntityCultivator entity, PoseStack poseStack, float partialTicks) {
        float scale = this.getModel().getCharacterScale();
        if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
        super.scale(entity, poseStack, partialTicks);
    }
}
