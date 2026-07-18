package dev.ergenverse.npc.rumor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Rumor — a single piece of information propagating through the world.
 *
 * <p>Per PROJECT_MASTER.md 6.11, rumors are the "missing glue" between
 * the Opportunity Engine (world simulation) and the Expectation Model
 * (NPC prediction). Opportunities generate signals, rumors carry those
 * signals with progressive distortion per hop, and NPCs form predictions
 * from the distorted signals.
 *
 * <h2>Distortion chain (worked example from 6.11)</h2>
 * <ul>
 *   <li>Hop 0 (source): "A spirit fruit ripened in Mosquito Valley"</li>
 *   <li>Hop 1: "Strange lights in Mosquito Valley."</li>
 *   <li>Hop 2: "A treasure was born in the eastern canyons."</li>
 *   <li>Hop 3: "An immortal inheritance has appeared somewhere near Zhao."</li>
 *   <li>Hop 4: "Wang Lin has returned." (totally fabricated but believed)</li>
 * </ul>
 *
 * <h2>Persistence</h2>
 * <p>Rumors are stored in {@link RumorNetwork} which uses SavedData.
 * Each rumor has a unique ID for tracking across server restarts.
 *
 * <h2>Actor classes</h2>
 * <p>Each hop records which actor class carried the rumor (6.11 table).
 * Different actor classes propagate at different speeds and distort differently.
 *
 * @see RumorDistortion  per-hop content morphing
 * @see RumorNetwork     the propagation engine + persistence
 */
public final class Rumor {

    // ─── NBT keys ────────────────────────────────────────────────
    static final String TAG_ID = "id";
    static final String TAG_SOURCE = "src";
    static final String TAG_CONTENT = "c";
    static final String TAG_ORIGINAL = "o";
    static final String TAG_HOPS = "h";
    static final String TAG_BORN = "born";
    static final String TAG_LAST_PROP = "lp";
    static final String TAG_VERACITY = "v";
    static final String TAG_LOCATION = "loc";
    static final String TAG_ORIGIN_TYPE = "ot";
    static final String TAG_DEAD = "dead";
    static final String TAG_CHAIN = "chain";
    static final String TAG_CHAIN_ENTRY = "ce";
    static final String TAG_CHAIN_CONTENT = "cc";
    static final String TAG_CHAIN_ACTOR = "ca";
    static final String TAG_CHAIN_HOP = "ch";
    static final String TAG_CHAIN_VERACITY = "cv";

    /** Maximum hops before a rumor is considered fully fabricated. */
    public static final int MAX_HOPS = 6;

    /** Veracity below this threshold = "completely fabricated." */
    public static final double FABRICATION_THRESHOLD = 0.15;

    /** Veracity decay per hop (base — RumorDistortion can modify). */
    public static final double BASE_VERACITY_DECAY = 0.20;

    /**
     * The origin type — what kind of event spawned this rumor.
     * Determines initial distortion patterns and propagation speed.
     */
    public enum OriginType {
        /** Spirit fruit ripened, herb matured, vein awakened. */
        SPIRIT_EVENT,
        /** Combat, death, breakthrough, tribulation. */
        COMBAT_EVENT,
        /** NPC/sect movement, political change. */
        FACTION_EVENT,
        /** Player action witnessed by NPC. */
        PLAYER_ACTION,
        /** Environmental: weather anomaly, beast migration. */
        ENVIRONMENTAL,
        /** World-law event: sealed realm cracking, dimension instability. */
        WORLD_LAW,
        /** Deliberate disinformation spread by an NPC. */
        DISINFORMATION,
        /** Generic fallback. */
        OTHER
    }

    /**
     * Actor class — who is carrying the rumor this hop (from 6.11 table).
     * Determines propagation speed and distortion characteristics.
     */
    public enum ActorClass {
        /** Villagers, farmers, mortals — overhear, witness directly. Hours. */
        MORTAL,
        /** Traveling merchants — road intelligence, trade-route info. Days. */
        MERCHANT,
        /** Sect patrol members — spiritual perception, scout reports. Hours-days. */
        SECT_SCOUT,
        /** Sect elders — filtered intelligence, faction analysis. Days-weeks. */
        SECT_ELDER,
        /** Wandering cultivators — inn hearsay, fragmentary divinations. Days. */
        WANDERING_CULTIVATOR,
        /** Player characters — hear distorted versions of all above. Variable. */
        PLAYER
    }

    // ─── Immutable rumor data ────────────────────────────────────
    private final String rumorId;
    private final String sourceNpcId;
    private final String currentContent;
    private final String originalContent;
    private final int hopCount;
    private final long bornTick;
    private final long lastPropagatedTick;
    private final double veracity;       // 1.0 = truth, 0.0 = fabrication
    private final String locationHint;   // vague location reference (distorts with hops)
    private final OriginType originType;
    private final boolean dead;          // true = expired or disproven
    private final List<ChainEntry> chain; // the full distortion chain

    private Rumor(Builder b) {
        this.rumorId = b.rumorId;
        this.sourceNpcId = b.sourceNpcId;
        this.currentContent = b.currentContent;
        this.originalContent = b.originalContent;
        this.hopCount = b.hopCount;
        this.bornTick = b.bornTick;
        this.lastPropagatedTick = b.lastPropagatedTick;
        this.veracity = b.veracity;
        this.locationHint = b.locationHint;
        this.originType = b.originType;
        this.dead = b.dead;
        this.chain = Collections.unmodifiableList(new ArrayList<>(b.chain));
    }

    // ─── Accessors ───────────────────────────────────────────────

    public String getRumorId() { return rumorId; }
    public String getSourceNpcId() { return sourceNpcId; }
    public String getCurrentContent() { return currentContent; }
    public String getOriginalContent() { return originalContent; }
    public int getHopCount() { return hopCount; }
    public long getBornTick() { return bornTick; }
    public long getLastPropagatedTick() { return lastPropagatedTick; }
    public double getVeracity() { return veracity; }
    public String getLocationHint() { return locationHint; }
    public OriginType getOriginType() { return originType; }
    public boolean isDead() { return dead; }
    public List<ChainEntry> getChain() { return chain; }

    /** Whether this rumor is fully fabricated (veracity below threshold). */
    public boolean isFabricated() {
        return veracity < FABRICATION_THRESHOLD;
    }

    /** Whether this rumor can propagate further (not dead, not max hops). */
    public boolean canPropagate() {
        return !dead && hopCount < MAX_HOPS && veracity > 0.01;
    }

    // ─── Mutation (returns new instance — immutable) ─────────────

    /**
     * Create a new Rumor with one additional hop applied.
     * The new content and veracity are computed by RumorDistortion.
     */
    public Rumor withHop(String newContent, double newVeracity,
                         ActorClass actor, String newLocationHint, long tick) {
        return new Builder(this)
                .currentContent(newContent)
                .veracity(newVeracity)
                .hopCount(hopCount + 1)
                .lastPropagatedTick(tick)
                .locationHint(newLocationHint)
                .chainEntry(new ChainEntry(newContent, actor, hopCount + 1, newVeracity))
                .build();
    }

    /** Mark this rumor as dead (expired or disproven). */
    public Rumor markDead() {
        return new Builder(this).dead(true).build();
    }

    // ─── Serialization ───────────────────────────────────────────

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_ID, rumorId);
        tag.putString(TAG_SOURCE, sourceNpcId);
        tag.putString(TAG_CONTENT, currentContent);
        tag.putString(TAG_ORIGINAL, originalContent);
        tag.putInt(TAG_HOPS, hopCount);
        tag.putLong(TAG_BORN, bornTick);
        tag.putLong(TAG_LAST_PROP, lastPropagatedTick);
        tag.putDouble(TAG_VERACITY, veracity);
        tag.putString(TAG_LOCATION, locationHint);
        tag.putString(TAG_ORIGIN_TYPE, originType.name());
        tag.putBoolean(TAG_DEAD, dead);

        ListTag chainList = new ListTag();
        for (ChainEntry ce : chain) {
            CompoundTag ceTag = new CompoundTag();
            ceTag.putString(TAG_CHAIN_CONTENT, ce.content);
            ceTag.putString(TAG_CHAIN_ACTOR, ce.actor.name());
            ceTag.putInt(TAG_CHAIN_HOP, ce.hop);
            ceTag.putDouble(TAG_CHAIN_VERACITY, ce.veracity);
            chainList.add(ceTag);
        }
        tag.put(TAG_CHAIN, chainList);
        return tag;
    }

    public static Rumor load(CompoundTag tag) {
        Builder b = new Builder()
                .rumorId(tag.getString(TAG_ID))
                .sourceNpcId(tag.getString(TAG_SOURCE))
                .currentContent(tag.getString(TAG_CONTENT))
                .originalContent(tag.getString(TAG_ORIGINAL))
                .hopCount(tag.getInt(TAG_HOPS))
                .bornTick(tag.getLong(TAG_BORN))
                .lastPropagatedTick(tag.getLong(TAG_LAST_PROP))
                .veracity(tag.getDouble(TAG_VERACITY))
                .locationHint(tag.getString(TAG_LOCATION))
                .originType(OriginType.valueOf(tag.getString(TAG_ORIGIN_TYPE)))
                .dead(tag.getBoolean(TAG_DEAD));

        if (tag.contains(TAG_CHAIN, Tag.TAG_LIST)) {
            ListTag chainList = tag.getList(TAG_CHAIN, Tag.TAG_COMPOUND);
            for (int i = 0; i < chainList.size(); i++) {
                CompoundTag ceTag = chainList.getCompound(i);
                b.chainEntry(new ChainEntry(
                        ceTag.getString(TAG_CHAIN_CONTENT),
                        ActorClass.valueOf(ceTag.getString(TAG_CHAIN_ACTOR)),
                        ceTag.getInt(TAG_CHAIN_HOP),
                        ceTag.getDouble(TAG_CHAIN_VERACITY)
                ));
            }
        }
        return b.build();
    }

    // ─── Chain entry (one hop in the distortion chain) ───────────

    /**
     * A single entry in the rumor's distortion chain.
     * Records what the rumor said, who carried it, at which hop, and how true it was.
     */
    public static final class ChainEntry {
        public final String content;
        public final ActorClass actor;
        public final int hop;
        public final double veracity;

        public ChainEntry(String content, ActorClass actor, int hop, double veracity) {
            this.content = content;
            this.actor = actor;
            this.hop = hop;
            this.veracity = veracity;
        }
    }

    // ─── Builder ─────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String rumorId;
        private String sourceNpcId = "";
        private String currentContent = "";
        private String originalContent = "";
        private int hopCount = 0;
        private long bornTick = 0;
        private long lastPropagatedTick = 0;
        private double veracity = 1.0;
        private String locationHint = "";
        private OriginType originType = OriginType.OTHER;
        private boolean dead = false;
        private final List<ChainEntry> chain = new ArrayList<>();

        public Builder() {}

        /** Copy from existing rumor (for mutation). */
        public Builder(Rumor r) {
            this.rumorId = r.rumorId;
            this.sourceNpcId = r.sourceNpcId;
            this.currentContent = r.currentContent;
            this.originalContent = r.originalContent;
            this.hopCount = r.hopCount;
            this.bornTick = r.bornTick;
            this.lastPropagatedTick = r.lastPropagatedTick;
            this.veracity = r.veracity;
            this.locationHint = r.locationHint;
            this.originType = r.originType;
            this.dead = r.dead;
            this.chain.addAll(r.chain);
        }

        public Builder rumorId(String v) { this.rumorId = v; return this; }
        public Builder sourceNpcId(String v) { this.sourceNpcId = v; return this; }
        public Builder currentContent(String v) { this.currentContent = v; return this; }
        public Builder originalContent(String v) { this.originalContent = v; return this; }
        public Builder hopCount(int v) { this.hopCount = v; return this; }
        public Builder bornTick(long v) { this.bornTick = v; return this; }
        public Builder lastPropagatedTick(long v) { this.lastPropagatedTick = v; return this; }
        public Builder veracity(double v) { this.veracity = v; return this; }
        public Builder locationHint(String v) { this.locationHint = v; return this; }
        public Builder originType(OriginType v) { this.originType = v; return this; }
        public Builder dead(boolean v) { this.dead = v; return this; }
        public Builder chainEntry(ChainEntry v) { this.chain.add(v); return this; }

        public Rumor build() {
            if (rumorId == null || rumorId.isBlank()) {
                rumorId = UUID.randomUUID().toString().substring(0, 8);
            }
            return new Rumor(this);
        }
    }

    @Override
    public String toString() {
        return "Rumor{" + rumorId + ", hops=" + hopCount
                + ", veracity=" + String.format("%.2f", veracity)
                + ", content='" + currentContent + "'"
                + (dead ? " [DEAD]" : "") + "}";
    }
}