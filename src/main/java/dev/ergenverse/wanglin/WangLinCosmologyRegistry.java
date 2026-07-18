package dev.ergenverse.wanglin;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.affinity.BridgingPolicy;
import dev.ergenverse.simulation.affinity.ManifestationGiftSystem;

/**
 * Wang Lin Cosmology Registry — the single bootstrap entry point.
 *
 * <p>Call {@link #bootstrap()} once during mod init to register all
 * Wang Lin cosmology systems into the game.
 *
 * <p><b>Three-Layer Architecture note:</b> This registry bridges Layer 1 (Canon)
 * and Layer 2 (Simulation). The canon systems (Cosmology, Timeline, Civilization,
 * Ecology, CanonicalDatabase, EdgeOfCanonState) are Layer 1 — immutable canon
 * reconstruction. The {@link BridgingPolicy} and {@link ManifestationGiftSystem}
 * are Layer 2 — Simulation-layer concepts that have been MOVED to
 * {@link dev.ergenverse.simulation.affinity}. They are imported here for
 * bootstrap-time logging only; the Canon layer never references them.
 *
 * <p>After this call, the following systems are live:
 * <ul>
 *   <li>{@link WangLinCosmology} — the 7 nested-sealed layers registered into CosmologicalTree</li>
 *   <li>{@link CaveWorldOwnership} — the world-as-farm ownership model</li>
 *   <li>{@link JossFlameEconomy} — the harvest loop (mortal faith → owner harvest)</li>
 *   <li>{@link HeavenDefyingBead} — Wang Lin's defining artifact (interior world + time dilation)</li>
 *   <li>{@link SamsaraDao} — the 14 Essences + 9 Heaven Trampling Bridges</li>
 *   <li>{@link WangLinAntagonists} — the two-layer antagonist structure (All-Seer + Seven-Colored Daoist)</li>
 *   <li>{@link RealmSealingGrandArray} — the seal that suppresses Third-Step cultivators in the Sealed Realm</li>
 *   <li>{@link BridgingPolicy} — <b>(Layer 2 — Simulation)</b> the canon-vs-generated policy gatekeeper</li>
 *   <li>{@link RITimelineEngine} — the spine of RI history (11 eras, 108 events)</li>
 *   <li>{@link RICivilizationEngine} — 45 factions, 6 axioms, economy model, lifecycle model</li>
 *   <li>{@link RIEcologyEngine} — 10 ecotopes, 6 global laws, 7 cross-region threads</li>
 *   <li>{@link RICanonicalDatabase} — 630 canon entries (158 chars, 80 locs, 178 arts, 214 techs)</li>
 *   <li>{@link RIEdgeOfCanonState} — the "State of the World at the Edge of Canon" (30 consequence chains,
 *       14 inheritance records, 6 decision horizons, 13 manifestation comments, faction/figure states)</li>
 *   <li>{@link ManifestationGiftSystem} — <b>(Layer 2 — Simulation)</b> the manifestation gift mechanic
 *       (canonical arsenal vs post-canon, four-question evaluation engine, 5 protagonist personality profiles, 18 gift records)</li>
 * </ul>
 */
public final class WangLinCosmologyRegistry {

    private WangLinCosmologyRegistry() {}

    private static volatile boolean bootstrapped = false;

    /**
     * Bootstrap all Wang Lin cosmology systems. Idempotent.
     */
    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        Ergenverse.LOGGER.info("[Ergenverse] ════════════════════════════════════════════════════════");
        Ergenverse.LOGGER.info("[Ergenverse]  WANG LIN COSMOLOGY — bootstrapping (Renegade Immortal)");
        Ergenverse.LOGGER.info("[Ergenverse] ════════════════════════════════════════════════════════");

        // 1. Cosmology layers — registers all 7 nested-sealed layers into CosmologicalTree
        WangLinCosmology.bootstrap();
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ 7 nested-sealed cosmology layers registered");

        // 2. Cave World ownership — the world-as-farm model (runtime data, no bootstrap needed)
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Cave World ownership model: 4 ownership records (Cave World, Sealed Realm, Outer Realm, IAC)");

        // 3. Joss Flame economy — runtime data, no bootstrap needed
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Joss Flame economy: harvest loop ready (30% owner harvest rate)");

        // 4. Heaven-Defying Bead — artifact definition (runtime data)
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Heaven-Defying Bead: 9 parts (6 canon + 3 bridging), 10× interior time dilation");

        // 5. Samsara Dao — the 14 Essences + 9 Heaven Trampling Bridges
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Samsara Dao: 14 Essences (6 substantial + 4 virtual + 4 special), 9 Heaven Trampling Bridges");

        // 6. Antagonists — 2 cosmic + 4 local
        long localThreats = WangLinAntagonists.getByLayer(WangLinAntagonists.Layer.LOCAL_THREAT).size();
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Antagonists: 3-layer structure ({} local threats + All-Seer mortal scheme + Seven-Colored Daoist cosmic ownership)",
            localThreats);

        // 7. Realm-Sealing Grand Array — the seal
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Realm-Sealing Grand Array: active, spirit (Heaven-Splitting Axe) dormant");

        // 8. Bridging Policy — canon-vs-generated gatekeeper (5-tier: A/5, B/4, C/3, D/2, D/1, F)
        java.util.Map<String, Long> bpCounts = BridgingPolicy.getSummaryCounts();
        long specD2 = bpCounts.getOrDefault("SPECULATION_D2_D2", 0L);
        long specD1 = bpCounts.getOrDefault("SPECULATION_D1_D1", 0L);
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Bridging Policy: {} entries (A={}, B={}, C={}, D2={}, D1={}, F={})",
            BridgingPolicy.POLICY.size(),
            bpCounts.getOrDefault(BridgingPolicy.Classification.CANON_CONCRETE.name(), 0L),
            bpCounts.getOrDefault(BridgingPolicy.Classification.CANON_IMPLIED.name(), 0L),
            bpCounts.getOrDefault(BridgingPolicy.Classification.REASONABLE_RECONSTRUCTION.name(), 0L),
            specD2, specD1,
            bpCounts.getOrDefault(BridgingPolicy.Classification.FORBIDDEN.name(), 0L));

        // 9. Timeline Engine — 11 eras, 108 events
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Timeline Engine: {} events across {} eras (E01 The Root Dao → E108 Transcendence)",
            RITimelineEngine.ALL_EVENTS.size(), RITimelineEngine.Era.values().length);

        // 10. Civilization Engine — 45 factions, 6 axioms
        long detailedCount = RICivilizationEngine.getDetailedFactions().size();
        long briefCount = RICivilizationEngine.getBriefFactions().size();
        long destroyedCount = RICivilizationEngine.getDestroyedFactions().size();
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Civilization Engine: {} factions ({} detailed + {} brief), {} destroyed, {} forbidden names",
            RICivilizationEngine.ALL_FACTIONS.size(), detailedCount, briefCount, destroyedCount,
            RICivilizationEngine.FORBIDDEN_ENTITIES.size());

        // 11. Ecology Engine — 10 ecotopes, 6 global laws, 7 threads
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Ecology Engine: {} ecotopes, {} global laws, {} cross-region threads, {} named beasts, {} heritage sites, {} canon-silent flags",
            RIEcologyEngine.ALL_REGIONS.size(), RIEcologyEngine.GLOBAL_LAWS.size(),
            RIEcologyEngine.CROSS_REGION_THREADS.size(), RIEcologyEngine.NAMED_BEASTS.size(),
            RIEcologyEngine.HERITAGE_SITES.size(), RIEcologyEngine.CANON_SILENT_FLAGS.size());

        // 12. Canonical Database — 630 entries (158 chars, 80 locs, 178 arts, 214 techs)
        java.util.Map<String, Integer> dbCounts = RICanonicalDatabase.getSummaryCounts();
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Canonical Database: {} chars, {} locs, {} artifacts, {} techniques ({} total entries)",
            dbCounts.get("characters"), dbCounts.get("locations"),
            dbCounts.get("artifacts"), dbCounts.get("techniques"),
            dbCounts.get("total"));

        // 13. Edge-of-Canon State — the foundational framing document made runtime-queryable
        java.util.Map<String, Integer> eocCounts = RIEdgeOfCanonState.getSummaryCounts();
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Edge-of-Canon State: {} consequence chains, {} inheritance records, {} decision horizons, {} manifestation comments",
            eocCounts.get("consequenceChains"), eocCounts.get("inheritanceRecords"),
            eocCounts.get("decisionHorizons"), eocCounts.get("manifestationComments"));
        Ergenverse.LOGGER.info("[Ergenverse]     [Reframe] {}", RIEdgeOfCanonState.REFRAME_STATEMENT);
        Ergenverse.LOGGER.info("[Ergenverse]     [Threats] {} categories (NOT replacement NPCs — the universe itself)", eocCounts.get("threatCategories"));
        Ergenverse.LOGGER.info("[Ergenverse]     [Factions] {} faction states ({} alive, {} gone, {} transformed/vacant)",
            eocCounts.get("factionStates"), eocCounts.get("aliveFigures"), eocCounts.get("deadFigures"),
            eocCounts.get("factionStates") - eocCounts.get("aliveFigures") - eocCounts.get("deadFigures"));

        // 14. Manifestation Gift System — canonical arsenal vs post-canon, four-question evaluation
        java.util.Map<String, Integer> giftCounts = ManifestationGiftSystem.getSummaryCounts();
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Manifestation Gift System: {} possession categories, {} gift categories, {} gift records ({} canonical, {} post-canon, {} identity-tied, {} teachable)",
            giftCounts.get("possessionCategories"), giftCounts.get("giftCategories"),
            giftCounts.get("giftRecords"), giftCounts.get("canonicalGifts"),
            giftCounts.get("postCanonGifts"), giftCounts.get("identityTiedGifts"),
            giftCounts.get("teachableGifts"));
        Ergenverse.LOGGER.info("[Ergenverse]     [Personalities] {} protagonist profiles (Wang Lin reserved, Meng Hao bargainer, Bai Xiaochun generous, Su Ming comprehension-seeker, Xu Qing silent judge)",
            giftCounts.get("personalityProfiles"));
        Ergenverse.LOGGER.info("[Ergenverse]     [Engine] {}-question evaluation (still-needed / trusted-ally / would-help / fits-personality) → {} decision outcomes",
            giftCounts.get("evaluationQuestions"), giftCounts.get("giftDecisions"));
        Ergenverse.LOGGER.info("[Ergenverse]     [Rules] everRemovesCanonicalOriginal={} (MUST be false); usesAffinityAsCurrency={} (MUST be false); usesRedirectionsForCanonItems={} (MUST be false); manifestationIsQuestGiver={} (MUST be false)",
            ManifestationGiftSystem.everRemovesCanonicalOriginal(),
            ManifestationGiftSystem.usesAffinityAsCurrency(),
            ManifestationGiftSystem.usesRedirectionsForCanonItems(),
            ManifestationGiftSystem.manifestationIsQuestGiver());

        Ergenverse.LOGGER.info("[Ergenverse] ──────────────────────────────────────────────────────");
        Ergenverse.LOGGER.info("[Ergenverse]  Wang Lin cosmology CONCRETED into the game.");
        Ergenverse.LOGGER.info("[Ergenverse]  Canon sources: CANON_RI_COMPLETE_WORLD.md (3,034 lines),");
        Ergenverse.LOGGER.info("[Ergenverse]                  CANON_RI_COMPLETE_ITEMS.md (2,019 lines),");
        Ergenverse.LOGGER.info("[Ergenverse]                  CANON_RI_COMPLETE_TECHNIQUES.md (1,793 lines),");
        Ergenverse.LOGGER.info("[Ergenverse]                  CANON_RI_TIMELINE.md (1,123 lines, 108 events),");
        Ergenverse.LOGGER.info("[Ergenverse]                  CANON_RI_EDGE_OF_CANON.md (the foundational framing),");
        Ergenverse.LOGGER.info("[Ergenverse]                  CANON_RI_ECOLOGY.md (2,087 lines), CANON_RI_CIVILIZATION.md (1,361 lines),");
        Ergenverse.LOGGER.info("[Ergenverse]                  DESIGN_MANIFESTATION_GIFTS.md (the gift-system design directive).");
        Ergenverse.LOGGER.info("[Ergenverse]  Bridging content: gated by BridgingPolicy 5-tier (A/5 B/4 C/3 D2/2 D1/1 F);");
        Ergenverse.LOGGER.info("[Ergenverse]    {} DERIVED_SPECULATION (D2) + {} BARE_CONJECTURE (D1) entries.",
            specD2, specD1);
        Ergenverse.LOGGER.info("[Ergenverse] ════════════════════════════════════════════════════════");
    }
}
