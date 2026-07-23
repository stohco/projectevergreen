package dev.ergenverse.simulation.settlement;

/**
 * ActorReasoningEngine — turns one shared {@link WorldSituation} into N
 * different {@link Activity} decisions, one per actor.
 *
 * <p>Per the user's architectural directive (the cycle after SettlementThreatIndex):
 * <blockquote>
 * World → Meaning → Reasoning → Decision → Presence.
 * <br><br>
 * Wang Lin thinks: Weak wolves. No danger. Observe. Maybe useful. Protect
 * family if necessary. Don't reveal strength.
 * <br>
 * Farmer Chen thinks: Wolf. Run.
 * <br>
 * Village hunter thinks: Grab spear. Protect sheep.
 * <br>
 * Merchant thinks: Pack goods. Hide.
 * <br><br>
 * Same event. Different minds. That difference shouldn't live inside
 * SettlementThreatIndex. It should emerge from the reasoning engine.
 * </blockquote>
 *
 * <p>This is that engine. It takes:
 * <ul>
 *   <li>One {@link WorldSituation} (the shared world — same for everyone).</li>
 *   <li>One {@link ActorProfile} (the actor's mind — different per actor).</li>
 *   <li>The {@link Settlement} (for home/location resolution).</li>
 * </ul>
 * And produces one {@link Activity} — or {@code null} when the situation does
 * not demand a decision (peaceful daily rhythm takes over).
 *
 * <h2>The reasoning rules (explicit, not learned)</h2>
 * <p>Each rule is a small, readable branch. The user can audit exactly why
 * Wang Lin observes while Da Niu guards. No neural net, no weights file —
 * just canon-faithful if/else that a reader can trace.
 *
 * <h3>When the situation is peaceful (no threat)</h3>
 * <p>Returns {@code null}. The {@link ActorMaterializer} falls back to the
 * time-of-day weighted daily-rhythm presence from {@link ActorPresence}. This
 * is deliberate: the reasoning engine does NOT replace the daily rhythm, it
 * layers on top when the world demands a decision.
 *
 * <h3>When a threat is active</h3>
 * <p>The engine applies the following priority (first match wins):
 * <ol>
 *   <li><b>HIDDEN_CULTIVATOR</b> (Wang Lin): if the threat is mortal-level
 *       (wolves) and combatConfidence ≥ 0.5 → {@link Activity.Type#OBSERVING_THREAT}.
 *       He assesses the threat as survivable and chooses to watch from cover
 *       rather than reveal his capability. If the threat is apex and he
 *       cannot prevail → {@link Activity.Type#FLEEING_HOME} (even Wang Lin
 *       flees a beast beyond his stage — canon: he survives by running).</li>
 *   <li><b>LABORER / HUNTER</b> (Da Niu): if the actor has assets to secure
 *       (livestock) → {@link Activity.Type#SECURING_ASSETS} at the asset
 *       location. If courage + combatConfidence ≥ 0.9 →
 *       {@link Activity.Type#GUARDING} the perimeter.</li>
 *   <li><b>CULTIVATOR</b> (sect disciples): if combatConfidence ≥ 0.4 →
 *       {@link Activity.Type#GUARDING} (sect duty). Else flee.</li>
 *   <li><b>FARMER / HOMEMAKER / ELDER / CHILD</b>: if effectiveCourage ≥ 0.7
 *       (rare — a bold elder) → {@link Activity.Type#GUARDING}. Else →
 *       {@link Activity.Type#FLEEING_HOME}.</li>
 * </ol>
 *
 * <h2>Location derivation</h2>
 * <p>Each activity type derives its own location:
 * <ul>
 *   <li>FLEEING_HOME → the actor's home residence center (or settlement center
 *       if homeless).</li>
 *   <li>OBSERVING_THREAT → a vantage point at the settlement edge in the
 *       direction of the threat (offset 22 blocks toward the threat — close
 *       enough to see, far enough to stay hidden).</li>
 *   <li>GUARDING → the settlement edge toward the threat (offset 16 blocks).</li>
 *   <li>SECURING_ASSETS → the nearest shared "asset" location (livestock pen /
 *       market). Falls back to settlement center.</li>
 * </ul>
 */
public final class ActorReasoningEngine {

    /** How far toward the threat an OBSERVING actor positions (blocks from center). */
    public static final int OBSERVE_VANTAGE_DISTANCE = 22;

    /** How far toward the threat a GUARDING actor positions (blocks from center). */
    public static final int GUARD_PERIMETER_DISTANCE = 16;

    private ActorReasoningEngine() {}

    /**
     * Reason over the shared situation through the actor's profile.
     *
     * @param actorId    the actor's id
     * @param situation  the shared world situation (same for everyone)
     * @param settlement the actor's settlement (for home/location resolution)
     * @return an Activity (the actor's decision), or null if the peaceful
     *         daily rhythm should take over.
     */
    public static Activity reason(String actorId, WorldSituation situation,
                                  Settlement settlement) {
        if (actorId == null || situation == null || settlement == null) return null;

        // Peaceful — no decision needed. Daily rhythm takes over.
        if (!situation.hasThreat()) return null;

        ActorProfile profile = ActorProfileRegistry.get(actorId);
        if (profile == null) {
            // No profile → default mortal flee behavior (the safe default).
            return fleeHome(settlement, actorId, situation, "No profile; defaulting to mortal flee.");
        }

        WorldSituation.Threat threat = situation.primaryThreat;
        float effCourage = profile.effectiveCourage(
                settlement.personality != null ? settlement.personality.mood
                        : SettlementPersonality.Mood.PEACEFUL);
        // Night makes everyone more cautious.
        boolean night = situation.timeOfDay == TimeOfDay.NIGHT
                || situation.timeOfDay == TimeOfDay.DUSK;
        float courage = night ? effCourage - 0.10f : effCourage;

        return switch (profile.role) {
            case HIDDEN_CULTIVATOR -> reasonHiddenCultivator(profile, threat, settlement, situation, courage);
            case LABORER, HUNTER -> reasonLaborerOrHunter(profile, threat, settlement, situation, courage);
            case CULTIVATOR -> reasonCultivator(profile, threat, settlement, situation, courage);
            case FARMER, HOMEMAKER, ELDER, CHILD -> reasonMortal(profile, threat, settlement, situation, courage);
            case MERCHANT -> reasonMerchant(profile, threat, settlement, situation, courage);
        };
    }

    // ── Per-role reasoning ──────────────────────────────────────────────

    /**
     * WANG LIN's reasoning (the keystone). Canon: he watches, he waits, he
     * does not posture. Against mortal wolves: observe from cover, don't reveal
     * strength, protect family only if truly necessary. Against an apex beast
     * beyond his stage: flee (he survives by running, not by dying heroically).
     */
    private static Activity reasonHiddenCultivator(ActorProfile p, WorldSituation.Threat threat,
                                                    Settlement s, WorldSituation sit, float courage) {
        // Apex threat beyond capability → even Wang Lin flees.
        if (threat.isApex() && p.combatConfidence < 0.7f) {
            return fleeHome(s, p.actorId, sit,
                    p.displayName + " assesses the apex beast as beyond his current stage and retreats to protect his family.");
        }

        // Mortal wolves — no danger. Observe from cover. Don't reveal strength.
        if (threat.isMortalLevel() && p.combatConfidence >= 0.5f) {
            int[] vantage = vantageToward(s, threat, OBSERVE_VANTAGE_DISTANCE);
            return new Activity(Activity.Type.OBSERVING_THREAT,
                    vantage[0], vantage[1], "observing",
                    threat.expiryTick(),
                    p.displayName + " judges the wolves as no real danger and watches from the treeline, concealing his capability.");
        }

        // Predator (mid-tier) — observe only if confident, else flee.
        if (p.combatConfidence >= 0.6f) {
            int[] vantage = vantageToward(s, threat, OBSERVE_VANTAGE_DISTANCE);
            return new Activity(Activity.Type.OBSERVING_THREAT,
                    vantage[0], vantage[1], "observing",
                    threat.expiryTick(),
                    p.displayName + " observes the predator from cover, assessing whether intervention is warranted.");
        }
        return fleeHome(s, p.actorId, sit,
                p.displayName + " retreats to his family — the threat is not worth revealing himself for.");
    }

    /**
     * DA NIU's reasoning (laborer/hunter). Responsible for livestock. Secures
     * the animals first; if confident, guards the pen.
     */
    private static Activity reasonLaborerOrHunter(ActorProfile p, WorldSituation.Threat threat,
                                                   Settlement s, WorldSituation sit, float courage) {
        // Strong and confident → guard the perimeter toward the threat.
        if (courage + p.combatConfidence >= 0.9f && !threat.isApex()) {
            int[] perim = vantageToward(s, threat, GUARD_PERIMETER_DISTANCE);
            return new Activity(Activity.Type.GUARDING,
                    perim[0], perim[1], "guarding",
                    threat.expiryTick(),
                    p.displayName + " grabs a tool and stands at the perimeter, ready to defend the settlement.");
        }
        // Otherwise → secure the livestock/assets, then stay there.
        int[] assets = nearestAssetLocation(s);
        return new Activity(Activity.Type.SECURING_ASSETS,
                assets[0], assets[1], "idle",
                threat.expiryTick(),
                p.displayName + " runs to secure the livestock before the wolves reach them.");
    }

    /**
     * Sect cultivator reasoning. Sect duty: guard. But flee if outmatched.
     */
    private static Activity reasonCultivator(ActorProfile p, WorldSituation.Threat threat,
                                              Settlement s, WorldSituation sit, float courage) {
        if (p.combatConfidence >= 0.4f && !threat.isApex()) {
            int[] perim = vantageToward(s, threat, GUARD_PERIMETER_DISTANCE);
            return new Activity(Activity.Type.GUARDING,
                    perim[0], perim[1], "guarding",
                    threat.expiryTick(),
                    p.displayName + " takes up a defensive position — a disciple's duty.");
        }
        return fleeHome(s, p.actorId, sit,
                p.displayName + " retreats to quarters — the threat exceeds his current ability.");
    }

    /**
     * Mortal reasoning (farmer, homemaker, elder, child). Bold ones guard;
     * the rest flee home. Most flee.
     */
    private static Activity reasonMortal(ActorProfile p, WorldSituation.Threat threat,
                                          Settlement s, WorldSituation sit, float courage) {
        if (courage >= 0.7f && !threat.isApex()) {
            int[] perim = vantageToward(s, threat, GUARD_PERIMETER_DISTANCE);
            return new Activity(Activity.Type.GUARDING,
                    perim[0], perim[1], "guarding",
                    threat.expiryTick(),
                    p.displayName + " is among the bold few who stand to defend the village.");
        }
        return fleeHome(s, p.actorId, sit,
                p.displayName + " flees home and bars the door — the mortal's response to danger.");
    }

    /**
     * Merchant reasoning. Pack goods, hide, then flee home.
     */
    private static Activity reasonMerchant(ActorProfile p, WorldSituation.Threat threat,
                                            Settlement s, WorldSituation sit, float courage) {
        int[] market = nearestAssetLocation(s);
        return new Activity(Activity.Type.SECURING_ASSETS,
                market[0], market[1], "idle",
                threat.expiryTick(),
                p.displayName + " packs the most valuable goods and hides them before fleeing.");
    }

    // ── Location helpers ────────────────────────────────────────────────

    /** Build a FLEEING_HOME activity at the actor's home (or settlement center). */
    private static Activity fleeHome(Settlement s, String actorId, WorldSituation sit, String reason) {
        Residence home = s.residenceFor(actorId);
        int x = (home != null && !home.isDestroyed()) ? home.centerX() : 0;
        int z = (home != null && !home.isDestroyed()) ? home.centerZ() : 0;
        return new Activity(Activity.Type.FLEEING_HOME, x, z, "idle",
                sit.primaryThreat != null ? sit.primaryThreat.expiryTick() : sit.gameTime + 2400L,
                reason);
    }

    /**
     * A vantage point at the settlement edge toward the threat.
     * @param distance blocks from settlement center
     */
    private static int[] vantageToward(Settlement s, WorldSituation.Threat threat, int distance) {
        // Direction vector (already normalized in the Threat record). Clamp to
        // the settlement edge at the given distance.
        int dx = Math.round(threat.directionX() * distance);
        int dz = Math.round(threat.directionZ() * distance);
        return new int[]{dx, dz};
    }

    /**
     * The nearest shared "asset" location (livestock pen, market). Falls back
     * to settlement center. Used by SECURING_ASSETS.
     */
    private static int[] nearestAssetLocation(Settlement s) {
        // Prefer locations whose id suggests livestock/market/farms.
        for (PresenceLocation loc : s.getSharedLocations()) {
            String id = loc.id.toLowerCase();
            if (id.contains("market") || id.contains("livestock")
                    || id.contains("farm") || id.contains("pen")) {
                return new int[]{loc.offsetX, loc.offsetZ};
            }
        }
        return new int[]{0, 0};
    }
}
