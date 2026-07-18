package dev.ergenverse.simulation.opportunity;

import java.util.List;

/**
 * ProtagonistSharingPhilosophy — the personality model for how each protagonist
 * shares opportunities with the player.
 *
 * <p>The user's directive: "Your affinity system becomes much more believable if
 * each protagonist has a sharing philosophy. Not a menu. A personality model."
 *
 * <p>Each canon protagonist has a distinct personality that governs WHAT they
 * share, UNDER WHAT CONDITIONS, and HOW. This makes the relationship itself
 * gameplay — you learn the protagonist's personality through interaction.
 *
 * <h2>The canon protagonists and their sharing philosophies</h2>
 * <ul>
 *   <li><b>Wang Lin (Renegade Immortal)</b> — extremely cautious. Would share:
 *       comprehension, techniques, survival knowledge. Would NOT casually give his
 *       deepest life-saving treasures unless trust is extraordinary.</li>
 *   <li><b>Meng Hao (ISSTH)</b> — more transactional. Would trade, would prank,
 *       would sell you things.</li>
 *   <li><b>Bai Xiaochun (AWWP)</b> — would accidentally give you too much.</li>
 *   <li><b>Xu Qing (Beyond the Timescape)</b> — careful and pragmatic.</li>
 *   <li><b>Su Ming (Pursuit of Truth)</b> — tests comprehension before sharing.</li>
 * </ul>
 *
 * <h2>How this interacts with {@link OpportunityClassification}</h2>
 * <p>The classification determines the CATEGORY (Transferable, Relationship
 * Exclusive, etc.). The sharing philosophy determines whether a SPECIFIC
 * protagonist will actually offer it. For example:
 * <ul>
 *   <li>A technique classified TRANSFERABLE + Wang Lin → offered at trust 60+
 *       (he's cautious but will teach survival knowledge).</li>
 *   <li>The same technique + Bai Xiaochun → offered at trust 20+ (he gives freely).</li>
 *   <li>A life-saving treasure classified RELATIONSHIP_EXCLUSIVE + Wang Lin →
 *       offered only at trust 95+ (he guards his deepest treasures).</li>
 * </ul>
 */
public record ProtagonistSharingPhilosophy(
        String protagonistId,           // e.g. "wanglin", "meng_hao"
        String protagonistName,
        SharingTemperament temperament,
        int baseTrustThreshold,         // the trust level at which they START sharing
        int deepTrustThreshold,         // the trust level at which they share their deepest things
        List<String> sharesFreely,      // what they share at baseTrustThreshold
        List<String> sharesReluctantly, // what they share only at deepTrustThreshold
        List<String> neverShares,       // what they never share (ABSOLUTE_UNIQUE items)
        String interactionStyle         // how they interact: "cautious", "transactional", "generous", "pragmatic", "testing"
) {
    public ProtagonistSharingPhilosophy {
        if (protagonistId == null || protagonistId.isBlank()) {
            throw new IllegalArgumentException("ProtagonistSharingPhilosophy requires a protagonistId");
        }
        if (temperament == null) temperament = SharingTemperament.CAUTIOUS;
        if (sharesFreely == null) sharesFreely = List.of();
        if (sharesReluctantly == null) sharesReluctantly = List.of();
        if (neverShares == null) neverShares = List.of();
        if (interactionStyle == null) interactionStyle = temperament.label;
        baseTrustThreshold = Math.max(0, Math.min(100, baseTrustThreshold));
        deepTrustThreshold = Math.max(baseTrustThreshold, Math.min(100, deepTrustThreshold));
    }

    /** The sharing temperament — the core personality axis. */
    public enum SharingTemperament {
        CAUTIOUS("cautious", "Shares survival knowledge freely, guards deepest treasures until extraordinary trust."),
        TRANSACTIONAL("transactional", "Trades, bargains, pranks. Everything has a price."),
        GENEROUS("generous", "Gives freely, sometimes too much. Hard to offend."),
        PRAGMATIC("pragmatic", "Careful and practical. Shares what makes strategic sense."),
        TESTING("testing", "Tests comprehension before sharing. You must prove you can handle it."),
        SILENT("silent", "Judges in silence. Shares only when you've earned it through action, not words.");

        public final String label;
        public final String description;
        SharingTemperament(String label, String description) {
            this.label = label;
            this.description = description;
        }
    }

    /**
     * Determine whether this protagonist would share a specific opportunity,
     * given the player's current trust level and the opportunity's classification.
     *
     * @return a ShareDecision: OFFERS_FREELY, OFFERS_RELUCTANTLY, REFUSES, or NEVER
     */
    public ShareDecision evaluateShare(OpportunityClassification classification, int currentTrust) {
        if (classification == null) return ShareDecision.REFUSES;

        // ABSOLUTE_UNIQUE items are never shared (unless explicitly in neverShares, which they should be)
        if (classification.category() == OpportunityCategory.ABSOLUTE_UNIQUE) {
            return ShareDecision.NEVER;
        }

        // Check neverShares list
        if (neverShares.stream().anyMatch(s -> classification.opportunityId().contains(s))) {
            return ShareDecision.NEVER;
        }

        // Check sharesReluctantly list (requires deep trust)
        boolean isReluctant = sharesReluctantly.stream().anyMatch(s -> classification.opportunityId().contains(s));
        if (isReluctant) {
            return currentTrust >= deepTrustThreshold ? ShareDecision.OFFERS_RELUCTANTLY : ShareDecision.REFUSES;
        }

        // Check sharesFreely list (requires base trust)
        boolean isFree = sharesFreely.stream().anyMatch(s -> classification.opportunityId().contains(s));
        if (isFree) {
            return currentTrust >= baseTrustThreshold ? ShareDecision.OFFERS_FREELY : ShareDecision.REFUSES;
        }

        // Default: use the category's relationship requirement + this protagonist's base threshold
        int requiredTrust = Math.max(classification.relationshipRequirement(), baseTrustThreshold);
        if (currentTrust >= deepTrustThreshold) {
            return ShareDecision.OFFERS_RELUCTANTLY;
        } else if (currentTrust >= requiredTrust) {
            return ShareDecision.OFFERS_FREELY;
        } else {
            return ShareDecision.REFUSES;
        }
    }

    /** The result of a share evaluation. */
    public enum ShareDecision {
        OFFERS_FREELY("The protagonist offers this willingly."),
        OFFERS_RELUCTANTLY("The protagonist offers this, but only because trust is extraordinary."),
        REFUSES("The protagonist refuses to share this (trust too low)."),
        NEVER("The protagonist will never share this — it is tied to their Dao.");

        public final String description;
        ShareDecision(String description) { this.description = description; }

        public boolean isPositive() { return this == OFFERS_FREELY || this == OFFERS_RELUCTANTLY; }
    }

    // ── Canon protagonist philosophies ─────────────────────────────────

    /** Wang Lin — extremely cautious. Shares survival knowledge, guards his deepest treasures. */
    public static ProtagonistSharingPhilosophy WANG_LIN = new ProtagonistSharingPhilosophy(
        "wanglin", "Wang Lin",
        SharingTemperament.CAUTIOUS,
        60,   // base: starts sharing survival knowledge at trust 60
        95,   // deep: shares deepest treasures only at trust 95+
        List.of("technique", "comprehension", "survival", "method", "art", "spell", "formation"), // shares freely
        List.of("treasure", "bead", "karma", "sword", "spear", "inheritance"),                     // shares reluctantly
        List.of("heaven_defying_bead"),                                                             // never shares
        "Wang Lin studies you for a long moment before speaking. His trust is hard-won."
    );

    /** Meng Hao — transactional. Everything has a price. */
    public static ProtagonistSharingPhilosophy MENG_HAO = new ProtagonistSharingPhilosophy(
        "meng_hao", "Meng Hao",
        SharingTemperament.TRANSACTIONAL,
        30,   // base: willing to deal early
        70,   // deep: gives real bargains at high trust
        List.of("pill", "recipe", "method", "trade", "fungible"),
        List.of("inheritance", "legacy", "personal"),
        List.of("paragon_brush", "blood_demon_grand_ming"),
        "Meng Hao grins. 'Friend, everything has a price. Let's make a deal.'"
    );

    /** Bai Xiaochun — generous. Gives freely, sometimes too much. */
    public static ProtagonistSharingPhilosophy BAI_XIAOCHUN = new ProtagonistSharingPhilosophy(
        "bai_xiaochun", "Bai Xiaochun",
        SharingTemperament.GENEROUS,
        15,   // base: shares almost immediately
        40,   // deep: gives freely at moderate trust
        List.of("technique", "pill", "method", "survival", "treasure", "formation"),
        List.of("life_saving"),
        List.of("heaven_dao_pill", "eternity_fire"),
        "Bai Xiaochun laughs nervously and hands you something before you even ask. 'Don't tell anyone I gave you this!'"
    );

    /** Xu Qing — pragmatic. Shares what makes strategic sense. */
    public static ProtagonistSharingPhilosophy XU_QING = new ProtagonistSharingPhilosophy(
        "xu_qing", "Xu Qing",
        SharingTemperament.PRAGMATIC,
        50,
        85,
        List.of("technique", "method", "survival", "strategic"),
        List.of("treasure", "inheritance"),
        List.of("night_preacher_lantern", "seven_bans_regulations"),
        "Xu Qing considers the tactical value before speaking. Trust is earned through competence."
    );

    /** Su Ming — tests comprehension before sharing. */
    public static ProtagonistSharingPhilosophy SU_MING = new ProtagonistSharingPhilosophy(
        "su_ming", "Su Ming",
        SharingTemperament.TESTING,
        40,
        80,
        List.of("comprehension", "insight"),
        List.of("technique", "inheritance", "barbarian"),
        List.of("blood_gathering_banner"),
        "Su Ming watches your comprehension. If you cannot understand it, he will not waste words."
    );

    /** All canon protagonist philosophies. */
    public static List<ProtagonistSharingPhilosophy> ALL = List.of(
        WANG_LIN, MENG_HAO, BAI_XIAOCHUN, XU_QING, SU_MING
    );

    /** Look up a protagonist's philosophy by ID. */
    public static ProtagonistSharingPhilosophy byId(String protagonistId) {
        return ALL.stream()
            .filter(p -> p.protagonistId().equals(protagonistId))
            .findFirst()
            .orElse(null);
    }
}
