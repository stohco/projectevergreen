package dev.ergenverse.wanglin.ai.reasoning;

import dev.ergenverse.simulation.opportunity.PlayerObserverRealm;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChallengeGenerator — generates thematically-tied challenges for the
 * {@link GiftingOutcome#CHALLENGE} outcome, per PROJECT_MASTER.md §4.4.
 *
 * <p>When Wang Lin offers a challenge instead of yes/no, the challenge is
 * always thematically tied to the requested item's domain. This turns a
 * rejection into gameplay — the player has a path forward that proves their
 * worth rather than simply waiting.
 *
 * <h2>Domain → Challenge mapping</h2>
 * <ul>
 *   <li><b>Restriction items</b> (jade slips, flags, seal/formation items):
 *       "Survive 3 days at Restriction Mountain with no cultivation."</li>
 *   <li><b>Combat items</b> (whips, swords, blades, spears, fans):
 *       "Defeat 5 spirit beasts of equal realm without using treasures."</li>
 *   <li><b>Cultivation manuals / techniques</b> (tech_*, manual, method, dao):
 *       "Meditate at a spirit vein for 7 in-game days without speaking."</li>
 *   <li><b>Soul / blood items</b> (soul, blood, karma, lasher):
 *       "Spend 24 hours in the Mosquito Valley."</li>
 *   <li><b>Generic</b> (anything not matching):
 *       "Bring me a herb that grows only on the highest peak of the Eastern Mountains."</li>
 * </ul>
 *
 * <h2>Active challenge tracking</h2>
 * <p>The generator tracks active challenges per player (a player may have at
 * most ONE active challenge at a time — issuing a new one supersedes any
 * prior). Challenge completion detection is a TODO for another subagent
 * (the spec says: "For v1, just generate the challenge description and store
 * it; challenge completion detection is a TODO").
 *
 * <h2>Canon flavor</h2>
 * <p>All challenge text is paraphrased from canon situations (Restriction
 * Mountain trial, Mosquito Valley, spirit vein meditation). The challenges
 * are deliberately hard — they are Wang Lin's exacting standard, not a
 * tutorial prompt.
 */
public final class ChallengeGenerator {

    private ChallengeGenerator() {}

    /** Inner data class: one active challenge issued by Wang Lin to a player. */
    public static final class ActiveChallenge {
        public final UUID playerId;
        public final String requestedItemId;
        public final String challengeDescription;
        public final long issuedTick;
        public volatile boolean completed;

        public ActiveChallenge(UUID playerId, String requestedItemId,
                                String challengeDescription, long issuedTick) {
            this.playerId = playerId;
            this.requestedItemId = requestedItemId;
            this.challengeDescription = challengeDescription;
            this.issuedTick = issuedTick;
            this.completed = false;
        }

        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("playerId", playerId);
            tag.putString("requestedItemId", requestedItemId);
            tag.putString("challengeDescription", challengeDescription);
            tag.putLong("issuedTick", issuedTick);
            tag.putBoolean("completed", completed);
            return tag;
        }

        public static ActiveChallenge deserializeNBT(CompoundTag tag) {
            if (tag == null) return null;
            UUID playerId = tag.hasUUID("playerId") ? tag.getUUID("playerId") : UUID.randomUUID();
            String itemId = tag.contains("requestedItemId") ? tag.getString("requestedItemId") : "";
            String desc = tag.contains("challengeDescription") ? tag.getString("challengeDescription") : "";
            long tick = tag.contains("issuedTick") ? tag.getLong("issuedTick") : 0L;
            ActiveChallenge c = new ActiveChallenge(playerId, itemId, desc, tick);
            c.completed = tag.contains("completed") && tag.getBoolean("completed");
            return c;
        }
    }

    /** Per-player active challenge. At most ONE per player. */
    private static final Map<UUID, ActiveChallenge> ACTIVE_CHALLENGES = new ConcurrentHashMap<>();

    // ─── Domain classification ─────────────────────────────────────────

    /** Internal: classify the requested item into one of five challenge domains. */
    private enum Domain {
        RESTRICTION,
        COMBAT,
        CULTIVATION_MANUAL,
        SOUL_BLOOD,
        GENERIC
    }

    private static Domain classifyDomain(String requestedItemId) {
        if (requestedItemId == null || requestedItemId.isEmpty()) return Domain.GENERIC;
        String lower = requestedItemId.toLowerCase();
        // Cultivation manuals/techniques — check first because tech_restriction_* would otherwise match restriction.
        if (lower.startsWith("tech_") || lower.contains("manual") || lower.contains("method")
                || lower.contains("dao") || lower.contains("essence") || lower.contains("art")) {
            return Domain.CULTIVATION_MANUAL;
        }
        if (lower.contains("restriction") || lower.contains("seal") || lower.contains("formation")
                || lower.contains("flag") || lower.contains("jade") || lower.contains("array")) {
            return Domain.RESTRICTION;
        }
        if (lower.contains("soul") || lower.contains("blood") || lower.contains("karma")
                || lower.contains("lasher") || lower.contains("yama")) {
            return Domain.SOUL_BLOOD;
        }
        if (lower.contains("whip") || lower.contains("sword") || lower.contains("blade")
                || lower.contains("spear") || lower.contains("fan") || lower.contains("axe")
                || lower.contains("guard") || lower.contains("puppet")) {
            return Domain.COMBAT;
        }
        return Domain.GENERIC;
    }

    // ─── Public API ────────────────────────────────────────────────────

    /**
     * Generate a thematically-tied challenge description for the requested
     * item. The challenge is canon-flavored and adjusted by player tier (a
     * mortal gets a less lethal phrasing than an Ascendant cultivator, but
     * the core task is the same — Wang Lin's standard does not drop).
     *
     * @param requestedItemId the Forge registry ID of the requested item
     * @param playerTier      the player's observer realm (for flavor)
     * @return a canon-flavored challenge description string.
     */
    public static String generateChallenge(String requestedItemId, PlayerObserverRealm playerTier) {
        Domain domain = classifyDomain(requestedItemId);
        String baseChallenge = switch (domain) {
            case RESTRICTION ->
                    "Survive three days at Restriction Mountain with no cultivation. No treasures, "
                            + "no techniques, no escape talisman. If you cannot endure the mountain's "
                            + "pressure with your bare self, the restriction art you ask for would "
                            + "shatter you the first time you tried to wield it.";
            case COMBAT ->
                    "Defeat five spirit beasts of equal realm without using any treasures. "
                            + "Your hands, your breath, your dao — nothing else. A cultivator who "
                            + "cannot face their equal without a crutch has no business wielding a "
                            + "weapon of weight.";
            case CULTIVATION_MANUAL ->
                    "Meditate at a spirit vein for seven in-game days without speaking a single "
                            + "word. No talismans, no pills, no companion. If your dao heart cannot "
                            + "hold silence for seven days, the technique I would teach you would "
                            + "find no root in your spirit.";
            case SOUL_BLOOD ->
                    "Spend twenty-four hours in the Mosquito Valley. No concealment, no escape. "
                            + "What you ask of me deals in souls and karma — if you cannot survive "
                            + "the swarm's patience, you cannot carry the weight of soul-work.";
            case GENERIC ->
                    "Bring me a herb that grows only on the highest peak of the Eastern Mountains. "
                            + "It blooms at midnight, withers by dawn. If your eyes cannot find it "
                            + "and your feet cannot reach it, what you ask of me is beyond your "
                            + "current grasp.";
        };

        // Tier-flavored prefix — Wang Lin addresses the player by their capacity.
        String prefix = switch (playerTier) {
            case MORTAL -> "You are mortal yet. ";
            case QI_CONDENSATION -> "Your Qi is thin but present. ";
            case FOUNDATION -> "Your foundation is set. ";
            case CORE_FORMATION -> "Your core is formed. ";
            case NASCENT_SOUL -> "Your Nascent Soul watches. ";
            case SOUL_FORMATION -> "Your soul is formed. ";
            case ASCENDANT_PLUS -> "Ascendant — you perceive the shape of the trial. ";
        };
        return prefix + baseChallenge;
    }

    /**
     * Issue a challenge to a player: generate the description, store it as
     * the player's active challenge, and return the description.
     *
     * <p>If the player already has an active (incomplete) challenge, it is
     * superseded by the new one.
     *
     * @param playerId        the player's UUID
     * @param requestedItemId the Forge registry ID of the requested item
     * @param playerTier      the player's observer realm (for flavor)
     * @param worldTick       the current world tick (for issuance tracking)
     * @return the canon-flavored challenge description.
     */
    public static String issueChallenge(UUID playerId, String requestedItemId,
                                         PlayerObserverRealm playerTier, long worldTick) {
        String description = generateChallenge(requestedItemId, playerTier);
        ActiveChallenge challenge = new ActiveChallenge(playerId, requestedItemId, description, worldTick);
        ACTIVE_CHALLENGES.put(playerId, challenge);
        return description;
    }

    /** Get the player's active challenge (or null if none). */
    public static ActiveChallenge getActiveChallenge(UUID playerId) {
        if (playerId == null) return null;
        return ACTIVE_CHALLENGES.get(playerId);
    }

    /** Whether the player has an active (incomplete) challenge. */
    public static boolean hasActiveChallenge(UUID playerId) {
        ActiveChallenge c = getActiveChallenge(playerId);
        return c != null && !c.completed;
    }

    /**
     * Mark the player's active challenge as completed (called by future
     * challenge-completion-detection subsystem). Returns true if a challenge
     * was marked complete; false if the player had no active challenge.
     */
    public static boolean markCompleted(UUID playerId) {
        ActiveChallenge c = getActiveChallenge(playerId);
        if (c == null || c.completed) return false;
        c.completed = true;
        return true;
    }

    /**
     * Clear the player's active challenge (called when the item is granted
     * post-challenge, or when the challenge is abandoned).
     */
    public static void clearActiveChallenge(UUID playerId) {
        ACTIVE_CHALLENGES.remove(playerId);
    }
}
