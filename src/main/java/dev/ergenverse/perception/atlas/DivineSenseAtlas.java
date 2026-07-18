package dev.ergenverse.perception.atlas;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.simulation.opportunity.PlayerObserverRealm;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DivineSenseAtlas — the per-player perception record holder.
 *
 * <p>Per PROJECT_MASTER.md §5.5, the atlas is a <b>knowledge artifact</b>
 * that evolves with the player's cultivation realm. It is NOT fog-of-war;
 * it is a record of what the player has genuinely perceived. The same
 * coordinate can host multiple entries across the four perception layers,
 * and the player's realm determines which layers render in the M-key UI.
 *
 * <h2>Storage</h2>
 * <p>The atlas is attached to the player via the {@link AtlasCapability}
 * Forge capability (see that class for the provider + attach handler).
 * Persistence is automatic via {@link net.minecraftforge.common.capabilities.ICapabilitySerializable}.
 *
 * <h2>Two stores</h2>
 * <ul>
 *   <li>{@code discoveredEntries} — positional knowledge: "I observed a
 *       spirit vein at this BlockPos on tick 1234 via divine sense."
 *       Keyed by BlockPos so re-observing the same position updates
 *       rather than duplicates.</li>
 *   <li>{@code knownRumors} — rumor IDs the player has heard (per §6.11).
 *       A rumor may have a position attached (recorded as an entry on
 *       the appropriate layer with {@code discoveredVia="rumor"}); the
 *       rumor ID itself is tracked here for the rumor-propagation system
 *       that a future subagent will build.</li>
 * </ul>
 *
 * <p>{@code currentTier} is derived on demand from the player's
 * {@link RealmId} via {@link #getCurrentTier(ServerPlayer)} — it is NOT
 * persisted, because it can be re-derived from the CultivationState.
 *
 * <h2>Thread safety</h2>
 * <p>All mutation happens server-side, on the main server thread. The
 * client receives an immutable snapshot via {@code AtlasSyncS2CPacket}
 * (see {@link AtlasNetworkPackets}) and never mutates this class.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class DivineSenseAtlas {

    /** Maximum entries per player. Caps memory; reasonable for any playthrough. */
    public static final int MAX_ENTRIES = 4096;

    /** Positional knowledge. Keyed by BlockPos; re-observation updates. */
    private final Map<BlockPos, AtlasEntry> discoveredEntries = new LinkedHashMap<>();

    /** Rumor IDs the player has heard (per §6.11). Strings, externally-defined. */
    private final Set<String> knownRumors = new HashSet<>();

    /**
     * Add (or update) an observation at the given position.
     *
     * <p>If the position is already known on the same layer, the entry is
     * replaced (the latest observation wins — the player's understanding
     * updated). If the position is known on a different layer, both
     * entries coexist (a coordinate can have entries on all four layers).
     *
     * <p>Keyed by BlockPos means same-layer re-observation updates
     * rather than appends. To preserve layer coexistence, the actual
     * key is layered: see {@link #layerKey(BlockPos, int)}.
     */
    public void observe(ServerPlayer player, BlockPos pos, AtlasEntry entry) {
        if (pos == null || entry == null) return;
        if (discoveredEntries.size() >= MAX_ENTRIES
                && !discoveredEntries.containsKey(layerKey(pos, entry.layer))) {
            Ergenverse.LOGGER.warn("[Ergenverse] Divine Sense Atlas full for player {} (cap {}); discarding new entry at {}",
                    player.getName().getString(), MAX_ENTRIES, pos);
            return;
        }
        discoveredEntries.put(layerKey(pos, entry.layer), entry);
    }

    /** Convenience: add an entry directly without a player reference (for tests / packets). */
    public void putEntry(AtlasEntry entry) {
        if (entry == null) return;
        if (discoveredEntries.size() >= MAX_ENTRIES
                && !discoveredEntries.containsKey(layerKey(entry.pos, entry.layer))) {
            Ergenverse.LOGGER.warn("[Ergenverse] Divine Sense Atlas full (cap {}); discarding entry at {}",
                    MAX_ENTRIES, entry.pos);
            return;
        }
        discoveredEntries.put(layerKey(entry.pos, entry.layer), entry);
    }

    /** Replace the entire entry set. Used by client-side packet handler. */
    public void setEntries(List<AtlasEntry> entries) {
        discoveredEntries.clear();
        if (entries == null) return;
        for (AtlasEntry e : entries) {
            if (e == null) continue;
            if (discoveredEntries.size() >= MAX_ENTRIES) break;
            discoveredEntries.put(layerKey(e.pos, e.layer), e);
        }
    }

    /** Mark a rumor ID as heard. Returns true if newly added. */
    public boolean addRumor(String rumorId) {
        if (rumorId == null || rumorId.isEmpty()) return false;
        return knownRumors.add(rumorId);
    }

    /** Has the player heard this rumor ID? */
    public boolean knowsRumor(String rumorId) {
        return rumorId != null && knownRumors.contains(rumorId);
    }

    /** Immutable view of all entries (regardless of layer). */
    public List<AtlasEntry> allEntries() {
        return Collections.unmodifiableList(new ArrayList<>(discoveredEntries.values()));
    }

    /** Immutable view of rumor IDs. */
    public Set<String> rumorIds() {
        return Collections.unmodifiableSet(new HashSet<>(knownRumors));
    }

    /**
     * Return only the entries the player at the given tier can perceive.
     *
     * <p>This is the canonical filter used by both the M-key UI and the
     * S2C sync packet builder. An entry on the Qi layer is filtered out
     * for a mortal — the mortal genuinely cannot perceive it.
     */
    public List<AtlasEntry> getEntriesForTier(PlayerObserverRealm tier) {
        if (tier == null) return Collections.emptyList();
        List<AtlasEntry> out = new ArrayList<>();
        for (AtlasEntry e : discoveredEntries.values()) {
            if (e.getLayerForRealm(tier)) out.add(e);
        }
        return out;
    }

    /** Number of entries currently stored (across all layers). */
    public int size() {
        return discoveredEntries.size();
    }

    /**
     * Derive the player's current perception tier from their CultivationState.
     *
     * <p>This is the integration point between the canonical {@link RealmId}
     * ladder (17 stages) and the simulation-layer {@link PlayerObserverRealm}
     * ladder (7 perception bands). Per the PA-3 worklog: "The mapping from
     * canonical RealmId to PlayerObserverRealm is the integration layer's
     * job." This method IS that integration layer for the atlas.
     */
    public PlayerObserverRealm getCurrentTier(ServerPlayer player) {
        if (player == null) return PlayerObserverRealm.MORTAL;
        var stateOpt = CultivationCapability.get(player);
        if (!stateOpt.isPresent()) {
            Ergenverse.LOGGER.warn("[Ergenverse] Divine Sense Atlas: no CultivationState on player {} — defaulting to MORTAL tier.",
                    player.getName().getString());
            return PlayerObserverRealm.MORTAL;
        }
        CultivationState state = stateOpt.resolve().get();
        return fromRealmId(state.getCurrentRealm());
    }

    /**
     * Map a canonical {@link RealmId} to a {@link PlayerObserverRealm}.
     *
     * <p>Public static so other integration code (Divine Sense Atlas UI,
     * future Opportunity Engine bridge, etc.) can reuse the same mapping.
     *
     * <p>Mapping:
     * <ul>
     *   <li>MORTAL → MORTAL</li>
     *   <li>QI_CONDENSATION → QI_CONDENSATION</li>
     *   <li>FOUNDATION → FOUNDATION</li>
     *   <li>CORE_FORMATION → CORE_FORMATION</li>
     *   <li>NASCENT_SOUL → NASCENT_SOUL</li>
     *   <li>SOUL_FORMATION, SOUL_TRANSFORMATION → SOUL_FORMATION</li>
     *   <li>ASCENDANT and everything above → ASCENDANT_PLUS</li>
     * </ul>
     */
    public static PlayerObserverRealm fromRealmId(RealmId realm) {
        if (realm == null) return PlayerObserverRealm.MORTAL;
        switch (realm) {
            case MORTAL:              return PlayerObserverRealm.MORTAL;
            case QI_CONDENSATION:     return PlayerObserverRealm.QI_CONDENSATION;
            case FOUNDATION:          return PlayerObserverRealm.FOUNDATION;
            case CORE_FORMATION:      return PlayerObserverRealm.CORE_FORMATION;
            case NASCENT_SOUL:        return PlayerObserverRealm.NASCENT_SOUL;
            case SOUL_FORMATION:
            case SOUL_TRANSFORMATION: return PlayerObserverRealm.SOUL_FORMATION;
            case ASCENDANT:
            case ILLUSORY_YIN:
            case CORPOREAL_YANG:
            case NIRVANA_SCRYER:
            case NIRVANA_CLEANSER:
            case NIRVANA_FRUIT:
            case SPIRIT_SEIZER:
            case TRUE_IMMORTAL:
            case ANCIENT:
            case PARAGON:
            case TRANSCENDENCE:
            default:                  return PlayerObserverRealm.ASCENDANT_PLUS;
        }
    }

    // ─── NBT persistence ────────────────────────────────────────────

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (AtlasEntry e : discoveredEntries.values()) {
            list.add(e.serializeNBT());
        }
        tag.put("entries", list);

        ListTag rumors = new ListTag();
        for (String r : knownRumors) rumors.add(net.minecraft.nbt.StringTag.valueOf(r));
        tag.put("rumors", rumors);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        discoveredEntries.clear();
        knownRumors.clear();
        if (tag == null) return;
        if (tag.contains("entries", Tag.TAG_LIST)) {
            ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                AtlasEntry e = AtlasEntry.deserializeNBT(list.getCompound(i));
                if (e != null) {
                    discoveredEntries.put(layerKey(e.pos, e.layer), e);
                }
            }
        }
        if (tag.contains("rumors", Tag.TAG_LIST)) {
            ListTag rumors = tag.getList("rumors", Tag.TAG_STRING);
            for (int i = 0; i < rumors.size(); i++) {
                knownRumors.add(rumors.getString(i));
            }
        }
    }

    /** Composite key: BlockPos + layer. Lets a coordinate host entries on multiple layers. */
    private static LayeredPosKey keyOf(BlockPos pos, int layer) {
        return new LayeredPosKey(pos, layer);
    }

    /**
     * Build a key that encodes both position and layer.
     *
     * <p>We use a derived BlockPos offset: layer 1 = original Y, layer 2 = Y+10_000_000,
     * layer 3 = Y+20_000_000, layer 4 = Y+30_000_000. The Y offset is well beyond
     * Minecraft's build limit (Y=-64..319), so collisions with real BlockPos are
     * impossible. This lets us reuse the LinkedHashMap&lt;BlockPos, AtlasEntry&gt;
     * without a custom key class (which would require Comparable/hashCode overrides).
     */
    private static BlockPos layerKey(BlockPos pos, int layer) {
        if (layer <= 1) return pos;
        return pos.above(10_000_000 * (layer - 1));
    }

    /** Decode a layered key back to its layer number (1-4). Used by tests only. */
    static int layerOfKey(BlockPos key) {
        int y = key.getY();
        if (y >= 30_000_000 - 64) return 4;
        if (y >= 20_000_000 - 64) return 3;
        if (y >= 10_000_000 - 64) return 2;
        return 1;
    }

    /** Internal placeholder kept for documentation; not used by the LinkedHashMap directly. */
    private static final class LayeredPosKey {
        final BlockPos pos;
        final int layer;
        LayeredPosKey(BlockPos pos, int layer) { this.pos = pos; this.layer = layer; }
    }
}
