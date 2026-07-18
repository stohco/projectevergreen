package dev.ergenverse.cultivation;

/**
 * The outcome of a breakthrough attempt.
 *
 * <p>In the Er Gen multiverse, breakthrough is never guaranteed. It is
 * event-based: sufficient Qi, deep comprehension of a Dao, and survival
 * of Heaven's Tribulation. Mortals cultivate to understand, not to
 * accumulate — and Heaven tests that understanding with lightning.
 *
 * <p>Per Renegade Immortal canon (RI Ch. 200-400+): Wang Lin's
 * breakthroughs were perilous events, not level-ups. Each carried real
 * risk: the tribulation could kill, heart demons could consume, karma
 * could chain, and comprehension could be insufficient. A failed
 * breakthrough often meant decades or centuries of preparation before
 * the next attempt.
 *
 * <h2>Breakthrough is EVENT-BASED, not XP-based</h2>
 * <p>Cultivation does NOT use XP. A breakthrough requires:
 * <ol>
 *   <li><b>Sufficient Qi</b> — the body must hold enough spiritual energy
 *       to sustain the transformation.</li>
 *   <li><b>Sufficient Comprehension</b> — understanding of a Dao path
 *       must reach the threshold for the next realm. Filling a jar
 *       without understanding its nature is pointless.</li>
 *   <li><b>Tribulation Survival</b> — Heaven strikes with lightning.
 *       The cultivator must endure. Higher realms face stronger
 *       tribulation.</li>
 * </ol>
 *
 * <p>The Prime Directive: reality is objective; cultivation changes
 * understanding, not existence. Breakthrough changes what the cultivator
 * can perceive and interact with — it does not change what exists.
 */
public record BreakthroughResult(
        Outcome outcome,
        RealmId from,
        RealmId to,
        String narrativeNote
) {
    /** The possible outcomes of a breakthrough attempt. */
    public enum Outcome {
        /** The cultivator successfully broke through. Qi and comprehension were sufficient; tribulation was survived. */
        SUCCESS("Breakthrough successful."),

        /** Heaven's tribulation struck and the cultivator could not endure. Qi depleted, body wounded. */
        FAILED_TRIBULATION("Heaven's tribulation was not survived."),

        /** A heart demon surfaced during the critical moment. The cultivator's karma or inner demons overwhelmed them. */
        HEART_DEMON("A heart demon erupted during the breakthrough."),

        /** The cultivator's Dao comprehension has not reached the threshold for this realm. More meditation is needed. */
        INSUFFICIENT_COMPREHENSION("Dao comprehension insufficient for this realm."),

        /** Karmic debt is too heavy. Heaven will not permit this breakthrough until the debt is settled or endured. */
        KARMA_BLOCKED("Karmic debt blocks the heavenly path.");

        public final String defaultNote;
        Outcome(String defaultNote) { this.defaultNote = defaultNote; }
    }

    /** Convenience factory: the cultivator's comprehension is too shallow. */
    public static BreakthroughResult insufficientComprehension(RealmId from, String note) {
        return new BreakthroughResult(Outcome.INSUFFICIENT_COMPREHENSION, from, from, note);
    }

    /** Convenience factory: karma blocks the path. */
    public static BreakthroughResult karmaBlocked(RealmId from, String note) {
        return new BreakthroughResult(Outcome.KARMA_BLOCKED, from, from, note);
    }

    /** Convenience factory: heart demon erupted. */
    public static BreakthroughResult heartDemon(RealmId from, String note) {
        return new BreakthroughResult(Outcome.HEART_DEMON, from, from, note);
    }

    /** Convenience factory: tribulation killed or wounded the cultivator. */
    public static BreakthroughResult failedTribulation(RealmId from, String note) {
        return new BreakthroughResult(Outcome.FAILED_TRIBULATION, from, from, note);
    }

    /** Convenience factory: the breakthrough succeeded. */
    public static BreakthroughResult success(RealmId from, RealmId to, String note) {
        return new BreakthroughResult(Outcome.SUCCESS, from, to, note);
    }

    /** Whether the breakthrough succeeded. */
    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }
}