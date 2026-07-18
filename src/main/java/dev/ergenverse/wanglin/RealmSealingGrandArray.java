package dev.ergenverse.wanglin;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.formation.Formation;

/**
 * The Realm-Sealing Grand Array (封界大阵) — the formation that seals the Sealed Realm.
 *
 * <p><b>Canon (C5, CANON_RI_COMPLETE_WORLD.md + CANON_FORMATIONS_TALISMANS_USES.md):</b>
 * <pre>
 *   The Realm-Sealing Grand Array separates the Sealed Realm (inner half of the
 *   Cave World) from the Outer Realm (outer half).
 *
 *   Its SPIRIT is the Heaven-Splitting Axe (开天斧) — a sentient artifact that
 *   serves as the array's enforcer.
 *
 *   Purpose: prevent Third-Step cultivators from rising inside the Sealed Realm.
 *   Only "Heaven Blight" (quasi-Third-Step) cultivators can squeeze through the seal.
 *
 *   Wang Lin's interaction:
 *     - Borrowed the Heaven-Splitting Axe to kill multiple Outer-Realm Third-Step cultivators
 *     - Reset the array at the end of the Sealed Realm War arc
 *     - The array's spirit (the Axe) chose to cooperate with Wang Lin
 *
 *   Final state: dissolved when Wang Lin killed the Seven-Colored Daoist and
 *   became the new Cave World owner. He had no need for the seal.
 * </pre>
 *
 * <p><b>Per user correction #14:</b> Formations are hybrid (block-based for
 * sect-scale, item-based for portable). The Realm-Sealing Grand Array is
 * BLOCK-BASED — it is anchored to the world itself (the Sealed Realm's boundary).
 *
 * <p><b>Per user correction #17:</b> Sect-Protecting Array is universal (every
 * novel has one). The Realm-Sealing Grand Array is the Wang Lin / RI version
 * of this universal archetype, scaled up to seal an entire half-world.
 *
 * <p><b>Per the Prime Directive:</b> the array exists objectively. Its seal
 * suppresses Third-Step cultivators whether they know about the array or not.
 * A cultivator who reaches Third-Step inside the Sealed Realm will be crushed
 * by the seal unless they can either (a) borrow the Heaven-Splitting Axe's
 * cooperation, (b) shatter the array, or (c) kill the array's creator (the
 * Seven-Colored Daoist).
 */
public final class RealmSealingGrandArray {

    private RealmSealingGrandArray() {}

    /** The array's spirit — the Heaven-Splitting Axe. */
    public enum SpiritState {
        DORMANT("The Axe is dormant inside the array. Will enforce the seal mechanically."),
        AWAKENING("The Axe stirs. Has noticed a cultivator attempting to bypass the seal. Will judge them."),
        COOPERATIVE("The Axe has chosen to cooperate with a cultivator (canon: Wang Lin). Will lend its power to kill Outer-Realm Third-Step cultivators."),
        SHATTERED("The Axe has been destroyed or the array broken. The seal is gone.");

        public final String description;
        SpiritState(String d) { this.description = d; }
    }

    /** The seal's enforcement modes. */
    public enum EnforcementMode {
        /** Default: any cultivator attempting Third-Step breakthrough inside the Sealed Realm is crushed. */
        CRUSH_BREAKTHROUGH("Crushes any Third-Step breakthrough attempt inside the Sealed Realm."),
        /** Activated when a cultivator tries to cross the Sealed Realm boundary: repels them. */
        REPEL_CROSSING("Repels any cultivator trying to cross the Sealed Realm boundary outward (Heaven Blight can squeeze through)."),
        /** Activated when the Axe cooperates with a cultivator: the array becomes a weapon. */
        WEAPON_MODE("The Axe lends its power to a cooperating cultivator (canon: Wang Lin killed multiple Outer-Realm Third-Step cultivators this way)."),
        /** Disabled: the array is no longer enforcing. */
        DISABLED("The array is dissolved (canon: after Wang Lin killed the Seven-Colored Daoist).");

        public final String description;
        EnforcementMode(String d) { this.description = d; }
    }

    /** Current state of the array (runtime). */
    private static volatile SpiritState spiritState = SpiritState.DORMANT;
    private static volatile EnforcementMode enforcementMode = EnforcementMode.CRUSH_BREAKTHROUGH;
    private static volatile boolean arrayActive = true;

    /**
     * The "can this cultivator break through to Third-Step inside the Sealed Realm?" check.
     *
     * <p>Canon: the seal crushes Third-Step breakthroughs. Only "Heaven Blight"
     * (quasi-Third-Step) cultivators can squeeze through — and even they can't
     * fully break through without the array's cooperation.
     *
     * <p>Exceptions (per canon):
     * <ul>
     *   <li>Heaven-Defying Bead: the bead's "defy heaven" nature allows ONE tier
     *       above the seal's ceiling (Wang Lin's signature loophole).</li>
     *   <li>Spirit cooperation: if the Axe cooperates with the cultivator, the
     *       seal becomes a weapon FOR them, not against them.</li>
     *   <li>Killing the Seven-Colored Daoist: dissolves the array entirely
     *       (the array was his creation; without him, it has no master).</li>
     * </ul>
     */
    public static boolean canBreakthroughInsideSealedRealm(int targetAbsoluteTier,
                                                            boolean hasHeavenDefyingBead,
                                                            boolean spiritCooperates) {
        if (!arrayActive) return true;  // array dissolved
        if (spiritCooperates) return true;  // spirit is helping
        // The seal's ceiling: absolute tier 9 (Nirvana Fruit) is the cap.
        // Heaven Blight (tier 10) can squeeze through but not fully break through.
        // Third-Step (tier 11+) is crushed.
        if (targetAbsoluteTier <= 9) return true;  // below the ceiling — fine
        if (targetAbsoluteTier == 10) {
            // Heaven Blight — squeeze through, but only with the bead's defiance
            return hasHeavenDefyingBead;
        }
        return false;  // Third-Step+ crushed
    }

    /**
     * The "can this cultivator cross the Sealed Realm boundary outward?" check.
     *
     * <p>Canon: only Heaven Blight (quasi-Third-Step) cultivators can squeeze through.
     * Below that, the array repels them.
     */
    public static boolean canCrossBoundaryOutward(int playerAbsoluteTier) {
        if (!arrayActive) return true;
        return playerAbsoluteTier >= 10;  // Heaven Blight or higher
    }

    /**
     * Borrow the Heaven-Splitting Axe's power (canon: Wang Lin did this to kill Outer-Realm Third-Step cultivators).
     *
     * <p>This is a one-time-per-arc action. The Axe judges the cultivator; if
     * they pass, it cooperates for a limited duration.
     */
    public static boolean borrowAxePower(int playerAbsoluteTier, boolean playerHasRestrictionEssence) {
        if (!arrayActive) return false;
        if (spiritState == SpiritState.SHATTERED) return false;
        // The Axe respects restriction-essence cultivators (its own nature is restriction/sealing)
        // Canon: Wang Lin had the Restriction Essence, which is why the Axe chose him
        if (playerAbsoluteTier >= 11 && playerHasRestrictionEssence) {
            spiritState = SpiritState.COOPERATIVE;
            enforcementMode = EnforcementMode.WEAPON_MODE;
            Ergenverse.LOGGER.info("[Ergenverse] RealmSealingGrandArray: Heaven-Splitting Axe now cooperates with cultivator (restriction-essence resonance).");
            return true;
        }
        return false;
    }

    /**
     * Dissolve the array entirely (canon: Wang Lin did this by killing the Seven-Colored Daoist).
     */
    public static void dissolve() {
        arrayActive = false;
        spiritState = SpiritState.SHATTERED;
        enforcementMode = EnforcementMode.DISABLED;
        Ergenverse.LOGGER.info("[Ergenverse] RealmSealingGrandArray: dissolved. The Sealed Realm is no longer sealed. The Cave World is unified.");
    }

    /**
     * The array's anchor structure — block-based, anchored to the Sealed Realm's boundary.
     *
     * <p>Per user correction #14: this is a block-based formation (anchored to the world).
     * It is NOT portable. The anchor blocks form a ring at the Sealed Realm's boundary.
     */
    public static final String[] ANCHOR_BLOCK_TYPES = {
        "ergenverse:realm_sealing_flag",     // the primary anchor flags
        "ergenverse:heaven_splitting_axe_pedestal",  // the Axe's pedestal (the spirit's seat)
        "ergenverse:dao_binding_stone"       // stones inscribed with the Seven-Colored Daoist's dao
    };

    /** The array's canon formation spec (for the Formation system). */
    public static Formation formationSpec() {
        // The Realm-Sealing Grand Array is registered as a DAO-GRADE formation
        // (the highest tier — it seals an entire half-world).
        // This is a placeholder; the actual Formation object would be constructed
        // by the Formation system's registry.
        return null;  // Formation registry will handle this
    }

    public static SpiritState getSpiritState() { return spiritState; }
    public static EnforcementMode getEnforcementMode() { return enforcementMode; }
    public static boolean isArrayActive() { return arrayActive; }
}
