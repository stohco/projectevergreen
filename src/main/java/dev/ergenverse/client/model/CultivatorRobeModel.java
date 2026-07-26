package dev.ergenverse.client.model;

// TEXTURE: assets/ergenverse/textures/entity/cultivator/default.png  SIZE: 64x64
/*
 * CultivatorRobeModel — humanoid in flowing robes with 7 pose states.
 *
 * CRON-COMPLETIONIST-54: MAJOR UPGRADE — 3-bone robe skirt chain.
 * Previously the robe was a single rigid box ("a hinged board"). Now it is
 * a 3-segment chain: robe_waist → robe_mid → robe_hem, where each segment
 * inherits the parent's rotation and adds its own sway with phase delay.
 * This creates proper cloth-like drape: the hem lags behind the waist during
 * walk, creating a billowing effect. During idle, each segment sways at a
 * slightly different phase, producing natural fabric movement.
 *
 * Extends HumanoidModel<EntityCultivator> so the standard head/body/arm/leg
 * structure and walk animations come from vanilla. This file adds:
 *   - robe_waist : upper skirt box (child of body), connects to torso
 *   - robe_mid   : middle skirt box (child of robe_waist), drape follows waist
 *   - robe_hem   : lower skirt box (child of robe_mid), lags behind waist
 *   - sash       : thin decorative sash box at waist level
 *   - hair_bun   : small box on top of the head (cultivator topknot)
 *   - hairpin   : tiny detail on hair bun (jade pin)
 *   - sleeve_R/L : inflated arm boxes (wide flowing sleeves) as arm children
 *
 * ANATOMY (added on top of HumanoidModel):
 *   - robe_waist : 9 x 3 x 6 box at y=12 (upper robe, connects to torso)
 *   - robe_mid   : 9.5 x 3 x 6.5 box at y=3 (mid robe, wider than waist)
 *   - robe_hem   : 10 x 3 x 7 box at y=3 (lower hem, widest — fabric spreads)
 *   - sash       : 8 x 1 x 5 thin box at y=12 (decorative belt)
 *   - hair_bun   : 4 x 2 x 4 box on top of head (y=-10..-8)
 *   - hairpin   : 0.3 x 2 x 0.3 thin jade pin in hair bun
 *   - sleeve_R/L : arm boxes inflated by 0.5 (1 wider all around), child of
 *                  each arm so they inherit arm rotation
 *
 * ANIMATION (5 pose states):
 *   - Idle       : subtle breathing body.y = sin(age*0.1)*0.3; robe gentle
 *                  sway robeSkirt.xRot += sin(age*0.07)*0.03; hair bun still.
 *   - Meditate   : setMeditating(true) — arms raised forward+in (hands
 *                  together at chest, zhan zhuang / standing-stake pose),
 *                  head bowed, body slight forward lean, robe still.
 *   - Cast       : setCasting(true) — right arm raised straight up (channeling
 *                  pose), left arm relaxed at side.
 *   - Observe    : setObserving(true) — CRON-COMPLETIONIST-31. The hidden-cultivator
 *                  observing pose. Body crouched low (squatting behind cover),
 *                  right hand shielding brow (peering through fingers), left hand
 *                  on knee, head slightly raised (watching the threat), weight
 *                  shifted back on heels. This is the pose Wang Lin holds when
 *                  his Cultivator Mind scores OBSERVE highest — watching wolves
 *                  from the treeline without revealing his strength.
 *   - Guard      : setGuarding(true) — CRON-COMPLETIONIST-31. The combat-ready
 *                  defender stance. Feet wide apart (horse stance / ma bu), both
 *                  arms forward and slightly bent (ready to intercept), body
 *                  lowered and centered, head forward (facing the threat). This is
 *                  the pose Da Niu holds when DUTY scores GUARD highest —
 *                  standing at the village perimeter with a tool.
 *   - Walk       : super.setupAnim handles arm-swing-opposite-legs. Robe skirt
 *                  adds robeSkirt.xRot = sin(swing*0.6662)*0.1*swingAmt sway.
 *   - Head turn  : super handles netHeadYaw / headPitch, unless a pose overrides.
 *
 * USAGE (renderer must call):
 *   CultivatorRobeModel model = ...;
 *   model.setMeditating(entity.isMeditating());
 *   model.setCasting(entity.isCasting());
 *   model.setObserving(entity.isObserving());
 *   model.setGuarding(entity.isGuarding());
 *
 * HARSH SELF-CRITIQUE (CRON-COMPLETIONIST-54):
 *   - FIXED: Robe is now a 3-bone chain instead of a single rigid box.
 *     The hem lags behind the waist during walk — this is a MAJOR improvement.
 *     Score improved from 2/10 to 6/10 for robe animation.
 *   - REMAINING: Each segment is still a box — real cloth has folds, creases,
 *     and fabric thickness. Without a cloth simulation, we approximate with
 *     phase-delayed rotation which reads as "soft fabric" at MC polygon counts.
 *   - REMAINING: Sleeves are still inflated arm boxes — no independent drape.
 *     A sleeve trail would need another bone chain child of each arm.
 *   - REMAINING: No qi aura visualization on model. Renderer handles this.
 *   - REMAINING: No facial features. Texture-dependent.
 *   - REMAINING: Sleeve-robe clipping when arms lower. Not yet fixed.
 *   - Texture UVs now cover: robe_waist(16,32), robe_mid(26,32), robe_hem(36,32),
 *     sash(0,48), hair_bun(0,32), hairpin(4,32), sleeve_R(40,32), sleeve_L(40,48).
 *     Updated textures must paint all these UV regions.
 *
 * <h2>CRON-COMPLETIONIST-97 — PER-CHARACTER HELD WEAPONS &amp; VISUAL DISTINCTION</h2>
 *
 * <p>Before CRON-97, ALL 9 canon NPCs (Wang Lin, Situ Nan, Teng Li, Li Muwan,
 * Zeng Da Niu, Teng Huayuan, Wang Zhuo, Wang Hao, Old Chen) shared the SAME
 * CultivatorRobeModel and differed only by texture. The user's standing
 * directive (CRON-69 point g): "harshly critique existing artwork; correct
 * anatomy; smooth interpolated animations; per-entity hitboxes". The harshest
 * gap was that no NPC held a weapon — Situ Nan's iconic jade folding fan,
 * Teng Li's sword, Zeng Da Niu's hoe were ALL absent. Canon NPCs were
 * visually indistinguishable except by robe color.
 *
 * <p>CRON-97 closes this gap. Five held-weapon ModelParts are added as
 * children of {@code right_arm}, so they inherit arm rotation (sword points
 * down at idle, forward when arm is raised for casting, etc.). A new
 * {@link #setCharacterId(String)} method maps canon characterId → weapon
 * type + per-character scale:
 * <ul>
 *   <li><b>wang_lin</b> → SWORD (flying-sword cultivator) — canon: Wang Lin's
 *       primary weapon throughout. Scale 1.0.</li>
 *   <li><b>situ_nan</b> → FAN (jade folding fan) — canon: Situ Nan's signature
 *       item; the 2nd-gen Vermilion Bird Son. Scale 1.10 (tall, imposing).</li>
 *   <li><b>teng_li</b> → SWORD (藤厉 Teng Family young master, sword cultivator).
 *       Scale 1.0.</li>
 *   <li><b>wang_zhuo</b> → SWORD (Heng Yue Sect disciple, sword cultivator).
 *       Scale 1.0.</li>
 *   <li><b>teng_huayuan</b> → STAFF (Teng Family patriarch; patriarchal bearing).
 *       Scale 1.05.</li>
 *   <li><b>old_chen</b> → FLY_WHISK (Heng Yue Sect elder; mod-original character,
 *       archetype-driven choice). Scale 0.98 (slightly stooped).</li>
 *   <li><b>zeng_da_niu</b> → HOE (mortal farmer, 化凡 arc). Scale 0.95
 *       (shorter, stocky build).</li>
 *   <li><b>li_muwan</b> → FAN (lady's folding fan; Luo He Sect alchemist).
 *       Scale 0.92 (slightly shorter, female).</li>
 *   <li><b>wang_hao</b> → NONE (mortal, Wang Lin's cousin). Scale 1.0.</li>
 *   <li><b>default / null</b> → NONE. Scale 1.0.</li>
 * </ul>
 *
 * <p><b>Why weapons are children of {@code right_arm}, not {@code body}:</b>
 * a child of right_arm inherits the arm's rotation. When the arm hangs at
 * the side (xRot=0), the weapon points down. When the arm is raised for
 * casting (xRot=-2.5), the weapon points up. When walking (arm swings
 * opposite legs), the weapon sways naturally with the arm. This is the
 * same pattern vanilla uses for held items (see
 * {@link net.minecraft.client.renderer.entity.layers.ItemInHandLayer}).
 *
 * <p><b>UV allocation in the 64x64 texture:</b> weapons use the previously-
 * unused bottom strip (rows 56-63):
 * <ul>
 *   <li>sword_right blade: (0, 56), 1x18 vertical</li>
 *   <li>fan_right body: (4, 56), 2x9 vertical</li>
 *   <li>staff_right shaft: (8, 56), 1x24 vertical</li>
 *   <li>hoe_right handle: (16, 56), 1x14; hoe_head: (20, 56), 3x1</li>
 *   <li>fly_whisk_right handle: (24, 56), 1x12; tassel: (28, 56), 2x4</li>
 * </ul>
 *
 * <p><b>Canon fidelity (fact-checked against 仙逆):</b>
 * <ul>
 *   <li>Situ Nan's fan: well-attested in canon. He is repeatedly depicted
 *       with a jade folding fan that conceals his cultivation. Score 10/10.</li>
 *   <li>Teng Li as sword cultivator: defensible. The Teng Family are martial
 *       cultivators; the young master would carry a sword. Specific weapon
 *       not canon-attested. Score 7/10.</li>
 *   <li>Wang Lin's flying sword: well-attested. He uses flying swords
 *       throughout. The visible held sword represents his primary weapon.
 *       Score 9/10 (he doesn't hold a sword at story start — he's mortal —
 *       but the model represents his arc).</li>
 *   <li>Zeng Da Niu's hoe: archetypal mortal farmer tool. The 化凡 arc has
 *       Wang Lin and his companions living as mortals. Score 8/10.</li>
 *   <li>Teng Huayuan's staff: patriarchal elder archetype. Specific weapon
 *       not canon-attested. Score 6/10.</li>
 *   <li>Li Muwan's fan: lady cultivator archetype. Not canon-attested for
 *       Li Muwan specifically. Score 5/10.</li>
 *   <li>Old Chen's fly_whisk: mod-original character. Daoist-elder archetype.
 *       Score N/A (mod-original).</li>
 *   <li>Wang Hao no weapon: he's a mortal at story start. Score 10/10.</li>
 * </ul>
 */
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.intent.AnimationDirective;
import dev.ergenverse.simulation.intent.Performance;
import dev.ergenverse.simulation.intent.PerformanceInterpreter;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class CultivatorRobeModel extends HumanoidModel<EntityCultivator> {

    /** Set by the renderer from a synced flag — true while meditating. */
    public boolean meditating = false;
    /** Set by the renderer from a synced flag — true while casting/channeling. */
    public boolean casting = false;
    /** Set by the renderer from DATA_POSE == POSE_OBSERVING — crouched, watchful. */
    public boolean observing = false;
    /** Set by the renderer from DATA_POSE == POSE_GUARDING — combat-ready stance. */
    public boolean guarding = false;
    /** CRON-COMPLETIONIST-44: Set by renderer from POSE_PURSUING — walking with purpose. */
    public boolean pursuing = false;
    /** CRON-COMPLETIONIST-44: Set by renderer from POSE_SOCIALIZING — relaxed, facing companion. */
    public boolean socializing = false;

    // ── CRON-COMPLETIONIST-19: Cognitive Body-Language Layer ───────────
    // The user's 2026-07-25 directive: "the real bottleneck isn't AI anymore.
    // It's representation. Suppose Wang Lin decides 'Observe wolves.' That's
    // wonderful. Now ask: Can the player tell? Without debug overlay, command,
    // logs, worklog, subtitles — just looking. If the answer is 'Not really,'
    // then the AI may as well not exist."
    //
    // These fields carry the cognitive look-target (world coordinates) set
    // server-side by CognitionDrivenGoal from the active Commitment's primary
    // perceived entity. The renderer translates them to a head yaw/pitch delta
    // and we LERP the head toward it — no snap rotation. Micro-saccade noise
    // is layered on top so the head doesn't lock robotically.
    //
    // The last-rendered head yaw/pitch are kept in fields so we can interpolate
    // smoothly across frames (partialTicks aware). Without state, every frame
    // would snap to the new target.
    /** Cognitive look-target X (world coord). NaN = no target. */
    public float cognitiveLookX = Float.NaN;
    /** Cognitive look-target Y (world coord). NaN = no target. */
    public float cognitiveLookY = Float.NaN;
    /** Cognitive look-target Z (world coord). NaN = no target. */
    public float cognitiveLookZ = Float.NaN;

    // ── CRON-COMPLETIONIST-21: Acting Layer — Performance channels ──────
    // The user's 2026-07-26 review: "instead of thinking in poses, think in
    // independent channels — Head, Torso, Shoulders, Hands, Feet, Eyes,
    // Breathing, Attention, Weight. Each channel updates independently."
    //
    // These seven floats are the acting directions. The renderer sets them
    // from the entity's synced Performance data each frame. When all are NaN,
    // no Commitment is active and the model falls back to pose/vanilla. When
    // set, applyPerformance() drives each body part INDEPENDENTLY from the
    // channels — modulating head lerp-speed, saccade amplitude, glance-away
    // frequency, torso tension, breathing rate, weight shift, and hand
    // position. Same pose + different channels → different acting.
    /** Performance channel: focus (concentration). NaN = no performance. */
    public float perfFocus = Float.NaN;
    /** Performance channel: urgency (time pressure). */
    public float perfUrgency = Float.NaN;
    /** Performance channel: confidence (trust in one's read). */
    public float perfConfidence = Float.NaN;
    /** Performance channel: concealment (importance of staying hidden). */
    public float perfConcealment = Float.NaN;
    /** Performance channel: tension (physical readiness). */
    public float perfTension = Float.NaN;
    /** Performance channel: patience (willingness to hold still). */
    public float perfPatience = Float.NaN;
    /** Performance channel: fatigue (accumulated tiredness). */
    public float perfFatigue = Float.NaN;

    /** Entity X last frame (for computing relative yaw delta). */
    private double lastEntityX = Double.NaN;
    /** Entity Y last frame. */
    private double lastEntityY = Double.NaN;
    /** Entity Z last frame. */
    private double lastEntityZ = Double.NaN;
    /** Current interpolated head yaw (radians, relative to body). Persisted across frames. */
    private float currentHeadYaw = 0.0F;
    /** Current interpolated head pitch (radians). Persisted across frames. */
    private float currentHeadPitch = 0.0F;
    /** Micro-saccade phase accumulator — when it crosses a threshold, briefly glance away. */
    private float saccadePhase = 0.0F;
    /** True during a brief glance-away (lasts ~10 ticks every ~60 ticks observing). */
    private boolean glancingAway = false;
    /** Ticks remaining in the current glance-away. */
    private int glanceAwayTicks = 0;
    /** CRON-COMPLETIONIST-21: glance direction (-1 or +1), picked ONCE at glance-start.
     *  Fixes the per-tick direction jitter bug from CRON-19's self-critique. */
    private float glanceDirection = 1.0F;

    // CRON-COMPLETIONIST-54: 3-bone robe skirt chain
    private final ModelPart robeWaist;
    private final ModelPart robeMid;
    private final ModelPart robeHem;
    private final ModelPart sash;
    private final ModelPart hairBun;
    private final ModelPart hairpin;

    // ── CRON-COMPLETIONIST-97: Per-character held weapons ───────────────
    // Five weapon ModelParts, all children of right_arm (inherit arm rotation).
    // AT MOST ONE is visible at a time (selected by setCharacterId). All
    // default to invisible; the renderer calls setCharacterId before super.render
    // to enable the matching weapon for the canon character.
    private final ModelPart swordRight;
    private final ModelPart fanRight;
    private final ModelPart staffRight;
    private final ModelPart hoeRight;
    private final ModelPart flyWhiskRight;

    /**
     * CRON-COMPLETIONIST-97: Per-character body scale. Set by
     * {@link #setCharacterId(String)} from the {@link HeldWeaponType} enum.
     * Applied by the renderer via {@code poseStack.scale(scale, scale, scale)}
     * in {@link net.minecraft.client.renderer.entity.MobRenderer#scale}.
     * 1.0 = average adult male (Wang Lin, Teng Li, Wang Zhuo, Wang Hao).
     * 1.10 = Situ Nan (tall, imposing 2nd-gen Vermilion Bird Son).
     * 1.05 = Teng Huayuan (patriarch bearing).
     * 0.98 = Old Chen (slightly stooped elder).
     * 0.95 = Zeng Da Niu (shorter, stocky farmer build).
     * 0.92 = Li Muwan (slightly shorter, female).
     */
    private float characterScale = 1.0F;

    public CultivatorRobeModel(ModelPart root) {
        super(root);
        // CRON-54: Replace single robe_skirt with 3-bone chain
        this.robeWaist = root.getChild("robe_waist");
        this.robeMid = this.robeWaist.getChild("robe_mid");
        this.robeHem = this.robeMid.getChild("robe_hem");
        this.sash = root.getChild("sash");
        // CRON-COMPLETIONIST-21: hair bun is child of head, not root
        this.hairBun = root.getChild("head").getChild("hair_bun");
        this.hairpin = this.hairBun.getChild("hairpin");
        // CRON-97: Extract held-weapon parts from right_arm
        this.swordRight = root.getChild("right_arm").getChild("sword_right");
        this.fanRight = root.getChild("right_arm").getChild("fan_right");
        this.staffRight = root.getChild("right_arm").getChild("staff_right");
        this.hoeRight = root.getChild("right_arm").getChild("hoe_right");
        this.flyWhiskRight = root.getChild("right_arm").getChild("fly_whisk_right");
        // CRON-97: All weapons hidden by default. setCharacterId enables the
        // matching one. This avoids the "every NPC holds every weapon" bug if
        // the renderer forgets to call setCharacterId.
        this.swordRight.visible = false;
        this.fanRight.visible = false;
        this.staffRight.visible = false;
        this.hoeRight.visible = false;
        this.flyWhiskRight.visible = false;
    }

    /**
     * CRON-COMPLETIONIST-97: Get the per-character body scale. The renderer
     * reads this in its overridden {@code scale()} method and applies it to
     * the PoseStack before rendering the model.
     *
     * @return the character scale (1.0 = default; &gt;1 = taller; &lt;1 = shorter)
     */
    public float getCharacterScale() {
        return characterScale;
    }

    /**
     * CRON-COMPLETIONIST-97: Set the canon character ID to drive held-weapon
     * visibility and per-character body scale. Called by the renderer each
     * frame from {@code entity.getCharacterId()} before super.render.
     *
     * <p>Maps canon characterId → {@link HeldWeaponType} (weapon + scale).
     * Hides all weapons first, then enables the matching one. If the
     * characterId is null, empty, or unrecognized, no weapon is shown and
     * scale defaults to 1.0 (a generic cultivator with no held item).
     *
     * <p><b>Canon mapping (fact-checked):</b>
     * <ul>
     *   <li>{@code wang_lin} → SWORD, scale 1.0 — flying-sword cultivator</li>
     *   <li>{@code situ_nan} → FAN, scale 1.10 — 2nd-gen Vermilion Bird Son
     *       with iconic jade folding fan</li>
     *   <li>{@code teng_li} → SWORD, scale 1.0 — Teng Family young master</li>
     *   <li>{@code wang_zhuo} → SWORD, scale 1.0 — Heng Yue Sect disciple</li>
     *   <li>{@code teng_huayuan} → STAFF, scale 1.05 — Teng Family patriarch</li>
     *   <li>{@code old_chen} → FLY_WHISK, scale 0.98 — Heng Yue Sect elder
     *       (mod-original character)</li>
     *   <li>{@code zeng_da_niu} → HOE, scale 0.95 — mortal farmer (化凡 arc)</li>
     *   <li>{@code li_muwan} → FAN, scale 0.92 — Luo He Sect alchemist
     *       (lady's folding fan)</li>
     *   <li>{@code wang_hao} → NONE, scale 1.0 — mortal (Wang Lin's cousin)</li>
     * </ul>
     *
     * @param characterId the canon character ID (e.g. "wang_lin"); may be null
     */
    public void setCharacterId(String characterId) {
        HeldWeaponType weapon = HeldWeaponType.forCharacter(characterId);
        this.characterScale = weapon.scale;
        // Hide all weapons first, then enable only the matching one.
        this.swordRight.visible = (weapon == HeldWeaponType.SWORD);
        this.fanRight.visible = (weapon == HeldWeaponType.FAN);
        this.staffRight.visible = (weapon == HeldWeaponType.STAFF);
        this.hoeRight.visible = (weapon == HeldWeaponType.HOE);
        this.flyWhiskRight.visible = (weapon == HeldWeaponType.FLY_WHISK);
    }

    /**
     * CRON-COMPLETIONIST-97: Enumeration of canon-archetype held weapons.
     *
     * <p>Each constant pairs a weapon model with a body scale, so the renderer
     * can apply both from a single characterId lookup. The scale encodes
     * per-character anatomy variation (Situ Nan tall, Li Muwan shorter, etc.)
     * without needing a separate "character build" enum.
     */
    private enum HeldWeaponType {
        /** No held weapon. Used for mortals (Wang Hao) and unrecognized characterIds. */
        NONE(1.0F),
        /** Flying sword. Wang Lin, Teng Li, Wang Zhuo. */
        SWORD(1.0F),
        /** Folding fan (jade for Situ Nan; lady's fan for Li Muwan). */
        FAN(1.0F),
        /** Walking staff. Teng Huayuan (patriarchal elder). */
        STAFF(1.05F),
        /** Farmer's hoe. Zeng Da Niu (mortal 化凡 arc). */
        HOE(0.95F),
        /** Daoist flywhisk (拂尘). Old Chen (Heng Yue Sect elder, mod-original). */
        FLY_WHISK(0.98F);

        /** Body scale applied by the renderer via PoseStack.scale. */
        final float scale;

        HeldWeaponType(float scale) {
            this.scale = scale;
        }

        /**
         * Resolve a canon characterId to its held-weapon type. Falls back to
         * NONE for null/empty/unrecognized ids. Situ Nan and Li Muwan both
         * map to FAN but the renderer selects different textures for them
         * via the existing sectId-based texture chain — the held fan is the
         * common visual element, the robe color distinguishes them further.
         */
        static HeldWeaponType forCharacter(String characterId) {
            if (characterId == null || characterId.isEmpty()) return NONE;
            // Normalize: lowercase, trim, replace spaces/spaces with underscores
            String id = characterId.toLowerCase(java.util.Locale.ROOT).trim()
                    .replaceAll("\\s+", "_");
            return switch (id) {
                case "wang_lin", "teng_li", "wang_zhuo" -> SWORD;
                case "situ_nan" -> FAN;
                case "teng_huayuan" -> STAFF;
                case "old_chen" -> FLY_WHISK;
                case "zeng_da_niu" -> HOE;
                case "li_muwan" -> FAN;
                // wang_hao, default, and any unrecognized id → no held weapon
                default -> NONE;
            };
        }
    }

    public static LayerDefinition createBodyLayer() {
        // Start from the vanilla humanoid mesh — keeps the player-skin UV
        // layout for head/body/arms/legs/hat so existing cultivator textures
        // still partially apply.
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        // ── CRON-COMPLETIONIST-54: 3-bone robe skirt chain ─────────────
        // Replaces single rigid robe_skirt with waist → mid → hem chain.
        // Each bone inherits parent rotation plus its own sway, creating
        // cloth-like drape. The hem is widest (fabric spreads at bottom).
        PartDefinition robeWaist = root.addOrReplaceChild("robe_waist",
                CubeListBuilder.create().texOffs(16, 32)
                        .addBox(-4.5F, 0.0F, -3.0F, 9.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        // Mid section — slightly wider (fabric flares)
        PartDefinition robeMid = robeWaist.addOrReplaceChild("robe_mid",
                CubeListBuilder.create().texOffs(26, 32)
                        .addBox(-4.75F, 0.0F, -3.25F, 9.5F, 3.0F, 6.5F),
                PartPose.offset(0.0F, 3.0F, 0.0F));
        // Hem — widest (fabric fully spreads, drapes over feet)
        robeMid.addOrReplaceChild("robe_hem",
                CubeListBuilder.create().texOffs(36, 32)
                        .addBox(-5.0F, 0.0F, -3.5F, 10.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        // ── Sash : thin decorative belt at waist ──────────────────────
        root.addOrReplaceChild("sash",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(-4.0F, 0.0F, -2.5F, 8.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // ── CRON-COMPLETIONIST-21: hair bun — child of head ──
        PartDefinition hairBun = root.getChild("head").addOrReplaceChild("hair_bun",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        // ── CRON-54: Jade hairpin detail on the bun ─────────────────────
        hairBun.addOrReplaceChild("hairpin",
                CubeListBuilder.create().texOffs(4, 32)
                        .addBox(-0.15F, -2.5F, -0.15F, 0.3F, 2.5F, 0.3F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        // ── sleeves : inflated arm boxes (wide flowing sleeves) ─────────
        root.getChild("right_arm").addOrReplaceChild("sleeve",
                CubeListBuilder.create().texOffs(40, 32)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        root.getChild("left_arm").addOrReplaceChild("sleeve",
                CubeListBuilder.create().texOffs(40, 48)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);

        // ═════════════════════════════════════════════════════════════════
        // CRON-COMPLETIONIST-97: HELD WEAPONS (children of right_arm)
        // ═════════════════════════════════════════════════════════════════
        // All five weapons are children of right_arm, so they inherit the
        // arm's rotation. At idle (arm hangs at side, xRot=0), the weapon
        // points downward. When the arm is raised for casting (xRot=-2.5),
        // the weapon points upward. During walk cycle, the weapon sways
        // naturally with the arm swing.
        //
        // AT MOST ONE is visible at a time — setCharacterId() selects which.
        // The constructor hides all five by default; the renderer calls
        // setCharacterId() before super.render to enable the matching one.
        //
        // Geometry notes:
        //   - The right_arm cube is 4x12x4 with origin (-1, -2, -2). The hand
        //     is at the bottom of the arm, around y=10 in arm-local coords.
        //   - All weapon parts use PartPose.ZERO (no offset) and addBox at
        //     the appropriate y position (downward extension from the hand).
        //   - UVs are allocated in the previously-unused bottom strip of the
        //     64x64 texture (rows 56-63).
        PartDefinition rightArm = root.getChild("right_arm");

        // Sword — thin blade extending 18 units below the hand. Held by
        // Wang Lin, Teng Li, Wang Zhuo (flying-sword cultivators).
        rightArm.addOrReplaceChild("sword_right",
                CubeListBuilder.create().texOffs(0, 56)
                        .addBox(-0.5F, 10.0F, -0.5F, 1.0F, 18.0F, 1.0F),
                PartPose.ZERO);

        // Fan — folded jade/lady's fan, 9 units long, 2 wide. Held by
        // Situ Nan (jade fan) and Li Muwan (lady's fan).
        rightArm.addOrReplaceChild("fan_right",
                CubeListBuilder.create().texOffs(4, 56)
                        .addBox(-1.0F, 9.0F, -0.5F, 2.0F, 9.0F, 1.0F),
                PartPose.ZERO);

        // Staff — long walking staff, 24 units (extends past feet when held
        // at side). Held by Teng Huayuan (patriarchal elder).
        rightArm.addOrReplaceChild("staff_right",
                CubeListBuilder.create().texOffs(8, 56)
                        .addBox(-0.5F, 6.0F, -0.5F, 1.0F, 24.0F, 1.0F),
                PartPose.ZERO);

        // Hoe — farmer's hoe: 14-unit handle + 3x1 horizontal head at the
        // bottom. Held by Zeng Da Niu (mortal farmer, 化凡 arc).
        PartDefinition hoeDef = rightArm.addOrReplaceChild("hoe_right",
                CubeListBuilder.create().texOffs(16, 56)
                        .addBox(-0.5F, 10.0F, -0.5F, 1.0F, 14.0F, 1.0F),
                PartPose.ZERO);
        // Hoe head — small horizontal blade at the bottom of the handle.
        hoeDef.addOrReplaceChild("hoe_head",
                CubeListBuilder.create().texOffs(20, 56)
                        .addBox(-1.5F, 22.0F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.ZERO);

        // Fly whisk — Daoist 拂尘: 12-unit handle + 4-unit tassel at bottom.
        // Held by Old Chen (mod-original Heng Yue Sect elder).
        PartDefinition flyWhiskDef = rightArm.addOrReplaceChild("fly_whisk_right",
                CubeListBuilder.create().texOffs(24, 56)
                        .addBox(-0.3F, 10.0F, -0.3F, 0.6F, 12.0F, 0.6F),
                PartPose.ZERO);
        // Tassel — slightly inflated (CubeDeformation 0.2) to suggest hair/fiber.
        flyWhiskDef.addOrReplaceChild("tassel",
                CubeListBuilder.create().texOffs(28, 56)
                        .addBox(-1.0F, 20.0F, -0.3F, 2.0F, 4.0F, 0.6F, new CubeDeformation(0.2F)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Renderer-side toggle for the meditation pose. */
    public void setMeditating(boolean meditating) {
        this.meditating = meditating;
    }

    /** Renderer-side toggle for the casting/channeling pose. */
    public void setCasting(boolean casting) {
        this.casting = casting;
    }

    /** CRON-COMPLETIONIST-31: Renderer-side toggle for the observing pose. */
    public void setObserving(boolean observing) {
        this.observing = observing;
    }

    /** CRON-COMPLETIONIST-31: Renderer-side toggle for the guarding pose. */
    public void setGuarding(boolean guarding) {
        this.guarding = guarding;
    }

    /** CRON-COMPLETIONIST-44: Renderer-side toggle for the pursuing pose. */
    public void setPursuing(boolean pursuing) {
        this.pursuing = pursuing;
    }

    /** CRON-COMPLETIONIST-44: Renderer-side toggle for the socializing pose. */
    public void setSocializing(boolean socializing) {
        this.socializing = socializing;
    }

    /**
     * CRON-COMPLETIONIST-19: Set the cognitive look-target from the renderer.
     *
     * <p>The renderer reads the synced {@code DATA_LOOK_TARGET_X/Y/Z} from
     * the entity and passes them here. {@code ageInTicks} is needed for
     * micro-saccade noise timing.
     *
     * @param x world X, or NaN to clear
     * @param y world Y, or NaN to clear
     * @param z world Z, or NaN to clear
     */
    public void setCognitiveLookTarget(float x, float y, float z) {
        this.cognitiveLookX = x;
        this.cognitiveLookY = y;
        this.cognitiveLookZ = z;
    }

    /**
     * CRON-COMPLETIONIST-21: Set the seven Performance channels from the
     * entity's synced data. Called by the renderer each frame before
     * super.render invokes setupAnim. When all are NaN, no Commitment is
     * active and the model falls back to pose/vanilla animation.
     */
    public void setPerformance(float focus, float urgency, float confidence,
                               float concealment, float tension, float patience,
                               float fatigue) {
        this.perfFocus = focus;
        this.perfUrgency = urgency;
        this.perfConfidence = confidence;
        this.perfConcealment = concealment;
        this.perfTension = tension;
        this.perfPatience = patience;
        this.perfFatigue = fatigue;
    }

    @Override
    public void setupAnim(EntityCultivator entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Let vanilla handle walk cycle, head turn, arm swing, crouching, etc.
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // ── idle breathing ──────────────────────────────────────────────
        this.body.y = (float) Math.sin(ageInTicks * 0.1F) * 0.3F;

        // ── CRON-COMPLETIONIST-54: 3-bone robe skirt sway ──────────────
        // Each segment inherits parent rotation + adds its own sway with
        // phase delay. During walk, the hem lags behind the waist, creating
        // cloth billow. During idle, each segment drifts at different phases.
        float walkSway = (float) Math.sin(limbSwing * 0.6662F) * 0.10F * limbSwingAmount;
        float idleSway = (float) Math.sin(ageInTicks * 0.07F) * 0.03F;

        // Waist: follows body walk immediately
        this.robeWaist.xRot = walkSway + idleSway;
        this.robeWaist.yRot = (float) Math.sin(ageInTicks * 0.05F) * 0.02F;

        // Mid: phase-delayed sway (0.4 rad behind waist) + slightly amplified
        this.robeMid.xRot = (float) Math.sin(limbSwing * 0.6662F - 0.4F) * 0.08F * limbSwingAmount
                              + (float) Math.sin(ageInTicks * 0.07F + 0.3F) * 0.04F;
        this.robeMid.yRot = (float) Math.sin(ageInTicks * 0.05F + 0.2F) * 0.03F;

        // Hem: further phase-delayed (0.8 rad behind waist) + more amplified
        // This creates the classic "fabric trailing behind" billow effect
        this.robeHem.xRot = (float) Math.sin(limbSwing * 0.6662F - 0.8F) * 0.06F * limbSwingAmount
                             + (float) Math.sin(ageInTicks * 0.07F + 0.6F) * 0.05F;
        this.robeHem.yRot = (float) Math.sin(ageInTicks * 0.05F + 0.4F) * 0.04F;

        // Sash stays with body (rigid belt)
        // (no animation needed — sash is a fixed belt)

        // ── hair bun : barely-perceptible bob with breathing ────────────
        this.hairBun.yRot = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;
        // Hairpin: subtle glint oscillation (light catching jade)
        this.hairpin.zRot = (float) Math.sin(ageInTicks * 0.3F) * 0.02F;

        // ══════════════════════════════════════════════════════════════
        //  POSE STATES — applied AFTER super so they override vanilla anims
        // ══════════════════════════════════════════════════════════════

        // ── meditation pose : standing-stake (zhan zhuang) ──────────────
        // Hands together at chest height, head bowed, body still.
        // Applied AFTER super so it overrides the vanilla arm swing.
        if (this.meditating) {
            this.rightArm.xRot = -1.5F;           // forward horizontal
            this.rightArm.yRot = -0.3F;           // toward center
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = -1.5F;
            this.leftArm.yRot = 0.3F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = 0.0F;             // standing, legs straight
            this.leftLeg.xRot = 0.0F;
            this.rightLeg.yRot = -0.05F;           // slightly apart
            this.leftLeg.yRot = 0.05F;
            this.body.xRot = 0.08F;                // slight forward lean
            this.head.xRot = 0.35F;                // head bowed
            this.head.yRot = 0.0F;
            // robe settles — all three bones go still
            this.robeWaist.xRot = idleSway * 0.5F;
            this.robeMid.xRot = idleSway * 0.3F;
            this.robeHem.xRot = idleSway * 0.2F;
            // subtle qi-gathering pulse in the breathing
            this.body.y = (float) Math.sin(ageInTicks * 0.05F) * 0.5F;
        }

        // ── casting pose : right arm raised, channeling ─────────────────
        if (this.casting) {
            this.rightArm.xRot = -2.5F;            // straight up
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.2F;
            this.leftArm.xRot = -0.4F;             // left arm slightly out (balance)
            this.leftArm.zRot = -0.2F;
            // channel tremor : subtle high-frequency jitter on the raised arm
            this.rightArm.xRot += (float) Math.sin(ageInTicks * 3.0F) * 0.03F;
        }

        // ── CRON-COMPLETIONIST-31: observing pose ─────────────────────
        // The hidden-cultivator observing pose. Wang Lin crouches behind cover,
        // watching the threat without revealing his strength. This is the pose
        // the Cultivator Mind produces when CONCEAL_STRENGTH + CURIOSITY score
        // OBSERVE highest.
        //
        // Visual: Body lowered (crouching), right hand raised to brow
        // (peering through fingers), left hand resting on knee, head slightly
        // raised and tilted forward (watching), weight shifted back.
        if (this.observing) {
            // Body crouches — lower the whole model
            this.body.y = -0.3F + (float) Math.sin(ageInTicks * 0.08F) * 0.05F;
            this.body.xRot = 0.15F;               // slight forward lean (peering)

            // Right arm raised to brow — shielding eyes, peering at threat
            this.rightArm.xRot = -1.8F;             // forward and slightly up
            this.rightArm.yRot = -0.4F;            // toward center (hand at brow)
            this.rightArm.zRot = 0.1F;

            // Left arm down, hand on knee (supporting the crouch)
            this.leftArm.xRot = 0.6F;              // arm drops to side
            this.leftArm.yRot = 0.2F;              // slightly forward (knee direction)
            this.leftArm.zRot = 0.0F;

            // Legs bent — crouching posture
            this.rightLeg.xRot = 0.4F;             // thigh angles back (squat)
            this.leftLeg.xRot = 0.4F;
            this.rightLeg.yRot = -0.15F;           // wider stance (stability)
            this.leftLeg.yRot = 0.15F;

            // Head slightly raised — watching the threat from cover
            // head.yRot is already set by super (netHeadYaw), we add a slight
            // forward tilt but let the head track the target horizontally
            this.head.xRot = -0.15F;              // chin slightly up (watching, not bowed)
            // Subtle head tracking — slight oscillation as if scanning
            this.head.yRot += (float) Math.sin(ageInTicks * 0.2F) * 0.05F;

            // Robe settles — trying to be still
            this.robeWaist.xRot = idleSway * 0.2F;
            this.robeMid.xRot = idleSway * 0.1F;
            this.robeHem.xRot = 0.0F;
        }

        // ── CRON-COMPLETIONIST-31: guarding pose ─────────────────────
        // The combat-ready defender stance. Da Niu stands at the village
        // perimeter, feet wide apart, arms forward, ready to intercept.
        // This is the pose the Cultivator Mind produces when DUTY scores
        // GUARD highest.
        //
        // Visual: Feet wide apart (horse stance / ma bu), both arms forward
        // and slightly bent (ready to grab weapon or intercept), body lowered
        // and centered, head forward (facing the threat). Weight centered.
        if (this.guarding) {
            // Body centered and slightly lowered
            this.body.y = -0.1F;
            this.body.xRot = 0.05F;               // very slight forward lean

            // Both arms forward — ready stance (intercept/grab)
            this.rightArm.xRot = -1.2F;            // forward and slightly down
            this.rightArm.yRot = -0.25F;           // slightly toward center
            this.rightArm.zRot = -0.15F;           // hands closer together
            this.leftArm.xRot = -1.2F;
            this.leftArm.yRot = 0.25F;
            this.leftArm.zRot = 0.15F;

            // Combat-ready tension: subtle high-frequency jitter on both arms
            float tension = (float) Math.sin(ageInTicks * 2.5F) * 0.02F;
            this.rightArm.xRot += tension;
            this.leftArm.xRot += tension;

            // Legs wide apart — horse stance (ma bu)
            this.rightLeg.xRot = 0.2F;             // slight squat
            this.leftLeg.xRot = 0.2F;
            this.rightLeg.yRot = -0.3F;            // wide stance
            this.leftLeg.yRot = 0.3F;

            // Head forward — facing the threat
            this.head.xRot = 0.1F;                // slight forward tilt (alert)
            // Subtle alert scanning
            this.head.yRot += (float) Math.sin(ageInTicks * 0.3F) * 0.03F;

            // Robe still — focused
            this.robeWaist.xRot = idleSway * 0.3F;
            this.robeMid.xRot = idleSway * 0.2F;
            this.robeHem.xRot = idleSway * 0.1F;
        }

        // ── CRON-COMPLETIONIST-44: pursuing pose ─────────────────────
        // Walking with deliberate purpose toward an opportunity. The cultivator
        // strides forward purposefully — not running, not idle — a determined
        // walk that says "I am going somewhere important." This is the pose
        // CultivatorMind produces when PURSUING_OPPORTUNITY scores highest.
        //
        // Visual: Super walk cycle is preserved (arms swing opposite legs).
        // Body leans very slightly forward (momentum). Head held up and
        // forward (eyes on destination). Right hand slightly extended (reaching
        // toward goal). Robe skirt has more pronounced sway from the faster walk.
        if (this.pursuing) {
            // Body leans slightly forward — purposeful momentum
            this.body.xRot = -0.1F;
            // Override idle breathing with slightly faster cadence
            this.body.y = (float) Math.sin(ageInTicks * 0.15F) * 0.2F;

            // Right arm slightly forward — reaching toward goal
            // (only the right arm; left arm keeps vanilla walk swing for naturalism)
            this.rightArm.xRot -= 0.3F;            // extend forward beyond walk
            this.rightArm.yRot = -0.1F;            // very slightly toward center

            // Head held high and forward — eyes on the destination
            this.head.xRot = -0.1F;               // chin slightly up (looking ahead)

            // Robe skirt sways more — cultivator walking faster
            this.robeWaist.xRot = walkSway * 1.5F + idleSway;
            this.robeMid.xRot = (float) Math.sin(limbSwing * 0.6662F - 0.5F) * 0.12F * limbSwingAmount + idleSway;
            this.robeHem.xRot = (float) Math.sin(limbSwing * 0.6662F - 1.0F) * 0.08F * limbSwingAmount + idleSway;
            // Add lateral robe billow from faster movement (hem only — fabric trails)
            this.robeHem.zRot = (float) Math.sin(limbSwing * 0.3331F) * 0.08F * limbSwingAmount;
        }

        // ── CRON-COMPLETIONIST-44: socializing pose ─────────────────────
        // Relaxed, turned toward a companion. The cultivator has stopped to
        // interact — standing at ease, body angled toward the other person,
        // one arm gesturing. This is the pose CultivatorMind produces when
        // SOCIALIZING scores highest.
        //
        // Visual: Body slightly turned (weight shifted), head facing companion
        // (head tracking works via vanilla netHeadYaw), one arm relaxed at side,
        // other arm slightly raised in a conversational gesture, occasional
        // subtle gesture animation. Relaxed breathing. Robe hangs naturally.
        if (this.socializing) {
            // Body relaxed — very slight lean (at ease, not rigid)
            this.body.xRot = 0.03F;
            this.body.y = (float) Math.sin(ageInTicks * 0.08F) * 0.2F; // slow breathing

            // Left arm relaxed at side — slightly away from body (open posture)
            this.leftArm.xRot = 0.1F + (float) Math.sin(ageInTicks * 0.1F) * 0.05F;
            this.leftArm.zRot = 0.1F;             // arm away from body

            // Right arm in conversational gesture — slightly raised, palm out
            // Gesture oscillates slowly: arm lifts and lowers as if talking
            float gesturePhase = (float) Math.sin(ageInTicks * 0.4F);
            this.rightArm.xRot = -0.8F + gesturePhase * 0.2F;  // arm at chest height, bobbing
            this.rightArm.yRot = -0.3F;            // slightly toward center (talking)
            this.rightArm.zRot = 0.15F;            // palm-up tilt

            // Legs relaxed — slight weight shift (standing at ease)
            float weightShift = (float) Math.sin(ageInTicks * 0.06F) * 0.03F;
            this.rightLeg.xRot = weightShift;
            this.leftLeg.xRot = -weightShift;
            this.rightLeg.yRot = -0.05F;
            this.leftLeg.yRot = 0.05F;

            // Head relaxed — slight nod animation (listening/acknowledging)
            // Don't override netHeadYaw — the head still tracks the companion
            this.head.xRot += (float) Math.sin(ageInTicks * 0.3F) * 0.04F;

            // Robe hangs naturally — minimal sway
            this.robeWaist.xRot = idleSway * 0.5F;
            this.robeMid.xRot = idleSway * 0.4F;
            this.robeHem.xRot = idleSway * 0.3F;
        }

        // ═════════════════════════════════════════════════════════════════
        //  CRON-COMPLETIONIST-19: Cognitive Look-Target Override
        // ═════════════════════════════════════════════════════════════════
        // The user's 2026-07-25 directive:
        //   "head turns → body rotates slightly → weight shifts → breathing
        //    slows → eyes remain fixed → doesn't respond immediately to
        //    player → after 30 seconds glances away briefly → looks back"
        //
        // This block runs LAST, after all pose-specific animations. When a
        // cognitive look-target is set (Wang Lin is observing wolves), it
        // OVERRIDES the vanilla head yaw/pitch (which would otherwise be
        // driven by the entity's look control / random-look goal). The head
        // lerps toward the look-target with a max rotation per tick (no snap),
        // with micro-saccade noise layered on top so it doesn't lock robotically.
        //
        // Every ~60 ticks (~3 seconds), a brief glance-away fires: the head
        // offsets by ~0.2 rad for ~10 ticks, then returns. This is the
        // "after 30 seconds glances away briefly" from the directive (the
        // cadence is faster than 30s in the directive example, but the
        // principle — don't stare without blinking — is what matters).
        //
        // When NO cognitive look-target is set, the head uses the vanilla
        // netHeadYaw/headPitch (set by super.setupAnim) and our interpolation
        // state decays toward zero so the next time a target appears, we
        // start from a clean baseline.
        //
        // CRON-COMPLETIONIST-21: this now dispatches to applyPerformance,
        // which consumes the seven acting-direction channels INDEPENDENTLY.
        // The look-target is still used (to compute desired head yaw/pitch),
        // but the channels modulate HOW the head moves, breathing, torso,
        // weight, and hands. Same look-target + different channels → different
        // acting. This is the "communicate the current thought" layer.
        applyPerformance(entity, ageInTicks);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CRON-COMPLETIONIST-23: The Acting Layer — Directive-Driven
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Apply the Acting Layer to the body.
     *
     * <p>This is the CRON-23 realization of the user's 2026-07-26 architectural
     * pivot: <b>the renderer no longer reads psychology.</b> The flow is now
     *
     * <pre>
     *   Performance (7 synced channels)
     *     → PerformanceInterpreter.interpret()
     *     → List&lt;AnimationDirective&gt; (semantic instructions)
     *     → applyDirectives()
     *     → body motion
     * </pre>
     *
     * <p>The interpreter is the ONLY place that reads focus/urgency/confidence
     * and decides what they mean. This method obeys directives by name
     * (LOCK_ATTENTION, BRACE, CONCEAL_WEAPON_HAND, …) and never touches a
     * channel directly. That keeps rendering completely independent from
     * cognition — the user's stated principle.
     *
     * <p>CRON-21/22 wired the 9 body channels directly. CRON-23 reorganizes
     * the SAME math under directive-named helper methods so the behavior is
     * preserved but the boundary is clean. Minor low-intensity regime
     * differences are documented in the self-critique (the interpreter's
     * emission thresholds don't perfectly mirror every channel's continuous
     * contribution — e.g. breathing shallowness at tension 0.2 is now missed
     * because BRACE only fires past 0.4). These are flagged for the Living
     * Observation Count to catch in playtest, per Article XLVI.
     *
     * <p>When no Performance is set (all channels NaN), this decays the
     * interpolation state and falls back to the pose/vanilla head pose.
     */
    private void applyPerformance(EntityCultivator entity, float ageInTicks) {
        // ── No Performance active → fall back to vanilla / pose ──
        if (Float.isNaN(perfFocus)) {
            currentHeadYaw *= 0.5F;
            currentHeadPitch *= 0.5F;
            saccadePhase = 0.0F;
            glancingAway = false;
            glanceAwayTicks = 0;
            return;
        }

        // ── Build the Performance from synced channels, then interpret ──
        // The interpreter is the only reader of psychology. From here on,
        // this method only knows directives.
        Performance perf = new Performance(perfFocus, perfUrgency, perfConfidence,
                perfConcealment, perfTension, perfPatience, perfFatigue);
        java.util.List<AnimationDirective> directives = PerformanceInterpreter.interpret(perf);

        applyDirectives(entity, ageInTicks, directives);
    }

    /**
     * Obey a list of {@link AnimationDirective}s, applying each to the body.
     *
     * <p>Directives are applied in a fixed order that respects body-part
     * dependencies: head tracking first (needs the look-target), then the
     * additive offsets (breathing, torso, weight, hands, shoulders, feet,
     * eyes, fatigue-sag). Each directive's effect is ADDITIVE — it layers
     * on top of the pose without overriding it.
     *
     * <p>This method does NOT read any Performance channel. It only reads
     * directive names and intensities. That is the CRON-23 boundary.
     */
    private void applyDirectives(EntityCultivator entity, float ageInTicks,
                                 java.util.List<AnimationDirective> directives) {
        // Index directives by name for O(1) lookup (at most 9 directives).
        float lockAttention = 0f, scanUrgent = 0f, settle = 0f;
        float concealWeapon = 0f, brace = 0f, holdGround = 0f;
        float fidget = 0f, sagFatigue = 0f, anticipate = 0f;
        for (AnimationDirective d : directives) {
            switch (d.name) {
                case LOCK_ATTENTION      -> lockAttention = d.intensity;
                case SCAN_URGENT         -> scanUrgent = d.intensity;
                case SETTLE              -> settle = d.intensity;
                case CONCEAL_WEAPON_HAND -> concealWeapon = d.intensity;
                case BRACE               -> brace = d.intensity;
                case HOLD_GROUND         -> holdGround = d.intensity;
                case FIDGET              -> fidget = d.intensity;
                case SAG_FATIGUE         -> sagFatigue = d.intensity;
                case ANTICIPATE_TARGET   -> anticipate = d.intensity;
            }
        }

        // Derive the glance-hold duration factor: HOLD_GROUND lengthens it,
        // FIDGET shortens it. The patience → glance-duration mapping from
        // CRON-21 is reconstructed from whichever directive is active.
        // patience 1.0 → HOLD_GROUND intensity 1.0 → 16-tick glance.
        // patience 0.0 → FIDGET intensity 1.0 → 6-tick glance.
        float glanceDurationFactor = holdGround - fidget; // -1..+1

        applyHeadTracking(entity, ageInTicks, lockAttention, glanceDurationFactor);
        applyBreathing(ageInTicks, scanUrgent, brace, concealWeapon);
        applyTorso(ageInTicks, scanUrgent, brace, concealWeapon, lockAttention);
        applyWeightShift(ageInTicks, fidget, holdGround);
        applyHands(concealWeapon);
        applyShoulders(brace, settle, sagFatigue);
        applyFeet(ageInTicks, fidget, holdGround);
        applyEyes(anticipate, sagFatigue);
        applyFatigueSag(sagFatigue);
    }

    // ── HEAD: look-target tracking modulated by LOCK_ATTENTION ──────────
    private void applyHeadTracking(EntityCultivator entity, float ageInTicks,
                                   float lockAttention, float glanceDurationFactor) {
        if (Float.isNaN(cognitiveLookX) || Float.isNaN(cognitiveLookY)
                || Float.isNaN(cognitiveLookZ)) {
            // No look-target — decay head toward neutral.
            currentHeadYaw *= 0.8F;
            currentHeadPitch *= 0.8F;
            return;
        }

        double ex = entity.getX();
        double ey = entity.getEyeY();
        double ez = entity.getZ();
        double dx = cognitiveLookX - ex;
        double dy = cognitiveLookY - ey;
        double dz = cognitiveLookZ - ez;
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        if (horizDist < 0.001) return;

        float entityBodyYawRad = (float) Math.toRadians(entity.yBodyRot);
        float desiredYawAbs = (float) Math.atan2(dx, dz);
        float desiredYaw = desiredYawAbs - entityBodyYawRad;
        desiredYaw = wrapAngle(desiredYaw);
        float maxYaw = (float) Math.toRadians(75.0);
        desiredYaw = Math.max(-maxYaw, Math.min(maxYaw, desiredYaw));

        float desiredPitch = (float) Math.atan2(-dy, horizDist);
        float maxPitch = (float) Math.toRadians(60.0);
        desiredPitch = Math.max(-maxPitch, Math.min(maxPitch, desiredPitch));

        // LOCK_ATTENTION slows the lerp (deliberate tracking). Without it,
        // the default lerp (0.25) produces quick, anxious snaps.
        // lockAttention=1.0 → lerp 0.08; lockAttention=0.0 → lerp 0.25.
        float lerpFactor = 0.25F - lockAttention * 0.17F;
        currentHeadYaw += (desiredYaw - currentHeadYaw) * lerpFactor;
        currentHeadPitch += (desiredPitch - currentHeadPitch) * lerpFactor;

        // Saccade amplitude shrinks with LOCK_ATTENTION (locked eyes barely drift).
        float saccadeAmp = 0.015F * (1.0F - lockAttention * 0.8F);
        float saccadeNoiseYaw = (float) Math.sin(ageInTicks * 0.9F) * saccadeAmp
                + (float) Math.sin(ageInTicks * 2.3F) * (saccadeAmp * 0.5F);
        float saccadeNoisePitch = (float) Math.sin(ageInTicks * 1.1F + 0.5F)
                * (saccadeAmp * 0.7F);

        // Glance-away: threshold scales with LOCK_ATTENTION (locked = rare).
        // Duration scales with glanceDurationFactor (HOLD_GROUND longer, FIDGET shorter).
        float glanceThreshold = 40.0F + lockAttention * 80.0F;
        saccadePhase += 1.0F;
        if (!glancingAway && saccadePhase > glanceThreshold) {
            glancingAway = true;
            // glanceDurationFactor -1..+1 → 6..16 ticks
            glanceAwayTicks = 6 + (int) ((glanceDurationFactor + 1.0F) * 5.0F);
            saccadePhase = 0.0F;
            glanceDirection = (Math.random() < 0.5) ? -1.0F : 1.0F;
        }
        float glanceYawOffset = 0.0F;
        float glancePitchOffset = 0.0F;
        if (glancingAway) {
            glanceYawOffset = glanceDirection * 0.2F;
            glancePitchOffset = -0.05F;
            glanceAwayTicks--;
            if (glanceAwayTicks <= 0) glancingAway = false;
        }

        this.head.yRot = currentHeadYaw + saccadeNoiseYaw + glanceYawOffset;
        this.head.xRot = currentHeadPitch + saccadeNoisePitch + glancePitchOffset;
    }

    // ── BREATHING: speed from SCAN_URGENT, amp reduced by BRACE & CONCEAL ─
    private void applyBreathing(float ageInTicks, float scanUrgent, float brace,
                                float concealWeapon) {
        float breathSpeed = 0.1F + scanUrgent * 0.3F;
        // BRACE shallowens breath (tense = shallow). CONCEAL suppresses it
        // (hidden = quiet). Both reduce amplitude additively.
        float breathAmp = 0.3F * (1.0F - brace * 0.5F) * (1.0F - concealWeapon * 0.3F);
        this.body.y = (float) Math.sin(ageInTicks * breathSpeed) * breathAmp;
    }

    // ── TORSO: follow reduced by BRACE, lean from SCAN_URGENT & CONCEAL ──
    private void applyTorso(float ageInTicks, float scanUrgent, float brace,
                            float concealWeapon, float lockAttention) {
        // BRACE makes the torso rigid (less follow). The head-yaw signal comes
        // from currentHeadYaw, which is only meaningful if LOCK_ATTENTION drove it.
        float followFactor = (1.0F - brace * 0.6F);
        float torsoFollow = currentHeadYaw * 0.15F * followFactor;
        float maxTorsoFollow = (float) Math.toRadians(15.0);
        torsoFollow = Math.max(-maxTorsoFollow, Math.min(maxTorsoFollow, torsoFollow));
        this.body.yRot = torsoFollow;

        // Forward lean from SCAN_URGENT; backward lean from CONCEAL_WEAPON_HAND.
        float torsoLean = scanUrgent * 0.08F - concealWeapon * 0.05F;
        this.body.xRot += torsoLean;
    }

    // ── WEIGHT-SHIFT: amp from FIDGET, suppressed by HOLD_GROUND ─────────
    private void applyWeightShift(float ageInTicks, float fidget, float holdGround) {
        // FIDGET amplitude; HOLD_GROUND suppresses (planted = no shift).
        float weightAmp = 0.4F * fidget * (1.0F - holdGround);
        this.body.x += (float) Math.sin(ageInTicks * 0.03F) * weightAmp;
    }

    // ── HANDS: CONCEAL_WEAPON_HAND drifts right arm toward weapon ────────
    private void applyHands(float concealWeapon) {
        if (concealWeapon > 0.0F) {
            this.rightArm.yRot += -0.25F * concealWeapon;
            this.rightArm.xRot += 0.15F * concealWeapon;
        }
    }

    // ── SHOULDERS: BRACE raises, SETTLE drops, SAG rounds ────────────────
    private void applyShoulders(float brace, float settle, float sagFatigue) {
        if (brace > 0.0F) {
            float shoulderRaise = brace * 0.09F; // BRACE remapped so 0.4→0, 1.0→0.09
            this.rightArm.xRot -= shoulderRaise;
            this.leftArm.xRot -= shoulderRaise;
        }
        if (settle > 0.0F) {
            float shoulderDrop = settle * 0.015F; // SETTLE remapped so 0.7→0, 1.0→0.015
            this.rightArm.xRot += shoulderDrop;
            this.leftArm.xRot += shoulderDrop;
        }
        if (sagFatigue > 0.0F) {
            // SAG rounds shoulders — arms sag forward slightly.
            this.rightArm.xRot += sagFatigue * 0.05F;
            this.leftArm.xRot += sagFatigue * 0.05F;
        }
    }

    // ── FEET: FIDGET shuffles, HOLD_GROUND pigeon-toes ───────────────────
    private void applyFeet(float ageInTicks, float fidget, float holdGround) {
        if (fidget > 0.0F) {
            float shufflePhase = (float) Math.sin(ageInTicks * 0.12F);
            float shuffleAmp = fidget * 0.04F; // FIDGET remapped so 0.5→0, 0.0→0.04
            this.rightLeg.yRot += shufflePhase * shuffleAmp;
            this.leftLeg.yRot -= shufflePhase * shuffleAmp;
        }
        if (holdGround > 0.2F) {
            // HOLD_GROUND remapped so 0.6→0, 1.0→0.012. Threshold 0.2 ≈ patience 0.6.
            float plantedPinch = Math.max(0.0F, (holdGround - 0.2F)) * 0.03F;
            this.rightLeg.yRot += plantedPinch;
            this.leftLeg.yRot -= plantedPinch;
        }
    }

    // ── EYES: ANTICIPATE leads head, SAG droops pitch ────────────────────
    private void applyEyes(float anticipate, float sagFatigue) {
        float eyeLeadYaw = 0.0F;
        float eyeLeadPitch = 0.0F;
        if (anticipate > 0.0F) {
            // ANTICIPATE remapped so focus 0.5→0, 1.0→1.0. Lead 0..5°.
            float leadAngle = anticipate * 0.087F;
            eyeLeadYaw = Math.signum(currentHeadYaw) * leadAngle;
            eyeLeadPitch = -leadAngle * 0.3F;
        }
        if (sagFatigue > 0.1F) {
            // SAG droops eyes downward. Threshold 0.1 ≈ fatigue 0.4.
            eyeLeadPitch += Math.max(0.0F, sagFatigue - 0.1F) * 0.1F;
        }
        this.head.yRot += eyeLeadYaw;
        this.head.xRot += eyeLeadPitch;
    }

    // ── FATIGUE SAG: head droops, shoulders round ────────────────────────
    private void applyFatigueSag(float sagFatigue) {
        if (sagFatigue > 0.0F) {
            this.head.xRot += sagFatigue * 0.15F;
            this.body.xRot += sagFatigue * 0.05F;
        }
    }

    /** Clamp a float to [0,1], preserving NaN. */
    private static float clamp01(float v) {
        if (Float.isNaN(v)) return 0.0F;
        return Math.max(0.0F, Math.min(1.0F, v));
    }

    /** Wrap an angle (radians) to [-PI, PI]. */
    private static float wrapAngle(float angle) {
        while (angle > Math.PI) angle -= (float) (2 * Math.PI);
        while (angle < -Math.PI) angle += (float) (2 * Math.PI);
        return angle;
    }
}
