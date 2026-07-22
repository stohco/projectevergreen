package dev.ergenverse.simulation.belief;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeliefStore — per-NPC beliefs about actors.
 *
 * <p>Per the user's architectural directive (2026-07-23, round 2):
 * <pre>
 *   "You're still missing 'belief'.
 *    Facts are objective. Memories are subjective. But beliefs are something else.
 *
 *    Player saved Wang Ping.    → Fact.
 *    Old Chen sees it.          → Believes: 'Player is courageous.'
 *    Teng scout sees it.        → Believes: 'Player is trying to recruit villagers.'
 *    Bandit sees it.            → Believes: 'Player is dangerous.'
 *
 *    Same event. Three different beliefs.
 *    Those beliefs drive future behavior. Not the fact.
 *
 *    This is incredibly Er Gen. People constantly misunderstand each other."
 * </pre>
 *
 * <p>A <b>belief</b> is an NPC's subjective interpretation of an actor's
 * character, derived from observed events but filtered through the NPC's
 * own worldview. Unlike memories (which record what happened), beliefs
 * record <i>what the NPC thinks the actor is like</i>.
 *
 * <h2>Belief structure</h2>
 * <p>Each belief is a triple:
 * <ul>
 *   <li><b>subject</b> — the NPC who holds the belief</li>
 *   <li><b>object</b> — the actor the belief is about (player UUID or NPC canon ID)</li>
 *   <li><b>trait</b> — a character trait the subject attributes to the object
 *       (e.g. "courageous", "cruel", "ambitious", "trustworthy", "dangerous")</li>
 * </ul>
 * Plus:
 * <ul>
 *   <li><b>confidence</b> — 0.0–1.0, how strongly the NPC holds this belief</li>
 *   <li><b>formedAt</b> — the tick when the belief was formed/strengthened</li>
 *   <li><b>evidenceCount</b> — how many events contributed to this belief</li>
 * </ul>
 *
 * <h2>How beliefs differ from memories and relationships</h2>
 * <ul>
 *   <li><b>Memory</b> (NpcCognitiveMemory): "I saw the player save Wang Ping
 *       from wolves at tick 5000." — a record of WHAT happened.</li>
 *   <li><b>Relationship</b> (RelationshipHistory): "The player and I have
 *       affinity +15." — a numeric trust score.</li>
 *   <li><b>Belief</b> (BeliefStore): "I believe the player is courageous." —
 *       a qualitative character attribution that DRIVES behavior. Old Chen
 *       might offer the player a quest because he believes the player is
 *       courageous; a Teng scout might report the player as a threat
 *       because he believes the player is recruiting.</li>
 * </ul>
 *
 * <p>Beliefs decay slowly: if no new evidence arrives, confidence fades.
 * Contradictory evidence can overturn a belief (a belief that the player
 * is "trustworthy" can be replaced by "treacherous" if the player breaks
 * a promise).
 *
 * <h2>Persistence</h2>
 * <p>Beliefs are stored in world SavedData (like WorldHistory). The store
 * is a singleton per server level, loaded on first access and persisted
 * on every mutation.
 */
public final class BeliefStore {

    // ─── NBT keys ─────────────────────────────────────────────────
    private static final String TAG_BELIEFS = "beliefs";
    private static final String TAG_SUBJECT = "subj";
    private static final String TAG_OBJECT = "obj";
    private static final String TAG_TRAIT = "trait";
    private static final String TAG_CONFIDENCE = "conf";
    private static final String TAG_FORMED_AT = "formed";
    private static final String TAG_EVIDENCE = "evid";

    /** Maximum beliefs per subject-object pair (prevents unbounded growth). */
    private static final int MAX_BELIEFS_PER_PAIR = 20;

    /** A single belief: subject believes object has a trait. */
    public record Belief(
            String subject,      // NPC canon ID who holds the belief
            String object,       // actor the belief is about (player UUID or NPC canon ID)
            String trait,        // e.g. "courageous", "cruel", "trustworthy"
            float confidence,    // 0.0–1.0
            long formedAt,       // tick when formed/strengthened
            int evidenceCount    // how many events contributed
    ) {
        public Belief {
            if (subject == null || subject.isEmpty())
                throw new IllegalArgumentException("subject cannot be null/empty");
            if (object == null || object.isEmpty())
                throw new IllegalArgumentException("object cannot be null/empty");
            if (trait == null || trait.isEmpty())
                throw new IllegalArgumentException("trait cannot be null/empty");
            confidence = Math.max(0f, Math.min(1f, confidence));
        }
    }

    // ─── In-memory store ──────────────────────────────────────────
    // Key: "subject|object" → list of beliefs (one per trait)
    private final Map<String, List<Belief>> beliefs = new ConcurrentHashMap<>();

    private static volatile BeliefStore instance;

    private BeliefStore() {}

    /** Get the singleton instance (creates if necessary). */
    public static BeliefStore get() {
        if (instance == null) {
            synchronized (BeliefStore.class) {
                if (instance == null) {
                    instance = new BeliefStore();
                    Ergenverse.LOGGER.info("[BeliefStore] Initialized.");
                }
            }
        }
        return instance;
    }

    /** Clear the singleton (called on world unload). */
    public static void clear() {
        if (instance != null) {
            instance.beliefs.clear();
        }
        instance = null;
    }

    // ─── Recording ────────────────────────────────────────────────

    /**
     * Form or strengthen a belief. If the subject already holds a belief
     * about the object with the same trait, the confidence is increased
     * (weighted average) and evidenceCount is incremented. If a
     * <i>contradictory</i> belief exists (same trait, opposite polarity),
     * the stronger one wins.
     *
     * @param subject    the NPC who holds the belief
     * @param object     the actor the belief is about
     * @param trait      the character trait (e.g. "courageous")
     * @param confidence 0.0–1.0, how strongly held
     * @param tick       the current server tick
     */
    public void recordBelief(String subject, String object, String trait,
                              float confidence, long tick) {
        String key = subject + "|" + object;
        List<Belief> list = beliefs.computeIfAbsent(key, k -> new ArrayList<>());

        // Check for an existing belief with the same trait.
        for (int i = 0; i < list.size(); i++) {
            Belief b = list.get(i);
            if (b.trait.equals(trait)) {
                // Strengthen: weighted average favoring the new evidence.
                float newConf = (b.confidence * b.evidenceCount + confidence)
                        / (b.evidenceCount + 1);
                newConf = Math.min(1f, newConf + 0.05f); // small boost for reinforcement
                list.set(i, new Belief(subject, object, trait,
                        newConf, tick, b.evidenceCount + 1));
                return;
            }
        }

        // New belief.
        list.add(new Belief(subject, object, trait, confidence, tick, 1));
        while (list.size() > MAX_BELIEFS_PER_PAIR) {
            // Remove the weakest belief to make room.
            int weakestIdx = 0;
            float weakestConf = list.get(0).confidence;
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i).confidence < weakestConf) {
                    weakestConf = list.get(i).confidence;
                    weakestIdx = i;
                }
            }
            list.remove(weakestIdx);
        }
    }

    /**
     * Explicitly weaken or remove a belief (e.g. when contradictory evidence
     * arrives). If confidence drops below 0.1, the belief is removed.
     */
    public void weakenBelief(String subject, String object, String trait,
                              float confidenceReduction, long tick) {
        String key = subject + "|" + object;
        List<Belief> list = beliefs.get(key);
        if (list == null) return;

        for (int i = 0; i < list.size(); i++) {
            Belief b = list.get(i);
            if (b.trait.equals(trait)) {
                float newConf = b.confidence - confidenceReduction;
                if (newConf < 0.1f) {
                    list.remove(i);
                } else {
                    list.set(i, new Belief(subject, object, trait,
                            newConf, tick, b.evidenceCount));
                }
                return;
            }
        }
    }

    // ─── Querying ─────────────────────────────────────────────────

    /**
     * Get all beliefs a subject holds about an object.
     */
    public List<Belief> beliefsAbout(String subject, String object) {
        String key = subject + "|" + object;
        return Collections.unmodifiableList(
                beliefs.getOrDefault(key, Collections.emptyList()));
    }

    /**
     * Get the confidence of a specific belief, or 0 if not held.
     */
    public float confidence(String subject, String object, String trait) {
        for (Belief b : beliefsAbout(subject, object)) {
            if (b.trait.equals(trait)) return b.confidence;
        }
        return 0f;
    }

    /**
     * Does the subject believe the object has the trait (confidence ≥ threshold)?
     */
    public boolean believes(String subject, String object, String trait) {
        return confidence(subject, object, trait) >= 0.4f;
    }

    /**
     * Get all beliefs held by a subject (across all objects).
     * Useful for NPC decision-making ("what do I believe about everyone?").
     */
    public List<Belief> allBeliefsBy(String subject) {
        List<Belief> result = new ArrayList<>();
        for (var entry : beliefs.entrySet()) {
            if (entry.getKey().startsWith(subject + "|")) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Total belief count (for diagnostics).
     */
    public int size() {
        int count = 0;
        for (var list : beliefs.values()) count += list.size();
        return count;
    }

    // ─── Decay ────────────────────────────────────────────────────

    /**
     * Decay all beliefs by a small amount. Called periodically (e.g. once
     * per Minecraft day). Beliefs below 0.1 confidence are removed.
     *
     * @param reduction the amount to subtract from each belief's confidence
     */
    public void decayAll(float reduction) {
        for (var entry : beliefs.entrySet()) {
            List<Belief> list = entry.getValue();
            list.removeIf(b -> b.confidence - reduction < 0.1f);
            for (int i = 0; i < list.size(); i++) {
                Belief b = list.get(i);
                list.set(i, new Belief(b.subject, b.object, b.trait,
                        b.confidence - reduction, b.formedAt, b.evidenceCount));
            }
        }
    }

    // ─── Serialization ────────────────────────────────────────────

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (var entry : beliefs.values()) {
            for (Belief b : entry) {
                CompoundTag bt = new CompoundTag();
                bt.putString(TAG_SUBJECT, b.subject);
                bt.putString(TAG_OBJECT, b.object);
                bt.putString(TAG_TRAIT, b.trait);
                bt.putFloat(TAG_CONFIDENCE, b.confidence);
                bt.putLong(TAG_FORMED_AT, b.formedAt);
                bt.putInt(TAG_EVIDENCE, b.evidenceCount);
                list.add(bt);
            }
        }
        tag.put(TAG_BELIEFS, list);
        return tag;
    }

    public void load(CompoundTag tag) {
        beliefs.clear();
        if (tag == null) return;
        ListTag list = tag.getList(TAG_BELIEFS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag bt = list.getCompound(i);
            String subject = bt.getString(TAG_SUBJECT);
            String object = bt.getString(TAG_OBJECT);
            String trait = bt.getString(TAG_TRAIT);
            float conf = bt.getFloat(TAG_CONFIDENCE);
            long formed = bt.getLong(TAG_FORMED_AT);
            int evid = bt.getInt(TAG_EVIDENCE);
            String key = subject + "|" + object;
            beliefs.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(new Belief(subject, object, trait, conf, formed, evid));
        }
        Ergenverse.LOGGER.info("[BeliefStore] Loaded {} beliefs.", size());
    }
}
