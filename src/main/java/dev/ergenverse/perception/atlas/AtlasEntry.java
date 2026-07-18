package dev.ergenverse.perception.atlas;

import dev.ergenverse.simulation.opportunity.PlayerObserverRealm;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;

/**
 * AtlasEntry — one observed location in the Divine Sense Atlas.
 *
 * <p>Per PROJECT_MASTER.md §5.5, the atlas is a <b>perception record</b>
 * (not a satellite view). It contains ONLY knowledge the player has
 * genuinely acquired — by direct sight, divine sense pulse, rumor, or
 * journal annotation. Each entry lives on exactly one of the four
 * perception layers (Physical / Qi / Restriction / Law); a single
 * coordinate can host multiple entries on different layers.
 *
 * <h2>Layer gating</h2>
 * <p>The four layers unlock strictly with {@link PlayerObserverRealm}:
 * <ul>
 *   <li><b>Layer 1 — Physical</b> (Mortal+): terrain, roads, rivers,
 *       villages, mountains.</li>
 *   <li><b>Layer 2 — Qi</b> (Foundation+): spirit veins, qi density,
 *       herb concentrations, cultivation hotspots.</li>
 *   <li><b>Layer 3 — Restriction</b> (Nascent Soul+): formations,
 *       seals, hidden arrays, spatial anchors.</li>
 *   <li><b>Layer 4 — Law</b> (Ascendant+): karmic nodes, space cracks,
 *       law distortions, ancient battle scars.</li>
 * </ul>
 *
 * <p>The mortal's atlas is NOT "fog of war" — the mortal genuinely
 * does not perceive the Qi layer. The Nascent Soul cultivator's atlas
 * is fundamentally different because they perceive things the mortal
 * literally cannot.
 *
 * <h2>Discovery channels</h2>
 * <p>Each entry remembers <b>how</b> the player learned of it:
 * <ul>
 *   <li>{@code direct_sight} — the player walked past and saw it.</li>
 *   <li>{@code divine_sense} — the player ran a divine sense pulse and
 *       the engine added the entry automatically.</li>
 *   <li>{@code rumor} — the player heard a rumor (§6.11) that pointed
 *       at this location; the entry's note may be distorted.</li>
 *   <li>{@code journal} — the player manually annotated the location
 *       (sneak-right-click empty-handed).</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class AtlasEntry {

    /** Layer 1 — Physical (Mortal+). */
    public static final int LAYER_PHYSICAL    = 1;
    /** Layer 2 — Qi (Foundation+). */
    public static final int LAYER_QI          = 2;
    /** Layer 3 — Restriction (Nascent Soul+). */
    public static final int LAYER_RESTRICTION = 3;
    /** Layer 4 — Law (Ascendant+). */
    public static final int LAYER_LAW         = 4;

    /** Discovery channel: player walked past and saw it. */
    public static final String VIA_DIRECT_SIGHT  = "direct_sight";
    /** Discovery channel: divine sense pulse revealed it. */
    public static final String VIA_DIVINE_SENSE  = "divine_sense";
    /** Discovery channel: heard via the rumor system (§6.11). */
    public static final String VIA_RUMOR         = "rumor";
    /** Discovery channel: manually annotated via sneak-right-click. */
    public static final String VIA_JOURNAL       = "journal";

    /** Canonical categories used by the auto-observer. Future-proofed: arbitrary strings allowed. */
    public static final String CAT_VILLAGE       = "village";
    public static final String CAT_ROAD          = "road";
    public static final String CAT_RIVER         = "river";
    public static final String CAT_MOUNTAIN      = "mountain";
    public static final String CAT_FOREST        = "forest";
    public static final String CAT_SPIRIT_VEIN   = "spirit_vein";
    public static final String CAT_HERB_GROVE    = "herb_grove";
    public static final String CAT_QI_HOTSPOT    = "qi_hotspot";
    public static final String CAT_FORMATION     = "formation";
    public static final String CAT_RESTRICTION   = "restriction";
    public static final String CAT_SEAL          = "seal";
    public static final String CAT_ARRAY         = "array";
    public static final String CAT_SPATIAL_ANCHOR = "spatial_anchor";
    public static final String CAT_KARMIC_NODE   = "karmic_node";
    public static final String CAT_SPACE_CRACK   = "space_crack";
    public static final String CAT_LAW_DISTORTION = "law_distortion";
    public static final String CAT_BATTLE_SCAR   = "battle_scar";
    public static final String CAT_LANDMARK      = "landmark";
    public static final String CAT_CULTIVATOR    = "cultivator";
    public static final String CAT_OTHER         = "other";

    /** BlockPos of the observed location (immutable). */
    public final BlockPos pos;
    /** Player-facing label, e.g. "Wang Family Village" or "Spirit Vein East-3". */
    public final String label;
    /** Category constant (see CAT_* above) — drives icon + color. */
    public final String category;
    /** Perception layer 1-4 (see LAYER_* above). */
    public final int layer;
    /** Player-recorded note (may be empty). */
    public final String observationNote;
    /** World tick at which the player first observed this location. */
    public final long worldTickObserved;
    /** How the player discovered this — see VIA_* constants. */
    public final String discoveredVia;

    public AtlasEntry(BlockPos pos, String label, String category, int layer,
                      String observationNote, long worldTickObserved, String discoveredVia) {
        this.pos = pos.immutable();
        this.label = label == null ? "" : label;
        this.category = category == null ? CAT_OTHER : category;
        this.layer = layer;
        this.observationNote = observationNote == null ? "" : observationNote;
        this.worldTickObserved = worldTickObserved;
        this.discoveredVia = discoveredVia == null ? VIA_DIRECT_SIGHT : discoveredVia;
    }

    /**
     * Can a player at the given perception tier perceive this layer?
     *
     * <p>Strict gating per the PA-5 task spec:
     * <ul>
     *   <li>Layer 1 (Physical) — always visible (Mortal+).</li>
     *   <li>Layer 2 (Qi) — Foundation+.</li>
     *   <li>Layer 3 (Restriction) — Nascent Soul+.</li>
     *   <li>Layer 4 (Law) — Ascendant+.</li>
     * </ul>
     *
     * <p>TODO (future): per §5.5 table, Qi Condensation sees "faint Qi hints",
     * Core Formation sees "faint Restriction glimmers", Soul Formation sees
     * "faint Law echoes". The strict gating is in effect for v1; the faint
     * half-layers are a future enhancement.
     */
    public boolean getLayerForRealm(PlayerObserverRealm tier) {
        if (tier == null) return false;
        switch (layer) {
            case LAYER_PHYSICAL:    return tier.isAtLeast(PlayerObserverRealm.MORTAL);
            case LAYER_QI:          return tier.isAtLeast(PlayerObserverRealm.FOUNDATION);
            case LAYER_RESTRICTION: return tier.isAtLeast(PlayerObserverRealm.NASCENT_SOUL);
            case LAYER_LAW:         return tier.isAtLeast(PlayerObserverRealm.ASCENDANT_PLUS);
            default:                return false;
        }
    }

    /** Human-readable layer name. */
    public static String layerName(int layer) {
        switch (layer) {
            case LAYER_PHYSICAL:    return "Physical";
            case LAYER_QI:          return "Qi";
            case LAYER_RESTRICTION: return "Restriction";
            case LAYER_LAW:         return "Law";
            default:                return "Unknown";
        }
    }

    /** Minimum {@link PlayerObserverRealm} required to perceive the given layer. */
    public static PlayerObserverRealm minimumTierForLayer(int layer) {
        switch (layer) {
            case LAYER_PHYSICAL:    return PlayerObserverRealm.MORTAL;
            case LAYER_QI:          return PlayerObserverRealm.FOUNDATION;
            case LAYER_RESTRICTION: return PlayerObserverRealm.NASCENT_SOUL;
            case LAYER_LAW:         return PlayerObserverRealm.ASCENDANT_PLUS;
            default:                return PlayerObserverRealm.ASCENDANT_PLUS;
        }
    }

    // ─── NBT serialization ──────────────────────────────────────────

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("pos", NbtUtils.writeBlockPos(pos));
        tag.putString("label", label);
        tag.putString("category", category);
        tag.putInt("layer", layer);
        tag.putString("note", observationNote);
        tag.putLong("tick", worldTickObserved);
        tag.putString("via", discoveredVia);
        return tag;
    }

    public static AtlasEntry deserializeNBT(CompoundTag tag) {
        BlockPos pos = tag.contains("pos") ? NbtUtils.readBlockPos(tag.getCompound("pos")) : BlockPos.ZERO;
        String label = tag.getString("label");
        String category = tag.getString("category");
        int layer = tag.getInt("layer");
        String note = tag.getString("note");
        long tick = tag.getLong("tick");
        String via = tag.getString("via");
        return new AtlasEntry(pos, label, category, layer, note, tick, via);
    }

    // ─── Network serialization (for S2C sync) ───────────────────────

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(label, 256);
        buf.writeUtf(category, 64);
        buf.writeByte(layer);
        buf.writeUtf(observationNote, 1024);
        buf.writeVarLong(worldTickObserved);
        buf.writeUtf(discoveredVia, 32);
    }

    public static AtlasEntry read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String label = buf.readUtf(256);
        String category = buf.readUtf(64);
        int layer = buf.readByte();
        String note = buf.readUtf(1024);
        long tick = buf.readVarLong();
        String via = buf.readUtf(32);
        return new AtlasEntry(pos, label, category, layer, note, tick, via);
    }

    @Override
    public String toString() {
        return "AtlasEntry{" + layerName(layer) + "/" + category
                + " @ " + pos + " '" + label + "' via=" + discoveredVia + "}";
    }
}
