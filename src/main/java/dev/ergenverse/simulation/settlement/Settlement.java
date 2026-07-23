package dev.ergenverse.simulation.settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Settlement — the simulation-owned object for an inhabited place.
 *
 * <p>Per Article XLIV (the user's directive): a Settlement owns:
 * <pre>
 *   Name, Canonical Location, Buildings, Residences, Road Graph,
 *   Population, Visitors, Economy, Ecology, Spirit Veins,
 *   History, Relationships, Events
 * </pre>
 *
 * <p>A Settlement is <b>NOT a Minecraft structure</b>. It is a simulation
 * object. Minecraft renders it (via builders like {@code WangFamilyVillageBuilder});
 * the simulation owns it. The player arriving does not cause the settlement to
 * exist — it already existed. The player merely intersects it.
 *
 * <h2>The inversion (Article XLIV)</h2>
 * <p>The prior architecture was implicitly Minecraft:
 * <pre>
 *   chunk loads → spawn NPCs → NPC exists
 * </pre>
 * This is backwards. The correct flow is:
 * <pre>
 *   Settlement owns population → actors have presence →
 *   renderer materializes those intersecting loaded chunks →
 *   Minecraft renders them
 * </pre>
 * Wang Lin never "spawns." He already existed before the player loaded. He was
 * already cultivating, thinking, planning, somewhere. Minecraft is simply
 * catching up to reality. The Settlement is the container that makes this
 * true — it holds the population and their residences independent of any
 * Minecraft entity.
 *
 * <h2>Shared locations</h2>
 * <p>Beyond residences (which are owned by actors), a settlement has
 * <b>shared presence locations</b> — the plaza, the market, the herb garden,
 * the meditation rock, the farms. These are communal; no single actor owns
 * them. The {@link ActorPresence} engine combines an actor's home (from their
 * residence) with the settlement's shared locations to derive the actor's
 * current position.
 *
 * <h2>Provenance and the Canon/Simulation NPC distinction (Article XLIV §3)</h2>
 * <p>A settlement's population is a mix of:
 * <ul>
 *   <li><b>Canon NPCs</b> — Wang Lin, Wang Tianshui, Situ Nan. Immutable.
 *       Sourced from the novels. Their existence is non-negotiable.</li>
 *   <li><b>Simulation NPCs</b> — Farmer Chen, Disciple Zhao, Merchant Xu.
 *       Generated to fill the spaces between canon. They must never
 *       contradict canon. They make the world feel inhabited beyond the
 *       named characters.</li>
 * </ul>
 * Both live in the same Settlement population set. The distinction is
 * carried on the {@code dev.ergenverse.simulation.actor.Actor} provenance
 * field ("canon:..." vs "simulation:...").
 */
public final class Settlement {

    /** Stable identifier (e.g. "wang_family_village"). */
    public final String id;

    /** Canonical name (e.g. "Wang Jia Cun / 王家村"). */
    public final String canonName;

    /** Settlement class: "village", "sect", "city", "capital". */
    public final String settlementClass;

    /** Canonical center X (absolute world coordinate). */
    public final int centerX;

    /** Canonical center Z (absolute world coordinate). */
    public final int centerZ;

    /** Residences keyed by id. Actors own these; buildings do not own actors. */
    private final Map<String, Residence> residences = new LinkedHashMap<>();

    /** Shared presence locations (plaza, market, herb garden, ...). */
    private final List<PresenceLocation> sharedLocations = new ArrayList<>();

    /** Resident actor ids (the population). Canon + Simulation NPCs. */
    private final Set<String> population = new LinkedHashSet<>();

    /** Transient visitor actor ids (merchants, travelers, recruiters). */
    private final Set<String> visitors = new LinkedHashSet<>();

    /** Spirit vein ids that this settlement depends on. */
    private final List<String> spiritVeinIds = new ArrayList<>();

    /** Append-only event history (Layer 3). */
    private final List<SettlementEvent> history = new ArrayList<>();

    // ── Stubs — filled by future cycles (economy, ecology, road graph) ──
    /** Primary economy descriptor (e.g. "subsistence_farming"). */
    public String primaryEconomy = "subsistence";
    /** Governance form (e.g. "village_elder_council"). */
    public String governance = "council";
    /** Decline/ascent trend (e.g. "slow_decline", "thriving"). */
    public String trend = "stable";

    /**
     * The settlement's collective personality (mood, identity, fears,
     * prosperity, security, cultivation level, recent memory).
     *
     * <p>Per the user's directive: "Settlements should have personalities. Not
     * just NPCs." This shapes every embedded actor's reasoning — a fearful
     * village lowers everyone's effective courage; a competitive sect raises
     * it. Read by {@link ActorReasoningEngine} via
     * {@link ActorProfile#effectiveCourage(SettlementPersonality)}.
     */
    public SettlementPersonality personality;

    public Settlement(String id, String canonName, String settlementClass,
                      int centerX, int centerZ) {
        this.id = id;
        this.canonName = canonName;
        this.settlementClass = settlementClass;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    // ── Residences ─────────────────────────────────────────────────────

    public void addResidence(Residence r) { residences.put(r.id, r); }

    public Residence getResidence(String id) { return residences.get(id); }

    public Collection<Residence> getResidences() {
        return Collections.unmodifiableCollection(residences.values());
    }

    /**
     * Find the residence an actor calls home (owner or resident).
     * @return the residence, or null if the actor has no home here.
     */
    public Residence residenceFor(String actorId) {
        for (Residence r : residences.values()) {
            if (actorId.equals(r.getOwner())) return r;
            if (r.getResidents().contains(actorId)) return r;
        }
        return null;
    }

    // ── Shared locations ───────────────────────────────────────────────

    public void addSharedLocation(PresenceLocation loc) { sharedLocations.add(loc); }

    public List<PresenceLocation> getSharedLocations() {
        return Collections.unmodifiableList(sharedLocations);
    }

    // ── Population / visitors ──────────────────────────────────────────

    public void registerPopulation(String actorId) { population.add(actorId); }

    public void registerPopulationAll(Collection<String> actorIds) { population.addAll(actorIds); }

    public void addVisitor(String actorId) { visitors.add(actorId); }

    public void removeVisitor(String actorId) { visitors.remove(actorId); }

    public Set<String> getPopulation() { return Collections.unmodifiableSet(population); }

    public Set<String> getVisitors() { return Collections.unmodifiableSet(visitors); }

    // ── Spirit veins ───────────────────────────────────────────────────

    public void addSpiritVein(String veinId) { spiritVeinIds.add(veinId); }

    public List<String> getSpiritVeinIds() { return Collections.unmodifiableList(spiritVeinIds); }

    // ── History ────────────────────────────────────────────────────────

    public void recordEvent(long tick, String type, String description) {
        history.add(new SettlementEvent(tick, type, description));
    }

    public List<SettlementEvent> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /** An immutable settlement event record. */
    public record SettlementEvent(long tick, String type, String description) {}
}
