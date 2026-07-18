package dev.ergenverse.simulation;

/**
 * CultivatorActivity — the Layer-2 simulation token describing what a
 * reified NPC cultivator is currently doing in the physical world.
 *
 * <p><b>Layer归属 / Layer归属:</b> this enum is <b>Layer 2 (Simulation)</b>,
 * not Layer 1 (Canon). The canon JSONs describe a character's static
 * attributes (name, realm, location). They do not prescribe a moment-by-moment
 * activity schedule. The activity schedule is the simulation's job —
 * the simulation decides "right now, Wang Tiangui is meditating," and the
 * ReificationScan propagates that decision to the entity shell.
 *
 * <p><b>Why a separate enum and not a boolean isMeditating:</b>
 * <ul>
 *   <li>Three states cover the V3 POC scope: wandering (default), meditating
 *       (the canonical cultivation pose), and breaking through (the rare
 *       visible event that's the payoff of the reification pipeline).</li>
 *   <li>Future activities (sparring, refining pills, sect meetings) can be
 *       added without breaking existing switch statements — they get a
 *       sensible default branch.</li>
 *   <li>SyncedEntityData carries a single string token, not multiple
 *       booleans — easier to debug, less bandwidth.</li>
 * </ul>
 *
 * <h2>State machine</h2>
 * <pre>
 *   WANDERING  ──(simulation says meditate)──►  MEDITATING
 *   MEDITATING ──(simulation says breakthrough)─►  BREAKING_THROUGH
 *   BREAKING_THROUGH ──(breakthrough completes)─►  WANDERING
 * </pre>
 *
 * <p>Transitions are driven by {@link CultivatorActivityResolver}, which is
 * pure (deterministic given characterId + tick). This means every client
 * and the server agree on what an NPC is doing without needing extra sync
 * packets — the {@code character_id} and the game time are sufficient.
 *
 * <h2>Sync token format</h2>
 * <p>The {@link #syncId} is the string written into the entity's
 * {@code SynchedEntityData}. It must be lowercase ASCII + underscores only
 * (no spaces, no Unicode) so it survives NBT round-trips cleanly.
 */
public enum CultivatorActivity {

    /**
     * The cultivator is wandering their home area — walking, looking around,
     * occasionally pausing. This is the default state. The entity's normal
     * AI goals (stroll, look around) run.
     */
    WANDERING("wandering"),

    /**
     * The cultivator is sitting in meditation — the iconic cultivation pose.
     * Movement is frozen, navigation is stopped, and ambient qi-gathering
     * particles spawn around the entity. This is the V3 POC's primary
     * "the simulation is visible" state.
     *
     * <p>Per the proposal: "simulation decides he should meditate →
     * ReificationScan updates his entity → player sees him sitting in
     * meditation with particle effects."
     */
    MEDITATING("meditating"),

    /**
     * The cultivator is attempting a breakthrough — a rare, dramatic event
     * visible from a distance. The entity is stationary, intense particles
     * burst outward, and (in v2) a tribulation may follow. For v1 this is
     * a visual flag only — no actual realm change.
     *
     * <p>Canon: breakthroughs are canon-attested events, but the timing of
     * any NPC's breakthrough is Layer 2 simulation, not Layer 1 canon.
     * Wang Lin's breakthrough moments are canon; whether Wang Tiangui
     * ever breaks through (and when) is simulation design.
     */
    BREAKING_THROUGH("breaking_through");

    /** Lowercase ASCII sync token written to SynchedEntityData and NBT. */
    public final String syncId;

    CultivatorActivity(String syncId) {
        this.syncId = syncId;
    }

    /**
     * Parse a sync token back into the enum. Returns {@link #WANDERING}
     * for unknown/null/empty tokens — never throws. This is the defensive
     * default: if NBT or a sync packet contains a token we don't recognize
     * (e.g. a future activity type on an older client), the entity falls
     * back to wandering rather than desyncing.
     */
    public static CultivatorActivity fromSyncId(String syncId) {
        if (syncId == null || syncId.isEmpty()) return WANDERING;
        for (CultivatorActivity a : values()) {
            if (a.syncId.equals(syncId)) return a;
        }
        return WANDERING;
    }

    /** True if this activity freezes the entity's movement. */
    public boolean isStationary() {
        return this == MEDITATING || this == BREAKING_THROUGH;
    }

    /** True if this activity should spawn ambient particles around the entity. */
    public boolean spawnsParticles() {
        return this == MEDITATING || this == BREAKING_THROUGH;
    }
}
