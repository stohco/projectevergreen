package dev.ergenverse.simulation.opportunity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * OpportunityState — the live state of ONE opportunity instance in the world.
 *
 * <p>This is the per-instance companion to the {@link OpportunityLifecycle}
 * enum. Where {@code OpportunityLifecycle} describes the <i>rules</i> (the
 * allowed states + transitions), this class holds the <i>current state</i>
 * of one specific opportunity: which lifecycle stage it is in, who knows
 * about it, how distorted the rumors have become, and what ultimately
 * happened.
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li><b>opportunityId</b> — stable identifier (e.g.
 *       {@code "ergenverse:spirit_fruit_120_-340"})</li>
 *   <li><b>posX/Y/Z</b> — world position of the opportunity (for spatial
 *       queries by {@link OpportunityRegistry}). Stored as three ints to
 *       keep NBT serialization simple and to avoid dimensional issues.</li>
 *   <li><b>lifecycle</b> — the current {@link OpportunityLifecycle} state</li>
 *   <li><b>worldTickCreated</b> — when the opportunity was first registered
 *       (t₀ for this opportunity)</li>
 *   <li><b>worldTickLastUpdated</b> — the last tick this state was advanced</li>
 *   <li><b>worldTickResolved</b> — when the opportunity entered the RESOLVED
 *       state (used for the RESOLVED→HISTORICAL cooldown)</li>
 *   <li><b>currentActors</b> — beast / cultivator / scout IDs currently
 *       aware of the opportunity. Order-preserving; no duplicates.</li>
 *   <li><b>distortionHops</b> — how many rumor-hops this opportunity has
 *       undergone (see PROJECT_MASTER.md §6.11 Rumor System). 0 = direct
 *       observation; 1+ = distorted by social propagation.</li>
 *   <li><b>resolutionOutcome</b> — nullable. Set when the opportunity
 *       resolves. One of: {@code "claimed_by_<actorId>"}, {@code "destroyed"},
 *       {@code "expired"}, {@code "rotted"}.</li>
 *   <li><b>historicalSummary</b> — nullable. Set when the opportunity
 *       becomes HISTORICAL. A short string the rumor system can quote
 *       (e.g. "Three years ago, a spirit fruit ripened in Mosquito Valley —
 *       many died for it.").</li>
 *   <li><b>driverType</b> — what kind of simulation driver owns this
 *       opportunity (e.g. {@code "spirit_fruit_timeline"} or {@code "noop"}).
 *       Determines which subsystem advances it each tick.</li>
 *   <li><b>driverTickZero</b> — the t=0 tick for the driver's internal
 *       timeline (e.g. when a spirit fruit ripened). Drivers compute
 *       elapsed time as {@code currentTick - driverTickZero}.</li>
 *   <li><b>firedMilestones</b> — which timeline milestones have already
 *       fired (prevents double-firing). Each driver names its own milestones
 *       (e.g. {@code "insects_gather"}, {@code "wolf_notices"}).</li>
 *   <li><b>queuedSpawnActions</b> — entity-spawn requests queued by the
 *       driver. The actual entity types may not exist yet (PA-C will create
 *       them); these strings preserve the spawn intent so a future
 *       subagent can drain the queue and spawn real entities.</li>
 * </ul>
 *
 * <h2>Methods</h2>
 * <ul>
 *   <li>{@link #tick(long)} — updates {@code worldTickLastUpdated}. Does NOT
 *       make transition decisions; the {@link OpportunityFSM} does that.</li>
 *   <li>{@link #transitionTo(OpportunityLifecycle, long)} — applies a state
 *       transition (validated by {@link OpportunityLifecycle#canTransitionTo}).</li>
 *   <li>{@link #addActor(String)} — adds an actor (deduplicated, order-preserving).</li>
 *   <li>{@link #resolve(String, long)} — sets {@code resolutionOutcome} and
 *       transitions to RESOLVED.</li>
 *   <li>{@link #toHistorical(long)} — transitions to HISTORICAL and sets
 *       {@code historicalSummary} from the resolution outcome.</li>
 *   <li>{@link #serializeNBT()} / {@link #deserializeNBT(CompoundTag)} — NBT
 *       persistence for {@link OpportunityRegistry}'s world save.</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * <p>Instances are NOT thread-safe. They are mutated only from the server
 * tick thread (via {@link OpportunityRegistry#tickAll}), which is single-
 * threaded by Minecraft contract. Reads from other threads must coordinate
 * externally.
 *
 * <h2>Prime Directive</h2>
 * <p>This class is the SIMULATION state. It does NOT record canon facts
 * (those are in {@link CanonOpportunityRecord}, Layer 1). The simulation may
 * advance, mutate, resolve, and historicize this state freely — that is the
 * simulation's job, not a violation of canon.
 */
public final class OpportunityState {

    // ─── Core identity & position ──────────────────────────────────────
    private final String opportunityId;
    private int posX, posY, posZ;

    // ─── Lifecycle ─────────────────────────────────────────────────────
    private OpportunityLifecycle lifecycle;
    private long worldTickCreated;
    private long worldTickLastUpdated;
    private long worldTickResolved;   // 0 = never resolved

    // ─── Actors & rumors ───────────────────────────────────────────────
    private final Set<String> currentActors = new LinkedHashSet<>();
    private int distortionHops = 0;

    // ─── Resolution & history ──────────────────────────────────────────
    private String resolutionOutcome;     // nullable
    private String historicalSummary;     // nullable

    // ─── Driver plumbing ───────────────────────────────────────────────
    private String driverType = "noop";
    private long driverTickZero = 0L;
    private final Set<String> firedMilestones = new LinkedHashSet<>();
    private final List<String> queuedSpawnActions = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════════
    //  Constructors
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Create a new DORMANT opportunity at the given position, with the
     * given creation tick.
     *
     * @param opportunityId stable identifier (e.g. "ergenverse:spirit_fruit_120_-340")
     * @param pos           world position (used by {@link OpportunityRegistry#getOpportunitiesNear})
     * @param worldTick     the world tick at creation (becomes worldTickCreated)
     */
    public OpportunityState(String opportunityId, BlockPos pos, long worldTick) {
        if (opportunityId == null || opportunityId.isBlank()) {
            throw new IllegalArgumentException("OpportunityState requires an opportunityId");
        }
        if (pos == null) pos = BlockPos.ZERO;
        this.opportunityId = opportunityId;
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.lifecycle = OpportunityLifecycle.DORMANT;
        this.worldTickCreated = worldTick;
        this.worldTickLastUpdated = worldTick;
        this.worldTickResolved = 0L;
    }

    /** Private constructor for NBT deserialization. The opportunityId MUST be set immediately. */
    private OpportunityState(String opportunityId) {
        if (opportunityId == null || opportunityId.isBlank()) {
            throw new IllegalArgumentException("OpportunityState requires an opportunityId");
        }
        this.opportunityId = opportunityId;
        this.lifecycle = OpportunityLifecycle.DORMANT;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════════

    /** The opportunity's stable identifier. */
    public String getOpportunityId() { return opportunityId; }

    /** World X coordinate of the opportunity. */
    public int getPosX() { return posX; }
    /** World Y coordinate of the opportunity. */
    public int getPosY() { return posY; }
    /** World Z coordinate of the opportunity. */
    public int getPosZ() { return posZ; }

    /** The opportunity's world position as a fresh {@link BlockPos}. */
    public BlockPos getPos() { return new BlockPos(posX, posY, posZ); }

    /** The current lifecycle state. */
    public OpportunityLifecycle getLifecycle() { return lifecycle; }

    /** When this opportunity was first registered. */
    public long getWorldTickCreated() { return worldTickCreated; }

    /** The last tick this state was advanced. */
    public long getWorldTickLastUpdated() { return worldTickLastUpdated; }

    /** When the opportunity entered RESOLVED. Returns 0 if never resolved. */
    public long getWorldTickResolved() { return worldTickResolved; }

    /**
     * Immutable view of the actors currently aware of the opportunity.
     * Order is insertion order; duplicates are absent.
     */
    public List<String> getCurrentActors() {
        return List.copyOf(currentActors);
    }

    /** How many rumor-hops this opportunity has undergone (0 = direct observation). */
    public int getDistortionHops() { return distortionHops; }

    /** Increment the distortion-hops counter (called by the rumor system). */
    public void incrementDistortionHops() {
        this.distortionHops++;
        markDirty();
    }

    /** The resolution outcome, or null if not yet resolved. */
    public String getResolutionOutcome() { return resolutionOutcome; }

    /** The historical summary, or null if not yet historical. */
    public String getHistoricalSummary() { return historicalSummary; }

    /** The driver type (e.g. "spirit_fruit_timeline", "noop"). */
    public String getDriverType() { return driverType; }

    /** Set the driver type. Called by driver factories at registration time. */
    public void setDriverType(String driverType) {
        this.driverType = (driverType == null || driverType.isBlank()) ? "noop" : driverType;
        markDirty();
    }

    /** The t=0 tick for the driver's internal timeline. */
    public long getDriverTickZero() { return driverTickZero; }

    /** Set the driver's t=0 tick (e.g. when a spirit fruit ripened). */
    public void setDriverTickZero(long tick) {
        this.driverTickZero = tick;
        markDirty();
    }

    /**
     * Update the world-tick timestamp. Called once per tick by the registry
     * before the FSM advances the state. Does NOT trigger transitions.
     */
    public void tick(long worldTick) {
        this.worldTickLastUpdated = worldTick;
        // markDirty is implicit — the registry owns the SavedData dirty flag.
    }

    /**
     * Apply a lifecycle transition.
     * <p>Validates that the transition is canon-faithful via
     * {@link OpportunityLifecycle#canTransitionTo}. Throws
     * {@link IllegalStateException} if the transition is illegal.
     *
     * @param next      the new lifecycle state
     * @param worldTick the world tick at which the transition occurs
     */
    public void transitionTo(OpportunityLifecycle next, long worldTick) {
        if (!lifecycle.canTransitionTo(next)) {
            throw new IllegalStateException(
                "Illegal opportunity transition: " + lifecycle + " → " + next +
                " (opportunityId=" + opportunityId + ")");
        }
        OpportunityLifecycle previous = this.lifecycle;
        this.lifecycle = next;
        this.worldTickLastUpdated = worldTick;
        if (next == OpportunityLifecycle.RESOLVED) {
            this.worldTickResolved = worldTick;
        }
        dev.ergenverse.core.Ergenverse.LOGGER.debug(
            "[Opportunity] {} transition: {} → {} (tick={})",
            opportunityId, previous, next, worldTick);
        markDirty();
    }

    /**
     * Add an actor to the "currently aware" set. Idempotent — adding an
     * actor already in the set is a no-op. Order-preserving.
     *
     * @param actorId the actor's stable ID (e.g. "wolf_spirit_beast_42")
     */
    public void addActor(String actorId) {
        if (actorId == null || actorId.isBlank()) return;
        if (currentActors.add(actorId)) {
            markDirty();
        }
    }

    /**
     * Resolve the opportunity with the given outcome. Sets
     * {@code resolutionOutcome} and transitions to RESOLVED. Must be in
     * CONTESTED (or earlier — CONTESTED is enforced for canon faithfulness,
     * but we allow RESOLVED→RESOLVED as a no-op idempotency guard).
     *
     * @param outcome   one of "claimed_by_<actorId>", "destroyed", "expired", "rotted"
     * @param worldTick the world tick at which the resolution occurs
     */
    public void resolve(String outcome, long worldTick) {
        if (outcome == null || outcome.isBlank()) {
            throw new IllegalArgumentException("Resolution outcome must not be null/blank");
        }
        if (lifecycle == OpportunityLifecycle.RESOLVED
            || lifecycle == OpportunityLifecycle.HISTORICAL) {
            // Already resolved — just update the outcome text (rare, but safe).
            this.resolutionOutcome = outcome;
            markDirty();
            return;
        }
        if (lifecycle != OpportunityLifecycle.CONTESTED) {
            throw new IllegalStateException(
                "Cannot resolve opportunity from state " + lifecycle +
                " (opportunityId=" + opportunityId + ")");
        }
        this.resolutionOutcome = outcome;
        transitionTo(OpportunityLifecycle.RESOLVED, worldTick);
    }

    /**
     * Promote the opportunity to HISTORICAL and synthesize a default
     * historical summary from the resolution outcome.
     * <p>Must be in RESOLVED. After this, the state is terminal.
     *
     * @param worldTick the world tick at which the promotion occurs
     */
    public void toHistorical(long worldTick) {
        if (lifecycle == OpportunityLifecycle.HISTORICAL) return;
        if (lifecycle != OpportunityLifecycle.RESOLVED) {
            throw new IllegalStateException(
                "Cannot promote to HISTORICAL from state " + lifecycle +
                " (opportunityId=" + opportunityId + ")");
        }
        transitionTo(OpportunityLifecycle.HISTORICAL, worldTick);
        if (this.historicalSummary == null) {
            this.historicalSummary = synthesizeHistoricalSummary();
        }
        markDirty();
    }

    /**
     * Explicitly set the historical summary (e.g. for richer narration).
     * Overrides the auto-synthesized version.
     */
    public void setHistoricalSummary(String summary) {
        this.historicalSummary = summary;
        markDirty();
    }

    // ─── Driver plumbing (milestones + queued spawn actions) ───────────

    /** Whether the named milestone has already fired for this opportunity. */
    public boolean hasFiredMilestone(String milestone) {
        return firedMilestones.contains(milestone);
    }

    /** Mark a milestone as fired. Idempotent. Returns true if newly marked. */
    public boolean markMilestoneFired(String milestone) {
        if (milestone == null || milestone.isBlank()) return false;
        boolean added = firedMilestones.add(milestone);
        if (added) markDirty();
        return added;
    }

    /**
     * Queue a spawn action for later processing.
     * <p>Used by drivers (e.g. {@link SpiritFruitTimeline}) when an entity
     * type doesn't exist yet — the spawn intent is preserved so a future
     * subagent (PA-C) can drain the queue and spawn real entities.
     *
     * @param spawnAction a string encoding the spawn intent
     *                    (e.g. "spirit_insect@120,64,-340")
     */
    public void queueSpawnAction(String spawnAction) {
        if (spawnAction == null || spawnAction.isBlank()) return;
        queuedSpawnActions.add(spawnAction);
        markDirty();
    }

    /** Immutable view of the queued spawn actions. */
    public List<String> getQueuedSpawnActions() {
        return List.copyOf(queuedSpawnActions);
    }

    /** Drain all queued spawn actions (after a future subagent processes them). */
    public void clearQueuedSpawnActions() {
        if (!queuedSpawnActions.isEmpty()) {
            queuedSpawnActions.clear();
            markDirty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  NBT serialization
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Serialize this state to a {@link CompoundTag} for world-save persistence.
     * <p>Round-trips with {@link #deserializeNBT(CompoundTag)}.
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("opportunityId", opportunityId);
        tag.putInt("posX", posX);
        tag.putInt("posY", posY);
        tag.putInt("posZ", posZ);
        tag.putString("lifecycle", lifecycle.name());
        tag.putLong("worldTickCreated", worldTickCreated);
        tag.putLong("worldTickLastUpdated", worldTickLastUpdated);
        tag.putLong("worldTickResolved", worldTickResolved);
        tag.putInt("distortionHops", distortionHops);
        if (resolutionOutcome != null) tag.putString("resolutionOutcome", resolutionOutcome);
        if (historicalSummary != null) tag.putString("historicalSummary", historicalSummary);
        tag.putString("driverType", driverType);
        tag.putLong("driverTickZero", driverTickZero);

        // currentActors as ListTag of StringTags
        ListTag actorsTag = new ListTag();
        for (String actor : currentActors) actorsTag.add(StringTag.valueOf(actor));
        tag.put("currentActors", actorsTag);

        // firedMilestones as ListTag of StringTags
        ListTag milestonesTag = new ListTag();
        for (String m : firedMilestones) milestonesTag.add(StringTag.valueOf(m));
        tag.put("firedMilestones", milestonesTag);

        // queuedSpawnActions as ListTag of StringTags
        ListTag spawnsTag = new ListTag();
        for (String s : queuedSpawnActions) spawnsTag.add(StringTag.valueOf(s));
        tag.put("queuedSpawnActions", spawnsTag);

        return tag;
    }

    /**
     * Deserialize from a {@link CompoundTag} (the inverse of {@link #serializeNBT()}).
     * <p>Returns a NEW {@link OpportunityState} instance. Fields not present
     * in the tag default to their canonical "fresh" values.
     */
    public static OpportunityState deserializeNBT(CompoundTag tag) {
        String id = tag.getString("opportunityId");
        if (id.isEmpty()) {
            throw new IllegalArgumentException("OpportunityState NBT missing opportunityId");
        }
        OpportunityState s = new OpportunityState(id);
        s.posX = tag.getInt("posX");
        s.posY = tag.getInt("posY");
        s.posZ = tag.getInt("posZ");
        try {
            s.lifecycle = OpportunityLifecycle.valueOf(tag.getString("lifecycle"));
        } catch (IllegalArgumentException ex) {
            s.lifecycle = OpportunityLifecycle.DORMANT;
        }
        s.worldTickCreated = tag.getLong("worldTickCreated");
        s.worldTickLastUpdated = tag.getLong("worldTickLastUpdated");
        s.worldTickResolved = tag.getLong("worldTickResolved");
        s.distortionHops = tag.getInt("distortionHops");
        s.resolutionOutcome = tag.contains("resolutionOutcome") ? tag.getString("resolutionOutcome") : null;
        s.historicalSummary = tag.contains("historicalSummary") ? tag.getString("historicalSummary") : null;
        s.driverType = tag.contains("driverType") ? tag.getString("driverType") : "noop";
        if (s.driverType.isEmpty()) s.driverType = "noop";
        s.driverTickZero = tag.getLong("driverTickZero");

        s.currentActors.clear();
        ListTag actors = tag.getList("currentActors", Tag.TAG_STRING);
        for (int i = 0; i < actors.size(); i++) {
            s.currentActors.add(actors.getString(i));
        }

        s.firedMilestones.clear();
        ListTag milestones = tag.getList("firedMilestones", Tag.TAG_STRING);
        for (int i = 0; i < milestones.size(); i++) {
            s.firedMilestones.add(milestones.getString(i));
        }

        s.queuedSpawnActions.clear();
        ListTag spawns = tag.getList("queuedSpawnActions", Tag.TAG_STRING);
        for (int i = 0; i < spawns.size(); i++) {
            s.queuedSpawnActions.add(spawns.getString(i));
        }
        return s;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Internals
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Mark this state as dirty so the owning {@link OpportunityRegistry}
     * knows to persist it. The registry installs a callback via
     * {@link #setDirtyCallback(Runnable)} on load.
     */
    private transient Runnable dirtyCallback = null;

    /** Install a dirty-marking callback (called by the registry on load). */
    public void setDirtyCallback(Runnable cb) { this.dirtyCallback = cb; }

    /** Mark this state as modified — the registry will persist it on next save. */
    private void markDirty() {
        if (dirtyCallback != null) dirtyCallback.run();
    }

    /**
     * Synthesize a default historical summary from the resolution outcome.
     * <p>Drivers can override this with {@link #setHistoricalSummary(String)}
     * for richer narration.
     */
    private String synthesizeHistoricalSummary() {
        if (resolutionOutcome == null) {
            return "An opportunity once existed here. Its fate is lost to history.";
        }
        if (resolutionOutcome.startsWith("claimed_by_")) {
            String actor = resolutionOutcome.substring("claimed_by_".length());
            return "An opportunity here was claimed by " + actor.replace('_', ' ') + ".";
        }
        return switch (resolutionOutcome) {
            case "destroyed" -> "An opportunity here was destroyed in conflict.";
            case "expired"   -> "An opportunity here expired, unclaimed.";
            case "rotted"    -> "An opportunity here rotted, untended.";
            default          -> "An opportunity here concluded: " + resolutionOutcome + ".";
        };
    }

    /** Human-readable debug summary (one line). */
    @Override
    public String toString() {
        return "OpportunityState{" +
            "id=" + opportunityId +
            ", lifecycle=" + lifecycle +
            ", pos=(" + posX + "," + posY + "," + posZ + ")" +
            ", actors=" + currentActors.size() +
            ", hops=" + distortionHops +
            (resolutionOutcome != null ? ", resolved=" + resolutionOutcome : "") +
            (driverType != null && !driverType.equals("noop") ? ", driver=" + driverType : "") +
            '}';
    }
}
