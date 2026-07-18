package dev.ergenverse.perception;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.core.WorldPhilosophy;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.world.WorldLaws;

import java.util.ArrayList;
import java.util.List;

/**
 * Divine Sense — the snapshot-pulse perception mechanic.
 *
 * <p>Per the {@link WorldPhilosophy}: "In Er Gen, Divine Sense is almost
 * another sense entirely. It's not just 'radar.' It becomes: reading
 * formations, identifying herbs, locating beasts, sensing hidden
 * cultivators, detecting killing intent, finding spatial cracks."
 *
 * <h2>How it works mechanically</h2>
 *
 * <p>Divine Sense is a <b>SNAPSHOT PULSE</b>, not continuous. The player
 * presses a button (keybind), the engine computes a single snapshot of
 * everything they can perceive within their radius, returns the result,
 * and the pulse ends. No ongoing tick cost. No lag.
 *
 * <p>This mirrors {@code activateKarmaVisionAction} in the Next.js
 * reference implementation — a snapshot pulse with a cooldown, not a
 * continuous effect.
 *
 * <h2>The Soul Power formula (anti-lag)</h2>
 *
 * <pre>
 *   S_sense = S_realm × (1 + ΣM_manual) + S_tempering
 * </pre>
 *
 * <p>S_realm baseline grows logarithmically in effect:
 * mortal=1, Qi=10, Foundation=100, Core=500, Nascent Soul=10000,
 * Soul Formation=30000, Ascendant=80000, Immortal=1000000,
 * Transcendence=100000000.
 *
 * <p>ΣM_manual = sum of soul-cultivation manual multipliers.
 * S_tempering = soul tempering (dark-arts), soft-capped at
 * S_realm × (realm_order+1).
 *
 * <h2>The radius formula (THE ANTI-LAG MECHANISM)</h2>
 *
 * <pre>
 *   R_sense = R_base × ln(S_sense + 1) × (1 − L_world/12) × Π(1 − σ_seal)
 * </pre>
 *
 * <p>This is logarithmic. The radius <b>stabilizes</b> at higher tiers
 * because:
 * <ul>
 *   <li>The logarithm grows slowly (ln(10M) vs ln(10K) is only ~2× difference)</li>
 *   <li>The world-law factor (1 − L_world/12) shrinks as you enter higher-law worlds</li>
 *   <li>Seals multiply in as (1 − σ_seal) reductions</li>
 * </ul>
 *
 * <p>Result: radius stays in the 50-150 block range across ALL tiers.
 * No lag. A Transcendent's pulse is not "scan the whole dimension" —
 * it's "scan ~100 blocks, but perceive EVERYTHING in those 100 blocks
 * at maximum depth."
 *
 * <h2>The NPC confrontation</h2>
 *
 * <p>When you pulse, NPCs in your radius react based on
 * ΔS = S_player − S_npc:
 *
 * <ul>
 *   <li><b>Arrogant Interception</b> (ΔS < 0, NPC stronger): face-obsessed
 *       NPCs counter-strike → Soul Fracture risk</li>
 *   <li><b>Absolute Suppression</b> (ΔS ≥ 5000): NPC drops to knees,
 *       hidden trade tabs unlock, taming bypassed for beasts</li>
 *   <li><b>Unmasking the Hypocrite</b> (righteous_hypocrite, ΔS ≥ 2000):
 *       false aura shatters, true soul matrix revealed</li>
 *   <li><b>Ally alerted</b> (20% chance if ΔS < 5000): ally recognizes you</li>
 *   <li><b>Unnoticed</b> (default): NPC doesn't notice</li>
 * </ul>
 *
 * <h2>The Spiritual Camouflage registry</h2>
 *
 * <p>Concealed objects have camouflage values (C). Your S_sense must
 * exceed C to perceive them:
 * <ul>
 *   <li>inheritance_vault: C=25000</li>
 *   <li>ancient_formation: C=20000</li>
 *   <li>sealed_pavilion: C=15000</li>
 *   <li>trap_array: C=8000</li>
 *   <li>spirit_vein: C=500</li>
 *   <li>hidden_cave: C=200</li>
 * </ul>
 */
public final class DivineSense {

    private DivineSense() {}

    // ─── S_realm baseline per tier ───────────────────────────────────
    // Grows logarithmically in effect (the radius formula uses ln(S+1)).
    public static final java.util.Map<RealmId, Long> S_REALM = java.util.Map.ofEntries(
        java.util.Map.entry(RealmId.MORTAL, 1L),
        java.util.Map.entry(RealmId.QI_CONDENSATION, 10L),
        java.util.Map.entry(RealmId.FOUNDATION, 100L),
        java.util.Map.entry(RealmId.CORE_FORMATION, 500L),
        java.util.Map.entry(RealmId.NASCENT_SOUL, 10000L),
        java.util.Map.entry(RealmId.SOUL_FORMATION, 30000L),
        java.util.Map.entry(RealmId.SOUL_TRANSFORMATION, 60000L),
        java.util.Map.entry(RealmId.ASCENDANT, 80000L),
        java.util.Map.entry(RealmId.ILLUSORY_YIN, 150000L),
        java.util.Map.entry(RealmId.CORPOREAL_YANG, 250000L),
        java.util.Map.entry(RealmId.NIRVANA_SCRYER, 400000L),
        java.util.Map.entry(RealmId.NIRVANA_CLEANSER, 600000L),
        java.util.Map.entry(RealmId.NIRVANA_FRUIT, 800000L),
        java.util.Map.entry(RealmId.SPIRIT_SEIZER, 900000L),
        java.util.Map.entry(RealmId.TRUE_IMMORTAL, 1000000L),
        java.util.Map.entry(RealmId.ANCIENT, 5000000L),
        java.util.Map.entry(RealmId.PARAGON, 20000000L),
        java.util.Map.entry(RealmId.TRANSCENDENCE, 100000000L)
    );

    /**
     * Compute S_sense = S_realm × (1 + ΣM_manual) + S_tempering.
     *
     * @param realm          the cultivator's realm
     * @param manualBonus    sum of soul-cultivation manual multipliers (ΣM)
     * @param tempering      soul tempering value (soft-capped)
     * @return the total soul power S_sense
     */
    public static long soulPowerTotal(RealmId realm, double manualBonus, long tempering) {
        long sRealm = S_REALM.getOrDefault(realm, 1L);
        // Soft cap: tempering cannot exceed S_realm × (realm_order+1)
        long temperingCap = sRealm * (realm.order + 1);
        long cappedTempering = Math.min(tempering, temperingCap);
        return Math.round(sRealm * (1 + manualBonus) + cappedTempering);
    }

    /**
     * Compute the divine sense radius (THE ANTI-LAG FORMULA).
     *
     * <pre>
     *   R = R_base × ln(S_sense + 1) × (1 − L_world/12) × Π(1 − σ_seal)
     * </pre>
     *
     * @param sSense        the player's soul power
     * @param worldLawTier  the world's law tier (0-12; higher = stronger world)
     * @param rBase         base radius (default 16; raised by arts)
     * @param seals         sealing-formation suppression values (0..1 each)
     * @return the radius in blocks (minimum 4)
     */
    public static int radius(long sSense, int worldLawTier, int rBase, double... seals) {
        double lawFactor = Math.max(0.05, 1.0 - worldLawTier / 12.0);
        double sealProduct = 1.0;
        for (double s : seals) sealProduct *= (1.0 - s);
        double r = rBase * Math.log(sSense + 1) * lawFactor * sealProduct;
        return Math.max(4, (int) Math.round(r));
    }

    /**
     * Activate a divine sense pulse — the SNAPSHOT.
     *
     * <p>This is button-triggered. It computes a single snapshot of
     * everything the player can perceive within their radius, returns
     * the result, and ends. No ongoing tick cost. No lag.
     *
     * @param playerRealm     the player's cultivation realm
     * @param playerSSense    the player's soul power S_sense
     * @param worldLawTier    the world's law tier
     * @param nearbyNpcs      NPCs in the player's vicinity (with their own S_sense)
     * @param concealedObjects concealed objects in the region (with camouflage values)
     * @param laws            the world laws at this location (affects perception)
     * @param rng             random source (for NPC confrontation outcomes)
     * @return the pulse result (radius, reactions, perceived objects, soul-fracture risk)
     */
    public static PulseResult pulse(RealmId playerRealm, long playerSSense, int worldLawTier,
                                    List<NpcContext> nearbyNpcs, List<ConcealedObjectContext> concealedObjects,
                                    WorldLaws laws, java.util.Random rng) {
        // ── Soul Fracture blinds divine sense ──
        // (Status check would go here in the full implementation.)

        int radius = radius(playerSSense, worldLawTier, 16);
        Ergenverse.LOGGER.debug("[Ergenverse] Divine Sense pulse: S_sense={}, radius={} blocks", playerSSense, radius);

        // ── NPC confrontation ──
        List<NpcReaction> reactions = new ArrayList<>();
        double soulFractureRisk = 0;
        for (NpcContext npc : nearbyNpcs) {
            long npcSSense = soulPowerTotal(npc.realm, npc.manualBonus, npc.tempering);
            long delta = playerSSense - npcSSense;

            NpcReaction r = confrontNpc(npc, delta, rng);
            reactions.add(r);
            if (r.soulFractureRisk > soulFractureRisk) {
                soulFractureRisk = r.soulFractureRisk;
            }
        }

        // ── Perceive concealed objects whose camouflage ≤ sSense ──
        List<PerceivedObject> perceived = new ArrayList<>();
        for (ConcealedObjectContext obj : concealedObjects) {
            if (!obj.perceived && obj.camouflage <= playerSSense) {
                perceived.add(new PerceivedObject(obj.name, obj.kind, obj.reward, obj.coords));
            }
        }

        // ── Soul Fracture from counter-strikes ──
        boolean soulFractureInflicted = false;
        if (soulFractureRisk > 0 && rng.nextDouble() < soulFractureRisk) {
            soulFractureInflicted = true;
            Ergenverse.LOGGER.info("[Ergenverse] Divine Sense pulse: mental counter-strike fractured the player's soul!");
        }

        return new PulseResult(radius, playerSSense, reactions, perceived, soulFractureInflicted);
    }

    private static NpcReaction confrontNpc(NpcContext npc, long delta, java.util.Random rng) {
        // Scenario A: Arrogant Interception (ΔS < 0, NPC stronger)
        if (delta < 0) {
            if (npc.faceObsession >= 0.7) {
                boolean counterStrike = rng.nextDouble() < 0.6;
                return new NpcReaction(npc.id, npc.name, npc.archetype,
                    "intercepted",
                    "ΔS=" + delta + " — " + npc.name + " detected your scan and, face-obsessed, read it as disrespect" +
                        (counterStrike ? " and fired a mental counter-strike (Soul Fracture risk!)" : ". They glower at you."),
                    counterStrike ? 0.5 : 0.0);
            }
            return new NpcReaction(npc.id, npc.name, npc.archetype,
                "intercepted",
                "ΔS=" + delta + " — " + npc.name + " detected your scan but dismissed it.",
                0.0);
        }

        // Scenario B: Absolute Spiritual Suppression (ΔS ≥ 5000)
        if (delta >= 5000) {
            if (npc.mortalityPanic >= 0.6 || npc.shamelessness >= 0.6) {
                return new NpcReaction(npc.id, npc.name, npc.archetype,
                    "suppressed",
                    "ΔS=" + delta + " — absolute mental dominance. " + npc.name + " drops to their knees, begging. Hidden trade tabs unlocked; taming requirements bypassed for beasts.",
                    0.0);
            }
        }

        // Scenario C: Unmasking the Hypocrite (righteous_hypocrite, ΔS ≥ 2000)
        if ("righteous_hypocrite".equals(npc.archetype) && delta >= 2000) {
            return new NpcReaction(npc.id, npc.name, npc.archetype,
                "unmasked",
                "ΔS=" + delta + " — your sense penetrates " + npc.name + "'s concealment script! Their false aura of altruism/honor shatters; the true matrix is revealed: max ambition, near-zero honor.",
                0.0);
        }

        // Ally alerted (20% chance if ΔS < 5000)
        if (delta < 5000 && rng.nextDouble() < 0.2) {
            return new NpcReaction(npc.id, npc.name, npc.archetype,
                "ally_alerted",
                npc.name + " felt your sense brush them but recognized you.",
                0.0);
        }

        // Default: unnoticed
        return new NpcReaction(npc.id, npc.name, npc.archetype,
            "unnoticed",
            npc.name + " did not notice your scan.",
            0.0);
    }

    // ─── Context types ───────────────────────────────────────────────

    /** Context for an NPC in the pulse radius. */
    public static final class NpcContext {
        public final String id;
        public final String name;
        public final String archetype;
        public final RealmId realm;
        public final double manualBonus;
        public final long tempering;
        public final double faceObsession;
        public final double mortalityPanic;
        public final double shamelessness;

        public NpcContext(String id, String name, String archetype, RealmId realm,
                          double manualBonus, long tempering,
                          double faceObsession, double mortalityPanic, double shamelessness) {
            this.id = id;
            this.name = name;
            this.archetype = archetype;
            this.realm = realm;
            this.manualBonus = manualBonus;
            this.tempering = tempering;
            this.faceObsession = faceObsession;
            this.mortalityPanic = mortalityPanic;
            this.shamelessness = shamelessness;
        }
    }

    /** Context for a concealed object in the region. */
    public static final class ConcealedObjectContext {
        public final String name;
        public final String kind;
        public final long camouflage;  // C-value
        public final String reward;
        public final String coords;
        public boolean perceived;

        public ConcealedObjectContext(String name, String kind, long camouflage,
                                      String reward, String coords) {
            this.name = name;
            this.kind = kind;
            this.camouflage = camouflage;
            this.reward = reward;
            this.coords = coords;
            this.perceived = false;
        }
    }

    /** A single NPC's reaction to the pulse. */
    public static final class NpcReaction {
        public final String npcId;
        public final String npcName;
        public final String archetype;
        public final String outcome; // intercepted | suppressed | unmasked | ally_alerted | unnoticed
        public final String detail;
        public final double soulFractureRisk;

        public NpcReaction(String npcId, String npcName, String archetype,
                           String outcome, String detail, double soulFractureRisk) {
            this.npcId = npcId;
            this.npcName = npcName;
            this.archetype = archetype;
            this.outcome = outcome;
            this.detail = detail;
            this.soulFractureRisk = soulFractureRisk;
        }
    }

    /** A concealed object perceived by the pulse. */
    public static final class PerceivedObject {
        public final String name;
        public final String kind;
        public final String reward;
        public final String coords;

        public PerceivedObject(String name, String kind, String reward, String coords) {
            this.name = name;
            this.kind = kind;
            this.reward = reward;
            this.coords = coords;
        }
    }

    /** The result of a divine sense pulse. */
    public static final class PulseResult {
        public final int radius;
        public final long sSense;
        public final List<NpcReaction> reactions;
        public final List<PerceivedObject> perceivedObjects;
        public final boolean soulFractureInflicted;

        public PulseResult(int radius, long sSense, List<NpcReaction> reactions,
                           List<PerceivedObject> perceivedObjects, boolean soulFractureInflicted) {
            this.radius = radius;
            this.sSense = sSense;
            this.reactions = reactions;
            this.perceivedObjects = perceivedObjects;
            this.soulFractureInflicted = soulFractureInflicted;
        }
    }

    // ─── Spiritual Camouflage registry (C-values) ────────────────────

    /** Camouflage values for concealed object kinds. */
    public static final java.util.Map<String, Long> CAMOUFLAGE_BY_KIND = java.util.Map.of(
        "inheritance_vault", 25000L,
        "ancient_formation", 20000L,
        "sealed_pavilion", 15000L,
        "trap_array", 8000L,
        "spirit_vein", 500L,
        "hidden_cave", 200L
    );

    public static long camouflageFor(String kind) {
        return CAMOUFLAGE_BY_KIND.getOrDefault(kind, 1000L);
    }

    // ─── Dual-mode: snapshot (click) + continuous (hold) ──────────────
    //
    // The divine sense button has TWO modes:
    //   1. CLICK (tap < 200ms): quick-cast the last divine sense technique
    //      used (default: Pulse). This is the snapshot pulse.
    //   2. HOLD (> 200ms): continuous active sense. Re-pulses every
    //      CONTINUOUS_PULSE_INTERVAL_TICKS, draining soul strain. Turns
    //      off on key release.
    //
    // The continuous mode is just a scheduled re-pulse with a soul-strain
    // cost. No new architecture — just a tick scheduler.

    /** Ticks between re-pulses in continuous mode (20 ticks = 1 second). */
    public static final int CONTINUOUS_PULSE_INTERVAL_TICKS = 20;

    /** Soul strain per second of continuous mode (1% of max S_sense). */
    public static final double CONTINUOUS_SOUL_STRAIN_PER_SECOND = 0.01;

    /** If S_sense drops below this fraction of max, soul fracture occurs. */
    public static final double SOUL_FRACTURE_THRESHOLD = 0.5;

    /**
     * Continuous-mode state, tracked per-player.
     *
     * <p>When the player holds the divine sense key, this state is active.
     * Each tick, the engine checks if it's time for a re-pulse (every
     * {@link #CONTINUOUS_PULSE_INTERVAL_TICKS} ticks), and if so, runs
     * {@link #pulse} again with the player's current context.
     *
     * <p>Soul strain accumulates: each second, S_sense drops by
     * {@link #CONTINUOUS_SOUL_STRAIN_PER_SECOND} × max S_sense. If S_sense
     * drops below {@link #SOUL_FRACTURE_THRESHOLD} × max, the player gets
     * the soul_fracture status (blinds divine sense, must brew elixir).
     */
    public static final class ContinuousState {
        public boolean active;
        public int ticksSinceLastPulse;
        public long soulStrainAccumulated;  // total S_sense drained this session
        public PulseResult lastPulseResult;  // cached for HUD rendering

        public void start() {
            this.active = true;
            this.ticksSinceLastPulse = 0;
            this.soulStrainAccumulated = 0;
        }

        public void stop() {
            this.active = false;
            this.lastPulseResult = null;
        }

        /**
         * Tick the continuous mode. Returns true if a re-pulse should fire
         * this tick.
         */
        public boolean tick() {
            if (!active) return false;
            ticksSinceLastPulse++;
            if (ticksSinceLastPulse >= CONTINUOUS_PULSE_INTERVAL_TICKS) {
                ticksSinceLastPulse = 0;
                return true;
            }
            return false;
        }

        /**
         * Apply soul strain for one second of continuous mode.
         *
         * @param maxSSense the player's max S_sense (without strain)
         * @return the new (reduced) S_sense after strain
         */
        public long applySoulStrain(long maxSSense) {
            long strain = Math.round(maxSSense * CONTINUOUS_SOUL_STRAIN_PER_SECOND);
            soulStrainAccumulated += strain;
            return Math.max(0, maxSSense - soulStrainAccumulated);
        }

        /**
         * Check if the player should get soul_fracture from overuse.
         */
        public boolean shouldInflictSoulFracture(long currentSSense, long maxSSense) {
            return maxSSense > 0 && currentSSense < maxSSense * SOUL_FRACTURE_THRESHOLD;
        }
    }

    /**
     * The divine sense manipulation techniques — a sub-category of the
     * Technique Wheel's "Divine Sense" category.
     *
     * <p>These are NOT just perception. They use divine sense as a FORCE:
     * ripping veins from underground, moving mountains, lifting objects.
     * All use the Voxel Factorization Engine with the {@code terraform}
     * block operator.
     *
     * <p>See DESIGN_DIVINE_SENSE_TERRAFORMING.md for the full proposal.
     */
    public enum ManipulationTechnique {
        /** The snapshot pulse — quick perception of everything in radius. */
        PULSE("Pulse", RealmId.QI_CONDENSATION, "Small S_sense",
            "The snapshot pulse. Perceive everything in your radius in a single instant."),

        /** Sense through solid ground — perceive underground formations/veins/caves. */
        EARTH_SENSE("Earth Sense", RealmId.FOUNDATION, "Medium S_sense",
            "Extend your divine sense through solid ground. Perceive underground formations, spirit veins, and concealed caves within your radius."),

        /** Rip a spirit vein directly from underground. */
        VEIN_EXTRACTION("Vein Extraction", RealmId.NASCENT_SOUL, "Large S_sense + Qi",
            "Rip a spirit vein directly from the earth. The vein (in the Spiritual Layer) is pulled into the Physical Layer as a harvestable Spirit Vein Core. Requires F_destruct >= R_voxel × 2 (terraform threshold)."),

        /** Telekinesis on a targeted block/entity. */
        OBJECT_LIFTING("Object Lifting", RealmId.FOUNDATION, "S_sense + Qi (sustained)",
            "Lift a single block or entity with divine sense. Move it with mouse aim. Sustained cost while holding. Voxel factorization: F_destruct vs R_voxel for the targeted block."),

        /** Lift and relocate a volume of physical blocks. */
        MOUNTAIN_MOVING("Mountain Moving", RealmId.SOUL_FORMATION, "Massive S_sense + Qi",
            "Lift and relocate an entire volume of physical blocks. The mountain moves; it is not destroyed. Requires F_destruct >= total R_voxel × 2. Scale gating: Soul Formation = hill, Ascendant = mountain, Transcendent = mountain range."),

        /** Detect hostile intent directed at you (passive once learned). */
        KILLING_INTENT_DETECTION("Killing Intent Detection", RealmId.FOUNDATION, "Passive",
            "Detect hostile intent directed at you within your radius. Passive — always on once learned."),

        /** Steal a technique from an NPC's mind (forbidden). */
        SOUL_SEARCH("Soul Search (forbidden)", RealmId.NASCENT_SOUL, "S_sense + Karma",
            "Steal a technique from an NPC's mind. High karma cost, heart-demon risk, soul-fracture on failure."),

        /** See karmic threads (snapshot with long cooldown). */
        KARMA_VISION("Karma Vision", RealmId.NASCENT_SOUL, "S_sense + 20y cooldown",
            "See karmic threads. Snapshot pulse with a 20-year cooldown."),

        /** Find spatial cracks to other dimensions. */
        SPATIAL_CRACK_DETECTION("Spatial Crack Detection", RealmId.TRANSCENDENCE, "Massive S_sense",
            "Find spatial cracks leading to other dimensions. Enables cross-branch cosmological travel.");

        public final String name;
        public final RealmId realmGate;
        public final String cost;
        public final String description;

        ManipulationTechnique(String name, RealmId realmGate, String cost, String description) {
            this.name = name;
            this.realmGate = realmGate;
            this.cost = cost;
            this.description = description;
        }
    }
}
